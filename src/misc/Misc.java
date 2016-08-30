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
package misc;

import java.io.File;
import java.util.ArrayList;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class Misc {

    private boolean debug = true;

    public void pause(int seconds) {
        int miliSeconds = seconds * 1000;
        try {
            Thread.sleep(miliSeconds);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    public long getUnixTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    public void Echo(Misc str) {
        System.out.println(str);
    }

    public void logit(String msg) {
        if (this.debug) {
            System.out.println("" + msg);
        }
    }

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    /**
     * list Files For Single Folder
     *
     * Usage: final File folder = new File("/home/you/Desktop");
     * ArrayList<String> allFiles = listFilesForSingleFolder(folder);
     *
     * Read a single directory and return files names
     *
     * @param folder
     * @return ArrayList<String>
     */
    public ArrayList<String> listFilesForSingleFolder(final File folder) {

        ArrayList<String> filesNames = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                continue;
            } else {
                filesNames.add(fileEntry.getName());
            }
        }
        return filesNames;
    }

}
