package com.darkecage.dcaicodegenerator.controller;

import com.darkecage.dcaicodegenerator.annotation.AuthCheck;
import com.darkecage.dcaicodegenerator.common.BaseResponse;
import com.darkecage.dcaicodegenerator.common.DeleteRequest;
import com.darkecage.dcaicodegenerator.common.ResultUtils;
import com.darkecage.dcaicodegenerator.common.constant.UserConstant;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.exception.ThrowUtils;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryAddRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryScrollRequest;
import com.darkecage.dcaicodegenerator.model.entity.ChatHistory;
import com.darkecage.dcaicodegenerator.model.enums.UserRoleEnum;
import com.darkecage.dcaicodegenerator.model.vo.ChatHistoryVO;
import com.darkecage.dcaicodegenerator.model.vo.LoginUserVO;
import com.darkecage.dcaicodegenerator.model.vo.ScrollResult;
import com.darkecage.dcaicodegenerator.service.ChatHistoryService;
import com.darkecage.dcaicodegenerator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 对话历史 控制层。
 *
 * @author kaiqi.hu
 */
@RestController
@RequestMapping("/chatHistory")
@Slf4j
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private UserService userService;

    // region 用户端接口

    /**
     * 保存一条对话消息（用户消息或 AI 消息）
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addChatHistory(@RequestBody ChatHistoryAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUser = userService.getLoginUser(request);
        chatHistoryService.saveChatHistory(addRequest, loginUser.getUserId());
        return ResultUtils.success(true);
    }

    /**
     * 游标分页查询对话历史（仅应用创建者和管理员可见）
     */
    @PostMapping("/scroll")
    public BaseResponse<ScrollResult<ChatHistoryVO>> scrollChatHistory(@RequestBody ChatHistoryScrollRequest scrollRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(scrollRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUser = userService.getLoginUser(request);
        boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
        ScrollResult<ChatHistoryVO> result = chatHistoryService.listChatHistoryByScroll(scrollRequest, loginUser.getUserId(), isAdmin);
        return ResultUtils.success(result);
    }

    // endregion

    // region 管理员接口

    /**
     * 管理员分页查询所有应用的对话历史
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> adminListChatHistoryByPage(@RequestBody ChatHistoryQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<ChatHistory> page = chatHistoryService.listChatHistoryPage(queryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 管理员删除单条对话历史
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteChatHistory(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = chatHistoryService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
