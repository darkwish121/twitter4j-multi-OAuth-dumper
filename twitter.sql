-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Aug 30, 2016 at 05:59 PM
-- Server version: 5.5.50-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4.19

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `twitter2`
--

-- --------------------------------------------------------

--
-- Table structure for table `app_auths`
--

CREATE TABLE IF NOT EXISTS `app_auths` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uScreenName` varchar(100) NOT NULL,
  `authUname` varchar(150) NOT NULL,
  `twitter_id` varchar(25) NOT NULL,
  `cKey` varchar(100) NOT NULL,
  `cSecret` varchar(100) NOT NULL,
  `uToken` varchar(100) NOT NULL,
  `uSecret` varchar(100) NOT NULL,
  `lastUsed` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `followers_parent`
--

CREATE TABLE IF NOT EXISTS `followers_parent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `targeteduser` varchar(50) NOT NULL,
  `nextcursor` varchar(50) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `followings_parent`
--

CREATE TABLE IF NOT EXISTS `followings_parent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `targeteduser` varchar(50) NOT NULL,
  `nextcursor` varchar(50) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `twitter_users`
--

CREATE TABLE IF NOT EXISTS `twitter_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `targeteduser` varchar(50) NOT NULL,
  `tweets_dumped_all` tinyint(1) NOT NULL,
  `tweets_all_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
