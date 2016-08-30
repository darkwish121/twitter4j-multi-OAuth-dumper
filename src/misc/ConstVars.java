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

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class ConstVars {

    // Database vars
    public final String DataBaseName = "twitter";
    public final String DataBaseUser = "twitter";
    public final String DataBasePass = "twitter";

    // OAuth values will assign from MysqlDB class
    public String screen_name;
    public String consumer_key;
    public String consumer_secret;
    public String user_token;
    public String user_secret;

    // values assign from GetRateLimitStatus class
    public String Endpoint;
    public int Limit;
    public int Remaining;
    public int ResetTimeInSeconds;
    public int SecondsUntilReset;

}