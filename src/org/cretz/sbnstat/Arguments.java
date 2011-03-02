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

import java.sql.Date;

import org.cretz.sbnstat.Operation.OperationType;

import com.beust.jcommander.Parameter;

public class Arguments {
    
    @Parameter(names = "-host",
            description = "The hostname or IP address of the MySQL database. Default is localhost.")
    private String databaseHost = "localhost";
    
    @Parameter(names = "-port",
            description = "The port number of the MySQL database. Default is 3306.")
    private int databasePort;
    
    @Parameter(names = "-db", required = true,
            description = "The name of the MySQL database schema.")
    private String databaseName;
    
    @Parameter(names = "-user", required = true,
            description = "The username of the MySQL database.")
    private String databaseUser;
    
    @Parameter(names = "-pass", 
            description = "The password of the MySQL database. If not present, prompt will be presented.")
    private String databasePass;
    
    @Parameter(names = "-domain", 
            description = "The domain of the SBN site sans protocol for use when operation is 'scrape' " +
            "(e.g. lonestarball.com)")
    private String domain;
    
    @Parameter(names = "-from", required = true, converter = DateConverter.class,
            description = "The inclusive date to start from in the format yyyy-mm-dd")
    private Date from;
    
    @Parameter(names = "-to", required = true, converter = DateConverter.class,
            description = "The inclusive date to end at in the format yyyy-mm-dd")
    private Date to;
    
    @Parameter(names = "-out",
            description = "The output file to (over)write to if operation is 'gource' or 'svn'. " +
            "This will output to stdout if not present.")
    private String out;
    
    @Parameter(names = "-oper", required = true, converter = OperationTypeConverter.class,
            description = "Operation to perform. If 'scrape', this will put all scraped data in the database. " +
            "If 'gource', this will output a file that can be read by gource. " +
            "If 'svn', this will output a file that resembles an SVN log.")
    private OperationType operation;

    public String getDatabaseHost() {
        return databaseHost;
    }

    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    public int getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(int databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePass() {
        return databasePass;
    }

    public void setDatabasePass(String databasePass) {
        this.databasePass = databasePass;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }
}
