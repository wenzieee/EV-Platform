-- 创建数据库（指定字符集为 utf8mb4，完美支持中英文和表情符号）
CREATE DATABASE IF NOT EXISTS `new_energy_vehicle` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `new_energy_vehicle`;

-- ==========================================
-- 1. 用户表 (sys_user)
-- ==========================================
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名/账号',
  `password` varchar(100) NOT NULL COMMENT '密码(推荐使用 BCrypt 加密存储)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `role` tinyint NOT NULL DEFAULT '1' COMMENT '角色: 0-管理员, 1-普通用户',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) COMMENT '用户名需唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 插入一条默认管理员数据 (密码为明文 123456 的示例，实际项目中需存入加密后字符串)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`) VALUES ('admin', '123456', '超级管理员', 0);


-- ==========================================
-- 2. 车辆信息表 (biz_vehicle)
-- ==========================================
CREATE TABLE `biz_vehicle` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand` varchar(50) NOT NULL COMMENT '品牌(如：比亚迪、特斯拉)',
  `model` varchar(100) NOT NULL COMMENT '车型名称(如：汉EV、Model 3)',
  `price` decimal(10,2) NOT NULL COMMENT '指导价格(万元)',
  `range_km` int NOT NULL COMMENT '续航里程(km)',
  `drive_type` varchar(20) DEFAULT NULL COMMENT '驱动类型(如：前置前驱、双电机四驱)',
  `image_url` varchar(255) DEFAULT NULL COMMENT '车辆主图URL',
  `configuration` text COMMENT '详细配置描述(建议前端传JSON格式或富文本)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '上架状态: 0-下架, 1-上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_brand` (`brand`),
  KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新能源汽车信息表';

-- 插入几条测试车辆数据
INSERT INTO `biz_vehicle` (`brand`, `model`, `price`, `range_km`, `drive_type`) VALUES 
('比亚迪', '汉EV 创世版', 26.98, 715, '前置前驱'),
('特斯拉', 'Model 3 后轮驱动版', 24.59, 606, '后置后驱'),
('理想', 'L7 Pro', 31.98, 210, '双电机四驱');


-- ==========================================
-- 3. 意向订单表 (biz_intent_order)
-- ==========================================
CREATE TABLE `biz_intent_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '提交意向的用户ID',
  `vehicle_id` bigint NOT NULL COMMENT '意向车型ID',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `message` varchar(500) DEFAULT NULL COMMENT '用户留言',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '跟进状态: 0-待跟进, 1-跟进中, 2-已成交, 3-已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_vehicle_id` (`vehicle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购车意向订单表';