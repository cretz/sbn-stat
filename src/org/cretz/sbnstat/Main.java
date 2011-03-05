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
package org.cretz.sbnstat;

import java.sql.Driver;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    static {
        try {
            //register the driver
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) {
        logger.debug("Beginning application");
        Arguments arguments = new Arguments();
        JCommander cmd = new JCommander(arguments);
        if (args == null || args.length == 0 || "-help".equals(args[0])) {
            cmd.usage();
            return;
        }
        try {
            cmd.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage() + " (use -help to see options)");
            System.exit(1);
            return;
        }
        try {
            run(arguments);
        } catch (Exception e) {
            logger.error("Error during execution", e);
            System.exit(1);
        }
    }
    
    public static void run(Arguments arguments) {
        logger.debug("Running operation {}", arguments.getOperation());
        arguments.getOperation().run(arguments);
    }
    
    private Main() {
    }
}
