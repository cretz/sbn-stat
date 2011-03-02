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
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.cretz.sbnstat.dao.model.Post;

public class PostUtils {
    
    public static void populateDateAndSbnIdFromUrl(Post post) {
        //Format: http://mydomain.com/yyyy/MM/dd/id/someotherstuff
        //so the first three slashed portions after a domain
        StringTokenizer st = new StringTokenizer(post.getUrl(), "/");
        boolean foundDomain = false;
        String token;
        Integer year = null;
        Integer month = null;
        Integer day = null;
        Long id = null;
        while (st.hasMoreElements()) {
            token = st.nextToken();
            if (!foundDomain) {
                foundDomain = token.contains(".");
            } else if (year == null) {
                year = Integer.parseInt(token);
            } else if (month == null) {
                month = Integer.parseInt(token);
            } else if (day == null) {
                day = Integer.parseInt(token);
            } else if (id == null) {
                id = Long.parseLong(token);
            } else {
                break;
            }
        }
        post.setDate(new Timestamp(new GregorianCalendar(year, month - 1, day).getTimeInMillis()));
        post.setSbnId(id);
    }
    
    private PostUtils() {
    }
}
