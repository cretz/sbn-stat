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
package org.cretz.sbnstat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {

    public static Connection connectToMySqlDatabase(String host, int port,
            String dbName, String username, String password) throws SQLException {
        return DriverManager.getConnection(new StringBuilder("jdbc:mysql://").
                append(host).append(':').
                append(port).append('/').
                append(dbName).toString(), 
                username, password);
    }
    
    public static void closeQuietly(Connection connection) {
        if (connection != null) {
            try { connection.close(); } catch (Exception e) { }
        }
    }
    
    public static void closeQuietly(Statement statement) {
        if (statement != null) {
            try { statement.close(); } catch (Exception e) { }
        }
    }
    
    public static void closeQuietly(ResultSet resultSet) {
        if (resultSet != null) {
            try { resultSet.close(); } catch (Exception e) { }
        }
    }
    
    private JdbcUtils() {
    }
}
