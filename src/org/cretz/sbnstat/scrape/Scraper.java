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

import java.io.IOException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;

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

public class Scraper implements Operation {
    
    private static final Logger logger = LoggerFactory.getLogger(Scraper.class);
    
    private static Document loadUrl(String url) throws IOException {
        return Jsoup.connect(url).
            //spoof user agent
            userAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.15) Gecko/20110303 Firefox/3.6.15 ( .NET CLR 3.5.30729; .NET4.0E)").
            //timeout 10 seconds
            timeout(10000).get();
    }
    
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
            //populate the fan posts
            PostLoader postLoader = new PostLoader(context);
            String url = "http://www." + args.getDomain() + "/fanposts/recent";
            do {
                logger.debug("Waiting 3 seconds, then loading fanpost list from: {}", url);
                Thread.sleep(3000);
                url = postLoader.populateFanPosts(loadUrl(url));
            } while (url != null);
            //populate the fan shots
            url = "http://www." + args.getDomain() + "/fanshots";
            do {
                logger.debug("Waiting 3 seconds, then loading fanshot list from: {}", url);
                Thread.sleep(3000);
                url = postLoader.populateFanShots(loadUrl(url));
            } while (url != null);
            //populate the front page stuff
            int year = context.getTo().get(Calendar.YEAR) + 1;
            do {
                url = "http://www." + args.getDomain() + "/stories/archive/" + --year;
                Thread.sleep(3000);
                logger.debug("Waiting 3 seconds, then loading frontpage post list from: {}", url);
            } while (postLoader.populateFrontPage(loadUrl(url), year));
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
                    logger.debug("Waiting 3 seconds, then getting comments from post {}", post.getUrl());
                    Thread.sleep(3000);
                    //get comments
                    List<Comment> comments = commentLoader.loadCommentsAndUpdatePost(
                            loadUrl(post.getUrl()), post);
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
}
