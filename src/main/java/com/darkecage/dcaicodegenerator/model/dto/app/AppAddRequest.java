package com.darkecage.dcaicodegenerator.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建应用请求
 *
 * @author kaiqi.hu
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt（必填）
     */
    private String initPrompt;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    private static final long serialVersionUID = 1L;
}
