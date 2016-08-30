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
package profiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import misc.Misc;
import oauth.AppOAuth;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.JSONException;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public final class FilesThreaderParser {

    public static void main(String[] args) throws ClassNotFoundException,
            SQLException, JSONException, FileNotFoundException,
            UnsupportedEncodingException {

        // Check how many arguments were passed in
        if ((args == null) || (args.length < 5)) {
            System.err.println("5 Parameters are required to launch a Job.");
            System.err.println("First: String 'INPUT: /input/path/'");
            System.err.println("Second: String 'OUTPUT: /output/path/'");
            System.err.println("Third: (int) Total Number Of Jobs");
            System.err.println("Fourth: (int) This Job Number");
            System.err.println("Fifth: (int) Number of seconds to pause");
            System.err.println("Example: fileToRun /input/path/ /output/path/ "
                    + "10 1 3");
            System.exit(-1);
        }

        // TODO documentation for command line
        AppOAuth AppOAuths = new AppOAuth();
        Misc helpers = new Misc();
        String endpoint = "/users/lookup";

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
                System.err.println("No targeted user file exists in: "
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
            System.out.println("My offSet is " + offSet);
            System.out.println("My chunkSizeToGet is " + chunkSizeToGet);
            System.out.println();

            // Load OAuh User
            TwitterFactory tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS,
                    JOB_NO);
            Twitter twitter = tf.getInstance();

            int RemainingCalls = AppOAuths.RemainingCalls;
            int RemainingCallsCounter = 0;
            System.out.println("First Time OAuth Remianing Calls: "
                    + RemainingCalls);

            String Screen_name = AppOAuths.screen_name;
            System.out.println("First Time Loaded OAuth Screen_name: "
                    + Screen_name);
            System.out.println();

            System.out.println("Going to get Profiles.");

            if (JOB_NO + 1 == TOTAL_JOBS) {
                chunkSizeToGet = TotalWorkLoad;
            }

            secondsToPause = (TOTAL_JOBS * secondsToPause)
                    - (JOB_NO * secondsToPause);
            System.out.println("secondsToPause: " + secondsToPause);
            helpers.pause(secondsToPause);

            // to write output in a file
            System.out.flush();

            List<String> fileNamesShare = allFiles
                    .subList(offSet, chunkSizeToGet);

            for (String fileName : fileNamesShare) {

                System.out.println("Going to parse file: " + fileName);

                try {

                    // open file to write all profiles
                    String filesPath = outputPath + "/";
                    PrintWriter writer = new PrintWriter(filesPath
                            + "/" + fileName, "UTF-8");

                    try (BufferedReader br = new BufferedReader(
                            new FileReader(inputPath + "/" + fileName))) {
                        String jsonString;
                        while ((jsonString = br.readLine()) != null) {
                            // process the line.

                            List<String> fileContent = new ArrayList<>();
                            fileContent.add(jsonString);

                            String[] strarray = fileContent.toArray(
                                    new String[0]);

                            String ss = Arrays.toString(strarray);

                            JSONArray obj = new JSONArray(ss);
                            JSONObject obj4 = (JSONObject) obj.get(0);

                            JSONArray idsS = (JSONArray) obj4.get("ids");

                            ArrayList<String> Idslist = new ArrayList<String>();
                            if (idsS != null) {
                                int len = idsS.length();
                                for (int i = 0; i < len; i++) {
                                    Idslist.add(idsS.get(i).toString());
                                }
                            }

                            for (int start = 0; start < Idslist.size(); 
                                    start += 100) {
                                int end = Math.min(start + 100, Idslist.size());
                                List<String> sublist = Idslist
                                        .subList(start, end);

                                long[] idsdata = new long[sublist.size()];
                                for (int i = 0; i < sublist.size(); i++) {
                                    idsdata[i] = Long.valueOf(sublist.get(i));
                                }
                                ResponseList<User> profiles = null;

                                while (true) {

                                    try {
                                        profiles = twitter.lookupUsers(idsdata);

                                        if (profiles.size() > 0) {
                                            for (User user : profiles) {
                                                String rawJSON = 
                                                        TwitterObjectFactory
                                                        .getRawJSON(user);

                                                // put profilesJSON in a file
                                                try {
                                                    writer.println(rawJSON);
                                                } catch (Exception e) {
                                                    System.err.println(e
                                                            .getMessage());
                                                    System.exit(0);
                                                }
                                            }

                                            break;
                                        }

                                    } catch (TwitterException te) {

                                        // If rate limit reached then switch Auth
                                        // user
                                        RemainingCallsCounter++;
                                        if (RemainingCallsCounter >= 
                                                RemainingCalls) {

                                            // Load OAuth user
                                            tf = AppOAuths.loadOAuthUser(
                                                    endpoint,
                                                    TOTAL_JOBS, JOB_NO);
                                            twitter = tf.getInstance();

                                            System.out.println("New User Loaded"
                                                    + " OAuth Screen_name: "
                                                    + AppOAuths.screen_name);

                                            RemainingCalls = AppOAuths.RemainingCalls - 2;
                                            RemainingCallsCounter = 0;

                                            System.out
                                                    .println("New Remianing Calls: "
                                                            + RemainingCalls);
                                        }

                                    }

                                } // while get profiles

                                // If rate limit reached then switch Auth user
                                RemainingCallsCounter++;
                                if (RemainingCallsCounter >= RemainingCalls) {

                                    // Load OAuth user
                                    tf = AppOAuths.loadOAuthUser(endpoint,
                                            TOTAL_JOBS, JOB_NO);
                                    twitter = tf.getInstance();

                                    System.out.println("New User Loaded OAuth "
                                            + "Screen_name: "
                                                    + AppOAuths.screen_name);

                                    RemainingCalls = AppOAuths.RemainingCalls - 2;
                                    RemainingCallsCounter = 0;

                                    System.out.println("New Remianing Calls: "
                                            + RemainingCalls);
                                }

                            }

                        }

                    }
                    writer.close();
                } // read my single file
                catch (IOException e) {
                    System.err.println("Failed to read lines from "
                            + fileName);
                }

                // delete file if processed
                File fileToDelete = new File(inputPath + "/"
                        + "/" + fileName);
                fileToDelete.delete();

                // to write output in a file
                System.out.flush();
            }// all my files

            // If rate limit reached then switch Auth user
            RemainingCallsCounter++;
            if (RemainingCallsCounter >= RemainingCalls) {

                // load auth user
                tf = AppOAuths.loadOAuthUser(endpoint, TOTAL_JOBS, JOB_NO);
                twitter = tf.getInstance();

                System.out.println("New Loaded OAuth User Screen_name: "
                        + AppOAuths.screen_name);

                RemainingCalls = AppOAuths.RemainingCalls;
                RemainingCallsCounter = 0;

                System.out.println("New OAuth Remianing Calls: "
                        + RemainingCalls);
            }

        } catch (TwitterException te) {
            // te.printStackTrace();
            System.err.println("Failed to get Profiles: " + te.getMessage());
            System.exit(-1);
        }
        System.out.println("!!!! DONE !!!!");

        // Close System.out for this thread which will
        // flush and close this thread.
        System.out.close();
    }

}
