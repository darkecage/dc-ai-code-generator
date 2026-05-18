package com.darkecage.dcaicodegenerator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用业务配置
 *
 * @author kaiqi.hu
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    /**
     * 应用名称为空时，从初始提示词截取的最大字数
     */
    private int appNameDefaultLength = 12;

    /**
     * 应用名称允许的最大字数
     */
    private int appNameMaxLength = 20;

    /**
     * 用户端分页接口每页最大条数
     */
    private int userPageSizeMax = 20;

    /**
     * 精选应用的 priority 阈值（>= 该值视为精选）
     */
    private int featuredPriorityThreshold = 99;
}
