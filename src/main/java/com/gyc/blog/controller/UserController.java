package com.gyc.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyc.blog.common.Result;
import com.gyc.blog.common.UserContext;
import com.gyc.blog.entity.Article;
import com.gyc.blog.entity.User;
import com.gyc.blog.entity.vo.ArticleVO;
import com.gyc.blog.mapper.UserMapper;
import com.gyc.blog.service.ArticleService;
import com.gyc.blog.service.LikeService;
import com.gyc.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private DataSource dataSource;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("用户名已存在");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@RequestParam String username,
                                @RequestParam String password) {
        String token = userService.login(username, password);
        if (token == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(token);
    }

    // 获取当前登录用户信息
    @GetMapping("/me")
    public Result<?> currentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        return Result.success(userService.getUserById(userId));
    }

    // 更新当前用户信息（昵称、头像）
    @PutMapping("/me")
    public Result<String> updateProfile(@RequestBody User user) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        user.setId(userId);
        return userService.updateProfile(user) ?
                Result.success("更新成功") : Result.error("更新失败");
    }

    // 获取当前用户点赞的文章
    @GetMapping("/likes")
    public Result<List<Article>> likedArticles() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        Set<String> ids = likeService.getUserLikedArticleIds(userId);
        List<Article> articles = new ArrayList<>();
        for (String idStr : ids) {
            try {
                Article article = articleService.getArticleById(Long.valueOf(idStr));
                if (article != null) articles.add(article);
            } catch (NumberFormatException ignored) {}
        }
        return Result.success(articles);
    }

    // 获取当前用户收藏的文章
    @GetMapping("/collects")
    public Result<List<Article>> collectedArticles() {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        Set<String> ids = likeService.getUserCollectedArticleIds(userId);
        List<Article> articles = new ArrayList<>();
        for (String idStr : ids) {
            try {
                Article article = articleService.getArticleById(Long.valueOf(idStr));
                if (article != null) articles.add(article);
            } catch (NumberFormatException ignored) {}
        }
        return Result.success(articles);
    }

    // 获取当前用户自己的文章
    @GetMapping("/articles")
    public Result<IPage<ArticleVO>> myArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        Page<ArticleVO> pageParam = new Page<>(page, size);
        IPage<ArticleVO> result = articleService.getArticleList(pageParam, null, null, null, userId);
        return Result.success(result);
    }

    // 健康检查 - 测试数据库连接
    @GetMapping("/health")
    public Result<String> health() {
        try (Connection conn = dataSource.getConnection()) {
            return Result.success("DB OK: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            return Result.error("DB FAIL: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}