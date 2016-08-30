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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import misc.Misc;
import oauth.AppOAuth;

import org.apache.commons.lang3.StringEscapeUtils;

import twitter4j.HttpResponseCode;
import twitter4j.IDs;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Lists Friends ids
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class FilesThreaderFriendsIDsParser {

    public static void main(String[] args) throws ClassNotFoundException,
            SQLException, JSONException, FileNotFoundException,
            UnsupportedEncodingException {

        // Check how many arguments were passed in
        if ((args == null) || (args.length < 5)) {
            System.err.println("5 Parameters are required  plus one optional "
                    + "parameter to launch a Job.");
            System.err.println("First: String 'INPUT: DB or /input/path/'");
            System.err.println("Second: String 'OUTPUT: /output/path/'");
            System.err.println("Third: (int) Total Number Of Jobs");
            System.err.println("Fourth: (int) This Job Number");
            System.err.println("Fifth: (int) Number of seconds to pause");
            System.err.println("Sixth: (int) Number of ids to fetch"
                    + "Provide number which increment by 5000 "
                    + "(5000, 10000, 15000 etc) "
                    + "or -1 to fetch all ids.");
            System.err
                    .println("Example: fileToRun /input/path/ "
                            + "/output/path/ 10 1 3 75000");
            System.exit(-1);
        }

        // TODO documentation for command line
        AppOAuth AppOAuths = new AppOAuth();
        Misc helpers = new Misc();
        String endpoint = "/friends/ids";

        String inputPath = null;
        try {
            inputPath = StringEscapeUtils.escapeJava(args[0]);
        } catch (Exception e) {
            System.err.println("Argument " + args[0] + " must be an String.");
            System.exit(-1);
        }

        String outputPath = null;
        try {
            outputPath = StringEscapeUtils.escapeJava(args[1]);
        } catch (Exception e) {
            System.err.println("Argument " + args[1] + " must be an String.");
            System.exit(-1);
        }

        int TOTAL_JOBS = 0;
        try {
            TOTAL_JOBS = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Argument " + args[2] + " must be an integer.");
            System.exit(1);
        }

        int JOB_NO = 0;
        try {
            JOB_NO = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Argument " + args[3] + " must be an integer.");
            System.exit(1);
        }

        int secondsToPause = 0;
        try {
            secondsToPause = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[4] + " must be an integer.");
            System.exit(-1);
        }

        int IDS_TO_FETCH_INT = -1;
        if (args.length == 6) {
            try {
                IDS_TO_FETCH_INT = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[5] + " must be an integer.");
                System.exit(-1);
            }
        }
        
        int IDS_TO_FETCH = 0;
        if (IDS_TO_FETCH_INT > 5000) {
            
            float IDS_TO_FETCH_F = (float) IDS_TO_FETCH_INT / 5000;
            IDS_TO_FETCH = (int) Math.ceil(IDS_TO_FETCH_F);
        } else if ((IDS_TO_FETCH_INT <= 5000) && (IDS_TO_FETCH_INT > 0)) {
            IDS_TO_FETCH = 1;
        }
        
        secondsToPause = (TOTAL_JOBS * secondsToPause)
                - (JOB_NO * secondsToPause);
        System.out.println("secondsToPause: " + secondsToPause);
        helpers.pause(secondsToPause);

        try {

            int TotalWorkLoad = 0;
            ArrayList<String> allFiles = null;
            try {
                final File folder = new File(inputPath);
                allFiles = helpers.listFilesForSingleFolder(folder);
                TotalWorkLoad = allFiles.size();
            } catch (Exception e) {

                System.err.println("Input folder is not exists: "
                        + e.getMessage());
                System.exit(-1);
            }

            System.out.println("Total Workload is: " + TotalWorkLoad);

            if (TotalWorkLoad < 1) {
                System.err.println("No screen names file exists in: "
                        + inputPath);
                System.exit(-1);
            }

            if (TOTAL_JOBS > TotalWorkLoad) {
                System.err.println("Number of jobs are more than total work"
                        + " load. Please reduce 'Number of jobs' to launch.");
                System.exit(-1);
            }

            float TotalWorkLoadf = TotalWorkLoad;
            float TOTAL_JOBSf = TOTAL_JOBS;
            float res = (TotalWorkLoadf / TOTAL_JOBSf);

            int chunkSize = (int) Math.ceil(res);
            int offSet = JOB_NO * chunkSize;
            int chunkSizeToGet = (JOB_NO + 1) * chunkSize;

            System.out.println("My Share is " + chunkSize);
            System.out.println();

            // Load OAuh User
            TwitterFactory tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS,
                    JOB_NO);
            Twitter twitter = tf.getInstance();

            int RemainingCalls = AppOAuths.RemainingCalls;
            int RemainingCallsCounter = 0;
            System.out.println("First Time OAuth Remianing Calls: " + RemainingCalls);

            String Screen_name = AppOAuths.screen_name;
            System.out.println("First Time Loaded OAuth Screen_name: "
                    + Screen_name);
            System.out.println();

            IDs ids;
            System.out.println("Going to get friends ids.");
            
            // to write output in a file
            System.out.flush();

            if (JOB_NO + 1 == TOTAL_JOBS) {
                chunkSizeToGet = TotalWorkLoad;
            }

            List<String> myFilesShare = allFiles
                    .subList(offSet, chunkSizeToGet);

            for (String myFile : myFilesShare) {
                System.out.println("Going to parse file: " + myFile);

                try (BufferedReader br = new BufferedReader(new FileReader(
                        inputPath + "/" + myFile))) {
                    String line;
                    OUTERMOST:
                    while ((line = br.readLine()) != null) {
                        // process the line.

                        System.out
                                .println("Going to get friends ids of Screen-name / user_id: "
                                        + line);
                        System.out.println();

                        String targetedUser = line.trim(); // tmp
                        long cursor = -1;
                        int idsLoopCounter = 0;
                        int totalIDs = 0;
                        
                        PrintWriter writer = new PrintWriter(
                                            outputPath
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

                                    ids = twitter.getFriendsIDs(
                                            LongValueTargetedUser, cursor);
                                } else {
                                    ids = twitter.getFriendsIDs(targetedUser,
                                            cursor);
                                }

                            } catch (TwitterException te) {

                                // do not throw if user has protected tweets, or
                                // if they deleted their account
                                if (te.getStatusCode() == HttpResponseCode.UNAUTHORIZED
                                        || te.getStatusCode() == HttpResponseCode.NOT_FOUND) {

                                    System.out
                                            .println(targetedUser
                                                    + " is protected or account is deleted");
                                } else {
                                    System.out
                                            .println("Friends Get Exception: "
                                                    + te.getMessage());
                                }

                                // If rate limit reached then switch Auth user
                                RemainingCallsCounter++;
                                if (RemainingCallsCounter >= RemainingCalls) {

                                    // load auth user
                                    tf = AppOAuths.loadOAuthUser(endpoint,
                                            TOTAL_JOBS, JOB_NO);
                                    twitter = tf.getInstance();

                                    System.out.println("New Loaded OAuth User "
                                            + " Screen_name: "
                                            + AppOAuths.screen_name);

                                    RemainingCalls = AppOAuths.RemainingCalls;
                                    RemainingCallsCounter = 0;

                                    System.out.println("New OAuth Remianing Calls: "
                                            + RemainingCalls);
                                }
                                
                                // Remove file if ids not found
                                if (totalIDs == 0) {
                            
                                    System.out.println("No ids fetched so removing "
                                            + "file " + targetedUser);

                                    
                                    File fileToDelete = new File(outputPath
                                                    + "/" + targetedUser);
                                    fileToDelete.delete();
                                }
                                System.out.println();

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
                                Object idsJSON = responseDetailsJson.put("ids",
                                        jsonArray);

                                writer.println(idsJSON);
                                
                            }

                            // If rate limit reached then switch Auth user
                            RemainingCallsCounter++;
                            if (RemainingCallsCounter >= RemainingCalls) {

                                // load auth user
                                tf = AppOAuths.loadOAuthUser(endpoint,
                                        TOTAL_JOBS, JOB_NO);
                                twitter = tf.getInstance();

                                System.out
                                        .println("New Loaded OAuth User Screen_name: "
                                                + AppOAuths.screen_name);

                                RemainingCalls = AppOAuths.RemainingCalls;
                                RemainingCallsCounter = 0;

                                System.out.println("New OAuth Remianing Calls: "
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
                        
                        // Remove file if ids not found
                        if (totalIDs == 0) {

                            System.out.println("No ids fetched so removing "
                                    + "file " + targetedUser);


                            File fileToDelete = new File(outputPath
                                            + "/" + targetedUser);
                            fileToDelete.delete();
                        }
                        System.out.println();

                    } // while get records from single file
                } // read my single file
                catch (IOException e) {
                    System.err.println("Failed to read lines from " + myFile);
                }
                
                // to write output in a file
                System.out.flush();
            } // all my files share

        } catch (TwitterException te) {
            // te.printStackTrace();
            System.err.println("Failed to get friends' ids: "
                    + te.getMessage());
            System.exit(-1);
        }
        System.out.println("!!!! DONE !!!!");

        // Close System.out for this thread which will
        // flush and close this thread.
        System.out.close();
    }

}
