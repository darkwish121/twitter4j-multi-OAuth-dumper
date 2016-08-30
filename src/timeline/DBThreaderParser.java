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
package timeline;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import misc.Misc;
import mySQL.MysqlDB;
import oauth.AppOAuth;

import org.apache.commons.lang3.StringEscapeUtils;

import twitter4j.HttpResponseCode;
import twitter4j.JSONException;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class DBThreaderParser {

    public static void main(String[] args) throws ClassNotFoundException,
            SQLException, JSONException, IOException {

        // Check how many arguments were passed in
        if ((args == null) || (args.length < 5)) {
            System.err.println("5 Parameters are required to launch a Job.");
            System.err.println("First: String 'OUTPUT_PATH'");
            System.err.println("Second: (int) Total Number Of Jobs");
            System.err.println("Third: (int) This Job Number");
            System.err.println("Fourth: (int) Seconds to pause between next launch");
            System.err.println("Fifth: (int) Number of Tweets to get. Max 3200");
            System.err.println("Example: fileName.class /output/path 10 1 2 3200");
            System.exit(-1);
        }

        String OutputDirPath = null;
        try {
            OutputDirPath = StringEscapeUtils.escapeJava(args[0]);
        } catch (Exception e) {
            System.err.println("Argument" + args[1] + " must be an String.");
            System.exit(1);
        }

        int TOTAL_JOBS = 0;
        try {
            TOTAL_JOBS = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[1] + " must be an integer.");
            System.exit(1);
        }

        int JOB_NO = 0;
        try {
            JOB_NO = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[2] + " must be an integer.");
            System.exit(1);
        }

        int NUMBER_OF_TWEETS = 0;
        try {
            NUMBER_OF_TWEETS = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[4] + " must be an integer.");
            System.exit(1);
        }

        MysqlDB DB = new MysqlDB();
        AppOAuth AppOAuths = new AppOAuth();
        Misc helpers = new Misc();
        String endpoint = "/statuses/user_timeline";

        try {

            String totalRowsQuery = "SELECT count( * ) as wLoad FROM `twitter_users`"
                    + " WHERE `tweets_dumped_all` = 0";
            ResultSet totalRowsResults = DB.selectQ(totalRowsQuery);

            int TotalWorkLoad = 0;
            while (totalRowsResults.next()) {
                TotalWorkLoad = totalRowsResults.getInt("wLoad");
            }

            if (TotalWorkLoad < 1) {
                System.err.println("No User in database to get Tweets");
                System.exit(-1);
            }

            // Free memmory
            totalRowsResults = null;

            if (TOTAL_JOBS > TotalWorkLoad) {
                System.err.println("Number of jobs are more than total work"
                        + " load Please reduce Number of jobs to launch.");
                System.exit(-1);
            }

            float TotalWorkLoadf = TotalWorkLoad;
            float TOTAL_JOBSf = TOTAL_JOBS;
            float res = (TotalWorkLoadf / TOTAL_JOBSf);

            int chunkSize = (int) Math.ceil(res);
            int offSet = JOB_NO * chunkSize;

            String selectQuery = "SELECT `id`,`targeteduser` FROM "
                    + "`twitter_users` WHERE "
                    + "`tweets_dumped_all` = 0 LIMIT "
                    + offSet + "," + chunkSize;

            ResultSet results = DB.selectQ(selectQuery);

            int numRows = DB.numRows(results);
            if (numRows < 1) {
                System.err.println("No User in database to get Tweets"
                        + " with offset " + offSet + " and limit " + chunkSize);
                System.exit(-1);
            }

            /**
             * wait before launching actual job
             */
            int secondsToPause = 0;
            try {
                secondsToPause = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[3] + " must be an integer.");
                System.exit(-1);
            }

            secondsToPause = (TOTAL_JOBS * secondsToPause) - (JOB_NO * secondsToPause);
            System.out.println("secondsToPause: " + secondsToPause);
            helpers.pause(secondsToPause);
            /**
             * wait before launching actual job
             */

            TwitterFactory tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS, JOB_NO);
            Twitter twitter = tf.getInstance();

            int RemainingCalls = AppOAuths.RemainingCalls - 2;
            int RemainingCallsCounter = 0;
            System.out.println("First Time Remianing Calls: " + RemainingCalls);

            String Screen_name = AppOAuths.screen_name;
            System.out.println("First Time Loaded OAuth Screen_name: "
                    + Screen_name);

            System.out.println("User's Tweets");

            System.out.println("Trying to create output directory");
            String filesPath = OutputDirPath + "/";
            File theDir = new File(filesPath);

            // If the directory does not exist, create it
            if (!theDir.exists()) {
                try {
                    theDir.mkdirs();

                } catch (SecurityException se) {

                    System.err.println("Could not create output "
                            + "directory: " + OutputDirPath);
                    System.err.println(se.getMessage());
                    System.exit(-1);
                }
            }

            System.out.flush();

            OUTERMOST:
            while (results.next()) {

                int targetedUserID = results.getInt("id");
                String targetedUser = results.getString("targeteduser");
                System.out.println("Targeted User: " + targetedUser);

                // Create User file to push tweets in it
                String fileName = filesPath + "/" + targetedUser;
                PrintWriter writer = new PrintWriter(fileName, "UTF-8");

                // Call different functions for screen_name and id_str
                Boolean chckedNumaric = helpers.isNumeric(targetedUser);

                List<Status> statuses = new ArrayList<>();
                int size = statuses.size();
                int pageno = 1;
                int totalTweets = 0;
                boolean tweetCounterReached = false;
                System.out.println("NUMBER_OF_TWEETS to get:" + NUMBER_OF_TWEETS);
                while (true) {
                    try {

                        Paging page = new Paging(pageno++, 200);

                        if (chckedNumaric) {

                            long LongValueTargetedUser = Long.valueOf(
                                    targetedUser).longValue();

                            statuses.addAll(twitter.getUserTimeline(
                                    LongValueTargetedUser, page));

                            if (statuses.size() > 0) {
                                for (Status status : statuses) {
                                    String rawJSON = TwitterObjectFactory
                                            .getRawJSON(status);
                                    writer.println(rawJSON);

                                    totalTweets += 1;
                                    if (totalTweets >= NUMBER_OF_TWEETS) {
                                        tweetCounterReached = true;
                                        break;
                                    }
                                }
                                if (tweetCounterReached) {
                                    break;
                                }
                            }
                        } else {

                            statuses.addAll(twitter.getUserTimeline(
                                    targetedUser, page));

                            if (statuses.size() > 0) {
                                for (Status status : statuses) {
                                    String rawJSON = TwitterObjectFactory
                                            .getRawJSON(status);
                                    writer.println(rawJSON);

                                    totalTweets += 1;
                                    if (totalTweets >= NUMBER_OF_TWEETS) {
                                        tweetCounterReached = true;
                                        break;
                                    }
                                }
                                if (tweetCounterReached) {
                                    break;
                                }
                            }
                        }

                        // If user's total tweet are less 
                        // than 195 then no next call
                        if (size == 0) {
                            if (totalTweets < 195) {
                                break;
                            }
                        }

                        // If user's all tweets parsed 
                        // then proceed to next user 
                        if (totalTweets == size) {
                            break;
                        }

                        size = totalTweets;

                        statuses.clear();

                    } catch (TwitterException e) {

                        // e.printStackTrace();
                        // do not throw if user has protected tweets, 
                        // or if they deleted their account
                        if (e.getStatusCode() == HttpResponseCode.UNAUTHORIZED
                                || e.getStatusCode() == HttpResponseCode.NOT_FOUND) {

                            System.out.println(targetedUser
                                    + " is protected or account is deleted");
                        } else {
                            System.out.println("Tweets Get Exception: "
                                    + e.getMessage());
                        }

                        // If rate limit reached then switch Auth user
                        RemainingCallsCounter++;
                        if (RemainingCallsCounter >= RemainingCalls) {

                            // Load OAuth user
                            tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS, JOB_NO);
                            twitter = tf.getInstance();

                            System.out.println("New User Loaded OAuth"
                                    + " Screen_name: " + AppOAuths.screen_name);

                            RemainingCalls = AppOAuths.RemainingCalls - 2;
                            RemainingCallsCounter = 0;

                            System.out
                                    .println("New Remianing Calls: " + RemainingCalls);
                        }

                        if (totalTweets < 1) {
                            writer.close();
                            // Remove file if tweets not found
                            File fileToDelete = new File(fileName);
                            fileToDelete.delete();
                        }

                        // Update stats
                        String fieldValues = "`tweets_dumped_all` = 2";
                        String where = "id = " + targetedUserID;
                        DB.Update("`twitter_users`", fieldValues, where);
                        continue OUTERMOST;

                    }

                    // If rate limit reached then switch Auth user
                    RemainingCallsCounter++;
                    if (RemainingCallsCounter >= RemainingCalls) {

                        // Load OAuth user
                        tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS, JOB_NO);
                        twitter = tf.getInstance();

                        System.out.println("New User Loaded OAuth Screen_name: "
                                + AppOAuths.screen_name);

                        RemainingCalls = AppOAuths.RemainingCalls - 2;
                        RemainingCallsCounter = 0;

                        System.out
                                .println("New Remianing Calls: " + RemainingCalls);
                    }

                } // while get tweets
                writer.close();

                if (totalTweets > 0) {

                    System.out.println("Total dumped tweets of "
                            + targetedUser + " are: " + totalTweets);
                    // Update stats
                    String fieldValues = "`tweets_dumped_all` = 1, "
                            + " `tweets_all_count` = " + totalTweets;
                    String where = "id = " + targetedUserID;
                    DB.Update("`twitter_users`", fieldValues, where);
                } else {

                    // Update stats
                    String fieldValues = "`tweets_dumped_all` = 2, "
                            + " `tweets_all_count` = " + totalTweets;
                    String where = "id = " + targetedUserID;
                    DB.Update("`twitter_users`", fieldValues, where);

                    // Remove file if tweets not found
                    File fileToDelete = new File(fileName);
                    fileToDelete.delete();
                }

                // If rate limit reached then switch Auth user
                RemainingCallsCounter++;
                if (RemainingCallsCounter >= RemainingCalls) {

                    // Load OAuth user
                    tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS, JOB_NO);
                    twitter = tf.getInstance();

                    System.out.println("New User Loaded OAuth Screen_name: "
                            + AppOAuths.screen_name);

                    RemainingCalls = AppOAuths.RemainingCalls - 2;
                    RemainingCallsCounter = 0;

                    System.out
                            .println("New Remianing Calls: " + RemainingCalls);
                }

                System.out.flush();
            } // while get users from database
        } catch (TwitterException te) {
            // te.printStackTrace();
            System.out.println("Failed to get tweets: "
                    + te.getMessage());
            System.exit(-1);
        }
        System.out.println("!!!! DONE !!!!");

        // Close System.out for this thread which will
        // flush and close this thread.
        System.out.close();
    }
}
