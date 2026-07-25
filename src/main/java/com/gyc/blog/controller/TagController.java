package com.gyc.blog.controller;

import com.gyc.blog.common.Result;
import com.gyc.blog.entity.Tag;
import com.gyc.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return Result.success(tagService.list());
    }

    @GetMapping("/article/{articleId}")
    public Result<List<Tag>> getByArticle(@PathVariable Long articleId) {
        return Result.success(tagService.getTagsByArticleId(articleId));
    }

    @PostMapping
    public Result<String> add(@RequestBody Tag tag) {
        return tagService.add(tag) ?
                Result.success("添加成功") : Result.error("添加失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return tagService.delete(id) ?
                Result.success("删除成功") : Result.error("删除失败");
    }
}
