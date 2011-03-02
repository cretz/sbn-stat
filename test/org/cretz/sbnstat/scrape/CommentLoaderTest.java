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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.dao.model.PostType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CommentLoaderTest {

    public void testCommentLoaderOnFanShot() throws Exception {
        ScrapeContext context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.FEBRUARY, 27), 
                new GregorianCalendar(2011, Calendar.FEBRUARY, 27));
        //make the post
        Post post = new Post();
        post.setDate(new Timestamp(new GregorianCalendar(2011, Calendar.FEBRUARY, 27).getTimeInMillis()));
        post.setRecommendationCount(0);
        post.setSbnId(2019182);
        post.setTitle(null);
        post.setType(PostType.FAN_SHOT);
        post.setUrl("http://www.lonestarball.com/2011/2/27/2019182/doing-a-little-testing");
        post.setUser(context.getUser("matchst1ckPuppet", 
                "http://www.sbnation.com/users/matchst1ckPuppet"));
        //parse the local doc
        @SuppressWarnings("unused")
        Document doc = Jsoup.parse(PostLoaderTest.class.getResourceAsStream("recentPostList.html.txt"), 
                "UTF8", "http://www.lonestarball.com/fanposts/recent");
        //TODO: finish this...
    }
}
