package com.darkecage.dcaicodegenerator.model.dto.chatHistory;

import lombok.Data;

import java.io.Serializable;

/**
 * 对话历史游标分页查询请求
 *
 * @author kaiqi.hu
 */
@Data
public class ChatHistoryScrollRequest implements Serializable {

    /**
     * 应用 id（必填）
     */
    private Long appId;

    /**
     * 每页条数（必填，1~50）
     */
    private int pageSize;

    /**
     * 上一页最后一条记录的自增 id（游标）
     * 首次查询不传，后续翻页传上一次响应中的 nextCursor
     */
    private Long lastId;

    private static final long serialVersionUID = 1L;
}
