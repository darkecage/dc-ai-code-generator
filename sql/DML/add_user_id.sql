-- 用户表新增 user_id 字段（雪花算法生成的唯一标识）
ALTER TABLE user
    ADD COLUMN user_id bigint NOT NULL COMMENT '用户id（雪花算法生成）' AFTER id,
    ADD UNIQUE KEY uk_user_id (user_id);