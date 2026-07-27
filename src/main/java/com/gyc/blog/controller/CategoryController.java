package com.gyc.blog.controller;

import com.gyc.blog.common.Result;
import com.gyc.blog.common.UserContext;
import com.gyc.blog.entity.Category;
import com.gyc.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<String> add(@RequestBody Category category) {
        if (UserContext.getUserId() == null) return Result.error("请先登录");
        return categoryService.add(category) ?
                Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Category category) {
        if (UserContext.getUserId() == null) return Result.error("请先登录");
        category.setId(id);
        return categoryService.update(category) ?
                Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (UserContext.getUserId() == null) return Result.error("请先登录");
        return categoryService.delete(id) ?
                Result.success("删除成功") : Result.error("删除失败");
    }
}
