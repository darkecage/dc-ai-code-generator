package com.darkecage.dcaicodegenerator.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户编辑应用请求（仅允许修改应用名称）
 *
 * @author kaiqi.hu
 */
@Data
public class AppUserUpdateRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
