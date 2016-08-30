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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import misc.Misc;
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
public class CmdUserTimeline {

    public static void main(String[] args) throws ClassNotFoundException,
            SQLException, JSONException, IOException {

        // Check how many arguments were passed in
        if ((args == null) || (args.length != 7)) {

            System.err.println("Please provide command as following.");
            System.err.println("java -cp twitter4j-multi-oauth-0.3.jar "
                    + "timeline.CmdUserTimeline consumer_key consumer_secret"
                    + " user_token user_secret output_path "
                    + "screen_name_or_userid "
                    + "number_of_tweets_to_get_max_is_3200");
            System.exit(-1);
        }

        AppOAuth AppOAuths = new AppOAuth();
        Misc helpers = new Misc();
        String endpoint = "/statuses/user_timeline";

        String consumer_key = null;
        try {
            consumer_key = StringEscapeUtils.escapeJava(args[0]);
        } catch (Exception e) {
            System.err.println("Argument" + args[0] + " must be an String.");
            System.exit(-1);
        }

        String consumer_secret = null;
        try {
            consumer_secret = StringEscapeUtils.escapeJava(args[1]);
        } catch (Exception e) {
            System.err.println("Argument" + args[1] + " must be an String.");
            System.exit(-1);
        }
        String user_token = null;
        try {
            user_token = StringEscapeUtils.escapeJava(args[2]);
        } catch (Exception e) {
            System.err.println("Argument" + args[2] + " must be an String.");
            System.exit(-1);
        }
        String user_secret = null;
        try {
            user_secret = StringEscapeUtils.escapeJava(args[3]);
        } catch (Exception e) {
            System.err.println("Argument" + args[3] + " must be an String.");
            System.exit(-1);
        }

        String OutputDirPath = null;
        try {
            OutputDirPath = StringEscapeUtils.escapeJava(args[4]);
        } catch (Exception e) {
            System.err.println("Argument" + args[4] + " must be an String.");
            System.exit(-1);
        }

        String targetedUser = "";
        try {
            targetedUser = StringEscapeUtils.escapeJava(args[5]);
        } catch (Exception e) {
            System.err
                    .println("Argument" + args[5] + " must be an String.");
            System.exit(-1);
        }

        int NUMBER_OF_TWEETS = 0;
        try {
            NUMBER_OF_TWEETS = Integer.parseInt(args[6]);
        } catch (Exception e) {
            System.err.println("Argument" + args[6] + " must be an integer.");
            System.exit(-1);
        }

        try {

            TwitterFactory tf = AppOAuths.loadOAuthUser(endpoint, consumer_key,
                    consumer_secret, user_token, user_secret);
            Twitter twitter = tf.getInstance();

            int RemainingCalls = AppOAuths.RemainingCalls - 2;
            int RemainingCallsCounter = 0;
            System.out.println("Remianing Calls: " + RemainingCalls);

            // screen_name / user_id provided by arguments
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

            String fileName = filesPath + targetedUser;
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

                        long LongValueTargetedUser = Long.valueOf(targetedUser)
                                .longValue();

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

                        statuses.addAll(twitter.getUserTimeline(targetedUser,
                                page));

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

                    // If user's total tweet are less than 195 then no next call
                    if (size == 0) {
                        if (totalTweets < 195) {
                            break;
                        }
                    }

                    // If user's all tweets parsed then exit
                    if (totalTweets == size) {
                        break;
                    }

                    size = totalTweets;

                    statuses.clear();

                } catch (TwitterException e) {

                    // e.printStackTrace();
                    // do not throw if user has protected tweets, or if they
                    // deleted their account
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

                        System.out.println("No more remianing calls");
                    }

                    if (totalTweets < 1) {
                        writer.close();
                        // Remove file if tweets not found
                        File fileToDelete = new File(fileName);
                        fileToDelete.delete();
                    }
                    break;
                }

                // If rate limit reached then switch Auth user
                RemainingCallsCounter++;
                if (RemainingCallsCounter >= RemainingCalls) {

                    System.out.println("No more remianing calls");
                    break;
                }

            } // while get tweets
            writer.close();

            if (totalTweets > 0) {
                System.out.println("Total dumped tweets of " + targetedUser
                        + " are: " + totalTweets);
            } else {

                // Remove file if tweets not found
                File fileToDelete = new File(fileName);
                fileToDelete.delete();
            }

        } catch (TwitterException te) {
            // te.printStackTrace();
            System.out.println("Failed to get followers' ids: "
                    + te.getMessage());
            System.exit(-1);
        }
        System.out.println("!!!! DONE !!!!");
    }
}
