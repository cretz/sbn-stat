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
package org.cretz.sbnstat.gource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cretz.sbnstat.Arguments;
import org.cretz.sbnstat.Operation;
import org.cretz.sbnstat.dao.CommentHandler;
import org.cretz.sbnstat.dao.SbnStatDao;
import org.cretz.sbnstat.util.DateUtils;
import org.cretz.sbnstat.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

public class Gourcer implements Operation {
    
    //XXX: I am just hacking this together, I know there are performance and memory issues
    
    private static final Logger logger = LoggerFactory.getLogger(Gourcer.class);
    
    private static String getGourceLine(Date timestamp, 
            String username, char type, String title) {
        return getGourceLine(timestamp, username, type, title, null);
    }
    
    private static String getGourceLine(Date timestamp, 
            String username, char type, String title, String color) {
        StringBuilder builder = new StringBuilder().
            append(timestamp.getTime() / 1000).
            append('|').
            append(username).
            append('|').
            append(type).
            append('|').
            append(title.replace('|', '-'));
        if (color != null) {
            builder.append('|');
            builder.append(color);
        }
        return builder.toString();
    }
    
    @Override
    public void run(Arguments args) {
        try {
            //ok, we're gonna dump to a custom file format
            //dates
            Calendar from = DateUtils.toBeginningOfDayCalendar(args.getFrom());
            Calendar to = DateUtils.toEndOfDayCalendar(args.getTo());
            logger.info("Dumping gource custom log from {} to {}",
                    from.getTime(), to.getTime());
            //build connection
            Connection conn = JdbcUtils.connectToMySqlDatabase(
                    args.getDatabaseHost(), 
                    args.getDatabasePort(), 
                    args.getDatabaseName(), 
                    args.getDatabaseUser(), 
                    args.getDatabasePass());
            //maps to hold data
            final NavigableSet<CommentState> allComments = new TreeSet<CommentState>();
            final Map<Long, CommentState> mappedComments = new HashMap<Long, CommentState>();
            final Map<String, PostState> mappedPosts = new HashMap<String, PostState>();
            try {
                //load data
                new SbnStatDao(conn).readComments(new Timestamp(from.getTimeInMillis()), 
                        new Timestamp(to.getTimeInMillis()), new CommentHandler() {
                            @Override
                            public void onComment(CommentInfo info) throws Exception {
                                PostState post = mappedPosts.get(info.getPostUrl());
                                if (post == null) {
                                    post = new PostState(info.getPostUrl(), info.getPostTitle(), 
                                            info.getPostUsername(), info.getPostDate());
                                    mappedPosts.put(post.url, post);
                                }
                                CommentState state = new CommentState(info.getId(), post, 
                                        mappedComments.get(info.getParentId()), info.getDate(), 
                                        info.getCommentUsername(), info.getSubject(),
                                        info.getCommentUserThumbnail());
                                allComments.add(state);
                                mappedComments.put(state.id, state);
                            }
                        });
            } finally {
                JdbcUtils.closeQuietly(conn);
            }
            //loop through and write lines
            PrintStream out;
            if (args.getOut() != null) {
                out = new PrintStream(new File(args.getOut()));
            } else {
                out = System.out;
            }
            try {
                Set<String> processedPosts = new HashSet<String>();
                File dir = args.getDir() == null ? null : new File(args.getDir());
                if (dir != null && !dir.exists()) {
                    dir.mkdirs();
                }
                List<String> lines = new ArrayList<String>();
                for (CommentState comment : allComments) {
                    //let's grab the user avatar image
                    if (dir != null && comment.thumbnail != null) {
                        saveUserAvatar(dir, comment);
                    }
                    String title;
                    if (comment.post.title != null) {
                        title = comment.post.title;
                    } else {
                        title = "";
                    }
                    if (!processedPosts.contains(comment.post.url)) {
                        //add a post
                        lines.add(getGourceLine(comment.post.date, comment.post.username, 
                                'A', "/" + title.replace('/', ' ')));
                        processedPosts.add(comment.post.url);
                    }
                    //make the parent string (bad performance)
                    String path = "";
                    CommentState parent = comment;
                    while (parent != null) {
                        path = "/" + parent.subject.replace('/', ' ') + path;
                        parent = parent.parent;
                    }
                    lines.add(getGourceLine(comment.date, comment.username, 
                            comment.parent == null ? 'A' : 'M', 
                            "/" + title.replace('/', ' ') + path));
                }
                Collections.sort(lines);
                for (String line : lines) {
                    out.println(line);
                }
            } finally {
                Closeables.closeQuietly(out);                
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void saveUserAvatar(File dir, CommentState comment) throws Exception {
        File image = new File(dir, comment.username + ".png");
        if (!image.exists()) {
            logger.debug("Waiting 3 seconds, then loading image: {}", comment.thumbnail);
            Thread.sleep(3000);
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(comment.thumbnail);
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = entity.getContent();
                    BufferedImage img = ImageIO.read(is);
                    ImageIO.write(img, "png", image);
                } finally {
                    Closeables.closeQuietly(is);
                    Closeables.closeQuietly(fos);
                }
            }
        }
    }

    private static class CommentState implements Comparable<CommentState> {
        private final long id;
        private final PostState post;
        private final CommentState parent;
        private final Date date;
        private final String username;
        private final String subject;
        private final String thumbnail;
        
        public CommentState(long id, PostState post, CommentState parent, Date date, 
                String username, String subject, String thumbnail) {
            this.id = id;
            this.post = post;
            this.parent = parent;
            this.date = date;
            this.username = username;
            this.subject = subject == null ? "" : subject;
            this.thumbnail = thumbnail;
        }

        @Override
        public int compareTo(CommentState other) {
            return date.compareTo(other.date);
        }
    }
    
    private static class PostState {
        private final String url;
        private final String title;
        private final String username;
        private final Date date;
        
        public PostState(String url, String title, String username, Date date) {
            this.url = url;
            this.title = title;
            this.username = username;
            this.date = date;
        }
    }
}
