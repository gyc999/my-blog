package com.gyc.blog.controller;

import com.gyc.blog.common.Result;
import com.gyc.blog.common.UserContext;
import com.gyc.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    // 点赞/取消点赞
    @PostMapping("/toggle/{articleId}")
    public Result<Boolean> toggleLike(@PathVariable Long articleId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean status = likeService.toggleLike(articleId, userId);
        return Result.success(status);
    }

    // 获取点赞状态
    @GetMapping("/status/{articleId}")
    public Result<Boolean> getLikeStatus(@PathVariable Long articleId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(likeService.getLikeStatus(articleId, userId));
    }

    // 获取点赞数
    @GetMapping("/count/{articleId}")
    public Result<Long> getLikeCount(@PathVariable Long articleId) {
        return Result.success(likeService.getLikeCount(articleId));
    }

    // 收藏/取消收藏
    @PostMapping("/collect/toggle/{articleId}")
    public Result<Boolean> toggleCollect(@PathVariable Long articleId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean status = likeService.toggleCollect(articleId, userId);
        return Result.success(status);
    }

    // 获取收藏状态
    @GetMapping("/collect/status/{articleId}")
    public Result<Boolean> getCollectStatus(@PathVariable Long articleId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(likeService.getCollectStatus(articleId, userId));
    }

    // 获取收藏数
    @GetMapping("/collect/count/{articleId}")
    public Result<Long> getCollectCount(@PathVariable Long articleId) {
        return Result.success(likeService.getCollectCount(articleId));
    }
}