package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.UserGameStat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserGameStatMapper extends BaseMapper<UserGameStat> {
}
