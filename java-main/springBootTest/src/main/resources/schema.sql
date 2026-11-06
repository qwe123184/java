-- 应用启动时由 spring.sql.init 自动执行（幂等，可重复运行）
-- 与 sql/init.sql 内容等价，这里让“建库建表”随应用启动自动完成。

CREATE DATABASE IF NOT EXISTS `zhan_shen`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(50)  NOT NULL,
  `password`    VARCHAR(100) NOT NULL,
  `email`       VARCHAR(120) DEFAULT NULL,
  `realm`       VARCHAR(20)  NOT NULL DEFAULT '池境',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
  `permission`  TINYINT      NOT NULL DEFAULT 1 COMMENT '权限：0 管理员(执政官) / 1 默认(普通守夜人)',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称（展示用）',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `bio`         VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `status`      TINYINT      NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 兼容已存在的旧表：补齐 role 列（首次创建已含该列时此句因列已存在而失败，被 continue-on-error 忽略）
ALTER TABLE `zhan_shen`.`t_user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN';

-- 兼容已存在的旧表：补齐 permission 列（首次创建已含该列时此句因列已存在而失败，被 continue-on-error 忽略）
ALTER TABLE `zhan_shen`.`t_user` ADD COLUMN `permission` TINYINT NOT NULL DEFAULT 1 COMMENT '权限：0 管理员(执政官) / 1 默认(普通守夜人)';

-- 兼容已存在的旧表：补齐资料字段（昵称/头像/手机/简介）
ALTER TABLE `zhan_shen`.`t_user` ADD COLUMN `nickname` VARCHAR(50)  DEFAULT NULL COMMENT '昵称（展示用）';
ALTER TABLE `zhan_shen`.`t_user` ADD COLUMN `avatar`   VARCHAR(255) DEFAULT NULL COMMENT '头像 URL';
ALTER TABLE `zhan_shen`.`t_user` ADD COLUMN `phone`    VARCHAR(20)  DEFAULT NULL COMMENT '手机号';
ALTER TABLE `zhan_shen`.`t_user` ADD COLUMN `bio`      VARCHAR(500) DEFAULT NULL COMMENT '个人简介';

-- 神域档案表（首页展示 / 检索的数据源：禁墟、神秘、守夜人、任务指令等）
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_archive` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(80)  NOT NULL,
  `type`        VARCHAR(20)  NOT NULL COMMENT '禁墟/神秘/守夜人/任务',
  `realm`       VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '境界/等级',
  `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '描述',
  `cover`       VARCHAR(255) DEFAULT NULL COMMENT '封面图 URL',
  `tags`        VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0 下架 / 1 展示',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序，越大越靠前',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 兼容已存在的旧表：补齐 cover / tags 列
ALTER TABLE `zhan_shen`.`t_archive` ADD COLUMN `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面图 URL';
ALTER TABLE `zhan_shen`.`t_archive` ADD COLUMN `tags`  VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）';

-- ============================================================
-- 用户间小游戏（神域对决中心）：对决擂台 / 禁墟答题 / 反应试炼
-- 主题 theme 取值：shenyu（斩神·神域） / tomjerry（猫和老鼠）
-- ============================================================

-- 用户游戏战绩汇总（对决胜/负/平、答题胜/负/最佳分、反应最佳毫秒）
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_user_game_stat` (
  `user_id`          BIGINT       NOT NULL,
  `duel_wins`        INT          NOT NULL DEFAULT 0,
  `duel_losses`      INT          NOT NULL DEFAULT 0,
  `duel_draws`       INT          NOT NULL DEFAULT 0,
  `quiz_wins`        INT          NOT NULL DEFAULT 0,
  `quiz_losses`      INT          NOT NULL DEFAULT 0,
  `quiz_best_score`  INT          NOT NULL DEFAULT 0,
  `reaction_best_ms` INT          NOT NULL DEFAULT 0 COMMENT '0 表示尚无记录',
  `reaction_plays`   INT          NOT NULL DEFAULT 0,
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 对决擂台房间（攻/防/术 三系对轰，最佳 N 胜）
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_duel_room` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `room_code`     VARCHAR(12)  NOT NULL,
  `challenger_id` BIGINT       NOT NULL COMMENT '发起者 = P1',
  `opponent_id`   BIGINT       NOT NULL COMMENT '被挑战者 = P2',
  `theme`         VARCHAR(20)  NOT NULL DEFAULT 'shenyu' COMMENT 'shenyu 神域 / tomjerry 猫鼠',
  `status`        VARCHAR(12)  NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/PLAYING/FINISHED/CANCELED',
  `current_round` INT          NOT NULL DEFAULT 1,
  `p1_wins`       INT          NOT NULL DEFAULT 0,
  `p2_wins`       INT          NOT NULL DEFAULT 0,
  `target_wins`   INT          NOT NULL DEFAULT 3 COMMENT '先达此胜场者获胜',
  `winner_id`     BIGINT       DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at`   DATETIME     DEFAULT NULL,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duel_code` (`room_code`),
  KEY `idx_duel_players` (`challenger_id`, `opponent_id`, `status`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 对决回合（每回合双方各出一手，服务器判定胜负）
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_duel_round` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `room_id`    BIGINT       NOT NULL,
  `round_no`   INT          NOT NULL,
  `p1_choice`  VARCHAR(10)  DEFAULT NULL COMMENT 'ATTACK/DEFEND/SKILL',
  `p2_choice`  VARCHAR(10)  DEFAULT NULL,
  `winner`     VARCHAR(8)   DEFAULT NULL COMMENT 'P1/P2/DRAW/null(未结算)',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_round_room` (`room_id`, `round_no`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 禁墟答题题库
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_quiz` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `question`   VARCHAR(255) NOT NULL,
  `option_a`   VARCHAR(120) NOT NULL,
  `option_b`   VARCHAR(120) NOT NULL,
  `option_c`   VARCHAR(120) DEFAULT NULL,
  `option_d`   VARCHAR(120) DEFAULT NULL,
  `answer`     CHAR(1)      NOT NULL COMMENT 'A/B/C/D',
  `category`   VARCHAR(20)  DEFAULT '神域',
  `difficulty` TINYINT      NOT NULL DEFAULT 1 COMMENT '1 易 / 2 中 / 3 难',
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '0 下架 / 1 启用',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 答题对战房间（双方答同一套抽题，比分定胜负）
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_quiz_room` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `room_code`     VARCHAR(12)  NOT NULL,
  `challenger_id` BIGINT       NOT NULL,
  `opponent_id`   BIGINT       NOT NULL,
  `theme`         VARCHAR(20)  NOT NULL DEFAULT 'shenyu',
  `status`        VARCHAR(12)  NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/PLAYING/FINISHED/CANCELED',
  `q_count`       INT          NOT NULL DEFAULT 5,
  `question_ids`  VARCHAR(512) NOT NULL COMMENT 'JSON 数组，本局抽中的题目 id',
  `p1_score`      INT          NOT NULL DEFAULT 0,
  `p2_score`      INT          NOT NULL DEFAULT 0,
  `winner_id`     BIGINT       DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at`   DATETIME     DEFAULT NULL,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quiz_code` (`room_code`),
  KEY `idx_quiz_players` (`challenger_id`, `opponent_id`, `status`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 答题记录（每位用户在每道题上的作答，唯一约束防止重复作答）
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_quiz_answer` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `room_id`     BIGINT   NOT NULL,
  `user_id`     BIGINT   NOT NULL,
  `question_id` BIGINT   NOT NULL,
  `choice`      CHAR(1)  DEFAULT NULL COMMENT 'A/B/C/D',
  `correct`     TINYINT  NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qa` (`room_id`, `user_id`, `question_id`),
  KEY `idx_qa_room` (`room_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

-- 用户闯关进度（禁墟试炼 · 境界突破）。与 t_user 一对一，主键 user_id。
-- current_realm：当前正在挑战的境界 key（尚未突破）；max_realm：已突破到的最高境界 key。
CREATE TABLE IF NOT EXISTS `zhan_shen`.`t_user_stage` (
  `user_id`       BIGINT       NOT NULL,
  `current_realm` VARCHAR(20)  NOT NULL DEFAULT 'fanchen' COMMENT '当前挑战境界：fanchen/chi/zhan/chuan/hai/wuliang/kelain/shenming',
  `max_realm`     VARCHAR(20)  DEFAULT NULL COMMENT '已突破最高境界（NULL=尚无一境突破）',
  `clears`        INT          NOT NULL DEFAULT 0 COMMENT '累计突破次数',
  `exp`           INT          NOT NULL DEFAULT 0 COMMENT '经验值（每答对一题 +10）',
  `best_streak`   INT          NOT NULL DEFAULT 0 COMMENT '单次闯关最高连对',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;
