-- playze_family_bot.guilds_levels_rewards definition

CREATE TABLE IF NOT EXISTS `guilds_levels_rewards` (
  `guild_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `level` int NOT NULL,
  `role_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`guild_id`,`level`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- playze_family_bot.guilds_members_custom_ranks definition

CREATE TABLE IF NOT EXISTS `guilds_members_custom_ranks` (
  `guild_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `member_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `role_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`guild_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- playze_family_bot.guilds_members_invites definition

CREATE TABLE IF NOT EXISTS `guilds_members_invites` (
  `guild_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `member_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `total` int NOT NULL,
  `lefts` int NOT NULL,
  PRIMARY KEY (`guild_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- playze_family_bot.guilds_members_invites_history definition

CREATE TABLE IF NOT EXISTS `guilds_members_invites_history` (
  `guild_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `member_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `invited_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `invited_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `invitation_code` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`guild_id`,`member_id`,`invited_id`,`invited_date`,`invitation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- playze_family_bot.guilds_members_profiles definition

CREATE TABLE IF NOT EXISTS `guilds_members_profiles` (
  `guild_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `member_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `level` int NOT NULL,
  `xp` int NOT NULL,
  `time_spent_in_voice` int NOT NULL,
  `total_messages` int NOT NULL,
  `total_reactions` int NOT NULL,
  `background_unlocked` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`guild_id`,`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- playze_family_bot.guilds_settings definition

CREATE TABLE IF NOT EXISTS `guilds_settings` (
  `guild_id` varchar(30) COLLATE utf8mb4_general_ci NOT NULL,
  `setting_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `value` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`guild_id`,`setting_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;