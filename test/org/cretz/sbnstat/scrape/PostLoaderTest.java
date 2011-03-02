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
import org.junit.Assert;
import org.junit.Test;

public class PostLoaderTest {

    @Test
    public void fanPostTest() throws Exception {
        //create context (feb to april)
        ScrapeContext context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.FEBRUARY, 1), 
                new GregorianCalendar(2011, Calendar.APRIL, 1));
        //parse the local doc
        Document doc = Jsoup.parse(PostLoaderTest.class.getResourceAsStream("recentPostList.html.txt"), 
                "UTF8", "http://www.lonestarball.com/fanposts/recent");
        //run loader
        String next = new PostLoader(context).populateFanPosts(doc);
        //count
        Assert.assertEquals(25, context.getPosts().size());
        //first post
        Post first = context.getPosts().get(
                "http://www.lonestarball.com/2011/3/1/2022589/judging-draft-success-or-something-like-that");
        Assert.assertNotNull(first);
        Assert.assertEquals(2022589L, first.getSbnId());
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.MARCH, 1).getTimeInMillis()), 
                first.getDate());
        Assert.assertEquals("Judging draft success or something like that.", first.getTitle());
        Assert.assertEquals(PostType.FAN_POST, first.getType());
        Assert.assertNotNull(first.getUser());
        Assert.assertEquals("Daveheart", first.getUser().getUsername());
        Assert.assertEquals("http://www.sbnation.com/users/Daveheart", first.getUser().getUrl());
        //last post
        Post last = context.getPosts().get(
            "http://www.lonestarball.com/2011/2/23/2010219/deron-williams-traded-to-the-nets");
        Assert.assertNotNull(last);
        Assert.assertEquals(2010219L, last.getSbnId());
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.FEBRUARY, 23).getTimeInMillis()), 
                last.getDate());
        Assert.assertEquals("Deron Williams traded to the Nets", last.getTitle());
        Assert.assertEquals(PostType.FAN_POST, last.getType());
        Assert.assertNotNull(last.getUser());
        Assert.assertEquals("Adam J. Morris", last.getUser().getUsername());
        Assert.assertEquals("http://www.sbnation.com/users/Adam%20J.%20Morris", last.getUser().getUrl());
        //and next...
        Assert.assertEquals("http://www.lonestarball.com/fanposts/recent/2", next);
        //let's try a version where we only get from march first
        context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.MARCH, 1), 
                new GregorianCalendar(2011, Calendar.APRIL, 1));
        //run loader
        next = new PostLoader(context).populateFanPosts(doc);
        //count
        Assert.assertEquals(6, context.getPosts().size());
        //next
        Assert.assertNull(next);
        //let's try a version where we only get until feb 28
        context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.JANUARY, 1), 
                new GregorianCalendar(2011, Calendar.FEBRUARY, 28));
        //run loader
        next = new PostLoader(context).populateFanPosts(doc);
        //count
        Assert.assertEquals(19, context.getPosts().size());
        //next
        Assert.assertEquals("http://www.lonestarball.com/fanposts/recent/2", next);
    }

    @Test
    public void fanShotTest() throws Exception {
        //create context (feb to april)
        ScrapeContext context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.FEBRUARY, 1), 
                new GregorianCalendar(2011, Calendar.APRIL, 1));
        //parse the local doc
        Document doc = Jsoup.parse(PostLoaderTest.class.getResourceAsStream("fanShotList.html.txt"), 
                "UTF8", "http://www.lonestarball.com/fanshots");
        //run loader
        String next = new PostLoader(context).populateFanShots(doc);
        //count
        Assert.assertEquals(10, context.getPosts().size());
        //first post
        Post first = context.getPosts().get(
                "http://www.lonestarball.com/2011/3/1/2023330/chris-carpenter-injures-hamstring-berkman-regrets-signing-with-st");
        Assert.assertNotNull(first);
        Assert.assertEquals(2023330L, first.getSbnId());
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.MARCH, 1).getTimeInMillis()), 
                first.getDate());
        Assert.assertEquals("Chris Carpenter Injures Hamstring, Berkman regrets signing with St. Louis", 
                first.getTitle());
        Assert.assertEquals(PostType.FAN_SHOT, first.getType());
        Assert.assertNotNull(first.getUser());
        Assert.assertEquals("NoNameOnCard", first.getUser().getUsername());
        Assert.assertEquals("http://www.sbnation.com/users/NoNameOnCard", first.getUser().getUrl());
        //last post
        Post last = context.getPosts().get(
            "http://www.lonestarball.com/2011/2/23/2010990/how-the-rangers-can-make-a-bad-situation-better");
        Assert.assertNotNull(last);
        Assert.assertEquals(2010990L, last.getSbnId());
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.FEBRUARY, 23).getTimeInMillis()), 
                last.getDate());
        Assert.assertEquals("How the Rangers Can Make a Bad Situation Better (??)", last.getTitle());
        Assert.assertEquals(PostType.FAN_SHOT, last.getType());
        Assert.assertNotNull(last.getUser());
        Assert.assertEquals("Jack Daddy", last.getUser().getUsername());
        Assert.assertEquals("http://www.sbnation.com/users/Jack%20Daddy", last.getUser().getUrl());
        //let's also test a null title
        last = context.getPosts().get(
                "http://www.lonestarball.com/2011/2/27/2019667/epl-thread-time-to-cancel-soccer");
        Assert.assertNull(last.getTitle());
        //and next...
        Assert.assertEquals("http://www.lonestarball.com/fanshots?page=2", next);
        //let's try a version where we only get from march first
        context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.MARCH, 1), 
                new GregorianCalendar(2011, Calendar.APRIL, 1));
        //run loader
        next = new PostLoader(context).populateFanShots(doc);
        //count
        Assert.assertEquals(1, context.getPosts().size());
        //next
        Assert.assertNull(next);
        //let's try a version where we only get until feb 28
        context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.JANUARY, 1), 
                new GregorianCalendar(2011, Calendar.FEBRUARY, 28));
        //run loader
        next = new PostLoader(context).populateFanShots(doc);
        //count
        Assert.assertEquals(9, context.getPosts().size());
        //next
        Assert.assertEquals("http://www.lonestarball.com/fanshots?page=2", next);
    }
    
    @Test
    public void frontPageTest() throws Exception {
        //create context (march to april)
        ScrapeContext context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.MARCH, 1), 
                new GregorianCalendar(2011, Calendar.APRIL, 1));
        //parse the local doc
        Document doc = Jsoup.parse(PostLoaderTest.class.getResourceAsStream("frontPageList.html.txt"), 
                "UTF8", "http://www.lonestarball.com/stories/archive/2011");
        //run loader
        boolean next = new PostLoader(context).populateFrontPage(doc, 2011);
        //count
        Assert.assertEquals(3, context.getPosts().size());
        //first post
        Post first = context.getPosts().get(
                "http://www.lonestarball.com/2011/3/1/2022942/ranger-4-and-5-starter-poll");
        Assert.assertNotNull(first);
        Assert.assertEquals(2022942L, first.getSbnId());
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.MARCH, 1).getTimeInMillis()), 
                first.getDate());
        Assert.assertEquals("Ranger #4 and #5 starter poll", first.getTitle());
        Assert.assertEquals(PostType.FRONT_PAGE, first.getType());
        Assert.assertNotNull(first.getUser());
        Assert.assertEquals("Adam J. Morris", first.getUser().getUsername());
        Assert.assertEquals("http://www.sbnation.com/users/Adam%20J.%20Morris", first.getUser().getUrl());
        //last post
        Post last = context.getPosts().get(
            "http://www.lonestarball.com/2011/3/1/2022352/tuesday-morning-rangers-stuff");
        Assert.assertNotNull(last);
        Assert.assertEquals(2022352L, last.getSbnId());
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.MARCH, 1).getTimeInMillis()), 
                last.getDate());
        Assert.assertEquals("Tuesday morning Rangers stuff", last.getTitle());
        Assert.assertEquals(PostType.FRONT_PAGE, last.getType());
        Assert.assertNotNull(last.getUser());
        Assert.assertEquals("Adam J. Morris", last.getUser().getUsername());
        Assert.assertEquals("http://www.sbnation.com/users/Adam%20J.%20Morris", last.getUser().getUrl());
        //and next...
        Assert.assertFalse(next);
        //let's try a version where we only get all of 2011
        context = new ScrapeContext(
                new GregorianCalendar(2011, Calendar.JANUARY, 1), 
                new GregorianCalendar(2011, Calendar.APRIL, 1));
        //run loader
        next = new PostLoader(context).populateFrontPage(doc, 2011);
        //XXX: I don't feel like counting these
        //next
        Assert.assertTrue(next);
    }
}
