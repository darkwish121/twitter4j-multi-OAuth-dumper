/*
 * Copyright 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import misc.*;
import account.GetRateLimitStatus;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class MysqlDB {

    Connection con;
    ConstVars StaticVars = new ConstVars();
    Misc misc = new Misc();

    public MysqlDB() throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        try {
            con = (Connection) DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/"
                    + StaticVars.DataBaseName,
                    StaticVars.DataBaseUser, StaticVars.DataBasePass);
        } catch (SQLException e) {
        }
    }

    public int numRows(ResultSet result) throws SQLException {
        result.last();
        int r = result.getRow();
        result.beforeFirst();
        return r;
    }

    public ResultSet selectQ(String sql) throws SQLException {
        PreparedStatement statement = con.prepareStatement(sql);
        ResultSet result = statement.executeQuery();
        return result;
    }

    public boolean Update(String table, String fieldValues, String where) 
            throws SQLException {
        PreparedStatement statement = con.prepareStatement("UPDATE " + table 
                + " SET " + fieldValues + " WHERE " + where);
        boolean result = statement.execute();
        return result;
    }

    public ConstVars loadOAuthUser(String endpoint) throws SQLException {
        // System.out.println("DEBUG MysqlDB.java");
        while (true) {

            ResultSet appAuthsList = selectQ("SELECT * FROM `app_auths` "
                    + "ORDER BY `lastUsed` ASC");

            while (appAuthsList.next()) {

                int authUID = appAuthsList.getInt("id");
                String screen_name = appAuthsList.getString("uScreenName");
                // System.out.println(screen_name);
                String consumer_key = appAuthsList.getString("cKey");
                String consumer_secret = appAuthsList.getString("cSecret");
                String user_token = appAuthsList.getString("uToken");
                String user_secret = appAuthsList.getString("uSecret");

                // Send OAuth credentials and get rate limit of endpoint
                String[] args = {endpoint, consumer_key, consumer_secret, 
                    user_token, user_secret, screen_name};
                StaticVars = GetRateLimitStatus.getRateLimit(args);

                int RemainingCalls = StaticVars.Remaining;
                if (RemainingCalls > 2) {

                    StaticVars.screen_name = screen_name;
                    StaticVars.consumer_key = consumer_key;
                    StaticVars.consumer_secret = consumer_secret;
                    StaticVars.user_token = user_token;
                    StaticVars.user_secret = user_secret;

                    // update current OAuth user
                    long lastUsed = misc.getUnixTimeStamp();
                    String fieldValues = "lastUsed = " + lastUsed;
                    String where = "`id` = " + authUID;
                    Update("`app_auths`", fieldValues, where);

                    // return OAuth credentials to calling script
                    return StaticVars;
                }
            }
            System.out.println("All OAuth Users have no limit so sleeping.");
            // sleep
            misc.pause(30);
        }
    }

    private int SetAvailableAuths() throws SQLException {
        String selectQuery = "SELECT * FROM `app_auths`";

        ResultSet results = selectQ(selectQuery);

        int numRows = numRows(results);

        return numRows;
    }

    public ConstVars loadOAuthUser(
            String endpoint,
            int TOTAL_JOBS,
            int JOB_NO) throws SQLException {
        // System.out.println("DEBUG MysqlDB.java");

        int availableAuths = SetAvailableAuths();

        int chunkSize = (int) Math.ceil(availableAuths / TOTAL_JOBS);
        int offSet = JOB_NO * chunkSize;

        while (true) {

            ResultSet appAuthsList = selectQ("SELECT * FROM ( SELECT * FROM "
                    + "app_auths limit " + offSet + "," + chunkSize 
                    + " ) as t1 ORDER BY `lastUsed`");

            while (appAuthsList.next()) {

                int authUID = appAuthsList.getInt("id");
                String screen_name = appAuthsList.getString("uScreenName");
                String consumer_key = appAuthsList.getString("cKey");
                String consumer_secret = appAuthsList.getString("cSecret");
                String user_token = appAuthsList.getString("uToken");
                String user_secret = appAuthsList.getString("uSecret");

                // temp
                System.out.println("Going to check OAuth limit "
                        + "of this screen name: " + screen_name);
                // Send OAuth credentials and get rate limit of endpoint
                String[] args = {endpoint, consumer_key, consumer_secret, 
                    user_token, user_secret, screen_name};
                StaticVars = GetRateLimitStatus.getRateLimit(args);

                int RemainingCalls = StaticVars.Remaining;
                if (RemainingCalls > 2) {

                    StaticVars.screen_name = screen_name;
                    StaticVars.consumer_key = consumer_key;
                    StaticVars.consumer_secret = consumer_secret;
                    StaticVars.user_token = user_token;
                    StaticVars.user_secret = user_secret;

                    // update current OAuth user
                    long lastUsed = misc.getUnixTimeStamp();
                    String fieldValues = "lastUsed = " + lastUsed;
                    String where = "`id` = " + authUID;
                    Update("`app_auths`", fieldValues, where);

                    // return OAuth credentials to calling script
                    return StaticVars;
                }
            }
            System.out.println("All OAuth Users have no limit so sleeping.");
            // sleep
            misc.pause(30);
        }
    }
}
