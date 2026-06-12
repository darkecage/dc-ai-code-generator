package com.darkecage.dcaicodegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.exception.ThrowUtils;
import com.darkecage.dcaicodegenerator.mapper.ChatHistoryMapper;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryAddRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryScrollRequest;
import com.darkecage.dcaicodegenerator.model.entity.App;
import com.darkecage.dcaicodegenerator.model.entity.ChatHistory;
import com.darkecage.dcaicodegenerator.model.enums.ChatHistoryMessageTypeEnum;
import com.darkecage.dcaicodegenerator.model.vo.ChatHistoryVO;
import com.darkecage.dcaicodegenerator.model.vo.ScrollResult;
import com.darkecage.dcaicodegenerator.service.AppService;
import com.darkecage.dcaicodegenerator.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author kaiqi.hu
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "create_time", "app_id", "user_id");

    /**
     * 使用 @Lazy 避免与 AppService 产生循环依赖
     */
    @Lazy
    @Autowired
    private AppService appService;

    @Override
    public Long saveChatHistory(ChatHistoryAddRequest addRequest, Long userId) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = addRequest.getAppId();
        String message = addRequest.getMessage();
        String messageType = addRequest.getMessageType();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(ChatHistoryMessageTypeEnum.getEnumByValue(messageType) == null, ErrorCode.PARAMS_ERROR, "消息类型不合法");
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!app.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权向该应用写入消息");
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(appId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType(messageType);
        chatHistory.setParentId(addRequest.getParentId());
        chatHistory.setUserId(userId);
        boolean saved = this.save(chatHistory);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "保存对话历史失败");
        return chatHistory.getId();
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        Long id = queryRequest.getId();
        String message = queryRequest.getMessage();
        Long appId = queryRequest.getAppId();
        String messageType = queryRequest.getMessageType();
        Long userId = queryRequest.getUserId();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, id != null)
                .like("message", message, StrUtil.isNotBlank(message))
                .eq("app_id", appId, appId != null)
                .eq("message_type", messageType, StrUtil.isNotBlank(messageType))
                .eq("user_id", userId, userId != null);
        if (StrUtil.isNotBlank(sortField)) {
            ThrowUtils.throwIf(!ALLOWED_SORT_FIELDS.contains(sortField), ErrorCode.PARAMS_ERROR, "非法排序字段");
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        if (chatHistory == null) {
            return null;
        }
        ChatHistoryVO vo = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, vo);
        return vo;
    }

    @Override
    public ScrollResult<ChatHistoryVO> listChatHistoryByScroll(ChatHistoryScrollRequest scrollRequest, Long loginUserId, boolean isAdmin) {
        ThrowUtils.throwIf(scrollRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = scrollRequest.getAppId();
        int pageSize = scrollRequest.getPageSize();
        Long lastId = scrollRequest.getLastId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "每页条数需在 1~50 之间");
        // 非管理员需校验是否为应用创建者
        if (!isAdmin) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
            if (!app.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
            }
        }
        // 多查一条用于判断是否还有更多数据，基于自增 id 游标倒序翻页
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId)
                .lt("id", lastId, lastId != null)
                .orderBy("id", false)
                .limit(pageSize + 1);
        List<ChatHistory> records = this.list(queryWrapper);
        boolean hasMore = records.size() > pageSize;
        if (hasMore) {
            records = records.subList(0, pageSize);
        }
        List<ChatHistoryVO> voList = records.stream()
                .map(this::getChatHistoryVO)
                .collect(Collectors.toList());
        // 游标为最后一条记录的自增 id
        Long nextCursor = (hasMore && !voList.isEmpty())
                ? voList.get(voList.size() - 1).getId()
                : null;
        return ScrollResult.of(voList, nextCursor, hasMore);
    }

    @Override
    public void deleteByAppId(Long appId) {
        if (appId == null || appId <= 0) {
            return;
        }
        this.remove(QueryWrapper.create().eq("app_id", appId));
    }

    @Override
    public Page<ChatHistory> listChatHistoryPage(ChatHistoryQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = queryRequest.getPageNum();
        long pageSize = queryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0, ErrorCode.PARAMS_ERROR, "页码必须为正数");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "每页条数需在 1~50 之间");
        return this.page(Page.of(pageNum, pageSize), getQueryWrapper(queryRequest));
    }
}
