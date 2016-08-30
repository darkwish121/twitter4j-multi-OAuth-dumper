# twitter4j-multi-OAuth-dumper
A simple command line utility that allows a user to dump twitter data without "Rate Limits" road blocks.

## <a name="toc">Table of Contents</a>
* [Introduction](#introduction)
* [How to Install](#how-to-install)
* [ISAK Commands](#commands)
* [License](#license)

## <a name="introduction">Introduction</a> [&#8593;](#toc)
An unofficial command line library for the [Twitter Rest API](https://dev.twitter.com/rest/public).

It is built on [twitter4j](https://github.com/yusuke/twitter4j).

If you want to dump lot of data from twitter (like followers/friends ids, profiles and tweets) and blocked from 15 Minute Windows
then this is the right place for you.

By inserting multiple OAuth details (consumerKey, consumerSecret, accessToken, accessTokenSecret and screen_name) in database's app_auths table, you are ready to dump data. 

### Features
* You can run single or multiple jobs to dump data.
* Threader scripts can devide work load and run jobs simuntaneously. 
* Every threader will write its own detailed logs in separate file.
* App will automatically change OAuths when Rate Limits reached of one OAuth.
* After using all OAuths if Rate limits reached but not 15 Minute Windows then script will sleep and will auto run as soon as 15 Minute Windows complete. (TIP: As much OAuth as low time to sleep)


Scripts are ready to run visit wiki page to read commands. 

## <a name="how-to-install">How to Install</a> [&#8593;](#toc)

* Download and extract or clone the repository using

<code>git clone https://github.com/darkwish121/twitter4j-multi-OAuth-dumper.git</code>

* Create a MYSQL database with (database name/user/password is named twitter and host is localhost).
* If you want to change above credentials then visit src/misc/ConstVars.java for (database name/user/password) and src/misc/MysqlDB.java for (host).
* Build the project with your favourit editor and make jar file.


## <a name="commands">Usage of Commands</a> [&#8593;](#toc)

Please see the [Commands Usage](https://github.com/darkwish121/twitter4j-multi-OAuth-dumper/wiki) for more details.

## <a name="license">License</a> [&#8593;](#toc)

The code is licensed under the [Apache License Version 2.0.](http://www.apache.org/licenses/LICENSE-2.0)
