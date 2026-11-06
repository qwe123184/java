package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.QuizAnswer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizAnswerMapper extends BaseMapper<QuizAnswer> {
}
