-- 应用启动时由 spring.sql.init 自动执行（幂等，可重复运行）
-- 借助 t_archive.uk_name 唯一约束，用 INSERT IGNORE 实现“仅首次写入、重启不重复插入”。
-- 编码：schema.sql 已配置 spring.sql.init.encoding=UTF-8，此处中文可正常写入。

INSERT IGNORE INTO `zhan_shen`.`t_archive` (`name`, `type`, `realm`, `description`, `status`, `sort`) VALUES
('凡尘神域',   '禁墟',   '无量', '林七夜觉醒之本源禁墟，可统御诸法、横刀向渊。', 1, 100),
('炽天使',     '禁墟',   '克莱因', '以炽白羽翼燃尽神明之秽，净化一切迷雾中的低语。', 1, 96),
('诸神精神病院', '禁墟', '无量', '封印癫狂旧神的禁墟，钥匙由总司令亲掌。', 1, 92),
('横刀向渊',   '禁墟',   '无量', '极致攻伐之禁墟，一刀斩出，渊面为之裂开。', 1, 88),
('夜幕小队',   '守夜人', '夜幕', '于万家灯火前立誓的守夜人编队，镇守凡尘最后一缕光。', 1, 80),
('守夜人·林七夜', '守夜人', '凡尘', '以凡尘之躯横刀向渊，于黯夜终临之时立于万万人前。', 1, 78),
('守夜人·安卿鱼', '守夜人', '海境', '于寂静中观测迷雾的守夜人，以理性镇压神性躁动。', 1, 74),
('克莱因之雾', '神秘',   '克莱因', '源自神战残响的迷雾，触及者神智渐失、化为低语。', 1, 60),
('星尘低语',   '神秘',   '川境', '飘散于星尘间的细碎低语，过度聆听将招致禁墟反噬。', 1, 56),
('肃清沧南市神秘', '任务', '海境', '清除盘踞沧南市的高等神秘，护佑城中平民撤离。', 1, 40),
('镇守沉龙关', '任务',   '无量', '长驻沉龙关，封印自渊中爬升的古老禁忌。', 1, 36),
('封印旧神',   '任务',   '无量', '集结夜幕小队，封印自精神病院逃出的旧神残念。', 1, 32);

-- 初始化一个管理员（执政官）账号：permission=0 才能进入后台管理系统。
-- 账号 admin / 密码 admin123（明文，按需求不加密）。如需更换凭据或移除，直接改/删此行即可。
-- INSERT IGNORE 保证幂等：重启不重复插入，也不会覆盖你手动改过的同名账号。
INSERT IGNORE INTO `zhan_shen`.`t_user` (`username`, `password`, `email`, `realm`, `role`, `permission`, `status`, `create_time`)
VALUES ('admin', 'admin123', NULL, '海境', 'ADMIN', 0, 0, NOW());

-- 更多神域档案（演示数据，含封面/标签），INSERT IGNORE 幂等。
INSERT IGNORE INTO `zhan_shen`.`t_archive` (`name`, `type`, `realm`, `description`, `cover`, `tags`, `status`, `sort`) VALUES
('霸剑绝境',   '禁墟', '川境', '以剑意凝绝境的攻伐禁墟，一剑出则万法避让。', NULL, '禁墟,攻伐', 1, 84),
('幽冥渡',     '禁墟', '海境', '沟通生死的诡异禁墟，可自幽冥召回残魂片刻。', NULL, '禁墟,亡灵', 1, 76),
('星轨观测者', '守夜人', '川境', '专司推演星轨、预判迷雾走向的守夜人，于寂静中守望。', NULL, '守夜人,推演', 1, 70),
('沧南守序者', '守夜人', '池境', '驻守沧南市的守夜人小队，护佑凡尘灯火不灭。', NULL, '守夜人,驻防', 1, 66),
('迷雾低语者', '神秘', '海境', '寄生人心的低语，诱使凡人走向自我献祭的古老神秘。', NULL, '神秘,精神', 1, 52),
('苍白王座',   '神秘', '克莱因', '传说中统御一切苍白死寂的旧神王座残响。', NULL, '神秘,旧神', 0, 48),
('巡检大夏边境', '任务', '海境', '沿大夏边境巡检，清剿自境外渗入的高等神秘。', NULL, '任务,巡检', 1, 34),
('护送信徒迁徙', '任务', '川境', '于迷雾中护送信徒向内侧大域安全迁徙。', NULL, '任务,护送', 0, 30);

-- 演示普通用户（permission=1，默认权限），密码明文 1234。
-- 如不需要演示数据，删除以下三段即可。
INSERT IGNORE INTO `zhan_shen`.`t_user` (`username`, `password`, `email`, `realm`, `role`, `permission`, `nickname`, `avatar`, `bio`, `status`, `create_time`)
VALUES ('linqiye', '1234', 'linqiye@zhan.cn', '凡尘', 'USER', 1, '林七夜', NULL, '以凡尘之躯横刀向渊，于黯夜终临之时立于万万人前。', 0, NOW());
INSERT IGNORE INTO `zhan_shen`.`t_user` (`username`, `password`, `email`, `realm`, `role`, `permission`, `nickname`, `avatar`, `bio`, `status`, `create_time`)
VALUES ('anqingyu', '1234', 'anqingyu@zhan.cn', '海境', 'USER', 1, '安卿鱼', NULL, '于寂静中观测迷雾，以理性镇压神性躁动。', 0, NOW());
INSERT IGNORE INTO `zhan_shen`.`t_user` (`username`, `password`, `email`, `realm`, `role`, `permission`, `nickname`, `avatar`, `bio`, `status`, `create_time`)
VALUES ('caoyuan', '1234', 'caoyuan@zhan.cn', '池境', 'USER', 1, '曹渊', NULL, '提剑而歌的守夜人，剑鸣处迷雾退散。', 0, NOW());
