package com.gyc.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gyc.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
