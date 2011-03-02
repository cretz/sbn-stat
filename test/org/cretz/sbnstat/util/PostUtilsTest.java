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
package org.cretz.sbnstat.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.cretz.sbnstat.dao.model.Post;
import org.junit.Assert;
import org.junit.Test;

public class PostUtilsTest {

    @Test
    public void testUrlParse() {
        Post post = new Post();
        post.setUrl("http://mydomain.com/2011/4/1/12345/rangers-opening-day");
        PostUtils.populateDateAndSbnIdFromUrl(post);
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2011, Calendar.APRIL, 1).
                getTimeInMillis()), post.getDate());
        Assert.assertEquals(12345L, post.getId());
        //try it w/ double digit month
        post.setUrl("http://mydomain.com/2010/10/15/12345/yankees-suck");
        PostUtils.populateDateAndSbnIdFromUrl(post);
        Assert.assertEquals(new Timestamp(new GregorianCalendar(2010, Calendar.OCTOBER, 15).
                getTimeInMillis()), post.getDate());
        Assert.assertEquals(12345L, post.getId());
    }
}
