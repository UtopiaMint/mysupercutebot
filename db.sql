-- phpMyAdmin SQL Dump
-- version 4.6.6deb5
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Apr 11, 2020 at 12:28 AM
-- Server version: 5.7.29-0ubuntu0.18.04.1
-- PHP Version: 7.2.24-0ubuntu0.18.04.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `mysupercutebot`
--

-- --------------------------------------------------------

--
-- Table structure for table `auth_codes`
--

CREATE TABLE `auth_codes` (
                              `ign` varchar(32) NOT NULL,
                              `uuid` char(32) NOT NULL,
                              `code` char(6) NOT NULL,
                              `good_thru` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `discord_terr_log`
--

CREATE TABLE `discord_terr_log` (
                                    `channel_id` bigint(20) NOT NULL,
                                    `guild` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `discord_war_log`
--

CREATE TABLE `discord_war_log` (
                                   `channel_id` bigint(20) NOT NULL,
                                   `guild` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

-- --------------------------------------------------------

--
-- Table structure for table `guild_tag`
--

CREATE TABLE `guild_tag` (
                             `tag` char(3) COLLATE utf8_bin NOT NULL,
                             `guild` varchar(50) CHARACTER SET utf8 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- --------------------------------------------------------

--
-- Table structure for table `imp_drain`
--

CREATE TABLE `imp_drain` (
                             `id` int(11) NOT NULL DEFAULT '0',
                             `guild` varchar(50) DEFAULT NULL,
                             `dur` bigint(13) NOT NULL,
                             `size` decimal(26,1) DEFAULT NULL,
                             `estimated_kills` decimal(38,0) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `player_war_log`
--

CREATE TABLE `player_war_log` (
                                  `id` int(11) NOT NULL,
                                  `uuid` char(32) NOT NULL,
                                  `ign` varchar(50) NOT NULL,
                                  `guild` varchar(50) NOT NULL,
                                  `war_id` int(11) DEFAULT NULL,
                                  `survived` bit(1) DEFAULT NULL,
                                  `won` bit(1) DEFAULT NULL,
                                  `count_total` int(11) NOT NULL DEFAULT '0',
                                  `count_survived` int(11) NOT NULL DEFAULT '0',
                                  `count_won` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Triggers `player_war_log`
--
DELIMITER $$
CREATE TRIGGER `playerwarlog_before_insert` BEFORE INSERT ON `player_war_log` FOR EACH ROW BEGIN
    declare total int;
    declare won int;
    declare survived int;

    select count_total, count_won, count_survived into total, won, survived from player_war_log where uuid=new.uuid and guild=new.guild order by id desc limit 1;
    set new.count_total=ifnull(total, 0)+1;
    set new.count_won=ifnull(won, 0);
    set new.count_survived=ifnull(survived, 0);
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `playerwarlog_before_update` BEFORE UPDATE ON `player_war_log` FOR EACH ROW playerwarlog_before_update: BEGIN
    if (new.won=old.won or (new.won is null and old.won is null)) and (new.survived=old.survived or (new.survived is null and old.survived is null)) then
        leave playerwarlog_before_update;
    end if;
    set new.count_won=old.count_won+ifnull(new.won, 0)-ifnull(old.won, 0);
    set new.count_survived=old.count_survived+ifnull(new.survived, 0)-ifnull(old.survived, 0);
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Stand-in structure for view `player_war_total`
-- (See below for the actual view)
--
CREATE TABLE `player_war_total` (
                                    `uuid` char(32)
    ,`ign` varchar(50)
    ,`guild` varchar(50)
    ,`count_total` int(11)
    ,`count_won` int(11)
    ,`count_survived` int(11)
);

-- --------------------------------------------------------

--
-- Table structure for table `sessions`
--

CREATE TABLE `sessions` (
                            `id` int(11) NOT NULL,
                            `ign` varchar(50) NOT NULL,
                            `uuid` char(32) NOT NULL,
                            `sess_token` char(64) NOT NULL,
                            `login_ip` varchar(39) NOT NULL,
                            `good_thru` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `terrs`
--

CREATE TABLE `terrs` (
                         `name` varchar(50) NOT NULL,
                         `assigned_to` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `terr_log`
--

CREATE TABLE `terr_log` (
                            `id` int(11) NOT NULL,
                            `defender` varchar(50) NOT NULL DEFAULT '0',
                            `attacker` varchar(50) NOT NULL DEFAULT '0',
                            `terr_name` varchar(50) NOT NULL DEFAULT '0',
                            `acquired` int(11) NOT NULL DEFAULT '0',
                            `hold_time` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `war_log`
--

CREATE TABLE `war_log` (
                           `id` int(11) NOT NULL,
                           `server` varchar(6) NOT NULL DEFAULT '0',
                           `guild` varchar(50) DEFAULT NULL,
                           `start_time` int(11) NOT NULL DEFAULT '-1',
                           `end_time` int(11) NOT NULL DEFAULT '-1',
                           `terr_entry` int(11) DEFAULT NULL,
                           `count_total` int(11) NOT NULL DEFAULT '0',
                           `count_won` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Triggers `war_log`
--
DELIMITER $$
CREATE TRIGGER `warlog_before_update` BEFORE UPDATE ON `war_log` FOR EACH ROW warlog_before_update: BEGIN
    declare total int;
    declare won int;
    if (old.guild=new.guild and old.terr_entry=new.terr_entry) then
        leave warlog_before_update;
    end if;
    if old.guild is null then

        select count_total, count_won into total, won from war_log where guild=new.guild order by id desc limit 1;
        set new.count_total=ifnull(total, 0)+1;
        set new.count_won=ifnull(won, 0);
    else

        if new.terr_entry is not null then

            set new.count_won=old.count_won+1;
        end if;
    end if;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Stand-in structure for view `war_total`
-- (See below for the actual view)
--
CREATE TABLE `war_total` (
                             `guild` varchar(50)
    ,`count_total` int(11)
    ,`count_won` int(11)
);

-- --------------------------------------------------------

--
-- Table structure for table `xp_log`
--

CREATE TABLE `xp_log` (
                          `timestamp` int(11) NOT NULL,
                          `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Structure for view `player_war_total`
--
DROP TABLE IF EXISTS `player_war_total`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `player_war_total`  AS  select `player_war_log`.`uuid` AS `uuid`,`player_war_log`.`ign` AS `ign`,`player_war_log`.`guild` AS `guild`,`player_war_log`.`count_total` AS `count_total`,`player_war_log`.`count_won` AS `count_won`,`player_war_log`.`count_survived` AS `count_survived` from `player_war_log` where `player_war_log`.`id` in (select max(`player_war_log`.`id`) from `player_war_log` group by `player_war_log`.`uuid`,`player_war_log`.`guild`) order by `player_war_log`.`count_total` desc,`player_war_log`.`count_won` desc ;

-- --------------------------------------------------------

--
-- Structure for view `war_total`
--
DROP TABLE IF EXISTS `war_total`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `war_total`  AS  select `war_log`.`guild` AS `guild`,`war_log`.`count_total` AS `count_total`,`war_log`.`count_won` AS `count_won` from `war_log` where `war_log`.`id` in (select max(`war_log`.`id`) from `war_log` group by `war_log`.`guild`) order by `war_log`.`count_total` desc ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `auth_codes`
--
ALTER TABLE `auth_codes`
    ADD UNIQUE KEY `code` (`code`);

--
-- Indexes for table `guild_tag`
--
ALTER TABLE `guild_tag`
    ADD KEY `tag` (`tag`);

--
-- Indexes for table `player_war_log`
--
ALTER TABLE `player_war_log`
    ADD PRIMARY KEY (`id`),
    ADD KEY `player_war_log_ibfk_1` (`war_id`),
    ADD KEY `ign` (`ign`),
    ADD KEY `guild` (`guild`),
    ADD KEY `uuid` (`uuid`);

--
-- Indexes for table `sessions`
--
ALTER TABLE `sessions`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `sess_token` (`sess_token`);

--
-- Indexes for table `terr_log`
--
ALTER TABLE `terr_log`
    ADD PRIMARY KEY (`id`),
    ADD KEY `acquired` (`acquired`),
    ADD KEY `terr_name` (`terr_name`);

--
-- Indexes for table `war_log`
--
ALTER TABLE `war_log`
    ADD PRIMARY KEY (`id`),
    ADD KEY `FK_war_log_terr_log_2` (`terr_entry`),
    ADD KEY `guild` (`guild`);

--
-- Indexes for table `xp_log`
--
ALTER TABLE `xp_log`
    ADD PRIMARY KEY (`timestamp`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `player_war_log`
--
ALTER TABLE `player_war_log`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=442492;
--
-- AUTO_INCREMENT for table `sessions`
--
ALTER TABLE `sessions`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=268;
--
-- AUTO_INCREMENT for table `terr_log`
--
ALTER TABLE `terr_log`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=283962;
--
-- AUTO_INCREMENT for table `war_log`
--
ALTER TABLE `war_log`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=263694;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `player_war_log`
--
ALTER TABLE `player_war_log`
    ADD CONSTRAINT `FK_player_war_log_war_log` FOREIGN KEY (`war_id`) REFERENCES `war_log` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `war_log`
--
ALTER TABLE `war_log`
    ADD CONSTRAINT `FK_war_log_terr_log_2` FOREIGN KEY (`terr_entry`) REFERENCES `terr_log` (`id`);
