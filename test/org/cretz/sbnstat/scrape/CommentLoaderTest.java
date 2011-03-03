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
import java.util.List;

import junit.framework.Assert;

import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.dao.model.PostType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class CommentLoaderTest {

    @Test
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
        Document doc = Jsoup.parse(PostLoaderTest.class.getResourceAsStream("fanShotComments.html.txt"), 
                "UTF8", "http://www.lonestarball.com/fanposts/recent");
        List<Comment> comments = new CommentLoader(context).loadCommentsAndUpdatePost(doc, post);
        //make sure it's 182
        Assert.assertEquals(182, comments.size());
        //let's test some of the children... (better test this in CST!)
        Comment comment = comments.get(0);
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.FEBRUARY, 
                27, 17, 10).getTimeInMillis()), comment.getDate());
        Assert.assertEquals("matchst1ckPuppet", comment.getUser().getUsername());
        Assert.assertEquals("Test 1", comment.getSubject());
        Assert.assertEquals(0, comment.getDepth());
        Assert.assertEquals(60227896L, comment.getSbnId());
        Assert.assertEquals(1, comment.getRecommendationCount());
        Assert.assertEquals(null, comment.getContents());
        //find the one entitled "Dumb?"
        comment = null;
        for (Comment possible : comments) {
            if ("Dumb?".equals(possible.getSubject())) {
                comment = possible;
                break;
            }
        }
        Assert.assertNotNull(comment);
        Assert.assertEquals("matchst1ck", comment.getUser().getUsername());
        Assert.assertEquals(4, comment.getDepth());
        Assert.assertEquals(60388513L, comment.getSbnId());
        Assert.assertEquals(0, comment.getRecommendationCount());
        Assert.assertEquals("tasatasd", comment.getContents());
        Assert.assertEquals("Hmm.", comment.getParent().getSubject());
        Assert.assertEquals(comments.get(0), comment.getTopLevelParent());
    }
}
