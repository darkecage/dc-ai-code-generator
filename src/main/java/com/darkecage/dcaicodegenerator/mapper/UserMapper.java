package com.darkecage.dcaicodegenerator.mapper;

import com.mybatisflex.core.BaseMapper;
import com.darkecage.dcaicodegenerator.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
