package com.darkecage.dcaicodegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.darkecage.dcaicodegenerator.config.AppConfig;
import com.darkecage.dcaicodegenerator.exception.BusinessException;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.mapper.AppMapper;
import com.darkecage.dcaicodegenerator.model.dto.app.AppQueryRequest;
import com.darkecage.dcaicodegenerator.model.entity.App;
import com.darkecage.dcaicodegenerator.model.entity.User;
import com.darkecage.dcaicodegenerator.model.enums.CodeGenTypeEnum;
import com.darkecage.dcaicodegenerator.model.vo.AppVO;
import com.darkecage.dcaicodegenerator.model.vo.UserVO;
import com.darkecage.dcaicodegenerator.service.AppService;
import com.darkecage.dcaicodegenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserService userService;

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
}
