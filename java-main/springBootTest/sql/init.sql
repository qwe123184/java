-- ============================================================
-- 《斩神之凡尘神域》后端 · 建库建表语句
-- 数据库：MySQL 8.0
-- 说明：本文件即“建库建表语句的文本留存”，可直接复制执行，
--       也可通过 sql/init-db.bat 一键执行（需本地已安装 MySQL 8）。
-- ============================================================

-- 1) 创建数据库（若不存在）
CREATE DATABASE IF NOT EXISTS `zhan_shen`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

-- 2) 使用数据库
USE `zhan_shen`;

-- 3) 创建守夜人账户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '守夜人代号（登录名，唯一）',
  `password`    VARCHAR(100) NOT NULL COMMENT '禁墟密钥（BCrypt 加密存储）',
  `email`       VARCHAR(120) DEFAULT NULL COMMENT '灵魂烙印（邮箱，可空）',
  `realm`       VARCHAR(20)  NOT NULL DEFAULT '池境' COMMENT '禁墟境界：盏境/池境/川境/海境',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '账号状态：0 正常 / 1 已封禁',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci
  COMMENT='守夜人账户表';
