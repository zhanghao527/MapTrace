package com.maptrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maptrace.model.entity.Report;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
