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
package account;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;

/**
 * Custom Classes
 *
 */
import misc.ConstVars;

/**
 * Gets rate limit status.
 *
 * @author Sohail Ahmed - sohail.ahmed21 at gmail.com
 */
public class GetRateLimitStatus {

    public static ConstVars getRateLimit(String[] args) {

        ConstVars StaticVars = new ConstVars();

        try {

            // init Twitter OAuth
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(args[1])
                    .setOAuthConsumerSecret(args[2])
                    .setOAuthAccessToken(args[3])
                    .setOAuthAccessTokenSecret(args[4]);

            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();

            // it returns RateLimits of all end-points
            Map<String, RateLimitStatus> rateLimitStatus = twitter
                    .getRateLimitStatus();

            // get RateLimit of required end-point
            RateLimitStatus status = rateLimitStatus.get(args[0]);
            String Endpoint = args[0];
            int Limit = status.getLimit();

            int Remaining = status.getRemaining();
            int ResetTimeInSeconds = status.getResetTimeInSeconds();
            int SecondsUntilReset = status.getSecondsUntilReset();

            // set and return rate limit info to ConstVars's variables
            StaticVars.Endpoint = Endpoint;
            StaticVars.Limit = Limit;
            StaticVars.Remaining = Remaining;
            StaticVars.ResetTimeInSeconds = ResetTimeInSeconds;
            StaticVars.SecondsUntilReset = SecondsUntilReset;

        } catch (TwitterException te) {
            if (args.length == 6) {
            System.err.println("Failed to get rate limit status of " 
                    + args[5] + " because: " + te.getMessage());
            } else {
                System.err.println("Failed to get rate limit status because: " 
                        + te.getMessage());
            }
        }
        return StaticVars;
    }
}
