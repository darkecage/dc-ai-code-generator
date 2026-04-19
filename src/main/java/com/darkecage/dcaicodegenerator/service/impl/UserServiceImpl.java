package com.darkecage.dcaicodegenerator.service.impl;

import com.darkecage.dcaicodegenerator.mapper.UserMapper;
import com.darkecage.dcaicodegenerator.model.entity.User;
import com.darkecage.dcaicodegenerator.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

}
