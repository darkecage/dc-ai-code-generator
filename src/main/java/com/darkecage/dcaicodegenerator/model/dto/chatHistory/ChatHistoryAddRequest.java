package com.darkecage.dcaicodegenerator.model.dto.chatHistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存对话历史请求
 *
 * @author kaiqi.hu
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 父消息 id（用于上下文关联，可选）
     */
    private Long parentId;

    private static final long serialVersionUID = 1L;
}
