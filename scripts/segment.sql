DROP TABLE IF EXISTS `c_id_gen`;
CREATE TABLE `c_id_gen`  (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '`',
    `cur_id` bigint(20) NOT NULL COMMENT '当前值',
    `step` int(11) NULL DEFAULT NULL COMMENT '步长',
    `version` bigint(20) NULL DEFAULT NULL,
    `create_at` datetime(0) NULL DEFAULT NULL,
    `update_at` datetime(0) NULL DEFAULT NULL,
    `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unx_biz_type`(`biz_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;