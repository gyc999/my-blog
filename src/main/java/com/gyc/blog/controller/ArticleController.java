package com.gyc.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyc.blog.common.Result;
import com.gyc.blog.common.UserContext;
import com.gyc.blog.entity.Article;
import com.gyc.blog.entity.vo.ArticleVO;
import com.gyc.blog.service.ArticleService;
import com.gyc.blog.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private LikeService likeService;

    // 1. 发布
    @PostMapping("/publish")
    public Result<String> publish(@Valid @RequestBody Article article) {
        Long authorId = UserContext.getUserId();
        if (authorId == null) {
            return Result.error("请先登录");
        }
        boolean success = articleService.publish(article, authorId);
        return success ? Result.success("文章发布成功") : Result.error("文章发布失败");
    }

    // 2. 分页列表
    @GetMapping("/list")
    public Result<IPage<ArticleVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<ArticleVO> pageParam = new Page<>(page, size);
        IPage<ArticleVO> result = articleService.getArticleList(pageParam, categoryId, status, keyword, null);
        // 注入 Redis 实时点赞/收藏数
        for (ArticleVO vo : result.getRecords()) {
            vo.setLikeCount(likeService.getLikeCount(vo.getId()).intValue());
            vo.setCollectCount(likeService.getCollectCount(vo.getId()).intValue());
        }
        return Result.success(result);
    }

    // ===== 新增：3. 查询单篇文章（详情/回显） =====
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        // 用 Redis 实时计数覆盖 DB 中的旧值
        article.setLikeCount(likeService.getLikeCount(id).intValue());
        article.setCollectCount(likeService.getCollectCount(id).intValue());
        return Result.success(article);
    }

    // ===== 新增：4. 编辑文章 =====
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody Article article) {
        // 确保路径上的 id 和 body 中的 id 一致（防止篡改）
        article.setId(id);
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        boolean success = articleService.updateArticle(article, currentUserId);
        if (!success) {
            return Result.error("编辑失败，可能文章不存在或非您本人的文章");
        }
        return Result.success("文章更新成功");
    }

    // ===== 新增：5. 删除文章 =====
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        boolean success = articleService.deleteArticle(id, currentUserId);
        if (!success) {
            return Result.error("删除失败，可能文章不存在或非您本人的文章");
        }
        return Result.success("文章删除成功");
    }

    // 6. 浏览量递增
    @PutMapping("/{id}/view")
    public Result<String> incrementView(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        return Result.success();
    }
}