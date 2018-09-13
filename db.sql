-- phpMyAdmin SQL Dump
-- version 4.6.6
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Sep 13, 2018 at 07:34 AM
-- Server version: 5.6.35
-- PHP Version: 7.1.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `mysupercutebot`
--

-- --------------------------------------------------------

--
-- Table structure for table `player_war_log`
--

DROP TABLE IF EXISTS `player_war_log`;
CREATE TABLE `player_war_log` (
  `id` int(11) NOT NULL,
  `uuid` char(32) NOT NULL,
  `ign` varchar(50) NOT NULL,
  `guild` varchar(50) NOT NULL,
  `war_id` int(11) DEFAULT NULL,
  `survived` bit(1) DEFAULT NULL,
  `won` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `player_war_log`
--
ALTER TABLE `player_war_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `player_war_log_ibfk_1` (`war_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `player_war_log`
--
ALTER TABLE `player_war_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `player_war_log`
--
ALTER TABLE `player_war_log`
  ADD CONSTRAINT `player_war_log_ibfk_1` FOREIGN KEY (`war_id`) REFERENCES `war_log` (`id`);
