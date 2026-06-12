package com.darkecage.dcaicodegenerator.service;

import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryAddRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryScrollRequest;
import com.darkecage.dcaicodegenerator.model.entity.ChatHistory;
import com.darkecage.dcaicodegenerator.model.vo.ChatHistoryVO;
import com.darkecage.dcaicodegenerator.model.vo.ScrollResult;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

/**
 * 对话历史 服务层。
 *
 * @author kaiqi.hu
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存一条对话消息
     *
     * @param addRequest 保存请求
     * @param userId     当前用户 id
     * @return 保存后的消息 id
     */
    Long saveChatHistory(ChatHistoryAddRequest addRequest, Long userId);

    /**
     * 获取查询条件（管理员分页查询使用）
     *
     * @param queryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest queryRequest);

    /**
     * 将实体转换为视图对象
     *
     * @param chatHistory 对话历史实体
     * @return 视图对象
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 游标分页查询对话历史（仅应用创建者和管理员可见）
     *
     * @param scrollRequest 游标分页请求
     * @param loginUserId   当前登录用户 id
     * @param isAdmin       是否为管理员
     * @return 游标分页结果
     */
    ScrollResult<ChatHistoryVO> listChatHistoryByScroll(ChatHistoryScrollRequest scrollRequest, Long loginUserId, boolean isAdmin);

    /**
     * 按应用 id 删除所有对话历史（逻辑删除，供删除应用时级联调用）
     *
     * @param appId 应用 id
     */
    void deleteByAppId(Long appId);

    /**
     * 管理员分页查询对话历史
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    Page<ChatHistory> listChatHistoryPage(ChatHistoryQueryRequest queryRequest);
}
