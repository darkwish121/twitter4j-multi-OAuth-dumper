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
package stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class Sample {

    private static PrintWriter writer = null;
    private static int FileCounter = 0;
    private static int counter = 0;
    private static String OutputDirPath = null;
    private static Boolean firstRun = true;
    private static String currentDirPath = null;

    /**
     * @param args
     */
    public static void main(String[] args) {

        // Check how many arguments were passed in
        if ((args == null) || (args.length == 0)) {
            System.err.println("Please provide output directory path");
            System.exit(-1);
        }

        try {
            OutputDirPath = StringEscapeUtils.escapeJava(args[0]);
        } catch (Exception e) {
            System.err.println("Argument" + args[0] + " must be an String.");
            System.exit(-1);
        }

        dataStoreManager();

        StatusListener listener = new StatusListener() {

            public void onStatus(Status status) {
                // System.out.println(status.getUser().getName() + " : " +
                // status.getText());
                String rawJSON = TwitterObjectFactory.getRawJSON(status);
                // System.out.println(rawJSON);
                writer.println(rawJSON);
                counter++;
                System.out.println(counter);
                if (counter >= 1000) {
                    dataStoreManager();
                }
            }

            public void onDeletionNotice(
                    StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            public void onException(Exception ex) {
            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStallWarning(StallWarning arg0) {
                // TODO Auto-generated method stub

            }
        };

        // mozellecandi
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("****")
                .setOAuthConsumerSecret(
                        "****")
                .setOAuthAccessToken(
                        "****")
                .setOAuthAccessTokenSecret(
                        "****")
                .setJSONStoreEnabled(true);

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
                .getInstance();
        twitterStream.addListener(listener);
        // sample() method internally creates a thread which manipulates
        // TwitterStream and calls these adequate listener methods continuously.
        twitterStream.sample();

    }

    public static void dataStoreManager() {

        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date1 = new Date(stamp.getTime());

        SimpleDateFormat format1 = new SimpleDateFormat(
                "E MMM dd hh:mm:ss z yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = format1.parse("" + date1);
        } catch (ParseException e) {
        }
        String dateDirName = format2.format(date);

        String dateDirPath = OutputDirPath + "/";
        File dateDir = new File(dateDirPath + dateDirName);

        // If the directory does not exist, create it
        if (!dateDir.exists()) {

            try {
                dateDir.mkdirs();
                FileCounter = 0;

            } catch (SecurityException se) {

                System.err.println("Could not create output "
                        + "directory: " + OutputDirPath);
                System.err.println(se.getMessage());
                System.exit(-1);
            }
        }

        if (firstRun || FileCounter >= 100) {

            if (dateDir.exists()) {

                currentDirPath = dateDirPath + dateDirName + "/"
                        + System.currentTimeMillis() / 1000;
                File currentDir = new File(currentDirPath);
                // If the directory does not exist, create it
                if (!currentDir.exists()) {

                    try {
                        currentDir.mkdirs();

                    } catch (SecurityException se) {

                        System.err.println("Could not create output "
                                + "directory: " + currentDirPath);
                        System.err.println(se.getMessage());
                        System.exit(-1);
                    }
                }

            }

            firstRun = false;
            FileCounter = 0;
        }

        if (writer != null) {
            writer.close();
        }

        counter = 0;
        FileCounter++;
        try {
            writer = new PrintWriter(currentDirPath + "/"
                    + FileCounter + ".txt", "UTF-8");

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
        }
    }

}
