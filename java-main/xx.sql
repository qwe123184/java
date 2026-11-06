/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : xx

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 06/11/2026 09:57:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_archive
-- ----------------------------
DROP TABLE IF EXISTS `t_archive`;
CREATE TABLE `t_archive`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '禁墟/神秘/守夜人/任务',
  `realm` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '境界/等级',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图 URL',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签（逗号分隔）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0 下架 / 1 展示',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，越大越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_archive
-- ----------------------------
INSERT INTO `t_archive` VALUES (1, '凡尘神域', '禁墟', '无量', '林七夜觉醒之本源禁墟，可统御诸法、横刀向渊。', '/covers/cover-01.png', NULL, 1, 100, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (2, '炽天使', '禁墟', '克莱因', '以炽白羽翼燃尽神明之秽，净化一切迷雾中的低语。', '/covers/cover-02.png', NULL, 1, 96, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (3, '诸神精神病院', '禁墟', '无量', '封印癫狂旧神的禁墟，钥匙由总司令亲掌。', '/covers/cover-03.png', NULL, 1, 92, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (4, '横刀向渊', '禁墟', '无量', '极致攻伐之禁墟，一刀斩出，渊面为之裂开。', '/covers/cover-04.png', NULL, 1, 88, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (5, '夜幕小队', '守夜人', '夜幕', '于万家灯火前立誓的守夜人编队，镇守凡尘最后一缕光。', '/covers/cover-05.png', NULL, 1, 80, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (6, '守夜人·林七夜', '守夜人', '凡尘', '以凡尘之躯横刀向渊，于黯夜终临之时立于万万人前。', '/covers/cover-06.png', NULL, 1, 78, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (7, '守夜人·安卿鱼', '守夜人', '海境', '于寂静中观测迷雾的守夜人，以理性镇压神性躁动。', '/covers/cover-07.png', NULL, 1, 74, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (8, '克莱因之雾', '神秘', '克莱因', '源自神战残响的迷雾，触及者神智渐失、化为低语。', '/covers/cover-08.png', NULL, 1, 60, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (9, '星尘低语', '神秘', '川境', '飘散于星尘间的细碎低语，过度聆听将招致禁墟反噬。', '/covers/cover-09.png', NULL, 1, 56, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (10, '肃清沧南市神秘', '任务', '海境', '清除盘踞沧南市的高等神秘，护佑城中平民撤离。', '/covers/cover-10.png', NULL, 1, 40, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (11, '镇守沉龙关', '任务', '无量', '长驻沉龙关，封印自渊中爬升的古老禁忌。', '/covers/cover-11.png', NULL, 1, 36, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (12, '封印旧神', '任务', '无量', '集结夜幕小队，封印自精神病院逃出的旧神残念。', '/covers/cover-12.png', NULL, 1, 32, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (13, '霸剑绝境', '禁墟', '川境', '以剑意凝绝境的攻伐禁墟，一剑出则万法避让。', '/covers/cover-13.png', '禁墟,攻伐', 1, 84, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (14, '幽冥渡', '禁墟', '海境', '沟通生死的诡异禁墟，可自幽冥召回残魂片刻。', '/covers/cover-14.png', '禁墟,亡灵', 1, 76, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (15, '星轨观测者', '守夜人', '川境', '专司推演星轨、预判迷雾走向的守夜人，于寂静中守望。', '/covers/cover-15.png', '守夜人,推演', 1, 70, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (16, '沧南守序者', '守夜人', '池境', '驻守沧南市的守夜人小队，护佑凡尘灯火不灭。', '/covers/cover-16.png', '守夜人,驻防', 1, 66, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (17, '迷雾低语者', '神秘', '海境', '寄生人心的低语，诱使凡人走向自我献祭的古老神秘。', '/covers/cover-17.png', '神秘,精神', 1, 52, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (18, '苍白王座', '神秘', '克莱因', '传说中统御一切苍白死寂的旧神王座残响。', '/covers/cover-18.png', '神秘,旧神', 0, 48, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (19, '巡检大夏边境', '任务', '海境', '沿大夏边境巡检，清剿自境外渗入的高等神秘。', '/covers/cover-19.png', '任务,巡检', 1, 34, '2026-11-02 08:44:51', '2026-11-02 09:07:55');
INSERT INTO `t_archive` VALUES (20, '护送信徒迁徙', '任务', '川境', '于迷雾中护送信徒向内侧大域安全迁徙。', '/covers/cover-20.png', '任务,护送', 0, 30, '2026-11-02 08:44:51', '2026-11-02 09:07:55');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `realm` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '池境',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
  `permission` tinyint NOT NULL DEFAULT 1 COMMENT '权限：0 管理员(执政官) / 1 默认(普通守夜人)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称（展示用）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像 URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `bio` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人简介',
  `status` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', '1234', NULL, '海境', 'ADMIN', 0, '火鸡面', 'https://img1.baidu.com/it/u=407656448,3149236102&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1738', NULL, NULL, 0, '2026-11-02 08:44:51', '2026-11-02 08:44:51');
INSERT INTO `t_user` VALUES (2, 'linqiye', '1234', NULL, '海境', 'USER', 1, '林七夜', NULL, NULL, NULL, 0, '2026-11-02 08:44:51', '2026-11-02 09:08:16');
INSERT INTO `t_user` VALUES (3, 'anqingyu', '1234', 'anqingyu@zhan.cn', '海境', 'USER', 1, '安卿鱼', NULL, NULL, '于寂静中观测迷雾，以理性镇压神性躁动。', 0, '2026-11-02 08:44:51', '2026-11-02 08:44:51');
INSERT INTO `t_user` VALUES (4, 'caoyuan', '1234', 'caoyuan@zhan.cn', '池境', 'USER', 1, '曹渊', NULL, NULL, '提剑而歌的守夜人，剑鸣处迷雾退散。', 0, '2026-11-02 08:44:51', '2026-11-02 08:44:51');

SET FOREIGN_KEY_CHECKS = 1;
