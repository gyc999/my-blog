package com.gyc.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gyc.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper，所有单表CRUD方法都有了，不用写一行SQL
}