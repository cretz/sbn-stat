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

import org.cretz.sbnstat.dao.model.Post;
import org.cretz.sbnstat.dao.model.PostType;
import org.cretz.sbnstat.util.PostUtils;
import org.cretz.sbnstat.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PostLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(PostLoader.class);
    
    private final ScrapeContext context;
    
    public PostLoader(ScrapeContext context) {
        this.context = context;
    }

    /**
     * Returns the URL to continue with if this should continue or
     * null if this should be considered finished.
     * 
     * @return
     */
    public String populateFanPosts(Document recentList) {
        //grab all the title elements
        Elements elements = recentList.select("span.title");
        //loop through and parse
        for (Element element : elements) {
            Element anchor = element.getElementsByTag("a").first();
            Post post = new Post();
            post.setUrl(PostUtils.fixPostUrl(anchor.attr("href")));
            PostUtils.populateDateAndSbnIdFromUrl(post);
            //too late?
            if (context.getTo().getTimeInMillis() < post.getDate().getTime()) {
                //skip
                continue;
            }
            //too early?
            if (context.getFrom().getTimeInMillis() > post.getDate().getTime()) {
                //get out
                return null;
            }
            //already there?
            if (context.getPosts().containsKey(post.getUrl())) {
                continue;
            }
            post.setType(PostType.FAN_POST);
            post.setTitle(StringUtils.normalize(anchor.ownText()));
            //get the author
            Element authorAnchor = element.select("span.author > a").first();
            if (authorAnchor == null) {
                //no anchor means it's a deleted user, 
                //  so we grab the username after the first instance of "by"
                authorAnchor = element.select("span.author").first();
                if (authorAnchor == null) {
                    logger.warn("Can't find author on post" + post.getUrl());
                    continue;
                }
                String text = authorAnchor.ownText();
                text = text.substring(text.indexOf("by") + 3).trim();
                post.setUser(context.getUser(StringUtils.normalize(text), null));
            } else {
                post.setUser(context.getUser(StringUtils.normalize(authorAnchor.ownText()), 
                        authorAnchor.attr("href")));
            }
            context.getPosts().put(post.getUrl(), post);
        }
        //is there a "next"?
        Elements nextAnchors = recentList.select("a.page-next");
        return nextAnchors.isEmpty() ? null : nextAnchors.first().attr("abs:href");
    }
    
    /**
     * Returns true if there are more or false if this should be considered finished.
     * 
     * @return
     */
    public boolean populateFanShots(Document allList) {
        //grab all the title elements
        Elements elements = allList.select("div.fanshot");
        //loop through and parse
        for (Element element : elements) {
            Element anchor = element.select("span.comments > a").first();
            Post post = new Post();
            post.setUrl(PostUtils.fixPostUrl(anchor.attr("href").replace("#comments", "")));
            PostUtils.populateDateAndSbnIdFromUrl(post);
            //too late?
            if (context.getTo().getTimeInMillis() < post.getDate().getTime()) {
                //skip
                continue;
            }
            //too early?
            if (context.getFrom().getTimeInMillis() > post.getDate().getTime()) {
                //get out
                return false;
            }
            //already there?
            if (context.getPosts().containsKey(post.getUrl())) {
                continue;
            }
            post.setType(PostType.FAN_SHOT);
            //not all of them have titles
            Elements title = element.select("h3.link-title > a");
            if (!title.isEmpty()) {
                post.setTitle(StringUtils.normalize(title.first().ownText()));
            }
            //get the author
            Element authorAnchor = element.select("span.user-avatar > a").first();
            //if the user is inactive just grab the text and have a null URL
            if (authorAnchor == null) {
                authorAnchor = element.select("span.user-avatar").first();
                post.setUser(context.getUser(StringUtils.normalize(authorAnchor.ownText()), null));
            } else {
                post.setUser(context.getUser(StringUtils.normalize(authorAnchor.ownText()), 
                        authorAnchor.attr("href")));
            }
            context.getPosts().put(post.getUrl(), post);
        }
        //is there a "next"?
        Elements nextAnchors = allList.select("a.page-next");
        return !nextAnchors.isEmpty();
    }
    
    /**
     * Returns true if the previous year should be loaded, false otherwise
     * 
     * @param frontPage
     * @param year
     * @return
     */
    public boolean populateFrontPage(Document frontPage, int year) {
        Elements elements = frontPage.select("div.archive-list > div.entry");
        for (Element element : elements) {
            Element anchor = element.select("h5 > a").first();
            Post post = new Post();
            post.setUrl(PostUtils.fixPostUrl(anchor.attr("href")));
            PostUtils.populateDateAndSbnIdFromUrl(post);
            //too late?
            if (context.getTo().getTimeInMillis() < post.getDate().getTime()) {
                //skip
                continue;
            }
            //too early?
            if (context.getFrom().getTimeInMillis() > post.getDate().getTime()) {
                //get out
                return false;
            }
            //already there?
            if (context.getPosts().containsKey(post.getUrl())) {
                continue;
            }
            post.setType(PostType.FRONT_PAGE);
            //not all of them have titles
            post.setTitle(StringUtils.normalize(anchor.ownText()));
            //get the author
            Element authorAnchor = element.select("p.byline > a").first();
            post.setUser(context.getUser(StringUtils.normalize(authorAnchor.ownText()), 
                    authorAnchor.attr("href")));
            context.getPosts().put(post.getUrl(), post);
        }
        //is there a link for the previous year?
        return !frontPage.select("a[href=/stories/archive/" + (year - 1) + "]").isEmpty();
    }
}
