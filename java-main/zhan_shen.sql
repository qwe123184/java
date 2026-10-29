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

 Date: 26/10/2026 09:50:18
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
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0 下架 / 1 展示',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，越大越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_archive
-- ----------------------------
INSERT INTO `t_archive` VALUES (1, '凡尘神域', '禁墟', '无量', '林七夜觉醒之本源禁墟，可统御诸法、横刀向渊。', 1, 100, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (2, '炽天使', '禁墟', '克莱因', '以炽白羽翼燃尽神明之秽，净化一切迷雾中的低语。', 1, 96, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (3, '诸神精神病院', '禁墟', '无量', '封印癫狂旧神的禁墟，钥匙由总司令亲掌。', 1, 92, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (4, '横刀向渊', '禁墟', '无量', '极致攻伐之禁墟，一刀斩出，渊面为之裂开。', 1, 88, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (5, '夜幕小队', '守夜人', '夜幕', '于万家灯火前立誓的守夜人编队，镇守凡尘最后一缕光。', 1, 80, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (6, '守夜人·林七夜', '守夜人', '凡尘', '以凡尘之躯横刀向渊，于黯夜终临之时立于万万人前。', 1, 78, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (7, '守夜人·安卿鱼', '守夜人', '海境', '于寂静中观测迷雾的守夜人，以理性镇压神性躁动。', 1, 74, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (8, '克莱因之雾', '神秘', '克莱因', '源自神战残响的迷雾，触及者神智渐失、化为低语。', 1, 60, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (9, '星尘低语', '神秘', '川境', '飘散于星尘间的细碎低语，过度聆听将招致禁墟反噬。', 1, 56, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (10, '肃清沧南市神秘', '任务', '海境', '清除盘踞沧南市的高等神秘，护佑城中平民撤离。', 1, 40, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (11, '镇守沉龙关', '任务', '无量', '长驻沉龙关，封印自渊中爬升的古老禁忌。', 1, 36, '2026-10-26 08:41:12', '2026-10-26 08:41:12');
INSERT INTO `t_archive` VALUES (12, '封印旧神', '任务', '无量', '集结夜幕小队，封印自精神病院逃出的旧神残念。', 1, 32, '2026-10-26 08:41:12', '2026-10-26 08:41:12');

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
  `status` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES (1, 'admin', 'admin123', NULL, '海境', 'ADMIN', 0, 0, '2026-10-26 08:59:33', '2026-10-26 08:59:32');
INSERT INTO `t_user` VALUES (2, 'ff', '123456', NULL, '池境', 'USER', 1, 0, '2026-10-26 08:59:35', '2026-10-26 08:59:34');
INSERT INTO `t_user` VALUES (3, 'normaluser', '123456', NULL, '盏境', 'USER', 1, 0, '2026-10-26 09:00:02', '2026-10-26 09:00:01');

SET FOREIGN_KEY_CHECKS = 1;
