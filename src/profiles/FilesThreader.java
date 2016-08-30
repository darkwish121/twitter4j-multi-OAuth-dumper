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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringEscapeUtils;

import twitter4j.JSONException;

import misc.Misc;
import threader.ThreadPrintStream;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class FilesThreader {

    static String INPUT = null;
    static String OUTPUT = null;
    static String TOTAL_JOBS_STR = null;
    static String PAUSE_STR = null;
    static int PAUSE = 0;

    static Misc helpers = new Misc();
    boolean runnable;

    public FilesThreader() {
        runnable = true;
    }

    public boolean isRunnable() {
        return runnable;
    }

    // change profile name from timestamp_counter to 
    // screen_name/user_id_timestamp_counter
    public static void main(String[] args) throws InterruptedException {

        // Check how many arguments were passed in
        if ((args == null) || (args.length < 4)) {
            System.err.println("4 Parameters are required to launch a Job.");
            System.err.println("First: String 'INPUT: /path/to/files/'");
            System.err.println("Second: String 'OUTPUT: /output/path/'");
            System.err.println("Third: (int) Total Number Of Jobs");
            System.err.println("Fourth: (int) Number of seconds to pause");
            System.err.println("Example: fileToRun /input/path/ /output/path/ "
                    + "10 2");
            System.exit(-1);
        }

        // to write output in a file
        ThreadPrintStream.replaceSystemOut();
        try {
            INPUT = StringEscapeUtils.escapeJava(args[0]);
        } catch (Exception e) {
            System.err.println("Argument" + args[0] + " must be an String.");
            System.exit(-1);
        }

        try {
            OUTPUT = StringEscapeUtils.escapeJava(args[1]);
        } catch (Exception e) {
            System.err.println("Argument" + args[1] + " must be an String.");
            System.exit(-1);
        }

        try {
            TOTAL_JOBS_STR = StringEscapeUtils.escapeJava(args[2]);
        } catch (Exception e) {
            System.err.println("Argument" + args[2] + " must be an integer.");
            System.exit(-1);
        }

        try {
            PAUSE_STR = StringEscapeUtils.escapeJava(args[3]);
        } catch (Exception e) {
            System.err.println("Argument" + args[3] + " must be an integer.");
            System.exit(-1);
        }

        try {
            PAUSE = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[3] + " must be an integer.");
            System.exit(-1);
        }

        int TOTAL_JOBS_INT = 0;
        try {
            TOTAL_JOBS_INT = Integer.parseInt(TOTAL_JOBS_STR);
        } catch (NumberFormatException e) {
            System.err.println("Argument" + TOTAL_JOBS_STR 
                    + " must be an integer.");
            System.exit(-1);
        }

        System.out.println("Going to launch jobs. "
                + "Please see logs files to track jobs.");

        ExecutorService threadPool = Executors.newFixedThreadPool(
                TOTAL_JOBS_INT);

        for (int i = 0; i < TOTAL_JOBS_INT; i++) {

            final String JOB_NO = Integer.toString(i);

            threadPool.submit(new Runnable() {

                public void run() {

                    try {
                        // Create a text file where System.out.println()
                        // will send its data for this thread.
                        String name = Thread.currentThread().getName();
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(name + "-logs.txt");
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.exit(0);
                        }

                        // Create a PrintStream that will write to the new file.
                        PrintStream stream = new PrintStream(
                                new BufferedOutputStream(fos));
                        // Install the PrintStream to be used 
                        // as System.out for this thread.
                        ((ThreadPrintStream) System.out).setThreadOut(stream);
                        // Output three messages to System.out.
                        System.out.println(name);
                        System.out.println();
                        System.out.println();

                        FilesThreader.execTask(
                                INPUT,
                                OUTPUT,
                                TOTAL_JOBS_STR,
                                JOB_NO,
                                PAUSE_STR);

                    } catch (IOException |
                            ClassNotFoundException |
                            SQLException |
                            JSONException e) {

                        // e.printStackTrace();
                        System.out.println(e.getMessage());
                    }
                }
            });

            helpers.pause(PAUSE);
        }
        threadPool.shutdown();
    }

    public static void execTask(
            String INPUT,
            String OUTPUT,
            String TOTAL_JOBS_STR,
            String JOB_NO,
            String PAUSE) throws
            ClassNotFoundException, SQLException, JSONException, IOException {

        String[] args = {INPUT, OUTPUT, TOTAL_JOBS_STR, JOB_NO, PAUSE};
        profiles.FilesThreaderParser.main(args);
    }
}
