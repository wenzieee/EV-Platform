-- ==============================================
-- SPU/SKU 设计 - 车系与车型功能数据库迁移脚本
-- ==============================================

-- 1. 修改 biz_vehicle 表：添加 min_price 和 max_price 字段
-- 将原来的 price 字段改为价格区间
ALTER TABLE `biz_vehicle` 
ADD COLUMN `min_price` DECIMAL(10,2) DEFAULT NULL COMMENT '最低指导价格(万元)' AFTER `model`,
ADD COLUMN `max_price` DECIMAL(10,2) DEFAULT NULL COMMENT '最高指导价格(万元)' AFTER `min_price`;

-- 将原有的 price 值迁移到 min_price 和 max_price
UPDATE `biz_vehicle` SET `min_price` = `price`, `max_price` = `price` WHERE `price` IS NOT NULL;

-- 可选：删除旧的 price 字段（如需保留历史数据可注释此行）
-- ALTER TABLE `biz_vehicle` DROP COLUMN `price`;

-- 2. 创建 biz_vehicle_trim 表（车型配置表/SKU）
CREATE TABLE `biz_vehicle_trim` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vehicle_id` bigint NOT NULL COMMENT '车辆ID（关联biz_vehicle表）',
  `trim_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称(如：标准版、豪华版、旗舰版)',
  `price` decimal(10,2) NOT NULL COMMENT '指导价格(万元)',
  `range_km` int DEFAULT NULL COMMENT '续航里程(km)',
  `battery_capacity` decimal(5,1) DEFAULT NULL COMMENT '电池容量(kWh)',
  `motor_power` int DEFAULT NULL COMMENT '电机功率(kW)',
  `max_speed` int DEFAULT NULL COMMENT '最高时速(km/h)',
  `acceleration_time` decimal(3,1) DEFAULT NULL COMMENT '加速时间(0-100km/h，秒)',
  `charge_time` int DEFAULT NULL COMMENT '充电时间(快充0-80%，分钟)',
  `colors` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '车身颜色(多个用逗号分隔)',
  `config_details` text COLLATE utf8mb4_unicode_ci COMMENT '配置详情(JSON格式)',
  `stock` int DEFAULT '0' COMMENT '库存数量',
  `is_hot` tinyint DEFAULT '0' COMMENT '是否主推: 0-否, 1-是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-下架, 1-上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车型配置表（SKU）';

-- 3. 为现有车辆创建默认配置（从原有的 price 值迁移）
INSERT INTO `biz_vehicle_trim` (
  `vehicle_id`, 
  `trim_name`, 
  `price`, 
  `range_km`, 
  `stock`, 
  `is_hot`, 
  `status`
) 
SELECT 
  `id` as `vehicle_id`,
  '标准版' as `trim_name`,
  `price` as `price`,
  `range_km` as `range_km`,
  100 as `stock`,
  1 as `is_hot`,
  `status` as `status`
FROM `biz_vehicle` 
WHERE `price` IS NOT NULL AND `is_deleted` = 0;

-- 4. 更新索引（如果需要）
ALTER TABLE `biz_vehicle` DROP INDEX IF EXISTS `idx_price`;
ALTER TABLE `biz_vehicle` ADD INDEX `idx_min_price` (`min_price`);
ALTER TABLE `biz_vehicle` ADD INDEX `idx_max_price` (`max_price`);

-- ==============================================
-- 迁移完成
-- ==============================================
