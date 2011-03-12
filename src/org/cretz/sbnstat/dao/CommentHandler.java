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
package org.cretz.sbnstat.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.cretz.sbnstat.dao.model.PostType;
import org.cretz.sbnstat.util.JdbcUtils;

public interface CommentHandler {

    void onComment(CommentInfo info) throws Exception;
    
    public static class CommentInfo {
        
        private final ResultSet rs;
        
        CommentInfo(ResultSet rs) {
            this.rs = rs;
        }
        
        boolean next() throws SQLException {
            return rs.next();
        }
        
        void close() {
            JdbcUtils.closeQuietly(rs);
        }
        
        public long getId() throws SQLException {
            return rs.getLong("Id");
        }
        
        public int getDepth() throws SQLException {
            return rs.getInt("Depth");
        }
        
        public String getSubject() throws SQLException {
            return rs.getString("Subject");
        }
        
        public int getRecommendationCount() throws SQLException {
            return rs.getInt("RecommendationCount");
        }
        
        public Timestamp getDate() throws SQLException {
            return rs.getTimestamp("Date");
        }
        
        public String getCommentUsername() throws SQLException {
            return rs.getString("CommentUsername");
        }
        
        public String getCommentUserThumbnail() throws SQLException {
            return rs.getString("CommentUserThumbnail");
        }
        
        public PostType getPostType() throws SQLException {
            return PostType.values()[rs.getInt("PostType")];
        }
        
        public Timestamp getPostDate() throws SQLException {
            return rs.getTimestamp("PostDate");
        }
        
        public String getPostTitle() throws SQLException {
            return rs.getString("PostTitle");
        }
        
        public String getPostUrl() throws SQLException {
            return rs.getString("PostUrl");
        }
        
        public int getPostRecommendationCount() throws SQLException {
            return rs.getInt("PostRecommendationCount");
        }
        
        public String getPostUsername() throws SQLException {
            return rs.getString("PostUsername");
        }
        
        public String getPostThumbnail() throws SQLException {
            return rs.getString("PostThumbnail");
        }
        
        public Long getParentId() throws SQLException {
            long val = rs.getLong("ParentId");
            return val == 0 ? null : val;
        }
        
        public String getParentSubject() throws SQLException {
            return rs.getString("ParentSubject");
        }
    }
}
