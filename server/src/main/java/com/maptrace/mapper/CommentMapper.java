package com.maptrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maptrace.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE t_comment SET deleted = 0 WHERE id = #{id}")
    void restoreById(@Param("id") Long id);
}
