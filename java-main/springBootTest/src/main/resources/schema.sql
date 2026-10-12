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
  `status`      TINYINT      NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;
