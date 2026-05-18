package com.darkecage.dcaicodegenerator.model.dto.app;

import com.darkecage.dcaicodegenerator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 应用分页查询请求
 *
 * @author kaiqi.hu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long id;

    /**
     * 应用名称（模糊搜索）
     */
    private String appName;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
