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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.UUID;

import com.google.common.io.Closeables;
import com.google.common.io.Files;

class Cache {
    
    //TODO: needs moar test case coverage!

    private final File directory;
    private final Properties urls;
    private final File urlsFile;
    
    public Cache(File directory) throws IOException {
        this.directory = directory;
        //directory doesn't exist? create it then...
        if (!directory.exists()) {
            directory.mkdirs();
        }
        urls = new Properties();
        //load the file if it's there
        urlsFile = new File(directory, "urls.properties");
        if (urlsFile.exists()) {
            Reader reader = new FileReader(urlsFile);
            try {
                urls.load(reader);
            } finally {
                Closeables.closeQuietly(reader);
            }
        }
    }
    
    public void add(String url, String html) throws IOException {
        //create a UUID that will define this document
        String uuid;
        File file;
        do {
            uuid = UUID.randomUUID().toString();
            file = new File(directory, uuid + ".html");
        } while (file.exists());
        //save HTML
        Files.write(html, file, Charset.forName("UTF8"));
        //add to URL property list
        urls.setProperty(url, uuid);
        //write the urls file
        Writer writer = new FileWriter(urlsFile);
        try {
            urls.store(writer, "Cache");
        } finally {
            Closeables.closeQuietly(writer);
        }
    }
    
    public String load(String url) throws IOException {
        String uuid = urls.getProperty(url);
        //if it's not there, screw it...return null
        if (uuid == null) {
            return null;
        }
        File file = new File(directory, uuid + ".html");
        if (!file.exists()) {
            return null;
        }
        //load it
        return Files.toString(file, Charset.forName("UTF8"));
    }
}
