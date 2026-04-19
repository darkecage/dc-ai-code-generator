package com.darkecage.dcaicodegenerator.config;

import cn.hutool.core.util.IdUtil;
import com.darkecage.dcaicodegenerator.model.entity.User;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 全局配置
 *
 * @author kaiqi.hu
 */
@Configuration
public class MyBatisFlexConfig implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        // 注册 User 实体的插入监听器，在插入前自动生成 userId（雪花算法）
        globalConfig.registerInsertListener(entity -> {
            if (entity instanceof User user && user.getUserId() == null) {
                user.setUserId(IdUtil.getSnowflakeNextId());
            }
        }, User.class);
    }
}
