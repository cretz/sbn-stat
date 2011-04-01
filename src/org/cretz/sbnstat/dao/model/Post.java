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
package org.cretz.sbnstat.dao.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Post implements Serializable {

    private static final long serialVersionUID = 2651005937734929042L;

    private long id;
    private long sbnId;
    private User user;
    private PostType type;
    private Timestamp date;
    private String title;
    private String url;
    private int recommendationCount;
    private boolean commentsLoaded;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSbnId() {
        return sbnId;
    }

    public void setSbnId(long sbnId) {
        this.sbnId = sbnId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public int getRecommendationCount() {
        return recommendationCount;
    }
    
    public void setRecommendationCount(int recommendationCount) {
        this.recommendationCount = recommendationCount;
    }
    
    public boolean isCommentsLoaded() {
        return commentsLoaded;
    }
    
    public void setCommentsLoaded(boolean commentsLoaded) {
        this.commentsLoaded = commentsLoaded;
    }
}
