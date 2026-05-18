package com.darkecage.dcaicodegenerator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.darkecage.dcaicodegenerator.annotation.AuthCheck;
import com.darkecage.dcaicodegenerator.common.BaseResponse;
import com.darkecage.dcaicodegenerator.common.DeleteRequest;
import com.darkecage.dcaicodegenerator.common.ResultUtils;
import com.darkecage.dcaicodegenerator.common.constant.UserConstant;
import com.darkecage.dcaicodegenerator.config.AppConfig;
import com.darkecage.dcaicodegenerator.exception.ErrorCode;
import com.darkecage.dcaicodegenerator.exception.ThrowUtils;
import com.darkecage.dcaicodegenerator.model.dto.app.AppAddRequest;
import com.darkecage.dcaicodegenerator.model.dto.app.AppAdminUpdateRequest;
import com.darkecage.dcaicodegenerator.model.dto.app.AppUserUpdateRequest;
import com.darkecage.dcaicodegenerator.model.dto.app.AppQueryRequest;
import com.darkecage.dcaicodegenerator.model.entity.App;
import com.darkecage.dcaicodegenerator.model.vo.AppVO;
import com.darkecage.dcaicodegenerator.model.vo.LoginUserVO;
import com.darkecage.dcaicodegenerator.service.AppService;
import com.darkecage.dcaicodegenerator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用 控制层。
 *
 * @author kaiqi.hu
 */
@RestController
@RequestMapping("/app")
@Slf4j
public class AppController {

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppConfig appConfig;

    // region 用户端接口

    /**
     * 创建应用
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        // 应用名称为空时，取初始提示词前 N 个字
        if (StrUtil.isBlank(app.getAppName()) && StrUtil.isNotBlank(appAddRequest.getInitPrompt())) {
            String prompt = appAddRequest.getInitPrompt().trim();
            int len = appConfig.getAppNameDefaultLength();
            app.setAppName(prompt.length() > len ? prompt.substring(0, len) : prompt);
        }
        // 参数校验
        appService.validApp(app, true);
        // 获取当前登录用户
        LoginUserVO loginUser = userService.getLoginUser(request);
        app.setUserId(loginUser.getUserId());
        app.setEditTime(LocalDateTime.now());
        // 保存
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 用户编辑自己的应用（仅支持修改应用名称）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUserUpdateRequest appUserUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUserUpdateRequest == null || appUserUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(appUserUpdateRequest.getAppName()), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        // 查询应用是否存在
        App oldApp = appService.getById(appUserUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验所有权
        LoginUserVO loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldApp.getUserId().equals(loginUser.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权编辑他人的应用");
        // 仅更新 appName 和 editTime
        App updateApp = new App();
        updateApp.setId(oldApp.getId());
        updateApp.setAppName(appUserUpdateRequest.getAppName());
        updateApp.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户删除自己的应用
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 查询应用是否存在
        App app = appService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验所有权
        LoginUserVO loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权删除他人的应用");
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 查看应用详情（用户端，返回 VO）
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页查询自己的应用列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 强制限制每页最大条数(由配置控制)
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = Math.min(appQueryRequest.getPageSize(), appConfig.getUserPageSizeMax());
        // 强制查询当前用户的应用
        LoginUserVO loginUser = userService.getLoginUser(request);
        appQueryRequest.setUserId(loginUser.getUserId());
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));
        // 转换为 VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选应用列表（priority >= 配置阈值）
     */
    @PostMapping("/featured/list/page/vo")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 强制限制每页最大条数(由配置控制)
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = Math.min(appQueryRequest.getPageSize(), appConfig.getUserPageSizeMax());
        // 构建查询条件，强制 priority >= 配置的精选阈值
        // 不限定 userId，所有用户的精选应用都可见
        appQueryRequest.setUserId(null);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        queryWrapper.ge("priority", appConfig.getFeaturedPriorityThreshold());
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 转换为 VO
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    // endregion

    // region 管理员接口

    /**
     * 管理员删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        // 查询应用是否存在
        App app = appService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员根据 id 查看应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> adminGetAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 管理员更新任意应用（支持更新应用名称、封面、优先级）
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        // 查询应用是否存在
        App oldApp = appService.getById(appAdminUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新字段
        App updateApp = new App();
        updateApp.setId(appAdminUpdateRequest.getId());
        updateApp.setAppName(appAdminUpdateRequest.getAppName());
        updateApp.setCover(appAdminUpdateRequest.getCover());
        updateApp.setPriority(appAdminUpdateRequest.getPriority());
        updateApp.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(updateApp);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页查询应用列表（支持所有非时间字段查询，页大小不限）
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> adminListAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));
        // 实体分页转换为 VO 分页
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
        return ResultUtils.success(appVOPage);
    }

    // endregion
}
