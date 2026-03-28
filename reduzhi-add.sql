USE `new_energy_vehicle`;

-- 1. 给 biz_vehicle 表新增 hot_score 字段，默认值为 0
ALTER TABLE `biz_vehicle` ADD COLUMN `hot_score` INT DEFAULT 0 COMMENT '热度值(浏览量)';

-- 2. 随机给所有车设置 0~500 的基础热度
UPDATE `biz_vehicle` SET `hot_score` = FLOOR(RAND() * 500);

-- 3. 手动造几辆“爆款神车”（热度拉满，确保它们等会儿能排在前6）
UPDATE `biz_vehicle` SET `hot_score` = 9999 WHERE `model` LIKE '%SU7%'; -- 小米SU7
UPDATE `biz_vehicle` SET `hot_score` = 8888 WHERE `model` LIKE '%Model Y%'; -- 特斯拉
UPDATE `biz_vehicle` SET `hot_score` = 7777 WHERE `model` LIKE '%L9%'; -- 理想
UPDATE `biz_vehicle` SET `hot_score` = 6666 WHERE `model` LIKE '%001%'; -- 极氪
UPDATE `biz_vehicle` SET `hot_score` = 5555 WHERE `model` LIKE '%汉%'; -- 比亚迪汉
UPDATE `biz_vehicle` SET `hot_score` = 4444 WHERE `model` LIKE '%M9%'; -- 问界M9