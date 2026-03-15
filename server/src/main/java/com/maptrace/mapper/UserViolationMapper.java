package com.maptrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maptrace.model.entity.UserViolation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserViolationMapper extends BaseMapper<UserViolation> {
}
