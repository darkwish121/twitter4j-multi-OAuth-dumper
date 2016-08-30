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
package friendsandfollowers;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import twitter4j.HttpResponseCode;
import twitter4j.IDs;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Custom Classes
 *
 */
import mySQL.MysqlDB;
import oauth.AppOAuth;
import misc.Misc;

/**
 * Lists Friends IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public final class DBFriendsIDs {

    public static void main(String[] args) throws ClassNotFoundException,
            SQLException, JSONException, FileNotFoundException,
            UnsupportedEncodingException {

        // Check arguments that passed in
        if ((args == null) || (args.length == 0)) {
            System.err.println("2 Parameters are required plus one optional "
                    + "parameter to launch a Job.");
            System.err.println("First: String 'OUTPUT: /output/path/'");
            System.err.println("Second: (int) Number of ids to fetch. "
                    + "Provide number which increment by 5000 "
                    + "(5000, 10000, 15000 etc) "
                    + "or -1 to fetch all ids.");
            System.err.println("Third (optional): 'screen_name / user_id_str'");
            System.err.println("If 3rd argument not provided then provide"
                    + " Twitter users through database.");
            System.exit(-1);
        }

        MysqlDB DB = new MysqlDB();
        AppOAuth AppOAuths = new AppOAuth();
        Misc helpers = new Misc();
        String endpoint = "/friends/ids";

        String OutputDirPath = null;
        try {
            OutputDirPath = StringEscapeUtils.escapeJava(args[0]);
        } catch (Exception e) {
            System.err.println("Argument" + args[0] + " must be an String.");
            System.exit(-1);
        }

        int IDS_TO_FETCH_INT = -1;
        try {
            IDS_TO_FETCH_INT = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[1] + " must be an integer.");
            System.exit(-1);
        }
        
        int IDS_TO_FETCH = 0;
        if (IDS_TO_FETCH_INT > 5000) {
            
            float IDS_TO_FETCH_F = (float) IDS_TO_FETCH_INT / 5000;
            IDS_TO_FETCH = (int) Math.ceil(IDS_TO_FETCH_F);
        } else if ((IDS_TO_FETCH_INT <= 5000) && (IDS_TO_FETCH_INT > 0)) {
            IDS_TO_FETCH = 1;
        }

        String targetedUser = "";
        if (args.length == 3) {
            try {
                targetedUser = StringEscapeUtils.escapeJava(args[2]);
            } catch (Exception e) {
                System.err.println("Argument" + args[2] 
                        + " must be an String.");
                System.exit(-1);
            }
        }

        try {

            TwitterFactory tf = AppOAuths.loadOAuthUser(endpoint);
            Twitter twitter = tf.getInstance();

            int RemainingCalls = AppOAuths.RemainingCalls;
            int RemainingCallsCounter = 0;
            System.out.println("First Time Remianing Calls: " + RemainingCalls);

            String Screen_name = AppOAuths.screen_name;
            System.out.println("First Time Loaded OAuth Screen_name: " 
                    + Screen_name);

            IDs ids;
            System.out.println("Listing friends ids.");

            // if targetedUser not provided by argument, then look into database.
            if (StringUtils.isEmpty(targetedUser)) {

                String selectQuery = "SELECT * FROM `followings_parent` WHERE "
                        + "`targeteduser` != '' AND "
                        + "`nextcursor` != '0' AND "
                        + "`nextcursor` != '2'";

                ResultSet results = DB.selectQ(selectQuery);

                int numRows = DB.numRows(results);
                if (numRows < 1) {
                    System.err.println("No User in database to get friendsIDS");
                    System.exit(-1);
                }

                OUTERMOST:
                while (results.next()) {

                    int following_parent_id = results.getInt("id");
                    targetedUser = results.getString("targeteduser");
                    long cursor = results.getLong("nextcursor");
                    System.out.println("Targeted User: " + targetedUser);

                    int idsLoopCounter = 0;
                    int totalIDs = 0;
                     
                    // put idsJSON in a file
                    PrintWriter writer = new PrintWriter(
                                            OutputDirPath
                                            + "/" + targetedUser,
                                            "UTF-8");

                    // call different functions for screen_name and id_str
                    Boolean chckedNumaric = helpers.isNumeric(targetedUser);

                    do {
                        ids = null;
                        try {

                            if (chckedNumaric) {

                                long LongValueTargetedUser = Long.valueOf(
                                        targetedUser).longValue();

                                ids = twitter.getFriendsIDs(LongValueTargetedUser,
                                        cursor);
                            } else {
                                ids = twitter.getFriendsIDs(targetedUser,
                                        cursor);
                            }

                        } catch (TwitterException te) {

                            // do not throw if user has protected tweets, 
                            // or if they deleted their account
                            if (te.getStatusCode() == HttpResponseCode
                                    .UNAUTHORIZED
                                    || te.getStatusCode() == HttpResponseCode
                                    .NOT_FOUND) {

                                System.out.println(targetedUser
                                        + " is protected or account is deleted");
                            } else {
                                System.out.println("Friends Get Exception: "
                                        + te.getMessage());
                            }

                            // If rate limit reached then switch Auth user
                            RemainingCallsCounter++;
                            if (RemainingCallsCounter >= RemainingCalls) {

                                // load auth user
                                tf = AppOAuths.loadOAuthUser(endpoint);
                                twitter = tf.getInstance();

                                System.out.println("New User Loaded OAuth"
                                        + " Screen_name: " 
                                        + AppOAuths.screen_name);

                                RemainingCalls = AppOAuths.RemainingCalls;
                                RemainingCallsCounter = 0;

                                System.out.println("New Remianing Calls: " 
                                        + RemainingCalls);
                            }

                            // update cursor in "followings_parent"
                            String fieldValues = "`nextcursor` = 2";
                            String where = "id = " + following_parent_id;
                            DB.Update("`followings_parent`", fieldValues, where);

                            // If error then switch to next user
                            continue OUTERMOST;
                        }

                        if (ids.getIDs().length > 0) {

                            idsLoopCounter++;
                            totalIDs += ids.getIDs().length;
                                System.out.println(idsLoopCounter
                                        + ": IDS length: "
                                        + ids.getIDs().length);

                            JSONObject responseDetailsJson = new JSONObject();
                            JSONArray jsonArray = new JSONArray();
                            for (long id : ids.getIDs()) {
                                jsonArray.put(id);
                            }
                            Object idsJSON = responseDetailsJson.put(
                                    "ids", jsonArray);
                            
                            writer.println(idsJSON);
                        }

                        // If rate limit reached then switch Auth user.
                        RemainingCallsCounter++;
                        if (RemainingCallsCounter >= RemainingCalls) {

                            // load auth user
                            tf = AppOAuths.loadOAuthUser(endpoint);
                            twitter = tf.getInstance();

                            System.out.println("New User Loaded OAuth "
                                    + "Screen_name: " + AppOAuths.screen_name);

                            RemainingCalls = AppOAuths.RemainingCalls;
                            RemainingCallsCounter = 0;

                            System.out.println("New Remianing Calls: "
                                    + RemainingCalls);
                        }
                        
                        if (IDS_TO_FETCH_INT != -1) {
                            if (idsLoopCounter == IDS_TO_FETCH) {
                                break;
                            }
                        }

                    } while ((cursor = ids.getNextCursor()) != 0);
                    writer.close();
                    System.out.println("Total ids dumped of "
                                + targetedUser + " are: " + totalIDs);
                    System.out.println();

                    // update cursor in "followings_parent"
                    String fieldValues = "`nextcursor` = " + cursor;
                    String where = "id = " + following_parent_id;
                    DB.Update("`followings_parent`", fieldValues, where);

                } // loop through every result found in db
            } else {

                // Second Argument Sets, so we are here.
                System.out.println("screen_name / user_id_str "
                        + "passed by argument");

                int idsLoopCounter = 0;
                int totalIDs = 0;
                     
                // put idsJSON in a file
                PrintWriter writer = new PrintWriter(
                                        OutputDirPath
                                        + "/" + targetedUser + "_ids_"
                                        + helpers.getUnixTimeStamp(),
                                        "UTF-8");

                // call different functions for screen_name and id_str
                Boolean chckedNumaric = helpers.isNumeric(targetedUser);
                long cursor = -1;

                do {
                    ids = null;
                    try {

                        if (chckedNumaric) {

                            long LongValueTargetedUser = Long.valueOf(
                                    targetedUser).longValue();

                            ids = twitter.getFriendsIDs(LongValueTargetedUser,
                                    cursor);
                        } else {
                            ids = twitter.getFriendsIDs(targetedUser,
                                    cursor);
                        }

                    } catch (TwitterException te) {

                        // do not throw if user has protected tweets, 
                        // or if they deleted their account
                        if (te.getStatusCode() == HttpResponseCode.UNAUTHORIZED
                                || te.getStatusCode() == HttpResponseCode
                                .NOT_FOUND) {

                            System.out.println(targetedUser
                                    + " is protected or account is deleted");
                        } else {
                            System.out.println("Friends Get Exception: "
                                    + te.getMessage());
                        }
                        System.exit(-1);
                    }

                    if (ids.getIDs().length > 0) {

                        idsLoopCounter++;
                        totalIDs += ids.getIDs().length;
                                System.out.println(idsLoopCounter
                                        + ": IDS length: "
                                        + ids.getIDs().length);

                        JSONObject responseDetailsJson = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        for (long id : ids.getIDs()) {
                            jsonArray.put(id);
                        }
                        Object idsJSON = responseDetailsJson.put(
                                "ids", 
                                jsonArray);

                        writer.println(idsJSON);

                    }

                    // If rate limit reach then switch Auth user
                    RemainingCallsCounter++;
                    if (RemainingCallsCounter >= RemainingCalls) {

                        // load auth user
                        tf = AppOAuths.loadOAuthUser(endpoint);
                        twitter = tf.getInstance();

                        System.out.println("New User Loaded OAuth Screen_name: "
                                + AppOAuths.screen_name);

                        RemainingCalls = AppOAuths.RemainingCalls;
                        RemainingCallsCounter = 0;

                        System.out.println("New Remianing Calls: "
                                + RemainingCalls);
                    }
                    
                    if (IDS_TO_FETCH_INT != -1) {
                        if (idsLoopCounter == IDS_TO_FETCH) {
                            break;
                        }
                    }

                } while ((cursor = ids.getNextCursor()) != 0);
                writer.close();
                System.out.println("Total ids dumped of "
                            + targetedUser + " are: " + totalIDs);
                System.out.println();
                
            }

        } catch (TwitterException te) {
            // te.printStackTrace();
            System.err
                    .println("Failed to get friends' ids: " + te.getMessage());
            System.exit(-1);
        }
        System.out.println("!!!! DONE !!!!");
    }
}
