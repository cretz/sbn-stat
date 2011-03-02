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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.cretz.sbnstat.dao.model.Comment;
import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CommentLoader {

    //XXX: This removes thread safety from this class
    private final DateFormat dateFormat = new SimpleDateFormat(
            "MMM dd, yyyy hh:mm aa zzz");
    private final ScrapeContext context;
    
    public CommentLoader(ScrapeContext context) {
        this.context = context;
    }
    
    public List<Comment> loadCommentsAndUpdatePost(Document doc, Post post) {
        //first let's update the post with the time posted and recs
        Elements timePosted = doc.select("p.byline > a.plain");
        if (!timePosted.isEmpty()) {
            //fan shots won't have this
            try {
                post.setDate(new Timestamp(dateFormat.parse(
                        StringUtils.normalize(timePosted.first().ownText().trim())).getTime()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        Elements recsSpan = doc.select("span.flag-counts > span");
        if (recsSpan.isEmpty()) {
            //fan shots' recs are in a different area
            recsSpan = doc.select("span.recs > span");
        }
        post.setRecommendationCount(Integer.parseInt(recsSpan.first().ownText()));
        //now, let's begin with the comments
        List<Comment> comments = new ArrayList<Comment>();
        for (Element element : doc.select("div.comment_master_list > div.citem")) {
            recursivelyAddComments(element, comments, post, null, null);
        }
        return comments;
    }
    
    private void recursivelyAddComments(Element citem, List<Comment> comments,
            Post post, Comment parent, Comment topLevel) {
        Comment comment = new Comment();
        //post, parent, top level, and depth
        comment.setPost(post);
        comment.setParent(parent);
        comment.setTopLevelParent(topLevel);
        comment.setDepth(parent == null ? 0 : parent.getDepth() + 1);
        //get that id
        String id = citem.id();
        id = id.substring(id.lastIndexOf('_') + 1);
        comment.setSbnId(Long.parseLong(id));
        //subject
        Elements elements = citem.select("div.comment > h5.comment_title > a");
        if (!elements.isEmpty()) {
            comment.setSubject(StringUtils.normalize(elements.first().ownText()));
        }
        //contents
        elements = citem.select("div.comment > div.cbody > p");
        if (!elements.isEmpty()) {
            comment.setContents(elements.first().html());
        }
        //by
        elements = citem.select("div.comment > p.by > a");
        comment.setUser(context.getUser(StringUtils.normalize(elements.first().ownText()), 
                elements.first().attr("href")));
        //time
        elements = citem.select("div.comment > p.by > span.time > a");
        try {
            comment.setDate(new Timestamp(dateFormat.parse(
                    StringUtils.normalize(elements.first().ownText().trim())).getTime()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //recs
        elements = citem.select("div.comment > p.by > span.cactions > span:not(.tools)");
        if (!elements.isEmpty()) {
            comment.setRecommendationCount(Integer.parseInt(StringUtils.normalize(
                    elements.first().ownText()).replace("recs", "").trim()));
        }
        //add to list
        comments.add(comment);
        //now all children
        for (Element child : citem.select("div.citem")) {
            recursivelyAddComments(child, comments, post, comment, 
                    topLevel == null ? comment : topLevel);
        }
    }
}
