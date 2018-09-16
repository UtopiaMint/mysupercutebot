/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

DROP DATABASE IF EXISTS `mysupercutebot`;
CREATE DATABASE IF NOT EXISTS `mysupercutebot` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `mysupercutebot`;

DROP TABLE IF EXISTS `player_war_log`;
CREATE TABLE IF NOT EXISTS `player_war_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` char(32) NOT NULL,
  `ign` varchar(50) NOT NULL,
  `guild` varchar(50) NOT NULL,
  `war_id` int(11) DEFAULT NULL,
  `survived` bit(1) DEFAULT NULL,
  `won` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `player_war_log_ibfk_1` (`war_id`),
  KEY `ign` (`ign`),
  KEY `guild` (`guild`),
  CONSTRAINT `FK_player_war_log_war_log` FOREIGN KEY (`war_id`) REFERENCES `war_log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `terr_log`;
CREATE TABLE IF NOT EXISTS `terr_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `defender` varchar(50) NOT NULL DEFAULT '0',
  `attacker` varchar(50) NOT NULL DEFAULT '0',
  `terr_name` varchar(50) NOT NULL DEFAULT '0',
  `acquired` int(11) NOT NULL DEFAULT 0,
  `hold_time` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `acquired` (`acquired`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `war_log`;
CREATE TABLE IF NOT EXISTS `war_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `server` varchar(6) NOT NULL DEFAULT '0',
  `guild` varchar(50) DEFAULT NULL,
  `start_time` int(11) NOT NULL DEFAULT -1,
  `end_time` int(11) NOT NULL DEFAULT -1,
  `terr_entry` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_war_log_terr_log_2` (`terr_entry`),
  CONSTRAINT `FK_war_log_terr_log_2` FOREIGN KEY (`terr_entry`) REFERENCES `terr_log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `xp_log`;
CREATE TABLE IF NOT EXISTS `xp_log` (
  `timestamp` int(11) NOT NULL,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
