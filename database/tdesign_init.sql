/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80033
 Source Host           : localhost:3306
 Source Schema         : tdesign

 Target Server Type    : MySQL
 Target Server Version : 80033
 File Encoding         : 65001

 Date: 23/01/2026 03:43:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_provider_settings
-- ----------------------------
DROP TABLE IF EXISTS `ai_provider_settings`;
CREATE TABLE `ai_provider_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `vendor` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `endpoint_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `temperature` double NULL DEFAULT NULL,
  `max_tokens` int NULL DEFAULT NULL,
  `is_default` tinyint NULL DEFAULT NULL,
  `enabled` tinyint NULL DEFAULT NULL,
  `extra_headers` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_test_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_test_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_tested_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_provider_settings
-- ----------------------------

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `publish_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `created_by_id` bigint NULL DEFAULT NULL,
  `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_broadcasted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of announcements
-- ----------------------------

-- ----------------------------
-- Table structure for file_download_history
-- ----------------------------
DROP TABLE IF EXISTS `file_download_history`;
CREATE TABLE `file_download_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL,
  `file_id` bigint NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `size_bytes` bigint NOT NULL DEFAULT 0,
  `downloaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_download_history_user_time`(`user_id` ASC, `downloaded_at` ASC) USING BTREE,
  INDEX `idx_file_download_history_file_time`(`file_id` ASC, `downloaded_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_download_history
-- ----------------------------
INSERT INTO `file_download_history` VALUES (1, 1, 1, 'TDesign设计规范.pdf', 5242880, '2026-01-13 22:45:31');
INSERT INTO `file_download_history` VALUES (2, 1, 2, '系统架构图.png', 2097152, '2026-01-13 23:10:58');

-- ----------------------------
-- Table structure for file_resources
-- ----------------------------
DROP TABLE IF EXISTS `file_resources`;
CREATE TABLE `file_resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_by_id` bigint NULL DEFAULT NULL,
  `created_by_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_resources
-- ----------------------------

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `to_user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL,
  `collected` tinyint NOT NULL,
  `quality` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_messages_user_time`(`to_user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_messages_user` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES ('631b2bb4-526a-44c1-833f-3a22c0a5b138', 1, '111', 'message', 0, 0, 'high', '2026-01-18 14:54:11');
INSERT INTO `messages` VALUES ('8ba05d7b-d74e-4877-9d6c-0ad0322f1a3a', 3, '111', 'message', 1, 0, 'high', '2026-01-18 14:54:11');
INSERT INTO `messages` VALUES ('a06fe751-8ab7-4e41-b898-8a7717306e0a', 1, '【公告】云计算技术发展趋势与未来展望：深入分析了云计算技术的演进历程，探讨了混合云、边缘计算和云原生等新兴技术趋势，展望了云计算对数字经济的推动作用。', 'announcement', 1, 0, 'high', '2026-01-09 04:03:19');
INSERT INTO `messages` VALUES ('b2109c0f-7a9a-4620-b561-8173b7590e3b', 3, '【公告】云计算技术发展趋势与未来展望：深入分析了云计算技术的演进历程，探讨了混合云、边缘计算和云原生等新兴技术趋势，展望了云计算对数字经济的推动作用。', 'announcement', 1, 0, 'high', '2026-01-09 04:03:19');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `publish_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  `created_by_id` bigint NULL DEFAULT NULL,
  `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `os` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `browser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `user_guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 379 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of operation_logs
-- ----------------------------
INSERT INTO `operation_logs` VALUES (379, 'LOGIN', '登录', '用户登录', 1, 'admin', '127.0.0.1', 'Windows 设备', 'Windows 10', 'Edge', '2026-01-23 03:36:20', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (380, 'DELETE', '公告管理', '删除公告: AI技术在医疗领域的创新应用与发展前景', 1, 'admin', '127.0.0.1', 'Windows 设备', 'Windows 10', 'Edge', '2026-01-23 03:36:57', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (381, 'DELETE', '公告管理', '删除公告: 大数据分析助力企业决策的实践案例', 1, 'admin', '127.0.0.1', 'Windows 设备', 'Windows 10', 'Edge', '2026-01-23 03:36:59', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (382, 'DELETE', '公告管理', '删除公告: 区块链技术在供应链管理中的应用', 1, 'admin', '127.0.0.1', 'Windows 设备', 'Windows 10', 'Edge', '2026-01-23 03:37:01', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (383, 'WITHDRAW', '公告管理', '更改公告状态: 云计算技术发展趋势与未来展望', 1, 'admin', '127.0.0.1', 'Windows 设备', 'Windows 10', 'Edge', '2026-01-23 03:37:03', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (384, 'DELETE', '公告管理', '删除公告: 云计算技术发展趋势与未来展望', 1, 'admin', '127.0.0.1', 'Windows 设备', 'Windows 10', 'Edge', '2026-01-23 03:37:10', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');

-- ----------------------------
-- Table structure for role_menus
-- ----------------------------
DROP TABLE IF EXISTS `role_menus`;
CREATE TABLE `role_menus`  (
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_menus
-- ----------------------------
INSERT INTO `role_menus` VALUES (1, 910);
INSERT INTO `role_menus` VALUES (1, 911);
INSERT INTO `role_menus` VALUES (1, 1000);
INSERT INTO `role_menus` VALUES (1, 1001);
INSERT INTO `role_menus` VALUES (1, 1002);
INSERT INTO `role_menus` VALUES (1, 1003);
INSERT INTO `role_menus` VALUES (1, 1004);
INSERT INTO `role_menus` VALUES (1, 1005);
INSERT INTO `role_menus` VALUES (1, 1006);
INSERT INTO `role_menus` VALUES (1, 1007);
INSERT INTO `role_menus` VALUES (1, 1013);
INSERT INTO `role_menus` VALUES (1, 1018);
INSERT INTO `role_menus` VALUES (1, 1019);
INSERT INTO `role_menus` VALUES (1, 1020);
INSERT INTO `role_menus` VALUES (1, 1021);
INSERT INTO `role_menus` VALUES (1, 1022);
INSERT INTO `role_menus` VALUES (1, 1023);
INSERT INTO `role_menus` VALUES (1, 1030);
INSERT INTO `role_menus` VALUES (1, 1031);
INSERT INTO `role_menus` VALUES (1, 1032);
INSERT INTO `role_menus` VALUES (1, 1034);
INSERT INTO `role_menus` VALUES (1, 1035);
INSERT INTO `role_menus` VALUES (1, 1036);
INSERT INTO `role_menus` VALUES (1, 1037);
INSERT INTO `role_menus` VALUES (1, 1038);
INSERT INTO `role_menus` VALUES (1, 1039);
INSERT INTO `role_menus` VALUES (1, 1040);
INSERT INTO `role_menus` VALUES (1, 1041);
INSERT INTO `role_menus` VALUES (1, 1042);
INSERT INTO `role_menus` VALUES (2, 911);
INSERT INTO `role_menus` VALUES (2, 1031);
INSERT INTO `role_menus` VALUES (2, 1032);

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `role_id` bigint NOT NULL,
  `permission` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`role_id`, `permission`) USING BTREE,
  CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 'system:announcement:create');
INSERT INTO `role_permissions` VALUES (1, 'system:announcement:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:announcement:query');
INSERT INTO `role_permissions` VALUES (1, 'system:announcement:update');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementCards:create');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementCards:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementCards:query');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementCards:update');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementTable:create');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementTable:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementTable:query');
INSERT INTO `role_permissions` VALUES (1, 'system:AnnouncementTable:update');
INSERT INTO `role_permissions` VALUES (1, 'system:example:create');
INSERT INTO `role_permissions` VALUES (1, 'system:example:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:example:query');
INSERT INTO `role_permissions` VALUES (1, 'system:example:update');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleGoods:create');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleGoods:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleGoods:query');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleGoods:update');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleOrder:create');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleOrder:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleOrder:query');
INSERT INTO `role_permissions` VALUES (1, 'system:ExampleOrder:update');
INSERT INTO `role_permissions` VALUES (1, 'system:ExamplePrint:query');
INSERT INTO `role_permissions` VALUES (1, 'system:MessageSend:create');
INSERT INTO `role_permissions` VALUES (1, 'system:MessageSend:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:MessageSend:query');
INSERT INTO `role_permissions` VALUES (1, 'system:MessageSend:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SwaggerUI:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SwaggerUI:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SwaggerUI:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SwaggerUI:update');
INSERT INTO `role_permissions` VALUES (1, 'system:system:create');
INSERT INTO `role_permissions` VALUES (1, 'system:system:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:system:query');
INSERT INTO `role_permissions` VALUES (1, 'system:system:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemAi:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemAi:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemAi:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemAi:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemLog:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemLog:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemLog:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemLog:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMenu:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMenu:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMenu:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMenu:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitor:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitor:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitor:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitor:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorDruid:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorOnlineUser:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorOnlineUser:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorOnlineUser:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorOnlineUser:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorRedis:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorRedis:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorRedis:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorRedis:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorServer:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorServer:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorServer:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemMonitorServer:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemPersonalize:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemPersonalize:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemPersonalize:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemPersonalize:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemRole:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemRole:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemRole:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemRole:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemSecurity:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemSecurity:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemSensitive:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemSensitive:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemSensitive:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemSensitive:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemStorage:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemStorage:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemUser:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemUser:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemUser:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemUser:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemVerification:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemVerification:update');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemWatermark:create');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemWatermark:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemWatermark:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemWatermark:update');
INSERT INTO `role_permissions` VALUES (1, 'system:user:create');
INSERT INTO `role_permissions` VALUES (1, 'system:user:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:user:query');
INSERT INTO `role_permissions` VALUES (1, 'system:user:update');
INSERT INTO `role_permissions` VALUES (1, 'system:UserIndex:create');
INSERT INTO `role_permissions` VALUES (1, 'system:UserIndex:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:UserIndex:query');
INSERT INTO `role_permissions` VALUES (1, 'system:UserIndex:update');
INSERT INTO `role_permissions` VALUES (1, 'system:users:create');
INSERT INTO `role_permissions` VALUES (1, 'system:users:delete');
INSERT INTO `role_permissions` VALUES (1, 'system:users:query');
INSERT INTO `role_permissions` VALUES (1, 'system:users:update');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleGoods:create');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleGoods:delete');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleGoods:query');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleGoods:update');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleOrder:create');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleOrder:delete');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleOrder:query');
INSERT INTO `role_permissions` VALUES (2, 'system:ExampleOrder:update');
INSERT INTO `role_permissions` VALUES (2, 'system:ExamplePrint:query');
INSERT INTO `role_permissions` VALUES (2, 'system:SystemLog:create');
INSERT INTO `role_permissions` VALUES (2, 'system:SystemLog:delete');
INSERT INTO `role_permissions` VALUES (2, 'system:SystemLog:query');
INSERT INTO `role_permissions` VALUES (2, 'system:SystemLog:update');
INSERT INTO `role_permissions` VALUES (2, 'system:UserIndex:create');
INSERT INTO `role_permissions` VALUES (2, 'system:UserIndex:delete');
INSERT INTO `role_permissions` VALUES (2, 'system:UserIndex:query');
INSERT INTO `role_permissions` VALUES (2, 'system:UserIndex:update');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'admin', '管理员 - 拥有所有权限');
INSERT INTO `roles` VALUES (2, 'user', '普通用户 - 基本查看权限');

-- ----------------------------
-- Table structure for security_captcha_settings
-- ----------------------------
DROP TABLE IF EXISTS `security_captcha_settings`;
CREATE TABLE `security_captcha_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `captcha_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `drag_captcha_width` int NULL DEFAULT NULL,
  `drag_captcha_height` int NULL DEFAULT NULL,
  `drag_captcha_threshold` int NULL DEFAULT NULL,
  `image_captcha_length` int NULL DEFAULT NULL,
  `image_captcha_noise_lines` int NULL DEFAULT NULL,
  `captcha_enabled` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_captcha_settings
-- ----------------------------
INSERT INTO `security_captcha_settings` VALUES (1, 'drag', 350, 200, 98, 5, 8, 1);

-- ----------------------------
-- Table structure for security_password_policy
-- ----------------------------
DROP TABLE IF EXISTS `security_password_policy`;
CREATE TABLE `security_password_policy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password_min_length` int NULL DEFAULT NULL,
  `password_require_uppercase` tinyint NULL DEFAULT NULL,
  `password_require_lowercase` tinyint NULL DEFAULT NULL,
  `password_require_special` tinyint NULL DEFAULT NULL,
  `password_allow_sequential` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_password_policy
-- ----------------------------
INSERT INTO `security_password_policy` VALUES (1, 6, 1, 1, 1, 0);

-- ----------------------------
-- Table structure for security_token_settings
-- ----------------------------
DROP TABLE IF EXISTS `security_token_settings`;
CREATE TABLE `security_token_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_timeout_minutes` int NULL DEFAULT NULL,
  `token_timeout_minutes` int NULL DEFAULT NULL,
  `token_refresh_grace_minutes` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_token_settings
-- ----------------------------
INSERT INTO `security_token_settings` VALUES (1, 1440, 1440, 60);

-- ----------------------------
-- Table structure for sensitive_page_settings
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_page_settings`;
CREATE TABLE `sensitive_page_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `page_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enabled` tinyint NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `page_key`(`page_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sensitive_page_settings
-- ----------------------------
INSERT INTO `sensitive_page_settings` VALUES (21, '/monitor/online-user', '系统监控 / 在线用户', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (22, '/monitor/server', '系统监控 / 服务监控', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (23, '/monitor/redis', '系统监控 / Redis监控', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (24, '/user/index', '个人中心 / 个人中心', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (25, '/system/menu', '系统设置 / 目录/页面管理', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (26, '/system/personalize', '系统设置 / 个性化设置', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (27, '/system/storage', '系统设置 / 对象存储', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (28, '/system/verification', '系统设置 / 验证设置', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (29, '/system/security', '系统设置 / 安全设置', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (30, '/system/sensitive', '系统设置 / 敏感词拦截', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (31, '/system/ai', '系统设置 / AI设置', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (32, '/user-settings/user', '用户设置 / 用户管理', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (33, '/user-settings/role', '用户设置 / 角色管理', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (34, '/user-settings/log', '用户设置 / 操作日志', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (35, '/announcement/table', '公告管理 / 公告列表', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (36, '/announcement/cards', '公告管理 / 公告卡片', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');

-- ----------------------------
-- Table structure for sensitive_settings
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_settings`;
CREATE TABLE `sensitive_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` tinyint NOT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sensitive_settings
-- ----------------------------
INSERT INTO `sensitive_settings` VALUES (1, 0, '2026-01-09 22:32:28');

-- ----------------------------
-- Table structure for sensitive_words
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_words`;
CREATE TABLE `sensitive_words`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `word` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `enabled` tinyint NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `word`(`word` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sensitive_words
-- ----------------------------

-- ----------------------------
-- Table structure for storage_settings
-- ----------------------------
DROP TABLE IF EXISTS `storage_settings`;
CREATE TABLE `storage_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `access_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `custom_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `path_prefix` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `public_read` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of storage_settings
-- ----------------------------
INSERT INTO `storage_settings` VALUES (1, 'LOCAL', NULL, NULL, NULL, NULL, 'zz', NULL, NULL, 1);

-- ----------------------------
-- Table structure for sys_menu_items
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_items`;
CREATE TABLE `sys_menu_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `node_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `route_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `title_zh_cn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title_en_us` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hidden` tinyint NOT NULL DEFAULT 0,
  `keep_alive` tinyint NOT NULL DEFAULT 1,
  `frame_src` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `frame_blank` tinyint NOT NULL DEFAULT 0,
  `enabled` tinyint NOT NULL DEFAULT 1,
  `require_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `require_permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `order_no` int NOT NULL DEFAULT 0,
  `version` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `actions` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_menu_route_name`(`route_name` ASC) USING BTREE,
  INDEX `idx_sys_menu_parent_order`(`parent_id` ASC, `order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1045 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu_items
-- ----------------------------
INSERT INTO `sys_menu_items` VALUES (910, NULL, 'DIR', '/user', 'user', 'LAYOUT', '/user/index', '个人中心', 'User Center', 'user-safety-filled', 0, 1, NULL, 0, 1, NULL, NULL, 3, 6, '2025-12-30 23:58:19', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (911, 910, 'PAGE', 'index', 'UserIndex', '/user/index', NULL, '个人中心', 'User Center', 'user', 0, 1, NULL, 0, 1, NULL, NULL, 0, 5, '2025-12-30 23:58:19', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1000, NULL, 'DIR', '/system', 'system', 'LAYOUT', NULL, '系统设置', 'System', 'setting', 0, 1, NULL, 0, 1, NULL, NULL, 6, 7, '2025-12-14 03:19:00', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1001, 1019, 'PAGE', 'user', 'SystemUser', '/system/user/index', NULL, '用户管理', 'Users', 'user', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 16, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1002, 1019, 'PAGE', 'role', 'SystemRole', '/system/role/index', NULL, '角色管理', 'Roles', 'usergroup', 0, 1, NULL, 0, 1, 'admin', NULL, 1, 12, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1003, 1000, 'PAGE', 'menu', 'SystemMenu', '/system/menu/index', NULL, '目录/页面管理', 'Menu Manager', 'tree-round-dot-vertical', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 7, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1004, NULL, 'DIR', '/monitor', 'SystemMonitor', 'LAYOUT', NULL, '系统监控', 'Monitor', 'chart-bar', 0, 1, NULL, 0, 1, 'admin', NULL, 5, 14, '2025-12-16 01:38:31', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1005, 1004, 'PAGE', 'online-user', 'SystemMonitorOnlineUser', '/system/monitor/online-user/index', NULL, '在线用户', 'Online User', 'usergroup-add', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 5, '2025-12-16 01:38:31', '2026-01-18 21:22:37', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1006, 1004, 'PAGE', 'server', 'SystemMonitorServer', '/system/monitor/server/index', NULL, '服务监控', 'Server Monitor', 'server', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 4, '2025-12-16 03:21:37', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1007, 1004, 'PAGE', 'redis', 'SystemMonitorRedis', '/system/monitor/redis/index', NULL, 'Redis监控', 'Redis Monitor', 'chart-3d', 0, 1, NULL, 0, 1, 'admin', NULL, 1, 9, '2025-12-16 03:28:05', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1013, 1000, 'PAGE', 'personalize', 'SystemPersonalize', '/system/personalize/index', NULL, '个性化设置', 'Personalize', 'setting-1', 0, 1, NULL, 0, 1, NULL, NULL, 2, 7, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1018, 1019, 'PAGE', 'log', 'SystemLog', '/system/log/index', NULL, '操作日志', 'Operation Logs', 'file', 0, 1, NULL, 0, 1, 'admin', NULL, 2, 6, '2025-12-29 14:21:55', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1019, NULL, 'DIR', '/user-settings', 'users', 'LAYOUT', NULL, '用户设置', NULL, 'usergroup', 0, 1, NULL, 0, 1, NULL, NULL, 4, 7, '2025-12-29 20:45:20', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1020, 1004, 'PAGE', 'swagger', 'SwaggerUI', 'IFRAME', NULL, '接口文档', 'API Docs', 'api', 0, 1, '/api/swagger-ui/index.html', 0, 1, NULL, NULL, 2, 5, '2025-12-29 21:40:49', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1021, 1000, 'PAGE', 'storage', 'SystemStorage', '/system/storage/index', NULL, '对象存储', 'Object Storage', 'cloud-upload', 0, 1, NULL, 0, 1, NULL, NULL, 3, 5, '2025-12-31 05:05:00', '2026-01-18 21:21:03', 'update,query');
INSERT INTO `sys_menu_items` VALUES (1022, 1000, 'PAGE', 'verification', 'SystemVerification', '/system/verification/index', NULL, '验证设置', 'Verification', 'check-circle', 0, 1, NULL, 0, 1, NULL, NULL, 4, 5, '2026-01-05 15:24:28', '2026-01-18 21:21:03', 'update,query');
INSERT INTO `sys_menu_items` VALUES (1023, 1000, 'PAGE', 'security', 'SystemSecurity', '/system/security/index', NULL, '安全设置', 'Security', 'lock-on', 0, 1, NULL, 0, 1, NULL, NULL, 5, 5, '2026-01-06 02:27:43', '2026-01-18 21:21:03', 'update,query');
INSERT INTO `sys_menu_items` VALUES (1030, NULL, 'DIR', '/example', 'example', 'LAYOUT', '/example/goods', '示例页面', 'Examples', 'file', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 12, '2026-01-05 15:49:43', '2026-01-19 00:09:21', NULL);
INSERT INTO `sys_menu_items` VALUES (1031, 1030, 'PAGE', 'goods', 'ExampleGoods', '/example/goods/index', NULL, '商品管理', 'Goods', NULL, 0, 1, NULL, 0, 1, 'admin', NULL, 0, 11, '2026-01-05 15:49:43', '2026-01-19 00:09:23', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1032, 1030, 'PAGE', 'order', 'ExampleOrder', '/example/order/index', NULL, '订单管理', 'Orders', NULL, 0, 1, NULL, 0, 1, 'admin', NULL, 1, 11, '2026-01-05 15:49:43', '2026-01-19 00:09:23', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1034, 1000, 'PAGE', 'sensitive', 'SystemSensitive', '/system/sensitive/index', NULL, '敏感词拦截', 'Sensitive Words', 'filter', 0, 1, NULL, 0, 1, NULL, NULL, 6, 4, '2026-01-08 21:22:11', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1035, NULL, 'DIR', '/announcement', 'announcement', 'LAYOUT', '/announcement/table', '公告管理', 'Announcements', 'notification', 0, 1, NULL, 0, 1, NULL, NULL, 2, 4, '2026-01-08 22:43:08', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1036, 1035, 'PAGE', 'table', 'AnnouncementTable', '/announcement/table/index', NULL, '公告管理', 'Announcement Table', NULL, 0, 1, NULL, 0, 1, NULL, NULL, 1, 5, '2026-01-08 22:43:20', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1037, 1035, 'PAGE', 'cards', 'AnnouncementCards', '/announcement/cards/index', NULL, '通知中心', 'Announcement Cards', NULL, 0, 1, NULL, 0, 1, NULL, NULL, 2, 5, '2026-01-08 22:43:25', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1038, 1000, 'PAGE', 'ai', 'SystemAi', '/system/ai/index', NULL, 'AI设置', 'AI Settings', 'chat', 0, 1, NULL, 0, 1, NULL, NULL, 7, 4, '2026-01-08 22:45:58', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1039, 1035, 'PAGE', 'send', 'MessageSend', '/message/send/index', NULL, '消息发送', 'Message Send', NULL, 0, 1, NULL, 0, 1, NULL, NULL, 0, 4, '2026-01-08 22:43:25', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1040, NULL, 'DIR', '/console', 'console', 'LAYOUT', '/console/download', '文件下载', 'Console', 'download', 0, 1, NULL, 0, 1, NULL, NULL, 1, 4, '2026-01-14 02:30:00', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1041, 1040, 'PAGE', 'download', 'ConsoleDownload', '/console/download/index', NULL, '文件下载', 'File Download', 'download', 0, 1, NULL, 0, 1, NULL, NULL, 0, 3, '2026-01-14 02:30:00', '2026-01-18 21:21:03', 'create,update,delete,query');

-- ----------------------------
-- Table structure for ui_brand_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_brand_settings`;
CREATE TABLE `ui_brand_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `website_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `logo_expanded_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `logo_collapsed_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `favicon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `qr_code_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_brand_settings
-- ----------------------------
INSERT INTO `ui_brand_settings` VALUES (1, 'Elexvx 脚手架系统', '1.0', '', '', NULL, '');

-- ----------------------------
-- Table structure for ui_footer_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_footer_settings`;
CREATE TABLE `ui_footer_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `footer_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `footer_icp` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `copyright_start_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_footer_settings
-- ----------------------------
INSERT INTO `ui_footer_settings` VALUES (1, 'Elexvx Inc', '苏ICP备2025160017号', '2025');

-- ----------------------------
-- Table structure for ui_layout_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_layout_settings`;
CREATE TABLE `ui_layout_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `default_home` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `show_footer` tinyint NULL DEFAULT NULL,
  `is_sidebar_compact` tinyint NULL DEFAULT NULL,
  `show_breadcrumb` tinyint NULL DEFAULT NULL,
  `menu_auto_collapsed` tinyint NULL DEFAULT NULL,
  `layout` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `split_menu` tinyint NULL DEFAULT NULL,
  `side_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_footer_aside` tinyint NULL DEFAULT NULL,
  `is_sidebar_fixed` tinyint NULL DEFAULT NULL,
  `is_header_fixed` tinyint NULL DEFAULT NULL,
  `is_use_tabs_router` tinyint NULL DEFAULT NULL,
  `show_header` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_layout_settings
-- ----------------------------
INSERT INTO `ui_layout_settings` VALUES (1, '/example/goods', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for ui_legal_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_legal_settings`;
CREATE TABLE `ui_legal_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `privacy_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_legal_settings
-- ----------------------------
INSERT INTO `ui_legal_settings` VALUES (1, '<h1 style=\"text-align: start;\">用户协议</h1><p style=\"text-align: start;\"><em>宏翔商道（南京）科技发展有限公司（Elexvx Inc）</em>（以下简称“我们”）依据本协议为用户（以下简称“你”）提供<em>Elexvx 脚手架系统</em>服务。本协议对你和我们均具有法律约束力。</p><h2 style=\"text-align: start;\">一、本服务的功能</h2><p style=\"text-align: start;\">你可以使用本服务企业底层脚手架系统。</p><p style=\"text-align: start;\"><br></p><h2 style=\"text-align: start;\">二、责任范围及限制</h2><p style=\"text-align: start;\">你使用本服务得到的结果仅供参考，实际情况以官方为准。</p><p style=\"text-align: start;\"><br></p><h2 style=\"text-align: start;\">三、隐私保护</h2><p style=\"text-align: start;\">我们重视对你隐私的保护，你的个人隐私信息将根据《隐私政策》受到保护与规范，详情请参阅《隐私政策》。</p><p style=\"text-align: start;\"><br></p><h2 style=\"text-align: start;\">四、其他条款</h2><p style=\"text-align: start;\">4.1 本协议所有条款的标题仅为阅读方便，本身并无实际涵义，不能作为本协议涵义解释的依据。</p><p style=\"text-align: start;\">4.2 本协议条款无论因何种原因部分无效或不可执行，其余条款仍有效，对双方具有约束力。</p>', '<h1 style=\"text-align: start;\">隐私政策</h1><p>更新日期：<strong>2026/1/8</strong></p><p>生效日期：<strong>2026/1/8</strong></p><h1 style=\"text-align: start;\">导言</h1><p style=\"text-align: start;\"><em>Elexvx 脚手架系统</em> 是一款由 <em>宏翔商道（南京）科技发展有限公司（Elexvx Inc）</em> （以下简称“我们”）提供的产品。 您在使用我们的服务时，我们可能会收集和使用您的相关信息。我们希望通过本《隐私政策》向您说明，在使用我们的服务时，我们如何收集、使用、储存和分享这些信息，以及我们为您提供的访问、更新、控制和保护这些信息的方式。 本《隐私政策》与您所使用的 <em>Elexvx 脚手架系统</em> 服务息息相关，希望您仔细阅读，在需要时，按照本《隐私政策》的指引，作出您认为适当的选择。本《隐私政策》中涉及的相关技术词汇，我们尽量以简明扼要的表述，并提供进一步说明的链接，以便您的理解。</p><p style=\"text-align: start;\"><strong>您使用或继续使用我们的服务，即意味着同意我们按照本《隐私政策》收集、使用、储存和分享您的相关信息。</strong></p><p style=\"text-align: start;\">如对本《隐私政策》或相关事宜有任何问题，请通过 <strong>elexvx@elexvx.com</strong> 与我们联系。</p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">1. 我们收集的信息</h1><p style=\"text-align: start;\">我们或我们的第三方合作伙伴提供服务时，可能会收集、储存和使用下列与您有关的信息。如果您不提供相关信息，可能无法注册成为我们的用户或无法享受我们提供的某些服务，或者无法达到相关服务拟达到的效果。</p><ul><li style=\"text-align: start;\"><strong>个人信息</strong>，您在注册账户或使用我们的服务时，向我们提供的相关个人信息，例如电话号码、电子邮件等。</li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">2. 信息的存储</h1><h2 style=\"text-align: start;\">2.1 信息存储的方式和期限</h2><ul><li style=\"text-align: start;\">我们会通过安全的方式存储您的信息，包括本地存储（例如利用APP进行数据缓存）、数据库和服务器日志。</li><li style=\"text-align: start;\">一般情况下，我们只会在为实现服务目的所必需的时间内或法律法规规定的条件下存储您的个人信息。</li></ul><h2 style=\"text-align: start;\">2.2 信息存储的地域</h2><ul><li style=\"text-align: start;\">我们会按照法律法规规定，将境内收集的用户个人信息存储于中国境内。</li><li style=\"text-align: start;\">目前我们不会跨境传输或存储您的个人信息。将来如需跨境传输或存储的，我们会向您告知信息出境的目的、接收方、安全保证措施和安全风险，并征得您的同意。</li></ul><h2 style=\"text-align: start;\">2.3 产品或服务停止运营时的通知</h2><ul><li style=\"text-align: start;\">当我们的产品或服务发生停止运营的情况时，我们将以推送通知、公告等形式通知您，并在合理期限内删除您的个人信息或进行匿名化处理，法律法规另有规定的除外。</li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">3. 信息安全</h1><p style=\"text-align: start;\">我们使用各种安全技术和程序，以防信息的丢失、不当使用、未经授权阅览或披露。例如，在某些服务中，我们将利用加密技术（例如SSL）来保护您提供的个人信息。但请您理解，由于技术的限制以及可能存在的各种恶意手段，在互联网行业，即便竭尽所能加强安全措施，也不可能始终保证信息百分之百的安全。您需要了解，您接入我们的服务所用的系统和通讯网络，有可能因我们可控范围外的因素而出现问题。</p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">4. 我们如何使用信息</h1><p style=\"text-align: start;\">我们可能将在向您提供服务的过程之中所收集的信息用作下列用途：</p><ul><li style=\"text-align: start;\">向您提供服务；</li><li style=\"text-align: start;\">在我们提供服务时，用于身份验证、客户服务、安全防范、诈骗监测、存档和备份用途，确保我们向您提供的产品和服务的安全性；</li><li style=\"text-align: start;\">帮助我们设计新服务，改善我们现有服务；</li><li style=\"text-align: start;\">使我们更加了解您如何接入和使用我们的服务，从而针对性地回应您的个性化需求，例如语言设定、位置设定、个性化的帮助服务和指示，或对您和其他用户作出其他方面的回应；</li><li style=\"text-align: start;\">向您提供与您更加相关的广告以替代普遍投放的广告；</li><li style=\"text-align: start;\">评估我们服务中的广告和其他促销及推广活动的效果，并加以改善；</li><li style=\"text-align: start;\">软件认证或管理软件升级；</li><li style=\"text-align: start;\">让您参与有关我们产品和服务的调查。</li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">5. 信息共享</h1><p style=\"text-align: start;\">目前，我们不会主动共享或转让您的个人信息至第三方，如存在其他共享或转让您的个人信息或您需要我们将您的个人信息共享或转让至第三方情形时，我们会直接或确认第三方征得您对上述行为的明示同意。</p><p style=\"text-align: start;\">为了投放广告，评估、优化广告投放效果等目的，我们需要向广告主及其代理商等第三方合作伙伴共享您的部分数据，要求其严格遵守我们关于数据隐私保护的措施与要求，包括但不限于根据数据保护协议、承诺书及相关数据处理政策进行处理，避免识别出个人身份，保障隐私安全。</p><p style=\"text-align: start;\">我们不会向合作伙伴分享可用于识别您个人身份的信息（例如您的姓名或电子邮件地址），除非您明确授权。</p><p style=\"text-align: start;\">我们不会对外公开披露所收集的个人信息，如必须公开披露时，我们会向您告知此次公开披露的目的、披露信息的类型及可能涉及的敏感信息，并征得您的明示同意。</p><p style=\"text-align: start;\">随着我们业务的持续发展，我们有可能进行合并、收购、资产转让等交易，我们将告知您相关情形，按照法律法规及不低于本《隐私政策》所要求的标准继续保护或要求新的控制者继续保护您的个人信息。</p><p style=\"text-align: start;\">另外，根据相关法律法规及国家标准，以下情形中，我们可能会共享、转让、公开披露个人信息无需事先征得您的授权同意：</p><ul><li style=\"text-align: start;\">与国家安全、国防安全直接相关的；</li><li style=\"text-align: start;\">与公共安全、公共卫生、重大公共利益直接相关的；</li><li style=\"text-align: start;\">犯罪侦查、起诉、审判和判决执行等直接相关的；</li><li style=\"text-align: start;\">出于维护个人信息主体或其他个人的生命、财产等重大合法权益但又很难得到本人同意的；</li><li style=\"text-align: start;\">个人信息主体自行向社会公众公开个人信息的；</li><li style=\"text-align: start;\">从合法公开披露的信息中收集个人信息的，如合法的新闻报道、政府信息公开等渠道。</li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">6. 您的权利</h1><p style=\"text-align: start;\">在您使用我们的服务期间，我们可能会视产品具体情况为您提供相应的操作设置，以便您可以查询、删除、更正或撤回您的相关个人信息，您可参考相应的具体指引进行操作。此外，我们还设置了投诉举报渠道，您的意见将会得到及时的处理。如果您无法通过上述途径和方式行使您的个人信息主体权利，您可以通过本《隐私政策》中提供的联系方式提出您的请求，我们会按照法律法规的规定予以反馈。</p><p style=\"text-align: start;\">当您决定不再使用我们的产品或服务时，可以申请注销账户。注销账户后，除法律法规另有规定外，我们将删除或匿名化处理您的个人信息。</p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">7. 变更</h1><p style=\"text-align: start;\">我们可能适时修订本《隐私政策》的条款。当变更发生时，我们会在版本更新时向您提示新的《隐私政策》，并向您说明生效日期。请您仔细阅读变更后的《隐私政策》内容，<strong>若您继续使用我们的服务，即表示您同意我们按照更新后的《隐私政策》处理您的个人信息。</strong></p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">8. 未成年人保护</h1><p style=\"text-align: start;\">我们鼓励父母或监护人指导未满十八岁的未成年人使用我们的服务。我们建议未成年人鼓励他们的父母或监护人阅读本《隐私政策》，并建议未成年人在提交的个人信息之前寻求父母或监护人的同意和指导。</p>');

-- ----------------------------
-- Table structure for ui_login_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_login_settings`;
CREATE TABLE `ui_login_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `login_bg_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `allow_multi_device_login` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_login_settings
-- ----------------------------
INSERT INTO `ui_login_settings` VALUES (1, '', 0);

-- ----------------------------
-- Table structure for ui_system_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_system_settings`;
CREATE TABLE `ui_system_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `log_retention_days` int NULL DEFAULT NULL,
  `ai_assistant_enabled` tinyint NULL DEFAULT NULL,
  `maintenance_enabled` tinyint NULL DEFAULT NULL,
  `maintenance_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_system_settings
-- ----------------------------
INSERT INTO `ui_system_settings` VALUES (1, 0, 1, 0, '系统维护中，请稍后访问');

-- ----------------------------
-- Table structure for ui_theme_settings
-- ----------------------------
DROP TABLE IF EXISTS `ui_theme_settings`;
CREATE TABLE `ui_theme_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auto_theme` tinyint NULL DEFAULT NULL,
  `light_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `dark_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `brand_theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ui_theme_settings
-- ----------------------------
INSERT INTO `ui_theme_settings` VALUES (1, 0, '06:00', '18:00', 'light', NULL);

-- ----------------------------
-- Table structure for user_parameters
-- ----------------------------
DROP TABLE IF EXISTS `user_parameters`;
CREATE TABLE `user_parameters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `param_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_parameters
-- ----------------------------

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
  `user_id` bigint NOT NULL,
  `role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`user_id`, `role`) USING BTREE,
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 'admin');
INSERT INTO `user_roles` VALUES (3, 'user');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `entity` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `leader` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `join_day` date NULL DEFAULT NULL,
  `team` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account`(`account` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 149 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '张三', '$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu', '+86 13800000000', '0000', 'admin@qq.com', 'T32F 001', '腾讯集团', 'CEO', '系统管理员', '2018-01-01', '腾讯/腾讯公司/管理层/系统管理组', 'male', '张三的昵称', '', '', '', '', '张三的简介', '/api/uploads/business/b27eeb829cd64ccaab15123e70678c24.jpg', '', '', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `users` VALUES (3, 'bob', 'Bob', '$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu', '+86 13923456789', '8889', 'bob@tencent.com', 'T32F 012', '腾讯集团', 'Sarah Li', '后端开发工程师', '2020-07-01', '腾讯/腾讯公司/CSIG事业群/云计算部/后端架构组', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '700e0805-c571-4e6a-873b-00899a32ede2');

-- ----------------------------
-- Table structure for verification_email_settings
-- ----------------------------
DROP TABLE IF EXISTS `verification_email_settings`;
CREATE TABLE `verification_email_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email_enabled` tinyint NULL DEFAULT NULL,
  `email_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_port` int NULL DEFAULT NULL,
  `email_username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_from` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_ssl` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of verification_email_settings
-- ----------------------------
INSERT INTO `verification_email_settings` VALUES (2, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for verification_sms_settings
-- ----------------------------
DROP TABLE IF EXISTS `verification_sms_settings`;
CREATE TABLE `verification_sms_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sms_enabled` tinyint NULL DEFAULT NULL,
  `sms_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_enabled` tinyint NULL DEFAULT NULL,
  `sms_aliyun_access_key_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_access_key_secret` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_sign_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_region_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_aliyun_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_enabled` tinyint NULL DEFAULT NULL,
  `sms_tencent_secret_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_secret_key` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_sign_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_template_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_tencent_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sms_sdk_app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of verification_sms_settings
-- ----------------------------
INSERT INTO `verification_sms_settings` VALUES (2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for watermark_settings
-- ----------------------------
DROP TABLE IF EXISTS `watermark_settings`;
CREATE TABLE `watermark_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `opacity` double NOT NULL,
  `size` int NOT NULL,
  `gap_x` int NOT NULL,
  `gap_y` int NOT NULL,
  `rotate` int NOT NULL,
  `enabled` tinyint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of watermark_settings
-- ----------------------------
INSERT INTO `watermark_settings` VALUES (1, 'text_single', '水印', '', 0.12, 30, 200, 200, 20, 1);

SET FOREIGN_KEY_CHECKS = 1;
