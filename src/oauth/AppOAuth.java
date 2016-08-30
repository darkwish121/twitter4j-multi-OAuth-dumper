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
package oauth;

import java.sql.SQLException;

import account.GetRateLimitStatus;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Custom Classes
 *
 */
import misc.ConstVars;
import mySQL.MysqlDB;

/**
 * Lists Followers IDs
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class AppOAuth {

    public int RemainingCalls;
    public String screen_name;
    ConstVars StaticVars = new ConstVars();

    public TwitterFactory loadOAuthUser(String endpoint)
            throws ClassNotFoundException, SQLException, TwitterException {

        MysqlDB DB = new MysqlDB();
        // System.out.println("DEBUG AppOAuth.java");

        // get OAuth cradentials by calling DB's loadOAuthUser(endpoint)
        ConstVars StaticVars = DB.loadOAuthUser(endpoint);

        String consumer_key = StaticVars.consumer_key;
        String consumer_secret = StaticVars.consumer_secret;
        String user_token = StaticVars.user_token;
        String user_secret = StaticVars.user_secret;

        RemainingCalls = StaticVars.Remaining;
        screen_name = StaticVars.screen_name;

        // working with twitter4j.properties
        // Twitter twitter = new TwitterFactory().getInstance(); 
        // initialize Twitter OAuth
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true)
                .setDebugEnabled(true)
                .setOAuthConsumerKey(consumer_key)
                .setOAuthConsumerSecret(consumer_secret)
                .setOAuthAccessToken(user_token)
                .setOAuthAccessTokenSecret(user_secret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf;
    }

    public TwitterFactory loadOAuthUser(String endpoint, int TOTAL_JOBS,
            int JOB_NO)
            throws ClassNotFoundException, SQLException, TwitterException {

        MysqlDB DB = new MysqlDB();

        // System.out.println("DEBUG AppOAuth.java");
        // get OAuth cradentials by calling DB's loadOAuthUser(endpoint)
        ConstVars StaticVars = DB.loadOAuthUser(endpoint, TOTAL_JOBS, JOB_NO);

        String consumer_key = StaticVars.consumer_key;
        String consumer_secret = StaticVars.consumer_secret;
        String user_token = StaticVars.user_token;
        String user_secret = StaticVars.user_secret;

        RemainingCalls = StaticVars.Remaining;
        screen_name = StaticVars.screen_name;

        // working with twitter4j.properties
        // Twitter twitter = new TwitterFactory().getInstance(); 
        // initialize Twitter OAuth
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true).setDebugEnabled(true)
                .setOAuthConsumerKey(consumer_key)
                .setOAuthConsumerSecret(consumer_secret)
                .setOAuthAccessToken(user_token)
                .setOAuthAccessTokenSecret(user_secret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf;
    }

    public TwitterFactory loadOAuthUser(String endpoint, String consumer_key,
            String consumer_secret, String user_token, String user_secret)
            throws ClassNotFoundException, SQLException, TwitterException {

        // Send OAuth credentials and get rate limit of endpoint
        String[] args = {endpoint, consumer_key, consumer_secret, user_token,
            user_secret};
        StaticVars = GetRateLimitStatus.getRateLimit(args);

        RemainingCalls = StaticVars.Remaining;

        // working with twitter4j.properties
        // Twitter twitter = new TwitterFactory().getInstance(); 
        // initialize Twitter OAuth
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true)
                .setDebugEnabled(true)
                .setOAuthConsumerKey(consumer_key)
                .setOAuthConsumerSecret(consumer_secret)
                .setOAuthAccessToken(user_token)
                .setOAuthAccessTokenSecret(user_secret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf;
    }
}
