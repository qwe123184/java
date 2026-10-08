package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 守夜人账户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
