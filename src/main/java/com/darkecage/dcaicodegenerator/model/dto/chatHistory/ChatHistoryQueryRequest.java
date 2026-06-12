package com.darkecage.dcaicodegenerator.model.dto.chatHistory;

import com.darkecage.dcaicodegenerator.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 对话历史分页查询请求（管理员使用）
 *
 * @author kaiqi.hu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 消息（模糊查询）
     */
    private String message;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
