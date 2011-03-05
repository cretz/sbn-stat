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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.List;

import org.cretz.sbnstat.Arguments;
import org.cretz.sbnstat.Operation;
import org.cretz.sbnstat.dao.SbnStatDao;
import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scraper implements Operation {
    
    private static final Logger logger = LoggerFactory.getLogger(Scraper.class);
    
    @Override
    public void run(Arguments args) {
        //create a context
        ScrapeContext context = new ScrapeContext(Calendar.getInstance(), Calendar.getInstance());
        context.getFrom().setTime(args.getFrom());
        context.getFrom().set(Calendar.HOUR_OF_DAY, 0);
        context.getFrom().set(Calendar.MINUTE, 9);
        context.getFrom().set(Calendar.SECOND, 0);
        context.getTo().setTime(args.getTo());
        context.getTo().set(Calendar.HOUR_OF_DAY, 23);
        context.getTo().set(Calendar.MINUTE, 59);
        context.getTo().set(Calendar.SECOND, 59);
        logger.info("Scraping all posts and comments from {} to {}", context.getFrom(), context.getTo());
        //population
        try {
            //populate the fan posts
            PostLoader postLoader = new PostLoader(context);
            String url = "http://www." + args.getDomain() + "/fanposts/recent";
            do {
                logger.debug("Loading fanpost list from: {}", url);
                url = postLoader.populateFanPosts(Jsoup.connect(url).get());
            } while (url != null);
            //populate the fan shots
            url = "http://www." + args.getDomain() + "/fanshots";
            do {
                logger.debug("Loading fanshot list from: {}", url);
                url = postLoader.populateFanShots(Jsoup.connect(url).get());
            } while (url != null);
            //populate the front page stuff
            int year = context.getTo().get(Calendar.YEAR) + 1;
            do {
                url = "http://www." + args.getDomain() + "/stories/archive/" + --year;
                logger.debug("Loading frontpage post list from: {}", url);
            } while (postLoader.populateFrontPage(Jsoup.connect(url).get(), year));
            //build a connection
            Connection conn = DriverManager.getConnection(new StringBuilder("jdbc:mysql://").
                    append(args.getDatabaseHost()).append(':').
                    append(args.getDatabasePort()).append('/').
                    append(args.getDatabaseName()).toString(), 
                    args.getDatabaseUser(), args.getDatabasePass());
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
                            Jsoup.connect(post.getUrl()).get(), post);
                    //persist unpersisted users
                    dao.persistUnpersistedUsers(context.getUsers());
                    //persist post
                    dao.persistPost(post);
                    //persist comments
                    logger.debug("Persisting {} comments", comments.size());
                    dao.persistComments(comments);
                }
            } finally {
                try { conn.close(); } catch (Exception e) { }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
