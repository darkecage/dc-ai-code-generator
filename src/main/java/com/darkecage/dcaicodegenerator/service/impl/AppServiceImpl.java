package com.darkecage.dcaicodegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.darkecage.dcaicodegenerator.ai.core.AiCodeGeneratorFacade;
import com.darkecage.dcaicodegenerator.config.AppConfig;
import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.exception.ThrowUtils;
import com.darkecage.dcaicodegenerator.mapper.AppMapper;
import com.darkecage.dcaicodegenerator.model.dto.app.AppQueryRequest;
import com.darkecage.dcaicodegenerator.model.dto.chatHistory.ChatHistoryAddRequest;
import com.darkecage.dcaicodegenerator.model.entity.App;
import com.darkecage.dcaicodegenerator.model.entity.User;
import com.darkecage.dcaicodegenerator.model.enums.ChatHistoryMessageTypeEnum;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;
import com.darkecage.dcaicodegenerator.model.vo.AppVO;
import com.darkecage.dcaicodegenerator.model.vo.LoginUserVO;
import com.darkecage.dcaicodegenerator.model.vo.UserVO;
import com.darkecage.dcaicodegenerator.service.AppService;
import com.darkecage.dcaicodegenerator.service.ChatHistoryService;
import com.darkecage.dcaicodegenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author kaiqi.hu
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        String codeGenType = app.getCodeGenType();
        // 创建时必填校验
        if (add) {
            if (StrUtil.isBlank(initPrompt)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "初始提示词不能为空");
            }
            if (StrUtil.isBlank(appName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            }
        }
        // 通用校验
        if (StrUtil.isNotBlank(appName) && appName.length() > appConfig.getAppNameMaxLength()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "应用名称不能超过 " + appConfig.getAppNameMaxLength() + " 个字");
        }
        if (StrUtil.isNotBlank(codeGenType) && CodeGenTypeEnum.getEnumByValue(codeGenType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不合法");
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, id != null)
                .like("app_name", appName, StrUtil.isNotBlank(appName))
                .eq("code_gen_type", codeGenType, StrUtil.isNotBlank(codeGenType))
                .eq("deploy_key", deployKey, StrUtil.isNotBlank(deployKey))
                .eq("priority", priority, priority != null)
                .eq("user_id", userId, userId != null);
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 填充创建者信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getOne(QueryWrapper.create().eq("user_id", userId));
            appVO.setUser(userService.getUserVO(user));
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量查询创建者，避免 N+1
        List<Long> userIds = appList.stream()
                .map(App::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, UserVO> userVOMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            userService.list(QueryWrapper.create().in("user_id", userIds))
                    .forEach(user -> userVOMap.put(user.getUserId(), userService.getUserVO(user)));
        }
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtil.copyProperties(app, appVO);
            appVO.setUser(userVOMap.get(app.getUserId()));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, LoginUserVO loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 保存用户消息，拿到 id 用于关联 AI 消息
        ChatHistoryAddRequest userMsgReq = new ChatHistoryAddRequest();
        userMsgReq.setAppId(appId);
        userMsgReq.setMessage(message);
        userMsgReq.setMessageType(ChatHistoryMessageTypeEnum.USER.getValue());
        Long userMsgId = chatHistoryService.saveChatHistory(userMsgReq, loginUser.getUserId());
        // 6. 调用 AI 生成流，收集完整回复后保存 AI 消息
        StringBuilder aiReply = new StringBuilder();
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId)
                .doOnNext(aiReply::append)
                .doOnComplete(() ->
                        saveAiChatHistory(appId, userMsgId, loginUser.getUserId(), aiReply.toString())
                )
                .doOnError(e -> {
                    log.error("AI 代码生成流发生错误, appId={}, userMsgId={}", appId, userMsgId, e);
                    String errorMsg = !aiReply.isEmpty()
                            ? aiReply + "\n\n[生成失败：" + e.getMessage() + "]"
                            : "[生成失败：" + e.getMessage() + "]";
                    saveAiChatHistory(appId, userMsgId, loginUser.getUserId(), errorMsg);
                })
                .doOnCancel(() -> {
                    log.warn("AI 代码生成流被取消, appId={}, userMsgId={}", appId, userMsgId);
                    String cancelMsg = !aiReply.isEmpty() ? aiReply + "\n\n[用户取消]" : "[用户取消]";
                    saveAiChatHistory(appId, userMsgId, loginUser.getUserId(), cancelMsg);
                });
    }

    private void saveAiChatHistory(Long appId, Long parentId, Long userId, String message) {
        try {
            ChatHistoryAddRequest aiMsgReq = new ChatHistoryAddRequest();
            aiMsgReq.setAppId(appId);
            aiMsgReq.setMessage(message);
            aiMsgReq.setMessageType(ChatHistoryMessageTypeEnum.AI.getValue());
            aiMsgReq.setParentId(parentId);
            chatHistoryService.saveChatHistory(aiMsgReq, userId);
        } catch (Exception e) {
            log.error("保存 AI 对话历史失败, appId={}", appId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAppById(Long appId) {
        boolean result = this.removeById(appId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.warn("删除应用[{}]的聊天历史失败，不影响应用删除流程", appId, e);
        }
    }

    @Override
    public String deployApp(Long appId, Long loginUserId) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUserId == null || loginUserId <= 0, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = appConfig.getCodeOutputRootDir() + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = appConfig.getCodeDeployRootDir() + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", appConfig.getCodeDeployHost(), deployKey);
    }


}
