package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.Archive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 神域档案 Mapper
 */
@Mapper
public interface ArchiveMapper extends BaseMapper<Archive> {
}
