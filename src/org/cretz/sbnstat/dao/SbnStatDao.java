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

import java.io.InputStreamReader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.cretz.sbnstat.Arguments;
import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.dao.model.User;

public class SbnStatDao {

    private final SqlSessionFactory sessionFactory;
    
    private static final void closeQuietly(SqlSession session) {
        if (session != null) {
            try { session.close(); } catch (Exception e) { }
        }
    }
    
    public SbnStatDao(Arguments arguments) {
        //register the driver
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //build the session factory
        sessionFactory = new SqlSessionFactoryBuilder().build(new InputStreamReader(
                getClass().getResourceAsStream("sql-map-config.xml")));
        DataSource ds;
        try {
            ds = (DataSource) Class.forName(
                    "com.mysql.jdbc.jdbc2.optional.MysqlDataSource").newInstance();
            ds.getClass().getMethod("setServerName").invoke(ds, arguments.getDatabaseHost());
            ds.getClass().getMethod("setPort").invoke(ds, arguments.getDatabasePort());
            ds.getClass().getMethod("setDatabaseName").invoke(ds, arguments.getDatabaseName());
            ds.getClass().getMethod("setUser").invoke(ds, arguments.getDatabaseUser());
            ds.getClass().getMethod("setPassword").invoke(ds, arguments.getDatabasePass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Environment environ = new Environment("production", 
                new JdbcTransactionFactory(), ds);
        sessionFactory.getConfiguration().setEnvironment(environ);
    }
    
    public void persistUnpersistedUsers(Collection<User> users, int batchSize) {
        List<User> userBatch = new ArrayList<User>(batchSize);
        for (User user : users) {
            if (user.getId() == null) {
                userBatch.add(user);
                if (userBatch.size() == batchSize) {
                    persistUsers(userBatch);
                    userBatch.clear();
                }
            }
        }
        if (!userBatch.isEmpty()) {
            persistUsers(userBatch);
        }
    }
    
    private void persistUsers(Collection<User> users) {
        SqlSession session = sessionFactory.openSession(ExecutorType.BATCH);
        try {
            for (User user : users) {
                if (user.getId() == null) {
                    session.insert("SbnStat.insertUser", user);
                }
            }
            session.commit();
        } finally {
            closeQuietly(session);
        }
    }
    
    public void persistPost(Post post) {
        SqlSession session = sessionFactory.openSession();
        try {
            session.insert("SbnStat.insertPost", post);
            session.commit();
        } finally {
            closeQuietly(session);
        }
    }
    
    public void persistComments(Collection<Comment> comments, int batchSize) {
        List<Comment> commentBatch = new ArrayList<Comment>(batchSize);
        for (Comment comment : comments) {
            commentBatch.add(comment);
            if (commentBatch.size() == batchSize) {
                persistComments(commentBatch);
                commentBatch.clear();
            }
        }
        if (!commentBatch.isEmpty()) {
            persistComments(commentBatch);
        }
    }
    
    private void persistComments(Collection<Comment> comments) {
        SqlSession session = sessionFactory.openSession(ExecutorType.BATCH);
        try {
            for (Comment comment : comments) {
                session.insert("SbnStat.insertComment", comment);
            }
            session.commit();
        } finally {
            closeQuietly(session);
        }
    }
}
