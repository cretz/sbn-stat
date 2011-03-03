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

import java.util.Calendar;
import java.util.List;

import org.cretz.sbnstat.Arguments;
import org.cretz.sbnstat.Operation;
import org.cretz.sbnstat.dao.SbnStatDao;
import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.jsoup.Jsoup;

public class Scraper implements Operation {

    @SuppressWarnings("unused")
    private Arguments arguments;
    
    @Override
    public void run(Arguments args) {
        arguments = args;
        //create a context
        ScrapeContext context = new ScrapeContext(Calendar.getInstance(), Calendar.getInstance());
        context.getFrom().setTime(args.getFrom());
        context.getTo().setTime(args.getTo());
        context.getTo().set(Calendar.HOUR_OF_DAY, 23);
        context.getTo().set(Calendar.MINUTE, 59);
        context.getTo().set(Calendar.SECOND, 59);
        //population
        try {
            //populate the fan posts
            PostLoader postLoader = new PostLoader(context);
            String url = "http://www." + args.getDomain() + "/fanposts/recent";
            do {
                url = postLoader.populateFanPosts(Jsoup.connect(url).get());
            } while (url != null);
            //populate the fan shots
            url = "http://www." + args.getDomain() + "/fanshots";
            do {
                url = postLoader.populateFanShots(Jsoup.connect(url).get());
            } while (url != null);
            //populate the front page stuff
            int year = context.getTo().get(Calendar.YEAR) + 1;
            do {
                year--;
            } while (postLoader.populateFrontPage(Jsoup.connect("http://www." + 
                    args.getDomain() + "/stories/archive/" + year).get(), year));
            //grab a DAO
            SbnStatDao dao = new SbnStatDao(args);
            //persist all the users
            dao.persistUnpersistedUsers(context.getUsers(), args.getBatchSize());
            //go post by post, grab comments and persist
            CommentLoader commentLoader = new CommentLoader(context);
            for (Post post : context.getPosts().values()) {
                //get comments
                List<Comment> comments = commentLoader.loadCommentsAndUpdatePost(
                        Jsoup.connect(post.getUrl()).get(), post);
                //persist post
                dao.persistPost(post);
                //persist comments
                dao.persistComments(comments, args.getBatchSize());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
