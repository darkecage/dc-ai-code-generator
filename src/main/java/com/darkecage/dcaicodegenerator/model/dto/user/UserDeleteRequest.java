package com.darkecage.dcaicodegenerator.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户删除请求
 *
 * @author kaiqi.hu
 */
@Data
public class UserDeleteRequest implements Serializable {

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
