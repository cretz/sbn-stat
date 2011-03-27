/*
 * Copyright 2011 Chad Retz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cretz.sbnstat.scrape;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.cretz.sbnstat.Arguments;
import org.cretz.sbnstat.Operation;
import org.cretz.sbnstat.dao.SbnStatDao;
import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.util.DateUtils;
import org.cretz.sbnstat.util.JdbcUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

public class Scraper implements Operation {
    
    private static final Logger logger = LoggerFactory.getLogger(Scraper.class);
    
    @Override
    public void run(Arguments args) {
        //create a context
        ScrapeContext context = new ScrapeContext(
                DateUtils.toBeginningOfDayCalendar(args.getFrom()),
                DateUtils.toEndOfDayCalendar(args.getTo()));
        logger.info("Scraping all posts and comments from {} to {}", 
                context.getFrom().getTime(), context.getTo().getTime());
        //population
        try {
            //create cache if dir is there
            Cache cache = null;
            if (args.getCacheDir() != null) {
                cache = new Cache(new File(args.getCacheDir()));
            }
            //populate the fan posts
            PostLoader postLoader = new PostLoader(context);
            String url = "http://www." + args.getDomain() + "/fanposts/recent";
            if (args.getStartPanPostPage() != null) {
                url += "/" + args.getStartPanPostPage();
            }
            do {
                logger.debug("Loading fanpost list from: {}", url);
                url = postLoader.populateFanPosts(loadUrl(url, cache));
            } while (url != null);
            //populate the fan shots
            url = "http://www." + args.getDomain() + "/fanshots";
            int page = 1;
            if (args.getStartFanShotPage() != null) {
                url += "?page=" + args.getStartFanShotPage();
                page = args.getStartFanShotPage();
            }
            do {
                logger.debug("Loading fanshot list from: {}", url);
                if (postLoader.populateFanShots(loadUrl(url, cache))) {
                    url = "http://www." + args.getDomain() + "/fanshots?page=" + (++page);
                } else {
                    url = null;
                }
            } while (url != null);
            //populate the front page stuff
            int year = context.getTo().get(Calendar.YEAR) + 1;
            do {
                url = "http://www." + args.getDomain() + "/stories/archive/" + --year;
                logger.debug("Loading frontpage post list from: {}", url);
            } while (postLoader.populateFrontPage(loadUrl(url, cache), year));
            //build a connection
            Connection conn = JdbcUtils.connectToMySqlDatabase(
                    args.getDatabaseHost(), 
                    args.getDatabasePort(), 
                    args.getDatabaseName(), 
                    args.getDatabaseUser(), 
                    args.getDatabasePass());
            try {
                //grab a DAO
                SbnStatDao dao = new SbnStatDao(conn);
                //persist all the users
                logger.debug("Initial user persist of {} users", context.getUsers().size());
                dao.persistUnpersistedUsers(context.getUsers());
                //go post by post, grab comments and persist
                CommentLoader commentLoader = new CommentLoader(context);
                for (Post post : context.getPosts().values()) {
                    logger.debug("Getting comments from post {}", post.getUrl());
                    //get comments
                    List<Comment> comments = commentLoader.loadCommentsAndUpdatePost(
                            loadUrl(post.getUrl(), cache), post);
                    //persist unpersisted users
                    dao.persistUnpersistedUsers(context.getUsers());
                    //persist post
                    dao.persistPost(post);
                    //persist comments
                    logger.debug("Persisting {} comments", comments.size());
                    dao.persistComments(comments);
                }
            } finally {
                JdbcUtils.closeQuietly(conn);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Document loadUrl(String url, Cache cache) throws IOException, InterruptedException {
        String html = null;
        if (cache != null) {
            html = cache.load(url);
        }
        if (html == null) {
            //wait three seconds...
            logger.debug("Waiting three seconds...");
            Thread.sleep(3000);
            //get HTML from URL
            HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
            HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10000);
            HttpGet get = new HttpGet(url);
            //spoof user agent
            get.setHeader("User-Agent", 
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.15) Gecko/20110303 Firefox/3.6.15 ( .NET CLR 3.5.30729; .NET4.0E)");
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Unable to load URL " + url +
                        ", received code " + response.getStatusLine().getStatusCode() +
                        " with reason: " + response.getStatusLine().getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new RuntimeException("Got no entity for url " + url);
            }
            html = CharStreams.toString(new InputStreamReader(entity.getContent()));
            //add it to the cache
            if (cache != null) {
                cache.add(url, html);
            }
        }
        return Jsoup.parse(html, url);
    }
}
