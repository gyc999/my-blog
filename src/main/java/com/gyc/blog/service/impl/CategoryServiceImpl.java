package com.gyc.blog.service.impl;

import com.gyc.blog.entity.Category;
import com.gyc.blog.mapper.CategoryMapper;
import com.gyc.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {
        return categoryMapper.selectList(null);
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public boolean add(Category category) {
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean update(Category category) {
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
}
