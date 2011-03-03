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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.dao.model.User;

/**
 * This is NOT thread safe!
 * 
 * @author Chad Retz
 */
class ScrapeContext {

    private final Calendar from;
    private final Calendar to;
    private final Map<String, Post> posts = new HashMap<String, Post>();
    private final Map<String, User> users = new HashMap<String, User>();
    
    public ScrapeContext(Calendar from, Calendar to) {
        this.from = from;
        this.to = to;
    }
    
    public Calendar getFrom() {
        return from;
    }
    
    public Calendar getTo() {
        return to;
    }

    public Map<String, Post> getPosts() {
        return posts;
    }
    
    public User getUser(String username, String url) {
        User user = users.get(username);
        if (user == null) {
            user = new User();
            user.setUrl(url);
            user.setUsername(username);
            users.put(username, user);
        }
        return user;
    }
    
    public Collection<User> getUsers() {
        return users.values();
    }
}
