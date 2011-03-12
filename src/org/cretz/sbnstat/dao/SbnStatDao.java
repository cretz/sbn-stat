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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Properties;

import org.cretz.sbnstat.dao.CommentHandler.CommentInfo;
import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.dao.model.User;
import org.cretz.sbnstat.util.JdbcUtils;

public class SbnStatDao {
    
    private static final Properties queries = new Properties();
    
    static {
        try {
            //load the properties
            queries.loadFromXML(SbnStatDao.class.getResourceAsStream("queries.xml"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private final Connection conn;
    
    public SbnStatDao(Connection conn) {
        this.conn = conn;
    }
    
    public void persistUnpersistedUsers(Collection<User> users) throws SQLException {
        PreparedStatement stmt = null;
        PreparedStatement thumbnailUpdateStmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(queries.getProperty("User.Insert"), Statement.RETURN_GENERATED_KEYS);
            thumbnailUpdateStmt = conn.prepareStatement(queries.getProperty("User.UpdateThumbnail"));
            for (User user : users) {
                if (user.getId() == null) {
                    //Username, Url, Thumbnail
                    stmt.setString(1, user.getUsername());
                    stmt.setString(2, user.getUrl());
                    stmt.setString(3, user.getThumbnail());
                    stmt.executeUpdate();
                    rs = stmt.getGeneratedKeys();
                    rs.next();
                    user.setId(rs.getLong(1));
                    rs.close();
                    if (user.getThumbnail() != null) {
                        user.setThumbnailPersisted(true);
                    }
                } else if (user.getThumbnail() != null && !user.isThumbnailPersisted()) {
                    thumbnailUpdateStmt.setString(1, user.getThumbnail());
                    thumbnailUpdateStmt.setLong(2, user.getId());
                    thumbnailUpdateStmt.executeUpdate();
                    user.setThumbnailPersisted(true);
                }
            }
        } finally {
            JdbcUtils.closeQuietly(rs);
            JdbcUtils.closeQuietly(thumbnailUpdateStmt);
            JdbcUtils.closeQuietly(stmt);
        }
    }
    
    public void persistPost(Post post) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(queries.getProperty("Post.Insert"), Statement.RETURN_GENERATED_KEYS);
            //SbnId, UserId, Type, Date, Title, Url, RecommendationCount
            stmt.setLong(1, post.getSbnId());
            stmt.setLong(2, post.getUser().getId());
            stmt.setInt(3, post.getType().ordinal());
            stmt.setTimestamp(4, post.getDate());
            stmt.setString(5, post.getTitle());
            stmt.setString(6, post.getUrl());
            stmt.setInt(7, post.getRecommendationCount());
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            rs.next();
            post.setId(rs.getLong(1));
        } finally {
            JdbcUtils.closeQuietly(rs);
            JdbcUtils.closeQuietly(stmt);
        }
    }
    
    public void persistComments(Collection<Comment> comments) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(queries.getProperty("Comment.Insert"), Statement.RETURN_GENERATED_KEYS);
            for (Comment comment : comments) {
                //SbnId, ParentId, TopLevelParentId, UserId, PostId, Depth, Subject,
                //  Contents, RecommendationCount, Date
                stmt.setLong(1, comment.getSbnId());
                if (comment.getParent() == null) {
                    stmt.setNull(2, Types.BIGINT);
                } else {
                    stmt.setLong(2, comment.getParent().getId());
                }
                if (comment.getTopLevelParent() == null) {
                    stmt.setNull(3, Types.BIGINT);
                } else {
                    stmt.setLong(3, comment.getTopLevelParent().getId());
                }
                stmt.setLong(4, comment.getUser().getId());
                stmt.setLong(5, comment.getPost().getId());
                stmt.setInt(6, comment.getDepth());
                stmt.setString(7, comment.getSubject());
                stmt.setString(8, comment.getContents());
                stmt.setInt(9, comment.getRecommendationCount());
                stmt.setTimestamp(10, comment.getDate());
                stmt.executeUpdate();
                rs = stmt.getGeneratedKeys();
                rs.next();
                comment.setId(rs.getLong(1));
                rs.close();
            }
        } finally {
            JdbcUtils.closeQuietly(rs);
            JdbcUtils.closeQuietly(stmt);
        }
    }
    
    public void readComments(Timestamp from, Timestamp to, CommentHandler handler) throws Exception {
        PreparedStatement stmt = null;
        CommentInfo info = null;
        try {
            stmt = conn.prepareStatement(queries.getProperty("Comment.GetBetweenDates"));
            stmt.setTimestamp(1, from);
            stmt.setTimestamp(2, to);
            info = new CommentInfo(stmt.executeQuery());
            while (info.next()) {
                handler.onComment(info);
            }
        } finally {
            if (info != null) {
                info.close();
            }
            JdbcUtils.closeQuietly(stmt);
        }
    }
}
