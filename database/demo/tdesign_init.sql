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
INSERT INTO `messages` VALUES ('a06fe751-8ab7-4e41-b898-8a7717306e0a', 1, '銆愬叕鍛娿€戜簯璁＄畻鎶€鏈彂灞曡秼鍔夸笌鏈潵灞曟湜锛氭繁鍏ュ垎鏋愪簡浜戣绠楁妧鏈殑婕旇繘鍘嗙▼锛屾帰璁ㄤ簡娣峰悎浜戙€佽竟缂樿绠楀拰浜戝師鐢熺瓑鏂板叴鎶€鏈秼鍔匡紝灞曟湜浜嗕簯璁＄畻瀵规暟瀛楃粡娴庣殑鎺ㄥ姩浣滅敤銆?, 'announcement', 1, 0, 'high', '2026-01-09 04:03:19');
INSERT INTO `messages` VALUES ('b2109c0f-7a9a-4620-b561-8173b7590e3b', 3, '銆愬叕鍛娿€戜簯璁＄畻鎶€鏈彂灞曡秼鍔夸笌鏈潵灞曟湜锛氭繁鍏ュ垎鏋愪簡浜戣绠楁妧鏈殑婕旇繘鍘嗙▼锛屾帰璁ㄤ簡娣峰悎浜戙€佽竟缂樿绠楀拰浜戝師鐢熺瓑鏂板叴鎶€鏈秼鍔匡紝灞曟湜浜嗕簯璁＄畻瀵规暟瀛楃粡娴庣殑鎺ㄥ姩浣滅敤銆?, 'announcement', 1, 0, 'high', '2026-01-09 04:03:19');

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
INSERT INTO `operation_logs` VALUES (379, 'LOGIN', '鐧诲綍', '鐢ㄦ埛鐧诲綍', 1, 'admin', '127.0.0.1', 'Windows 璁惧', 'Windows 10', 'Edge', '2026-01-23 03:36:20', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (380, 'DELETE', '鍏憡绠＄悊', '鍒犻櫎鍏憡: AI鎶€鏈湪鍖荤枟棰嗗煙鐨勫垱鏂板簲鐢ㄤ笌鍙戝睍鍓嶆櫙', 1, 'admin', '127.0.0.1', 'Windows 璁惧', 'Windows 10', 'Edge', '2026-01-23 03:36:57', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (381, 'DELETE', '鍏憡绠＄悊', '鍒犻櫎鍏憡: 澶ф暟鎹垎鏋愬姪鍔涗紒涓氬喅绛栫殑瀹炶返妗堜緥', 1, 'admin', '127.0.0.1', 'Windows 璁惧', 'Windows 10', 'Edge', '2026-01-23 03:36:59', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (382, 'DELETE', '鍏憡绠＄悊', '鍒犻櫎鍏憡: 鍖哄潡閾炬妧鏈湪渚涘簲閾剧鐞嗕腑鐨勫簲鐢?, 1, 'admin', '127.0.0.1', 'Windows 璁惧', 'Windows 10', 'Edge', '2026-01-23 03:37:01', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (383, 'WITHDRAW', '鍏憡绠＄悊', '鏇存敼鍏憡鐘舵€? 浜戣绠楁妧鏈彂灞曡秼鍔夸笌鏈潵灞曟湜', 1, 'admin', '127.0.0.1', 'Windows 璁惧', 'Windows 10', 'Edge', '2026-01-23 03:37:03', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');
INSERT INTO `operation_logs` VALUES (384, 'DELETE', '鍏憡绠＄悊', '鍒犻櫎鍏憡: 浜戣绠楁妧鏈彂灞曡秼鍔夸笌鏈潵灞曟湜', 1, 'admin', '127.0.0.1', 'Windows 璁惧', 'Windows 10', 'Edge', '2026-01-23 03:37:10', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827');

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
INSERT INTO `role_menus` VALUES (1, 1008);
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
INSERT INTO `role_menus` VALUES (1, 1043);
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
INSERT INTO `role_permissions` VALUES (1, 'system:SystemModule:query');
INSERT INTO `role_permissions` VALUES (1, 'system:SystemModule:update');
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
INSERT INTO `roles` VALUES (1, 'admin', '绠＄悊鍛?- 鎷ユ湁鎵€鏈夋潈闄?);
INSERT INTO `roles` VALUES (2, 'user', '鏅€氱敤鎴?- 鍩烘湰鏌ョ湅鏉冮檺');

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
  `allow_url_token_param` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of security_token_settings
-- ----------------------------
INSERT INTO `security_token_settings` VALUES (1, 1440, 1440, 60, 0);

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
INSERT INTO `sensitive_page_settings` VALUES (21, '/monitor/online-user', '绯荤粺鐩戞帶 / 鍦ㄧ嚎鐢ㄦ埛', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (22, '/monitor/server', '绯荤粺鐩戞帶 / 鏈嶅姟鐩戞帶', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (23, '/monitor/redis', '绯荤粺鐩戞帶 / Redis鐩戞帶', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (24, '/user/index', '涓汉涓績 / 涓汉涓績', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (25, '/system/menu', '绯荤粺璁剧疆 / 鐩綍/椤甸潰绠＄悊', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (26, '/system/personalize', '绯荤粺璁剧疆 / 涓€у寲璁剧疆', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (27, '/system/storage', '绯荤粺璁剧疆 / 瀵硅薄瀛樺偍', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (28, '/system/verification', '绯荤粺璁剧疆 / 楠岃瘉璁剧疆', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (29, '/system/security', '绯荤粺璁剧疆 / 瀹夊叏璁剧疆', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (30, '/system/sensitive', '绯荤粺璁剧疆 / 鏁忔劅璇嶆嫤鎴?, 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (31, '/system/ai', '绯荤粺璁剧疆 / AI璁剧疆', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (32, '/user-settings/user', '鐢ㄦ埛璁剧疆 / 鐢ㄦ埛绠＄悊', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (33, '/user-settings/role', '鐢ㄦ埛璁剧疆 / 瑙掕壊绠＄悊', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (34, '/user-settings/log', '鐢ㄦ埛璁剧疆 / 鎿嶄綔鏃ュ織', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (35, '/announcement/table', '鍏憡绠＄悊 / 鍏憡鍒楄〃', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');
INSERT INTO `sensitive_page_settings` VALUES (36, '/announcement/cards', '鍏憡绠＄悊 / 鍏憡鍗＄墖', 0, '2026-01-09 22:20:58', '2026-01-09 22:32:28');

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
INSERT INTO `sys_menu_items` VALUES (910, NULL, 'DIR', '/user', 'user', 'LAYOUT', '/user/index', '涓汉涓績', 'User Center', 'user-safety-filled', 0, 1, NULL, 0, 1, NULL, NULL, 3, 6, '2025-12-30 23:58:19', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (911, 910, 'PAGE', 'index', 'UserIndex', '/user/index', NULL, '涓汉涓績', 'User Center', 'user', 0, 1, NULL, 0, 1, NULL, NULL, 0, 5, '2025-12-30 23:58:19', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1000, NULL, 'DIR', '/system', 'system', 'LAYOUT', NULL, '绯荤粺璁剧疆', 'System', 'setting', 0, 1, NULL, 0, 1, NULL, NULL, 6, 7, '2025-12-14 03:19:00', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1001, 1019, 'PAGE', 'user', 'SystemUser', '/system/user/index', NULL, '鐢ㄦ埛绠＄悊', 'Users', 'user', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 16, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1002, 1019, 'PAGE', 'role', 'SystemRole', '/system/role/index', NULL, '瑙掕壊绠＄悊', 'Roles', 'usergroup', 0, 1, NULL, 0, 1, 'admin', NULL, 1, 12, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1008, 1019, 'PAGE', 'org', 'SystemOrg', '/system/org/index', NULL, '鏈烘瀯绠＄悊', 'Organization', 'tree-round-dot-vertical', 0, 1, NULL, 0, 1, 'admin', NULL, 2, 0, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1003, 1000, 'PAGE', 'menu', 'SystemMenu', '/system/menu/index', NULL, '鐩綍/椤甸潰绠＄悊', 'Menu Manager', 'tree-round-dot-vertical', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 7, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1004, NULL, 'DIR', '/monitor', 'SystemMonitor', 'LAYOUT', NULL, '绯荤粺鐩戞帶', 'Monitor', 'chart-bar', 0, 1, NULL, 0, 1, 'admin', NULL, 5, 14, '2025-12-16 01:38:31', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1005, 1004, 'PAGE', 'online-user', 'SystemMonitorOnlineUser', '/system/monitor/online-user/index', NULL, '鍦ㄧ嚎鐢ㄦ埛', 'Online User', 'usergroup-add', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 5, '2025-12-16 01:38:31', '2026-01-18 21:22:37', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1006, 1004, 'PAGE', 'server', 'SystemMonitorServer', '/system/monitor/server/index', NULL, '鏈嶅姟鐩戞帶', 'Server Monitor', 'server', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 4, '2025-12-16 03:21:37', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1007, 1004, 'PAGE', 'redis', 'SystemMonitorRedis', '/system/monitor/redis/index', NULL, 'Redis鐩戞帶', 'Redis Monitor', 'chart-3d', 0, 1, NULL, 0, 1, 'admin', NULL, 1, 9, '2025-12-16 03:28:05', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1013, 1000, 'PAGE', 'personalize', 'SystemPersonalize', '/system/personalize/index', NULL, '涓€у寲璁剧疆', 'Personalize', 'setting-1', 0, 1, NULL, 0, 1, NULL, NULL, 2, 7, '2025-12-14 03:19:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1018, 1019, 'PAGE', 'log', 'SystemLog', '/system/log/index', NULL, '鎿嶄綔鏃ュ織', 'Operation Logs', 'file', 0, 1, NULL, 0, 1, 'admin', NULL, 2, 6, '2025-12-29 14:21:55', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1019, NULL, 'DIR', '/user-settings', 'users', 'LAYOUT', NULL, '鐢ㄦ埛璁剧疆', NULL, 'usergroup', 0, 1, NULL, 0, 1, NULL, NULL, 4, 7, '2025-12-29 20:45:20', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1020, 1004, 'PAGE', 'swagger', 'SwaggerUI', 'IFRAME', NULL, '鎺ュ彛鏂囨。', 'API Docs', 'api', 0, 1, '/api/swagger-ui/index.html', 0, 1, NULL, NULL, 2, 5, '2025-12-29 21:40:49', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1021, 1000, 'PAGE', 'storage', 'SystemStorage', '/system/storage/index', NULL, '瀵硅薄瀛樺偍', 'Object Storage', 'cloud-upload', 0, 1, NULL, 0, 1, NULL, NULL, 3, 5, '2025-12-31 05:05:00', '2026-01-18 21:21:03', 'update,query');
INSERT INTO `sys_menu_items` VALUES (1022, 1000, 'PAGE', 'verification', 'SystemVerification', '/system/verification/index', NULL, '楠岃瘉璁剧疆', 'Verification', 'check-circle', 0, 1, NULL, 0, 1, NULL, NULL, 4, 5, '2026-01-05 15:24:28', '2026-01-18 21:21:03', 'update,query');
INSERT INTO `sys_menu_items` VALUES (1023, 1000, 'PAGE', 'security', 'SystemSecurity', '/system/security/index', NULL, '瀹夊叏璁剧疆', 'Security', 'lock-on', 0, 1, NULL, 0, 1, NULL, NULL, 5, 5, '2026-01-06 02:27:43', '2026-01-18 21:21:03', 'update,query');
INSERT INTO `sys_menu_items` VALUES (1030, NULL, 'DIR', '/example', 'example', 'LAYOUT', '/example/goods', '绀轰緥椤甸潰', 'Examples', 'file', 0, 1, NULL, 0, 1, 'admin', NULL, 0, 12, '2026-01-05 15:49:43', '2026-01-19 00:09:21', NULL);
INSERT INTO `sys_menu_items` VALUES (1031, 1030, 'PAGE', 'goods', 'ExampleGoods', '/example/goods/index', NULL, '鍟嗗搧绠＄悊', 'Goods', NULL, 0, 1, NULL, 0, 1, 'admin', NULL, 0, 11, '2026-01-05 15:49:43', '2026-01-19 00:09:23', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1032, 1030, 'PAGE', 'order', 'ExampleOrder', '/example/order/index', NULL, '璁㈠崟绠＄悊', 'Orders', NULL, 0, 1, NULL, 0, 1, 'admin', NULL, 1, 11, '2026-01-05 15:49:43', '2026-01-19 00:09:23', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1034, 1000, 'PAGE', 'sensitive', 'SystemSensitive', '/system/sensitive/index', NULL, '鏁忔劅璇嶆嫤鎴?, 'Sensitive Words', 'filter', 0, 1, NULL, 0, 1, NULL, NULL, 6, 4, '2026-01-08 21:22:11', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1035, NULL, 'DIR', '/announcement', 'announcement', 'LAYOUT', '/announcement/table', '鍏憡绠＄悊', 'Announcements', 'notification', 0, 1, NULL, 0, 1, NULL, NULL, 2, 4, '2026-01-08 22:43:08', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1036, 1035, 'PAGE', 'table', 'AnnouncementTable', '/announcement/table/index', NULL, '鍏憡绠＄悊', 'Announcement Table', NULL, 0, 1, NULL, 0, 1, NULL, NULL, 1, 5, '2026-01-08 22:43:20', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1037, 1035, 'PAGE', 'cards', 'AnnouncementCards', '/announcement/cards/index', NULL, '閫氱煡涓績', 'Announcement Cards', NULL, 0, 1, NULL, 0, 1, NULL, NULL, 2, 5, '2026-01-08 22:43:25', '2026-01-18 21:21:03', 'query,create,update,delete');
INSERT INTO `sys_menu_items` VALUES (1038, 1000, 'PAGE', 'ai', 'SystemAi', '/system/ai/index', NULL, 'AI璁剧疆', 'AI Settings', 'chat', 0, 1, NULL, 0, 1, NULL, NULL, 7, 4, '2026-01-08 22:45:58', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1039, 1035, 'PAGE', 'send', 'MessageSend', '/message/send/index', NULL, '娑堟伅鍙戦€?, 'Message Send', NULL, 0, 1, NULL, 0, 1, NULL, NULL, 0, 4, '2026-01-08 22:43:25', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1040, NULL, 'DIR', '/console', 'console', 'LAYOUT', '/console/download', '鏂囦欢涓嬭浇', 'Console', 'download', 0, 1, NULL, 0, 1, NULL, NULL, 1, 4, '2026-01-14 02:30:00', '2026-01-18 21:21:03', NULL);
INSERT INTO `sys_menu_items` VALUES (1041, 1040, 'PAGE', 'download', 'ConsoleDownload', '/console/download/index', NULL, '鏂囦欢涓嬭浇', 'File Download', 'download', 0, 1, NULL, 0, 1, NULL, NULL, 0, 3, '2026-01-14 02:30:00', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1042, 1000, 'PAGE', 'dict', 'SystemDict', '/system/dict/index', NULL, '瀛楀吀绠＄悊', 'Dictionary', 'book', 0, 1, NULL, 0, 1, 'admin', NULL, 8, 4, '2026-01-18 21:21:03', '2026-01-18 21:21:03', 'create,update,delete,query');
INSERT INTO `sys_menu_items` VALUES (1043, 1000, 'PAGE', 'modules', 'SystemModule', '/system/modules/index', NULL, '????', 'Modules', 'grid-view', 0, 1, NULL, 0, 1, NULL, NULL, 6, 4, '2026-01-18 21:21:03', '2026-01-18 21:21:03', 'update,query');

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
INSERT INTO `ui_brand_settings` VALUES (1, 'Elexvx 鑴氭墜鏋剁郴缁?, '1.0', '', '', NULL, '');

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
INSERT INTO `ui_footer_settings` VALUES (1, 'Elexvx Inc', '鑻廔CP澶?025160017鍙?, '2025');

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
INSERT INTO `ui_legal_settings` VALUES (1, '<h1 style=\"text-align: start;\">鐢ㄦ埛鍗忚</h1><p style=\"text-align: start;\"><em>瀹忕繑鍟嗛亾锛堝崡浜級绉戞妧鍙戝睍鏈夐檺鍏徃锛圗lexvx Inc锛?/em>锛堜互涓嬬畝绉扳€滄垜浠€濓級渚濇嵁鏈崗璁负鐢ㄦ埛锛堜互涓嬬畝绉扳€滀綘鈥濓級鎻愪緵<em>Elexvx 鑴氭墜鏋剁郴缁?/em>鏈嶅姟銆傛湰鍗忚瀵逛綘鍜屾垜浠潎鍏锋湁娉曞緥绾︽潫鍔涖€?/p><h2 style=\"text-align: start;\">涓€銆佹湰鏈嶅姟鐨勫姛鑳?/h2><p style=\"text-align: start;\">浣犲彲浠ヤ娇鐢ㄦ湰鏈嶅姟浼佷笟搴曞眰鑴氭墜鏋剁郴缁熴€?/p><p style=\"text-align: start;\"><br></p><h2 style=\"text-align: start;\">浜屻€佽矗浠昏寖鍥村強闄愬埗</h2><p style=\"text-align: start;\">浣犱娇鐢ㄦ湰鏈嶅姟寰楀埌鐨勭粨鏋滀粎渚涘弬鑰冿紝瀹為檯鎯呭喌浠ュ畼鏂逛负鍑嗐€?/p><p style=\"text-align: start;\"><br></p><h2 style=\"text-align: start;\">涓夈€侀殣绉佷繚鎶?/h2><p style=\"text-align: start;\">鎴戜滑閲嶈瀵逛綘闅愮鐨勪繚鎶わ紝浣犵殑涓汉闅愮淇℃伅灏嗘牴鎹€婇殣绉佹斂绛栥€嬪彈鍒颁繚鎶や笌瑙勮寖锛岃鎯呰鍙傞槄銆婇殣绉佹斂绛栥€嬨€?/p><p style=\"text-align: start;\"><br></p><h2 style=\"text-align: start;\">鍥涖€佸叾浠栨潯娆?/h2><p style=\"text-align: start;\">4.1 鏈崗璁墍鏈夋潯娆剧殑鏍囬浠呬负闃呰鏂逛究锛屾湰韬苟鏃犲疄闄呮兜涔夛紝涓嶈兘浣滀负鏈崗璁兜涔夎В閲婄殑渚濇嵁銆?/p><p style=\"text-align: start;\">4.2 鏈崗璁潯娆炬棤璁哄洜浣曠鍘熷洜閮ㄥ垎鏃犳晥鎴栦笉鍙墽琛岋紝鍏朵綑鏉℃浠嶆湁鏁堬紝瀵瑰弻鏂瑰叿鏈夌害鏉熷姏銆?/p>', '<h1 style=\"text-align: start;\">闅愮鏀跨瓥</h1><p>鏇存柊鏃ユ湡锛?strong>2026/1/8</strong></p><p>鐢熸晥鏃ユ湡锛?strong>2026/1/8</strong></p><h1 style=\"text-align: start;\">瀵艰█</h1><p style=\"text-align: start;\"><em>Elexvx 鑴氭墜鏋剁郴缁?/em> 鏄竴娆剧敱 <em>瀹忕繑鍟嗛亾锛堝崡浜級绉戞妧鍙戝睍鏈夐檺鍏徃锛圗lexvx Inc锛?/em> 锛堜互涓嬬畝绉扳€滄垜浠€濓級鎻愪緵鐨勪骇鍝併€?鎮ㄥ湪浣跨敤鎴戜滑鐨勬湇鍔℃椂锛屾垜浠彲鑳戒細鏀堕泦鍜屼娇鐢ㄦ偍鐨勭浉鍏充俊鎭€傛垜浠笇鏈涢€氳繃鏈€婇殣绉佹斂绛栥€嬪悜鎮ㄨ鏄庯紝鍦ㄤ娇鐢ㄦ垜浠殑鏈嶅姟鏃讹紝鎴戜滑濡備綍鏀堕泦銆佷娇鐢ㄣ€佸偍瀛樺拰鍒嗕韩杩欎簺淇℃伅锛屼互鍙婃垜浠负鎮ㄦ彁渚涚殑璁块棶銆佹洿鏂般€佹帶鍒跺拰淇濇姢杩欎簺淇℃伅鐨勬柟寮忋€?鏈€婇殣绉佹斂绛栥€嬩笌鎮ㄦ墍浣跨敤鐨?<em>Elexvx 鑴氭墜鏋剁郴缁?/em> 鏈嶅姟鎭伅鐩稿叧锛屽笇鏈涙偍浠旂粏闃呰锛屽湪闇€瑕佹椂锛屾寜鐓ф湰銆婇殣绉佹斂绛栥€嬬殑鎸囧紩锛屼綔鍑烘偍璁や负閫傚綋鐨勯€夋嫨銆傛湰銆婇殣绉佹斂绛栥€嬩腑娑夊強鐨勭浉鍏虫妧鏈瘝姹囷紝鎴戜滑灏介噺浠ョ畝鏄庢壖瑕佺殑琛ㄨ堪锛屽苟鎻愪緵杩涗竴姝ヨ鏄庣殑閾炬帴锛屼互渚挎偍鐨勭悊瑙ｃ€?/p><p style=\"text-align: start;\"><strong>鎮ㄤ娇鐢ㄦ垨缁х画浣跨敤鎴戜滑鐨勬湇鍔★紝鍗虫剰鍛崇潃鍚屾剰鎴戜滑鎸夌収鏈€婇殣绉佹斂绛栥€嬫敹闆嗐€佷娇鐢ㄣ€佸偍瀛樺拰鍒嗕韩鎮ㄧ殑鐩稿叧淇℃伅銆?/strong></p><p style=\"text-align: start;\">濡傚鏈€婇殣绉佹斂绛栥€嬫垨鐩稿叧浜嬪疁鏈変换浣曢棶棰橈紝璇烽€氳繃 <strong>elexvx@elexvx.com</strong> 涓庢垜浠仈绯汇€?/p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">1. 鎴戜滑鏀堕泦鐨勪俊鎭?/h1><p style=\"text-align: start;\">鎴戜滑鎴栨垜浠殑绗笁鏂瑰悎浣滀紮浼存彁渚涙湇鍔℃椂锛屽彲鑳戒細鏀堕泦銆佸偍瀛樺拰浣跨敤涓嬪垪涓庢偍鏈夊叧鐨勪俊鎭€傚鏋滄偍涓嶆彁渚涚浉鍏充俊鎭紝鍙兘鏃犳硶娉ㄥ唽鎴愪负鎴戜滑鐨勭敤鎴锋垨鏃犳硶浜彈鎴戜滑鎻愪緵鐨勬煇浜涙湇鍔★紝鎴栬€呮棤娉曡揪鍒扮浉鍏虫湇鍔℃嫙杈惧埌鐨勬晥鏋溿€?/p><ul><li style=\"text-align: start;\"><strong>涓汉淇℃伅</strong>锛屾偍鍦ㄦ敞鍐岃处鎴锋垨浣跨敤鎴戜滑鐨勬湇鍔℃椂锛屽悜鎴戜滑鎻愪緵鐨勭浉鍏充釜浜轰俊鎭紝渚嬪鐢佃瘽鍙风爜銆佺數瀛愰偖浠剁瓑銆?/li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">2. 淇℃伅鐨勫瓨鍌?/h1><h2 style=\"text-align: start;\">2.1 淇℃伅瀛樺偍鐨勬柟寮忓拰鏈熼檺</h2><ul><li style=\"text-align: start;\">鎴戜滑浼氶€氳繃瀹夊叏鐨勬柟寮忓瓨鍌ㄦ偍鐨勪俊鎭紝鍖呮嫭鏈湴瀛樺偍锛堜緥濡傚埄鐢ˋPP杩涜鏁版嵁缂撳瓨锛夈€佹暟鎹簱鍜屾湇鍔″櫒鏃ュ織銆?/li><li style=\"text-align: start;\">涓€鑸儏鍐典笅锛屾垜浠彧浼氬湪涓哄疄鐜版湇鍔＄洰鐨勬墍蹇呴渶鐨勬椂闂村唴鎴栨硶寰嬫硶瑙勮瀹氱殑鏉′欢涓嬪瓨鍌ㄦ偍鐨勪釜浜轰俊鎭€?/li></ul><h2 style=\"text-align: start;\">2.2 淇℃伅瀛樺偍鐨勫湴鍩?/h2><ul><li style=\"text-align: start;\">鎴戜滑浼氭寜鐓ф硶寰嬫硶瑙勮瀹氾紝灏嗗鍐呮敹闆嗙殑鐢ㄦ埛涓汉淇℃伅瀛樺偍浜庝腑鍥藉鍐呫€?/li><li style=\"text-align: start;\">鐩墠鎴戜滑涓嶄細璺ㄥ浼犺緭鎴栧瓨鍌ㄦ偍鐨勪釜浜轰俊鎭€傚皢鏉ュ闇€璺ㄥ浼犺緭鎴栧瓨鍌ㄧ殑锛屾垜浠細鍚戞偍鍛婄煡淇℃伅鍑哄鐨勭洰鐨勩€佹帴鏀舵柟銆佸畨鍏ㄤ繚璇佹帾鏂藉拰瀹夊叏椋庨櫓锛屽苟寰佸緱鎮ㄧ殑鍚屾剰銆?/li></ul><h2 style=\"text-align: start;\">2.3 浜у搧鎴栨湇鍔″仠姝㈣繍钀ユ椂鐨勯€氱煡</h2><ul><li style=\"text-align: start;\">褰撴垜浠殑浜у搧鎴栨湇鍔″彂鐢熷仠姝㈣繍钀ョ殑鎯呭喌鏃讹紝鎴戜滑灏嗕互鎺ㄩ€侀€氱煡銆佸叕鍛婄瓑褰㈠紡閫氱煡鎮紝骞跺湪鍚堢悊鏈熼檺鍐呭垹闄ゆ偍鐨勪釜浜轰俊鎭垨杩涜鍖垮悕鍖栧鐞嗭紝娉曞緥娉曡鍙︽湁瑙勫畾鐨勯櫎澶栥€?/li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">3. 淇℃伅瀹夊叏</h1><p style=\"text-align: start;\">鎴戜滑浣跨敤鍚勭瀹夊叏鎶€鏈拰绋嬪簭锛屼互闃蹭俊鎭殑涓㈠け銆佷笉褰撲娇鐢ㄣ€佹湭缁忔巿鏉冮槄瑙堟垨鎶湶銆備緥濡傦紝鍦ㄦ煇浜涙湇鍔′腑锛屾垜浠皢鍒╃敤鍔犲瘑鎶€鏈紙渚嬪SSL锛夋潵淇濇姢鎮ㄦ彁渚涚殑涓汉淇℃伅銆備絾璇锋偍鐞嗚В锛岀敱浜庢妧鏈殑闄愬埗浠ュ強鍙兘瀛樺湪鐨勫悇绉嶆伓鎰忔墜娈碉紝鍦ㄤ簰鑱旂綉琛屼笟锛屽嵆渚跨灏芥墍鑳藉姞寮哄畨鍏ㄦ帾鏂斤紝涔熶笉鍙兘濮嬬粓淇濊瘉淇℃伅鐧惧垎涔嬬櫨鐨勫畨鍏ㄣ€傛偍闇€瑕佷簡瑙ｏ紝鎮ㄦ帴鍏ユ垜浠殑鏈嶅姟鎵€鐢ㄧ殑绯荤粺鍜岄€氳缃戠粶锛屾湁鍙兘鍥犳垜浠彲鎺ц寖鍥村鐨勫洜绱犺€屽嚭鐜伴棶棰樸€?/p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">4. 鎴戜滑濡備綍浣跨敤淇℃伅</h1><p style=\"text-align: start;\">鎴戜滑鍙兘灏嗗湪鍚戞偍鎻愪緵鏈嶅姟鐨勮繃绋嬩箣涓墍鏀堕泦鐨勪俊鎭敤浣滀笅鍒楃敤閫旓細</p><ul><li style=\"text-align: start;\">鍚戞偍鎻愪緵鏈嶅姟锛?/li><li style=\"text-align: start;\">鍦ㄦ垜浠彁渚涙湇鍔℃椂锛岀敤浜庤韩浠介獙璇併€佸鎴锋湇鍔°€佸畨鍏ㄩ槻鑼冦€佽瘓楠楃洃娴嬨€佸瓨妗ｅ拰澶囦唤鐢ㄩ€旓紝纭繚鎴戜滑鍚戞偍鎻愪緵鐨勪骇鍝佸拰鏈嶅姟鐨勫畨鍏ㄦ€э紱</li><li style=\"text-align: start;\">甯姪鎴戜滑璁捐鏂版湇鍔★紝鏀瑰杽鎴戜滑鐜版湁鏈嶅姟锛?/li><li style=\"text-align: start;\">浣挎垜浠洿鍔犱簡瑙ｆ偍濡備綍鎺ュ叆鍜屼娇鐢ㄦ垜浠殑鏈嶅姟锛屼粠鑰岄拡瀵规€у湴鍥炲簲鎮ㄧ殑涓€у寲闇€姹傦紝渚嬪璇█璁惧畾銆佷綅缃瀹氥€佷釜鎬у寲鐨勫府鍔╂湇鍔″拰鎸囩ず锛屾垨瀵规偍鍜屽叾浠栫敤鎴蜂綔鍑哄叾浠栨柟闈㈢殑鍥炲簲锛?/li><li style=\"text-align: start;\">鍚戞偍鎻愪緵涓庢偍鏇村姞鐩稿叧鐨勫箍鍛婁互鏇夸唬鏅亶鎶曟斁鐨勫箍鍛婏紱</li><li style=\"text-align: start;\">璇勪及鎴戜滑鏈嶅姟涓殑骞垮憡鍜屽叾浠栦績閿€鍙婃帹骞挎椿鍔ㄧ殑鏁堟灉锛屽苟鍔犱互鏀瑰杽锛?/li><li style=\"text-align: start;\">杞欢璁よ瘉鎴栫鐞嗚蒋浠跺崌绾э紱</li><li style=\"text-align: start;\">璁╂偍鍙備笌鏈夊叧鎴戜滑浜у搧鍜屾湇鍔＄殑璋冩煡銆?/li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">5. 淇℃伅鍏变韩</h1><p style=\"text-align: start;\">鐩墠锛屾垜浠笉浼氫富鍔ㄥ叡浜垨杞鎮ㄧ殑涓汉淇℃伅鑷崇涓夋柟锛屽瀛樺湪鍏朵粬鍏变韩鎴栬浆璁╂偍鐨勪釜浜轰俊鎭垨鎮ㄩ渶瑕佹垜浠皢鎮ㄧ殑涓汉淇℃伅鍏变韩鎴栬浆璁╄嚦绗笁鏂规儏褰㈡椂锛屾垜浠細鐩存帴鎴栫‘璁ょ涓夋柟寰佸緱鎮ㄥ涓婅堪琛屼负鐨勬槑绀哄悓鎰忋€?/p><p style=\"text-align: start;\">涓轰簡鎶曟斁骞垮憡锛岃瘎浼般€佷紭鍖栧箍鍛婃姇鏀炬晥鏋滅瓑鐩殑锛屾垜浠渶瑕佸悜骞垮憡涓诲強鍏朵唬鐞嗗晢绛夌涓夋柟鍚堜綔浼欎即鍏变韩鎮ㄧ殑閮ㄥ垎鏁版嵁锛岃姹傚叾涓ユ牸閬靛畧鎴戜滑鍏充簬鏁版嵁闅愮淇濇姢鐨勬帾鏂戒笌瑕佹眰锛屽寘鎷絾涓嶉檺浜庢牴鎹暟鎹繚鎶ゅ崗璁€佹壙璇轰功鍙婄浉鍏虫暟鎹鐞嗘斂绛栬繘琛屽鐞嗭紝閬垮厤璇嗗埆鍑轰釜浜鸿韩浠斤紝淇濋殰闅愮瀹夊叏銆?/p><p style=\"text-align: start;\">鎴戜滑涓嶄細鍚戝悎浣滀紮浼村垎浜彲鐢ㄤ簬璇嗗埆鎮ㄤ釜浜鸿韩浠界殑淇℃伅锛堜緥濡傛偍鐨勫鍚嶆垨鐢靛瓙閭欢鍦板潃锛夛紝闄ら潪鎮ㄦ槑纭巿鏉冦€?/p><p style=\"text-align: start;\">鎴戜滑涓嶄細瀵瑰鍏紑鎶湶鎵€鏀堕泦鐨勪釜浜轰俊鎭紝濡傚繀椤诲叕寮€鎶湶鏃讹紝鎴戜滑浼氬悜鎮ㄥ憡鐭ユ娆″叕寮€鎶湶鐨勭洰鐨勩€佹姭闇蹭俊鎭殑绫诲瀷鍙婂彲鑳芥秹鍙婄殑鏁忔劅淇℃伅锛屽苟寰佸緱鎮ㄧ殑鏄庣ず鍚屾剰銆?/p><p style=\"text-align: start;\">闅忕潃鎴戜滑涓氬姟鐨勬寔缁彂灞曪紝鎴戜滑鏈夊彲鑳借繘琛屽悎骞躲€佹敹璐€佽祫浜ц浆璁╃瓑浜ゆ槗锛屾垜浠皢鍛婄煡鎮ㄧ浉鍏虫儏褰紝鎸夌収娉曞緥娉曡鍙婁笉浣庝簬鏈€婇殣绉佹斂绛栥€嬫墍瑕佹眰鐨勬爣鍑嗙户缁繚鎶ゆ垨瑕佹眰鏂扮殑鎺у埗鑰呯户缁繚鎶ゆ偍鐨勪釜浜轰俊鎭€?/p><p style=\"text-align: start;\">鍙﹀锛屾牴鎹浉鍏虫硶寰嬫硶瑙勫強鍥藉鏍囧噯锛屼互涓嬫儏褰腑锛屾垜浠彲鑳戒細鍏变韩銆佽浆璁┿€佸叕寮€鎶湶涓汉淇℃伅鏃犻渶浜嬪厛寰佸緱鎮ㄧ殑鎺堟潈鍚屾剰锛?/p><ul><li style=\"text-align: start;\">涓庡浗瀹跺畨鍏ㄣ€佸浗闃插畨鍏ㄧ洿鎺ョ浉鍏崇殑锛?/li><li style=\"text-align: start;\">涓庡叕鍏卞畨鍏ㄣ€佸叕鍏卞崼鐢熴€侀噸澶у叕鍏卞埄鐩婄洿鎺ョ浉鍏崇殑锛?/li><li style=\"text-align: start;\">鐘姜渚︽煡銆佽捣璇夈€佸鍒ゅ拰鍒ゅ喅鎵ц绛夌洿鎺ョ浉鍏崇殑锛?/li><li style=\"text-align: start;\">鍑轰簬缁存姢涓汉淇℃伅涓讳綋鎴栧叾浠栦釜浜虹殑鐢熷懡銆佽储浜х瓑閲嶅ぇ鍚堟硶鏉冪泭浣嗗張寰堥毦寰楀埌鏈汉鍚屾剰鐨勶紱</li><li style=\"text-align: start;\">涓汉淇℃伅涓讳綋鑷鍚戠ぞ浼氬叕浼楀叕寮€涓汉淇℃伅鐨勶紱</li><li style=\"text-align: start;\">浠庡悎娉曞叕寮€鎶湶鐨勪俊鎭腑鏀堕泦涓汉淇℃伅鐨勶紝濡傚悎娉曠殑鏂伴椈鎶ラ亾銆佹斂搴滀俊鎭叕寮€绛夋笭閬撱€?/li></ul><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">6. 鎮ㄧ殑鏉冨埄</h1><p style=\"text-align: start;\">鍦ㄦ偍浣跨敤鎴戜滑鐨勬湇鍔℃湡闂达紝鎴戜滑鍙兘浼氳浜у搧鍏蜂綋鎯呭喌涓烘偍鎻愪緵鐩稿簲鐨勬搷浣滆缃紝浠ヤ究鎮ㄥ彲浠ユ煡璇€佸垹闄ゃ€佹洿姝ｆ垨鎾ゅ洖鎮ㄧ殑鐩稿叧涓汉淇℃伅锛屾偍鍙弬鑰冪浉搴旂殑鍏蜂綋鎸囧紩杩涜鎿嶄綔銆傛澶栵紝鎴戜滑杩樿缃簡鎶曡瘔涓炬姤娓犻亾锛屾偍鐨勬剰瑙佸皢浼氬緱鍒板強鏃剁殑澶勭悊銆傚鏋滄偍鏃犳硶閫氳繃涓婅堪閫斿緞鍜屾柟寮忚浣挎偍鐨勪釜浜轰俊鎭富浣撴潈鍒╋紝鎮ㄥ彲浠ラ€氳繃鏈€婇殣绉佹斂绛栥€嬩腑鎻愪緵鐨勮仈绯绘柟寮忔彁鍑烘偍鐨勮姹傦紝鎴戜滑浼氭寜鐓ф硶寰嬫硶瑙勭殑瑙勫畾浜堜互鍙嶉銆?/p><p style=\"text-align: start;\">褰撴偍鍐冲畾涓嶅啀浣跨敤鎴戜滑鐨勪骇鍝佹垨鏈嶅姟鏃讹紝鍙互鐢宠娉ㄩ攢璐︽埛銆傛敞閿€璐︽埛鍚庯紝闄ゆ硶寰嬫硶瑙勫彟鏈夎瀹氬锛屾垜浠皢鍒犻櫎鎴栧尶鍚嶅寲澶勭悊鎮ㄧ殑涓汉淇℃伅銆?/p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">7. 鍙樻洿</h1><p style=\"text-align: start;\">鎴戜滑鍙兘閫傛椂淇鏈€婇殣绉佹斂绛栥€嬬殑鏉℃銆傚綋鍙樻洿鍙戠敓鏃讹紝鎴戜滑浼氬湪鐗堟湰鏇存柊鏃跺悜鎮ㄦ彁绀烘柊鐨勩€婇殣绉佹斂绛栥€嬶紝骞跺悜鎮ㄨ鏄庣敓鏁堟棩鏈熴€傝鎮ㄤ粩缁嗛槄璇诲彉鏇村悗鐨勩€婇殣绉佹斂绛栥€嬪唴瀹癸紝<strong>鑻ユ偍缁х画浣跨敤鎴戜滑鐨勬湇鍔★紝鍗宠〃绀烘偍鍚屾剰鎴戜滑鎸夌収鏇存柊鍚庣殑銆婇殣绉佹斂绛栥€嬪鐞嗘偍鐨勪釜浜轰俊鎭€?/strong></p><p style=\"text-align: start;\"><br></p><h1 style=\"text-align: start;\">8. 鏈垚骞翠汉淇濇姢</h1><p style=\"text-align: start;\">鎴戜滑榧撳姳鐖舵瘝鎴栫洃鎶や汉鎸囧鏈弧鍗佸叓宀佺殑鏈垚骞翠汉浣跨敤鎴戜滑鐨勬湇鍔°€傛垜浠缓璁湭鎴愬勾浜洪紦鍔变粬浠殑鐖舵瘝鎴栫洃鎶や汉闃呰鏈€婇殣绉佹斂绛栥€嬶紝骞跺缓璁湭鎴愬勾浜哄湪鎻愪氦鐨勪釜浜轰俊鎭箣鍓嶅姹傜埗姣嶆垨鐩戞姢浜虹殑鍚屾剰鍜屾寚瀵笺€?/p>');

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
INSERT INTO `ui_system_settings` VALUES (1, 0, 1, 0, '绯荤粺缁存姢涓紝璇风◢鍚庤闂?);

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
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sort` int NOT NULL DEFAULT 0,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_dict_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` (`id`, `sort`, `name`, `code`, `status`, `remark`, `created_at`, `updated_at`) VALUES
  (2001, 1, '鎬у埆', 'gender', 1, '鎬у埆閫夐」', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2004, 4, '鍦板潃-鍖?, 'address_district', 1, '鍦板潃鍖哄幙', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2005, 5, '鍏憡-绫诲瀷', 'announcement_type', 1, '鍏憡绫诲瀷', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2006, 6, '鍏憡-浼樺厛绾?, 'announcement_priority', 1, '鍏憡浼樺厛绾?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2007, 7, '鍏憡-鐘舵€?, 'announcement_status', 1, '鍏憡鐘舵€?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2008, 8, '閫氱煡-绫诲瀷', 'notification_type', 1, '閫氱煡绫诲瀷', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2009, 9, '閫氱煡-浼樺厛绾?, 'notification_priority', 1, '閫氱煡浼樺厛绾?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2010, 10, '閫氱煡-鐘舵€?, 'notification_status', 1, '閫氱煡鐘舵€?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2011, 11, '娑堟伅-浼樺厛绾?, 'message_quality', 1, '娑堟伅浼樺厛绾?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2012, 12, '鏃ュ織-鎿嶄綔绫诲瀷', 'log_action', 1, '鎿嶄綔鏃ュ織绫诲瀷', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2013, 13, '鐢ㄦ埛-鐘舵€?, 'user_status', 1, '鐢ㄦ埛鐘舵€?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2014, 14, '鏈烘瀯-绫诲瀷', 'org_type', 1, '鏈烘瀯绫诲瀷', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2015, 15, '鏈烘瀯-鐘舵€?, 'org_status', 1, '鏈烘瀯鐘舵€?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2016, 16, '鑿滃崟-鑺傜偣绫诲瀷', 'menu_node_type', 1, '鑿滃崟鑺傜偣绫诲瀷', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2017, 17, '鑿滃崟-鏉冮檺鍔ㄤ綔', 'menu_action', 1, '鑿滃崟鏉冮檺鍔ㄤ綔', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2018, 18, '瀛樺偍-鎻愪緵鍟?, 'storage_provider', 1, '瀛樺偍鎻愪緵鍟?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2019, 19, '鐭俊-閫氶亾', 'sms_provider', 1, '鐭俊閫氶亾', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2020, 20, 'AI-鍘傚晢', 'ai_vendor', 1, 'AI鍘傚晢', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Table structure for sys_di_gender_de9feaea
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_gender_de9feaea`;
CREATE TABLE `sys_di_gender_de9feaea`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_gender_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_gender_de9feaea` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2101, 1, '鐢?, 'male', 'string', 1, 'success', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2102, 2, '濂?, 'female', 'string', 1, 'danger', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2103, 3, '鏈煡', 'unknown', 'string', 1, 'warning', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Table structure for sys_di_address_distri_a4210592
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_address_distri_a4210592`;
CREATE TABLE `sys_di_address_distri_a4210592`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_address_distri_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_address_distri_a4210592` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `province`, `city`, `district`, `created_at`, `updated_at`) VALUES
  (2401, 1, '鍗楀北鍖?, '鍗楀北鍖?, 'string', 1, NULL, '骞夸笢鐪?, '娣卞湷甯?, '鍗楀北鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2402, 2, '绂忕敯鍖?, '绂忕敯鍖?, 'string', 1, NULL, '骞夸笢鐪?, '娣卞湷甯?, '绂忕敯鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2403, 3, '瀹濆畨鍖?, '瀹濆畨鍖?, 'string', 1, NULL, '骞夸笢鐪?, '娣卞湷甯?, '瀹濆畨鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2404, 4, '榫欏矖鍖?, '榫欏矖鍖?, 'string', 1, NULL, '骞夸笢鐪?, '娣卞湷甯?, '榫欏矖鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2405, 5, '澶╂渤鍖?, '澶╂渤鍖?, 'string', 1, NULL, '骞夸笢鐪?, '骞垮窞甯?, '澶╂渤鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2406, 6, '瓒婄鍖?, '瓒婄鍖?, 'string', 1, NULL, '骞夸笢鐪?, '骞垮窞甯?, '瓒婄鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2407, 7, '榛勬郸鍖?, '榛勬郸鍖?, 'string', 1, NULL, '涓婃捣甯?, '涓婃捣甯?, '榛勬郸鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2408, 8, '寰愭眹鍖?, '寰愭眹鍖?, 'string', 1, NULL, '涓婃捣甯?, '涓婃捣甯?, '寰愭眹鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2409, 9, '鏈濋槼鍖?, '鏈濋槼鍖?, 'string', 1, NULL, '鍖椾含甯?, '鍖椾含甯?, '鏈濋槼鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2410, 10, '娴锋穩鍖?, '娴锋穩鍖?, 'string', 1, NULL, '鍖椾含甯?, '鍖椾含甯?, '娴锋穩鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2411, 11, '瑗挎箹鍖?, '瑗挎箹鍖?, 'string', 1, NULL, '娴欐睙鐪?, '鏉窞甯?, '瑗挎箹鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2412, 12, '婊ㄦ睙鍖?, '婊ㄦ睙鍖?, 'string', 1, NULL, '娴欐睙鐪?, '鏉窞甯?, '婊ㄦ睙鍖?, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

-- ----------------------------
-- Shared sys_di_* table template
-- ----------------------------
DROP TABLE IF EXISTS `sys_di_announcement_t_de30e733`;
CREATE TABLE `sys_di_announcement_t_de30e733`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_announcement_t_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_announcement_t_de30e733` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2501, 1, '鍏憡', 'announcement', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_announcement_p_ef193e74`;
CREATE TABLE `sys_di_announcement_p_ef193e74`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_announcement_p_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_announcement_p_ef193e74` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2511, 1, '楂?, 'high', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2512, 2, '涓?, 'middle', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2513, 3, '浣?, 'low', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_announcement_s_f91c61fe`;
CREATE TABLE `sys_di_announcement_s_f91c61fe`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_announcement_s_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_announcement_s_f91c61fe` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2521, 1, '鑽夌', 'draft', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2522, 2, '宸插彂甯?, 'published', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2523, 3, '宸叉挙鍥?, 'withdrawn', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_notification_t_301379fe`;
CREATE TABLE `sys_di_notification_t_301379fe`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_notification_t_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_notification_t_301379fe` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2601, 1, '閫氱煡', 'notification', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_notification_p_d682ffca`;
CREATE TABLE `sys_di_notification_p_d682ffca`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_notification_p_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_notification_p_d682ffca` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2611, 1, '楂?, 'high', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2612, 2, '涓?, 'middle', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2613, 3, '浣?, 'low', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_notification_s_32716fa4`;
CREATE TABLE `sys_di_notification_s_32716fa4`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_notification_s_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_notification_s_32716fa4` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2621, 1, '鑽夌', 'draft', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2622, 2, '宸插彂甯?, 'published', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2623, 3, '宸叉挙鍥?, 'withdrawn', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_message_qualit_6ab11001`;
CREATE TABLE `sys_di_message_qualit_6ab11001`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_message_qualit_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_message_qualit_6ab11001` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2701, 1, '楂?, 'high', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2702, 2, '涓?, 'middle', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2703, 3, '浣?, 'low', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_log_action_8705ceac`;
CREATE TABLE `sys_di_log_action_8705ceac`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_log_action_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_log_action_8705ceac` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2801, 1, '鐧诲綍', 'LOGIN', 'string', 1, 'primary', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2802, 2, '鏂板', 'CREATE', 'string', 1, 'success', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2803, 3, '淇敼', 'UPDATE', 'string', 1, 'warning', '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2804, 4, '鍒犻櫎', 'DELETE', 'string', 1, 'danger', '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_user_status_e4abeb4f`;
CREATE TABLE `sys_di_user_status_e4abeb4f`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_user_status_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_user_status_e4abeb4f` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (2901, 1, '姝ｅ父', '1', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (2902, 2, '鍋滅敤', '0', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_org_type_5a06609e`;
CREATE TABLE `sys_di_org_type_5a06609e`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_org_type_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_org_type_5a06609e` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3001, 1, '鍗曚綅', 'UNIT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3002, 2, '閮ㄩ棬', 'DEPARTMENT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3003, 3, '绉戝', 'SECTION', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3004, 4, '鐝粍', 'TEAM', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3005, 5, '鐢ㄦ埛', 'USER', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_org_status_adc56f2a`;
CREATE TABLE `sys_di_org_status_adc56f2a`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_org_status_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_org_status_adc56f2a` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3011, 1, '姝ｅ父', '1', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3012, 2, '鍋滅敤', '0', 'number', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_menu_node_type_83d1630e`;
CREATE TABLE `sys_di_menu_node_type_83d1630e`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_menu_node_type_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_menu_node_type_83d1630e` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3101, 1, '鐩綍', 'DIR', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3102, 2, '椤甸潰', 'PAGE', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3103, 3, '鎸夐挳', 'BTN', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_menu_action_6118cb39`;
CREATE TABLE `sys_di_menu_action_6118cb39`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_menu_action_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_menu_action_6118cb39` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3201, 1, '鏌ヨ', 'query', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3202, 2, '鏂板', 'create', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3203, 3, '淇敼', 'update', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3204, 4, '鍒犻櫎', 'delete', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_storage_provid_f9bb387a`;
CREATE TABLE `sys_di_storage_provid_f9bb387a`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_storage_provid_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_storage_provid_f9bb387a` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3301, 1, '鏈湴瀛樺偍', 'LOCAL', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3302, 2, '闃块噷浜?OSS', 'ALIYUN', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3303, 3, '鑵捐浜?COS', 'TENCENT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_sms_provider_d65c9878`;
CREATE TABLE `sys_di_sms_provider_d65c9878`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_sms_provider_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_sms_provider_d65c9878` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3401, 1, '闃块噷浜戠煭淇℃湇鍔?, 'aliyun', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3402, 2, '鑵捐浜戠煭淇℃湇鍔?, 'tencent', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

DROP TABLE IF EXISTS `sys_di_ai_vendor_f0a49eba`;
CREATE TABLE `sys_di_ai_vendor_f0a49eba`  (
  `id` bigint NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `value_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string',
  `status` tinyint NOT NULL DEFAULT 1,
  `tag_color` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_di_ai_vendor_value`(`value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `sys_di_ai_vendor_f0a49eba` (`id`, `sort`, `label`, `value`, `value_type`, `status`, `tag_color`, `created_at`, `updated_at`) VALUES
  (3501, 1, 'OpenAI / 鍏煎', 'OPENAI', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3502, 2, 'Azure OpenAI', 'AZURE_OPENAI', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3503, 3, 'DeepSeek', 'DEEPSEEK', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3504, 4, '鏈堜箣鏆楅潰 / Moonshot', 'MOONSHOT', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3505, 5, '閫氫箟鍗冮棶 (鍏煎妯″紡)', 'QWEN', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03'),
  (3506, 6, 'Ollama 鏈湴閮ㄧ讲', 'OLLAMA', 'string', 1, NULL, '2026-01-18 21:21:03', '2026-01-18 21:21:03');

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
  `province_id` int NULL DEFAULT NULL,
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `city_id` int NULL DEFAULT NULL,
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `district_id` int NULL DEFAULT NULL,
  `district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `town_id` int NULL DEFAULT NULL,
  `town` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `street_id` int NULL DEFAULT NULL,
  `street` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `zip_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `account`(`account` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 149 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '寮犱笁', '$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu', '+86 13800000000', '0000', 'admin@qq.com', 'T32F 001', '鑵捐闆嗗洟', 'CEO', '绯荤粺绠＄悊鍛?, '2018-01-01', '鑵捐/鑵捐鍏徃/绠＄悊灞?绯荤粺绠＄悊缁?, 'male', '寮犱笁鐨勬樀绉?, NULL, '', NULL, '', NULL, '', NULL, NULL, NULL, NULL, NULL, '', '寮犱笁鐨勭畝浠?, '/api/uploads/business/b27eeb829cd64ccaab15123e70678c24.jpg', '', '', 'e59c3cd1-3b52-47c7-bf88-fad5b2281827', 1, NOW(), NOW());
INSERT INTO `users` VALUES (3, 'bob', 'Bob', '$2a$10$BbVSQCIChdR.4gfwiG1OduJiKE/KpUTbhBXd.7Sr.uwi8eggDpYeu', '+86 13923456789', '8889', 'bob@tencent.com', 'T32F 012', '鑵捐闆嗗洟', 'Sarah Li', '鍚庣寮€鍙戝伐绋嬪笀', '2020-07-01', '鑵捐/鑵捐鍏徃/CSIG浜嬩笟缇?浜戣绠楅儴/鍚庣鏋舵瀯缁?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '700e0805-c571-4e6a-873b-00899a32ede2', 1, NOW(), NOW());

-- ----------------------------
-- Table structure for areas
-- ----------------------------
DROP TABLE IF EXISTS `areas`;
CREATE TABLE `areas`  (
  `id` int NOT NULL,
  `parent_id` int NOT NULL DEFAULT 0,
  `name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
  `zip_code` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `level` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent` (`parent_id`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for org_units
-- ----------------------------
DROP TABLE IF EXISTS `org_units`;
CREATE TABLE `org_units`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `short_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for org_unit_leaders
-- ----------------------------
DROP TABLE IF EXISTS `org_unit_leaders`;
CREATE TABLE `org_unit_leaders`  (
  `org_unit_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`org_unit_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_org_units
-- ----------------------------
DROP TABLE IF EXISTS `user_org_units`;
CREATE TABLE `user_org_units`  (
  `user_id` bigint NOT NULL,
  `org_unit_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `org_unit_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_departments
-- ----------------------------
DROP TABLE IF EXISTS `user_departments`;
CREATE TABLE `user_departments`  (
  `user_id` bigint NOT NULL,
  `department_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`, `department_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for module_registry
-- ----------------------------
DROP TABLE IF EXISTS `module_registry`;
CREATE TABLE `module_registry`  (
  `module_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `enabled` tinyint NULL DEFAULT 1,
  `install_state` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `installed_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`module_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- ----------------------------

-- ----------------------------
-- Records of module_registry
-- ----------------------------
INSERT INTO `module_registry` VALUES ('sms', '鐭俊楠岃瘉', '1.0.0', 1, 'PENDING', NULL);
INSERT INTO `module_registry` VALUES ('email', '閭楠岃瘉', '1.0.0', 1, 'PENDING', NULL);

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
INSERT INTO `watermark_settings` VALUES (1, 'text_single', '姘村嵃', '', 0.12, 30, 200, 200, 20, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table/Column comments
-- ----------------------------
ALTER TABLE `ai_provider_settings` COMMENT = '琛? ai_provider_settings';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `vendor` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'vendor';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'base_url';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `endpoint_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'endpoint_path';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'model';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `api_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api_key';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `api_version` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api_version';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `temperature` double NULL DEFAULT NULL COMMENT 'temperature';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `max_tokens` int NULL DEFAULT NULL COMMENT 'max_tokens';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `is_default` tinyint NULL DEFAULT NULL COMMENT 'is_default';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `enabled` tinyint NULL DEFAULT NULL COMMENT 'enabled';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `extra_headers` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'extra_headers';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'remark';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `last_test_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'last_test_status';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `last_test_message` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'last_test_message';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `last_tested_at` datetime NULL DEFAULT NULL COMMENT 'last_tested_at';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `ai_provider_settings` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `announcements` COMMENT = '琛? announcements';
ALTER TABLE `announcements` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `announcements` MODIFY COLUMN `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'title';
ALTER TABLE `announcements` MODIFY COLUMN `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'summary';
ALTER TABLE `announcements` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `announcements` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'type';
ALTER TABLE `announcements` MODIFY COLUMN `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'priority';
ALTER TABLE `announcements` MODIFY COLUMN `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'status';
ALTER TABLE `announcements` MODIFY COLUMN `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cover_url';
ALTER TABLE `announcements` MODIFY COLUMN `publish_at` datetime NULL DEFAULT NULL COMMENT 'publish_at';
ALTER TABLE `announcements` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `announcements` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `announcements` MODIFY COLUMN `created_by_id` bigint NULL DEFAULT NULL COMMENT 'created_by_id';
ALTER TABLE `announcements` MODIFY COLUMN `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'created_by_name';
ALTER TABLE `announcements` MODIFY COLUMN `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_url';
ALTER TABLE `announcements` MODIFY COLUMN `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_name';
ALTER TABLE `announcements` MODIFY COLUMN `is_broadcasted` tinyint NOT NULL DEFAULT 0 COMMENT 'is_broadcasted';
ALTER TABLE `file_resources` COMMENT = '琛? file_resources';
ALTER TABLE `file_resources` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `file_resources` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `file_resources` MODIFY COLUMN `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'file_name';
ALTER TABLE `file_resources` MODIFY COLUMN `suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'suffix';
ALTER TABLE `file_resources` MODIFY COLUMN `file_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'file_url';
ALTER TABLE `file_resources` MODIFY COLUMN `created_by_id` bigint NULL DEFAULT NULL COMMENT 'created_by_id';
ALTER TABLE `file_resources` MODIFY COLUMN `created_by_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'created_by_name';
ALTER TABLE `file_resources` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `file_resources` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `messages` COMMENT = '琛? messages';
ALTER TABLE `messages` MODIFY COLUMN `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'id';
ALTER TABLE `messages` MODIFY COLUMN `to_user_id` bigint NOT NULL COMMENT 'to_user_id';
ALTER TABLE `messages` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `messages` MODIFY COLUMN `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'type';
ALTER TABLE `messages` MODIFY COLUMN `status` tinyint NOT NULL COMMENT 'status';
ALTER TABLE `messages` MODIFY COLUMN `collected` tinyint NOT NULL COMMENT 'collected';
ALTER TABLE `messages` MODIFY COLUMN `quality` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'quality';
ALTER TABLE `messages` MODIFY COLUMN `created_at` datetime NOT NULL COMMENT 'created_at';
ALTER TABLE `notifications` COMMENT = '琛? notifications';
ALTER TABLE `notifications` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `notifications` MODIFY COLUMN `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'title';
ALTER TABLE `notifications` MODIFY COLUMN `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'summary';
ALTER TABLE `notifications` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'content';
ALTER TABLE `notifications` MODIFY COLUMN `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'priority';
ALTER TABLE `notifications` MODIFY COLUMN `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'status';
ALTER TABLE `notifications` MODIFY COLUMN `publish_at` datetime NULL DEFAULT NULL COMMENT 'publish_at';
ALTER TABLE `notifications` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `notifications` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `notifications` MODIFY COLUMN `created_by_id` bigint NULL DEFAULT NULL COMMENT 'created_by_id';
ALTER TABLE `notifications` MODIFY COLUMN `created_by_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'created_by_name';
ALTER TABLE `notifications` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'type';
ALTER TABLE `notifications` MODIFY COLUMN `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cover_url';
ALTER TABLE `notifications` MODIFY COLUMN `attachment_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_url';
ALTER TABLE `notifications` MODIFY COLUMN `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'attachment_name';
ALTER TABLE `operation_logs` COMMENT = '琛? operation_logs';
ALTER TABLE `operation_logs` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `operation_logs` MODIFY COLUMN `action` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'action';
ALTER TABLE `operation_logs` MODIFY COLUMN `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'module';
ALTER TABLE `operation_logs` MODIFY COLUMN `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'detail';
ALTER TABLE `operation_logs` MODIFY COLUMN `user_id` bigint NULL DEFAULT NULL COMMENT 'user_id';
ALTER TABLE `operation_logs` MODIFY COLUMN `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'account';
ALTER TABLE `operation_logs` MODIFY COLUMN `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ip_address';
ALTER TABLE `operation_logs` MODIFY COLUMN `device_model` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'device_model';
ALTER TABLE `operation_logs` MODIFY COLUMN `os` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'os';
ALTER TABLE `operation_logs` MODIFY COLUMN `browser` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'browser';
ALTER TABLE `operation_logs` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `operation_logs` MODIFY COLUMN `user_guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'user_guid';
ALTER TABLE `role_menus` COMMENT = '琛? role_menus';
ALTER TABLE `role_menus` MODIFY COLUMN `role_id` bigint NOT NULL COMMENT 'role_id';
ALTER TABLE `role_menus` MODIFY COLUMN `menu_id` bigint NOT NULL COMMENT 'menu_id';
ALTER TABLE `role_permissions` COMMENT = '琛? role_permissions';
ALTER TABLE `role_permissions` MODIFY COLUMN `role_id` bigint NOT NULL COMMENT 'role_id';
ALTER TABLE `role_permissions` MODIFY COLUMN `permission` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'permission';
ALTER TABLE `roles` COMMENT = '琛? roles';
ALTER TABLE `roles` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `roles` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `roles` MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'description';
ALTER TABLE `security_captcha_settings` COMMENT = '琛? security_captcha_settings';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `captcha_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'captcha_type';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `drag_captcha_width` int NULL DEFAULT NULL COMMENT 'drag_captcha_width';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `drag_captcha_height` int NULL DEFAULT NULL COMMENT 'drag_captcha_height';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `drag_captcha_threshold` int NULL DEFAULT NULL COMMENT 'drag_captcha_threshold';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `image_captcha_length` int NULL DEFAULT NULL COMMENT 'image_captcha_length';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `image_captcha_noise_lines` int NULL DEFAULT NULL COMMENT 'image_captcha_noise_lines';
ALTER TABLE `security_captcha_settings` MODIFY COLUMN `captcha_enabled` tinyint NULL DEFAULT NULL COMMENT 'captcha_enabled';
ALTER TABLE `security_password_policy` COMMENT = '琛? security_password_policy';
ALTER TABLE `security_password_policy` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_min_length` int NULL DEFAULT NULL COMMENT 'password_min_length';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_require_uppercase` tinyint NULL DEFAULT NULL COMMENT 'password_require_uppercase';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_require_lowercase` tinyint NULL DEFAULT NULL COMMENT 'password_require_lowercase';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_require_special` tinyint NULL DEFAULT NULL COMMENT 'password_require_special';
ALTER TABLE `security_password_policy` MODIFY COLUMN `password_allow_sequential` tinyint NULL DEFAULT NULL COMMENT 'password_allow_sequential';
ALTER TABLE `security_token_settings` COMMENT = '琛? security_token_settings';
ALTER TABLE `security_token_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `security_token_settings` MODIFY COLUMN `session_timeout_minutes` int NULL DEFAULT NULL COMMENT 'session_timeout_minutes';
ALTER TABLE `security_token_settings` MODIFY COLUMN `token_timeout_minutes` int NULL DEFAULT NULL COMMENT 'token_timeout_minutes';
ALTER TABLE `security_token_settings` MODIFY COLUMN `token_refresh_grace_minutes` int NULL DEFAULT NULL COMMENT 'token_refresh_grace_minutes';
ALTER TABLE `security_token_settings` MODIFY COLUMN `allow_url_token_param` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'allow_url_token_param';
ALTER TABLE `sensitive_page_settings` COMMENT = '琛? sensitive_page_settings';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'page_key';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `page_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'page_name';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `sensitive_page_settings` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `sensitive_settings` COMMENT = '琛? sensitive_settings';
ALTER TABLE `sensitive_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sensitive_settings` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
ALTER TABLE `sensitive_settings` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `sensitive_words` COMMENT = '琛? sensitive_words';
ALTER TABLE `sensitive_words` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sensitive_words` MODIFY COLUMN `word` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'word';
ALTER TABLE `sensitive_words` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
ALTER TABLE `sensitive_words` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `sensitive_words` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `storage_settings` COMMENT = '琛? storage_settings';
ALTER TABLE `storage_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `storage_settings` MODIFY COLUMN `provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'provider';
ALTER TABLE `storage_settings` MODIFY COLUMN `bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bucket';
ALTER TABLE `storage_settings` MODIFY COLUMN `region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'region';
ALTER TABLE `storage_settings` MODIFY COLUMN `endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'endpoint';
ALTER TABLE `storage_settings` MODIFY COLUMN `access_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'access_key';
ALTER TABLE `storage_settings` MODIFY COLUMN `secret_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'secret_key';
ALTER TABLE `storage_settings` MODIFY COLUMN `custom_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'custom_domain';
ALTER TABLE `storage_settings` MODIFY COLUMN `path_prefix` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'path_prefix';
ALTER TABLE `storage_settings` MODIFY COLUMN `public_read` tinyint NULL DEFAULT NULL COMMENT 'public_read';
ALTER TABLE `sys_menu_items` COMMENT = '琛? sys_menu_items';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `parent_id` bigint NULL DEFAULT NULL COMMENT 'parent_id';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `node_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'node_type';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'path';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `route_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'route_name';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'component';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'redirect';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `title_zh_cn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'title_zh_cn';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `title_en_us` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'title_en_us';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'icon';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `hidden` tinyint NOT NULL DEFAULT 0 COMMENT 'hidden';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `keep_alive` tinyint NOT NULL DEFAULT 1 COMMENT 'keep_alive';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `frame_src` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'frame_src';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `frame_blank` tinyint NOT NULL DEFAULT 0 COMMENT 'frame_blank';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `enabled` tinyint NOT NULL DEFAULT 1 COMMENT 'enabled';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `require_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'require_role';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `require_permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'require_permission';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `order_no` int NOT NULL DEFAULT 0 COMMENT 'order_no';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `version` int NOT NULL DEFAULT 0 COMMENT 'version';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `sys_menu_items` MODIFY COLUMN `actions` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'actions';
ALTER TABLE `ui_brand_settings` COMMENT = '琛? ui_brand_settings';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `website_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'website_name';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'app_version';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `logo_expanded_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'logo_expanded_url';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `logo_collapsed_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'logo_collapsed_url';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `favicon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'favicon_url';
ALTER TABLE `ui_brand_settings` MODIFY COLUMN `qr_code_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'qr_code_url';
ALTER TABLE `ui_footer_settings` COMMENT = '琛? ui_footer_settings';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `footer_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'footer_company';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `footer_icp` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'footer_icp';
ALTER TABLE `ui_footer_settings` MODIFY COLUMN `copyright_start_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'copyright_start_year';
ALTER TABLE `ui_layout_settings` COMMENT = '琛? ui_layout_settings';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `default_home` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'default_home';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `show_footer` tinyint NULL DEFAULT NULL COMMENT 'show_footer';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_sidebar_compact` tinyint NULL DEFAULT NULL COMMENT 'is_sidebar_compact';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `show_breadcrumb` tinyint NULL DEFAULT NULL COMMENT 'show_breadcrumb';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `menu_auto_collapsed` tinyint NULL DEFAULT NULL COMMENT 'menu_auto_collapsed';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `layout` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'layout';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `split_menu` tinyint NULL DEFAULT NULL COMMENT 'split_menu';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `side_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'side_mode';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_footer_aside` tinyint NULL DEFAULT NULL COMMENT 'is_footer_aside';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_sidebar_fixed` tinyint NULL DEFAULT NULL COMMENT 'is_sidebar_fixed';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_header_fixed` tinyint NULL DEFAULT NULL COMMENT 'is_header_fixed';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `is_use_tabs_router` tinyint NULL DEFAULT NULL COMMENT 'is_use_tabs_router';
ALTER TABLE `ui_layout_settings` MODIFY COLUMN `show_header` tinyint NULL DEFAULT NULL COMMENT 'show_header';
ALTER TABLE `ui_legal_settings` COMMENT = '琛? ui_legal_settings';
ALTER TABLE `ui_legal_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_legal_settings` MODIFY COLUMN `user_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'user_agreement';
ALTER TABLE `ui_legal_settings` MODIFY COLUMN `privacy_agreement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'privacy_agreement';
ALTER TABLE `ui_login_settings` COMMENT = '琛? ui_login_settings';
ALTER TABLE `ui_login_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_login_settings` MODIFY COLUMN `login_bg_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'login_bg_url';
ALTER TABLE `ui_login_settings` MODIFY COLUMN `allow_multi_device_login` tinyint NULL DEFAULT NULL COMMENT 'allow_multi_device_login';
ALTER TABLE `ui_system_settings` COMMENT = '琛? ui_system_settings';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `log_retention_days` int NULL DEFAULT NULL COMMENT 'log_retention_days';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `ai_assistant_enabled` tinyint NULL DEFAULT NULL COMMENT 'ai_assistant_enabled';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `maintenance_enabled` tinyint NULL DEFAULT NULL COMMENT 'maintenance_enabled';
ALTER TABLE `ui_system_settings` MODIFY COLUMN `maintenance_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'maintenance_message';
ALTER TABLE `ui_theme_settings` COMMENT = '琛? ui_theme_settings';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `auto_theme` tinyint NULL DEFAULT NULL COMMENT 'auto_theme';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `light_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'light_start_time';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `dark_start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'dark_start_time';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mode';
ALTER TABLE `ui_theme_settings` MODIFY COLUMN `brand_theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'brand_theme';
ALTER TABLE `user_parameters` COMMENT = '琛? user_parameters';
ALTER TABLE `user_parameters` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `user_parameters` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_parameters` MODIFY COLUMN `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'param_key';
ALTER TABLE `user_parameters` MODIFY COLUMN `param_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'param_value';
ALTER TABLE `user_parameters` MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'description';
ALTER TABLE `user_parameters` MODIFY COLUMN `created_at` datetime NULL DEFAULT NULL COMMENT 'created_at';
ALTER TABLE `user_parameters` MODIFY COLUMN `updated_at` datetime NULL DEFAULT NULL COMMENT 'updated_at';
ALTER TABLE `user_roles` COMMENT = '琛? user_roles';
ALTER TABLE `user_roles` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_roles` MODIFY COLUMN `role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'role';
ALTER TABLE `users` COMMENT = '琛? users';
ALTER TABLE `users` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `users` MODIFY COLUMN `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'account';
ALTER TABLE `users` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `users` MODIFY COLUMN `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'password_hash';
ALTER TABLE `users` MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mobile';
ALTER TABLE `users` MODIFY COLUMN `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'phone';
ALTER TABLE `users` MODIFY COLUMN `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'email';
ALTER TABLE `users` MODIFY COLUMN `seat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'seat';
ALTER TABLE `users` MODIFY COLUMN `entity` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'entity';
ALTER TABLE `users` MODIFY COLUMN `leader` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'leader';
ALTER TABLE `users` MODIFY COLUMN `position` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'position';
ALTER TABLE `users` MODIFY COLUMN `join_day` date NULL DEFAULT NULL COMMENT 'join_day';
ALTER TABLE `users` MODIFY COLUMN `team` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'team';
ALTER TABLE `users` MODIFY COLUMN `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'gender';
ALTER TABLE `users` MODIFY COLUMN `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'nickname';
ALTER TABLE `users` MODIFY COLUMN `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'province';
ALTER TABLE `users` MODIFY COLUMN `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'city';
ALTER TABLE `users` MODIFY COLUMN `district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'district';
ALTER TABLE `users` MODIFY COLUMN `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'address';
ALTER TABLE `users` MODIFY COLUMN `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'introduction';
ALTER TABLE `users` MODIFY COLUMN `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'avatar';
ALTER TABLE `users` MODIFY COLUMN `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'tags';
ALTER TABLE `users` MODIFY COLUMN `id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'id_card';
ALTER TABLE `users` MODIFY COLUMN `guid` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'guid';
ALTER TABLE `users` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT 'status';
ALTER TABLE `users` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `users` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `org_units` COMMENT = '琛? org_units';
ALTER TABLE `org_units` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `org_units` MODIFY COLUMN `parent_id` bigint NULL DEFAULT NULL COMMENT 'parent_id';
ALTER TABLE `org_units` MODIFY COLUMN `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `org_units` MODIFY COLUMN `short_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'short_name';
ALTER TABLE `org_units` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'type';
ALTER TABLE `org_units` MODIFY COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT 'sort_order';
ALTER TABLE `org_units` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT 'status';
ALTER TABLE `org_units` MODIFY COLUMN `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'phone';
ALTER TABLE `org_units` MODIFY COLUMN `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'email';
ALTER TABLE `org_units` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `org_units` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `org_unit_leaders` COMMENT = '琛? org_unit_leaders';
ALTER TABLE `org_unit_leaders` MODIFY COLUMN `org_unit_id` bigint NOT NULL COMMENT 'org_unit_id';
ALTER TABLE `org_unit_leaders` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_org_units` COMMENT = '琛? user_org_units';
ALTER TABLE `user_org_units` MODIFY COLUMN `user_id` bigint NOT NULL COMMENT 'user_id';
ALTER TABLE `user_org_units` MODIFY COLUMN `org_unit_id` bigint NOT NULL COMMENT 'org_unit_id';
ALTER TABLE `module_registry` COMMENT = '琛? module_registry';
ALTER TABLE `module_registry` MODIFY COLUMN `module_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'module_key';
ALTER TABLE `module_registry` MODIFY COLUMN `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'name';
ALTER TABLE `module_registry` MODIFY COLUMN `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'version';
ALTER TABLE `module_registry` MODIFY COLUMN `enabled` tinyint NULL DEFAULT 1 COMMENT 'enabled';
ALTER TABLE `module_registry` MODIFY COLUMN `install_state` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'install_state';
ALTER TABLE `module_registry` MODIFY COLUMN `installed_at` datetime NULL DEFAULT NULL COMMENT 'installed_at';
ALTER TABLE `sys_dict` COMMENT = '琛? sys_dict';
ALTER TABLE `sys_dict` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `sys_dict` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'name';
ALTER TABLE `sys_dict` MODIFY COLUMN `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'code';
ALTER TABLE `sys_dict` MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT 'status';
ALTER TABLE `sys_dict` MODIFY COLUMN `sort` int NOT NULL DEFAULT 0 COMMENT 'sort';
ALTER TABLE `sys_dict` MODIFY COLUMN `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'remark';
ALTER TABLE `sys_dict` MODIFY COLUMN `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created_at';
ALTER TABLE `sys_dict` MODIFY COLUMN `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated_at';
ALTER TABLE `watermark_settings` COMMENT = '琛? watermark_settings';
ALTER TABLE `watermark_settings` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `watermark_settings` MODIFY COLUMN `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'type';
ALTER TABLE `watermark_settings` MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'content';
ALTER TABLE `watermark_settings` MODIFY COLUMN `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'image_url';
ALTER TABLE `watermark_settings` MODIFY COLUMN `opacity` double NOT NULL COMMENT 'opacity';
ALTER TABLE `watermark_settings` MODIFY COLUMN `size` int NOT NULL COMMENT 'size';
ALTER TABLE `watermark_settings` MODIFY COLUMN `gap_x` int NOT NULL COMMENT 'gap_x';
ALTER TABLE `watermark_settings` MODIFY COLUMN `gap_y` int NOT NULL COMMENT 'gap_y';
ALTER TABLE `watermark_settings` MODIFY COLUMN `rotate` int NOT NULL COMMENT 'rotate';
ALTER TABLE `watermark_settings` MODIFY COLUMN `enabled` tinyint NOT NULL COMMENT 'enabled';
