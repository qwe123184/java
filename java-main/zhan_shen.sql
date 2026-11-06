/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : zhan_shen

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 06/11/2026 15:15:00
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
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_archive
-- ----------------------------
INSERT INTO `t_archive` VALUES (1, '凡尘神域', '禁墟', '无量', '林七夜觉醒之本源禁墟，可统御诸法、横刀向渊。', NULL, NULL, 1, 100, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (2, '炽天使', '禁墟', '克莱因', '以炽白羽翼燃尽神明之秽，净化一切迷雾中的低语。', NULL, NULL, 1, 96, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (3, '诸神精神病院', '禁墟', '无量', '封印癫狂旧神的禁墟，钥匙由总司令亲掌。', NULL, NULL, 1, 92, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (4, '横刀向渊', '禁墟', '无量', '极致攻伐之禁墟，一刀斩出，渊面为之裂开。', NULL, NULL, 1, 88, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (5, '夜幕小队', '守夜人', '夜幕', '于万家灯火前立誓的守夜人编队，镇守凡尘最后一缕光。', NULL, NULL, 1, 80, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (6, '守夜人·林七夜', '守夜人', '凡尘', '以凡尘之躯横刀向渊，于黯夜终临之时立于万万人前。', NULL, NULL, 1, 78, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (7, '守夜人·安卿鱼', '守夜人', '海境', '于寂静中观测迷雾的守夜人，以理性镇压神性躁动。', NULL, NULL, 1, 74, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (8, '克莱因之雾', '神秘', '克莱因', '源自神战残响的迷雾，触及者神智渐失、化为低语。', NULL, NULL, 1, 60, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (9, '星尘低语', '神秘', '川境', '飘散于星尘间的细碎低语，过度聆听将招致禁墟反噬。', NULL, NULL, 1, 56, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (10, '肃清沧南市神秘', '任务', '海境', '清除盘踞沧南市的高等神秘，护佑城中平民撤离。', NULL, NULL, 1, 40, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (11, '镇守沉龙关', '任务', '无量', '长驻沉龙关，封印自渊中爬升的古老禁忌。', NULL, NULL, 1, 36, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (12, '封印旧神', '任务', '无量', '集结夜幕小队，封印自精神病院逃出的旧神残念。', NULL, NULL, 1, 32, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (13, '霸剑绝境', '禁墟', '川境', '以剑意凝绝境的攻伐禁墟，一剑出则万法避让。', NULL, '禁墟,攻伐', 1, 84, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (14, '幽冥渡', '禁墟', '海境', '沟通生死的诡异禁墟，可自幽冥召回残魂片刻。', NULL, '禁墟,亡灵', 1, 76, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (15, '星轨观测者', '守夜人', '川境', '专司推演星轨、预判迷雾走向的守夜人，于寂静中守望。', NULL, '守夜人,推演', 1, 70, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (16, '沧南守序者', '守夜人', '池境', '驻守沧南市的守夜人小队，护佑凡尘灯火不灭。', NULL, '守夜人,驻防', 1, 66, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (17, '迷雾低语者', '神秘', '海境', '寄生人心的低语，诱使凡人走向自我献祭的古老神秘。', NULL, '神秘,精神', 1, 52, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (18, '苍白王座', '神秘', '克莱因', '传说中统御一切苍白死寂的旧神王座残响。', NULL, '神秘,旧神', 0, 48, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (19, '巡检大夏边境', '任务', '海境', '沿大夏边境巡检，清剿自境外渗入的高等神秘。', NULL, '任务,巡检', 1, 34, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_archive` VALUES (20, '护送信徒迁徙', '任务', '川境', '于迷雾中护送信徒向内侧大域安全迁徙。', NULL, '任务,护送', 0, 30, '2026-11-06 14:05:30', '2026-11-06 14:05:30');

-- ----------------------------
-- Table structure for t_duel_room
-- ----------------------------
DROP TABLE IF EXISTS `t_duel_room`;
CREATE TABLE `t_duel_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `challenger_id` bigint NOT NULL COMMENT '发起者 = P1',
  `opponent_id` bigint NOT NULL COMMENT '被挑战者 = P2',
  `theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'shenyu' COMMENT 'shenyu 神域 / tomjerry 猫鼠',
  `status` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/PLAYING/FINISHED/CANCELED',
  `current_round` int NOT NULL DEFAULT 1,
  `p1_wins` int NOT NULL DEFAULT 0,
  `p2_wins` int NOT NULL DEFAULT 0,
  `target_wins` int NOT NULL DEFAULT 3 COMMENT '先达此胜场者获胜',
  `winner_id` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_duel_code`(`room_code` ASC) USING BTREE,
  INDEX `idx_duel_players`(`challenger_id` ASC, `opponent_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_duel_room
-- ----------------------------
INSERT INTO `t_duel_room` VALUES (1, 'JCMSW9', 1, 9, 'shenyu', 'WAITING', 1, 0, 0, 2, NULL, '2026-11-06 14:48:59', NULL, '2026-11-06 14:48:59');

-- ----------------------------
-- Table structure for t_duel_round
-- ----------------------------
DROP TABLE IF EXISTS `t_duel_round`;
CREATE TABLE `t_duel_round`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `round_no` int NOT NULL,
  `p1_choice` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ATTACK/DEFEND/SKILL',
  `p2_choice` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `winner` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'P1/P2/DRAW/null(未结算)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_round_room`(`room_id` ASC, `round_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_duel_round
-- ----------------------------

-- ----------------------------
-- Table structure for t_quiz
-- ----------------------------
DROP TABLE IF EXISTS `t_quiz`;
CREATE TABLE `t_quiz`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `option_a` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `option_b` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `option_c` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `option_d` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `answer` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'A/B/C/D',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '神域',
  `difficulty` tinyint NOT NULL DEFAULT 1 COMMENT '1 易 / 2 中 / 3 难',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0 下架 / 1 启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_quiz
-- ----------------------------
INSERT INTO `t_quiz` VALUES (1, '「凡尘神域」是谁觉醒的本源禁墟？', '林七夜', '安卿鱼', '曹渊', '炽天使', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (2, '守夜人小队「夜幕小队」镇守的是？', '沉龙关', '凡尘最后一缕光', '沧南市', '星轨', 'B', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (3, '「克莱因之雾」在档案中属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (4, '「横刀向渊」是何种性质的禁墟？', '防御', '攻伐', '治疗', '召唤', 'B', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (5, '守夜人安卿鱼的境界是？', '海境', '川境', '池境', '盏境', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (6, '「诸神精神病院」封印的是？', '旧神', '守夜人', '神秘', '任务', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (7, '档案中曹渊的称号前缀是？', '提剑而歌', '观测星轨', '横刀向渊', '镇守沉龙', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (8, '「镇守沉龙关」这一任务标注的境界是？', '无量', '海境', '川境', '池境', 'A', '神域', 2, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (9, '守夜人·林七夜在档案中的境界是？', '凡尘', '无量', '克莱因', '海境', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (10, '「星尘低语」属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (11, '在设定中，「克莱因」代表？', '一种神秘等级', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (12, '「巡检大夏边境」属于哪一类？', '任务', '禁墟', '守夜人', '神秘', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (13, '安卿鱼最擅长？', '观测迷雾', '横刀斩敌', '治愈伤势', '召唤亡魂', 'A', '神域', 1, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (14, '「迷雾低语者」会诱导凡人？', '自我献祭', '觉醒禁墟', '成为守夜人', '逃离神域', 'A', '神域', 2, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (15, '「苍白王座」是？', '旧神王座残响', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (16, '下列档案中状态为「下架」(status=0)的示例是？', '苍白王座', '凡尘神域', '炽天使', '夜幕小队', 'A', '神域', 2, 1, '2026-11-06 14:05:30');
INSERT INTO `t_quiz` VALUES (17, '「凡尘神域」是谁觉醒的本源禁墟？', '林七夜', '安卿鱼', '曹渊', '炽天使', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (18, '守夜人小队「夜幕小队」镇守的是？', '沉龙关', '凡尘最后一缕光', '沧南市', '星轨', 'B', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (19, '「克莱因之雾」在档案中属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (20, '「横刀向渊」是何种性质的禁墟？', '防御', '攻伐', '治疗', '召唤', 'B', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (21, '守夜人安卿鱼的境界是？', '海境', '川境', '池境', '盏境', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (22, '「诸神精神病院」封印的是？', '旧神', '守夜人', '神秘', '任务', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (23, '档案中曹渊的称号前缀是？', '提剑而歌', '观测星轨', '横刀向渊', '镇守沉龙', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (24, '「镇守沉龙关」这一任务标注的境界是？', '无量', '海境', '川境', '池境', 'A', '神域', 2, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (25, '守夜人·林七夜在档案中的境界是？', '凡尘', '无量', '克莱因', '海境', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (26, '「星尘低语」属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (27, '在设定中，「克莱因」代表？', '一种神秘等级', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (28, '「巡检大夏边境」属于哪一类？', '任务', '禁墟', '守夜人', '神秘', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (29, '安卿鱼最擅长？', '观测迷雾', '横刀斩敌', '治愈伤势', '召唤亡魂', 'A', '神域', 1, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (30, '「迷雾低语者」会诱导凡人？', '自我献祭', '觉醒禁墟', '成为守夜人', '逃离神域', 'A', '神域', 2, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (31, '「苍白王座」是？', '旧神王座残响', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (32, '下列档案中状态为「下架」(status=0)的示例是？', '苍白王座', '凡尘神域', '炽天使', '夜幕小队', 'A', '神域', 2, 1, '2026-11-06 14:33:03');
INSERT INTO `t_quiz` VALUES (33, '「凡尘神域」是谁觉醒的本源禁墟？', '林七夜', '安卿鱼', '曹渊', '炽天使', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (34, '守夜人小队「夜幕小队」镇守的是？', '沉龙关', '凡尘最后一缕光', '沧南市', '星轨', 'B', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (35, '「克莱因之雾」在档案中属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (36, '「横刀向渊」是何种性质的禁墟？', '防御', '攻伐', '治疗', '召唤', 'B', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (37, '守夜人安卿鱼的境界是？', '海境', '川境', '池境', '盏境', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (38, '「诸神精神病院」封印的是？', '旧神', '守夜人', '神秘', '任务', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (39, '档案中曹渊的称号前缀是？', '提剑而歌', '观测星轨', '横刀向渊', '镇守沉龙', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (40, '「镇守沉龙关」这一任务标注的境界是？', '无量', '海境', '川境', '池境', 'A', '神域', 2, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (41, '守夜人·林七夜在档案中的境界是？', '凡尘', '无量', '克莱因', '海境', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (42, '「星尘低语」属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (43, '在设定中，「克莱因」代表？', '一种神秘等级', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (44, '「巡检大夏边境」属于哪一类？', '任务', '禁墟', '守夜人', '神秘', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (45, '安卿鱼最擅长？', '观测迷雾', '横刀斩敌', '治愈伤势', '召唤亡魂', 'A', '神域', 1, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (46, '「迷雾低语者」会诱导凡人？', '自我献祭', '觉醒禁墟', '成为守夜人', '逃离神域', 'A', '神域', 2, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (47, '「苍白王座」是？', '旧神王座残响', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (48, '下列档案中状态为「下架」(status=0)的示例是？', '苍白王座', '凡尘神域', '炽天使', '夜幕小队', 'A', '神域', 2, 1, '2026-11-06 14:45:28');
INSERT INTO `t_quiz` VALUES (49, '「凡尘神域」是谁觉醒的本源禁墟？', '林七夜', '安卿鱼', '曹渊', '炽天使', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (50, '守夜人小队「夜幕小队」镇守的是？', '沉龙关', '凡尘最后一缕光', '沧南市', '星轨', 'B', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (51, '「克莱因之雾」在档案中属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (52, '「横刀向渊」是何种性质的禁墟？', '防御', '攻伐', '治疗', '召唤', 'B', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (53, '守夜人安卿鱼的境界是？', '海境', '川境', '池境', '盏境', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (54, '「诸神精神病院」封印的是？', '旧神', '守夜人', '神秘', '任务', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (55, '档案中曹渊的称号前缀是？', '提剑而歌', '观测星轨', '横刀向渊', '镇守沉龙', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (56, '「镇守沉龙关」这一任务标注的境界是？', '无量', '海境', '川境', '池境', 'A', '神域', 2, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (57, '守夜人·林七夜在档案中的境界是？', '凡尘', '无量', '克莱因', '海境', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (58, '「星尘低语」属于哪一类？', '禁墟', '神秘', '守夜人', '任务', 'B', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (59, '在设定中，「克莱因」代表？', '一种神秘等级', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (60, '「巡检大夏边境」属于哪一类？', '任务', '禁墟', '守夜人', '神秘', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (61, '安卿鱼最擅长？', '观测迷雾', '横刀斩敌', '治愈伤势', '召唤亡魂', 'A', '神域', 1, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (62, '「迷雾低语者」会诱导凡人？', '自我献祭', '觉醒禁墟', '成为守夜人', '逃离神域', 'A', '神域', 2, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (63, '「苍白王座」是？', '旧神王座残响', '一种禁墟', '一名守夜人', '一项任务', 'A', '神域', 2, 1, '2026-11-06 14:48:06');
INSERT INTO `t_quiz` VALUES (64, '下列档案中状态为「下架」(status=0)的示例是？', '苍白王座', '凡尘神域', '炽天使', '夜幕小队', 'A', '神域', 2, 1, '2026-11-06 14:48:06');

-- ----------------------------
-- Table structure for t_quiz_answer
-- ----------------------------
DROP TABLE IF EXISTS `t_quiz_answer`;
CREATE TABLE `t_quiz_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `choice` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'A/B/C/D',
  `correct` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_qa`(`room_id` ASC, `user_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_qa_room`(`room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_quiz_answer
-- ----------------------------

-- ----------------------------
-- Table structure for t_quiz_room
-- ----------------------------
DROP TABLE IF EXISTS `t_quiz_room`;
CREATE TABLE `t_quiz_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `challenger_id` bigint NOT NULL,
  `opponent_id` bigint NOT NULL,
  `theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'shenyu',
  `status` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/PLAYING/FINISHED/CANCELED',
  `q_count` int NOT NULL DEFAULT 5,
  `question_ids` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'JSON 数组，本局抽中的题目 id',
  `p1_score` int NOT NULL DEFAULT 0,
  `p2_score` int NOT NULL DEFAULT 0,
  `winner_id` bigint NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quiz_code`(`room_code` ASC) USING BTREE,
  INDEX `idx_quiz_players`(`challenger_id` ASC, `opponent_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_quiz_room
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', 'admin123', NULL, '海境', 'ADMIN', 0, NULL, NULL, NULL, NULL, 0, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_user` VALUES (2, 'linqiye', '1234', 'linqiye@zhan.cn', '凡尘', 'USER', 1, '林七夜', NULL, NULL, '以凡尘之躯横刀向渊，于黯夜终临之时立于万万人前。', 0, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_user` VALUES (3, 'anqingyu', '1234', 'anqingyu@zhan.cn', '海境', 'USER', 1, '安卿鱼', NULL, NULL, '于寂静中观测迷雾，以理性镇压神性躁动。', 0, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_user` VALUES (4, 'caoyuan', '1234', 'caoyuan@zhan.cn', '池境', 'USER', 1, '曹渊', NULL, NULL, '提剑而歌的守夜人，剑鸣处迷雾退散。', 0, '2026-11-06 14:05:30', '2026-11-06 14:05:30');
INSERT INTO `t_user` VALUES (9, 'ff', '6713', NULL, '海境', 'USER', 0, NULL, NULL, NULL, NULL, 0, '2026-11-06 14:34:56', '2026-11-06 14:34:56');

-- ----------------------------
-- Table structure for t_user_game_stat
-- ----------------------------
DROP TABLE IF EXISTS `t_user_game_stat`;
CREATE TABLE `t_user_game_stat`  (
  `user_id` bigint NOT NULL,
  `duel_wins` int NOT NULL DEFAULT 0,
  `duel_losses` int NOT NULL DEFAULT 0,
  `duel_draws` int NOT NULL DEFAULT 0,
  `quiz_wins` int NOT NULL DEFAULT 0,
  `quiz_losses` int NOT NULL DEFAULT 0,
  `quiz_best_score` int NOT NULL DEFAULT 0,
  `reaction_best_ms` int NOT NULL DEFAULT 0 COMMENT '0 表示尚无记录',
  `reaction_plays` int NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user_game_stat
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
