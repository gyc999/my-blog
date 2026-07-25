package com.gyc.blog.service;

import com.gyc.blog.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> list();
    Category getById(Long id);
    boolean add(Category category);
    boolean update(Category category);
    boolean delete(Long id);
}
