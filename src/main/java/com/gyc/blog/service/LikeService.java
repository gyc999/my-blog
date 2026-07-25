package com.gyc.blog.service;

public interface LikeService {
    boolean toggleLike(Long articleId, Long userId);
    boolean getLikeStatus(Long articleId, Long userId);
    Long getLikeCount(Long articleId);
    boolean toggleCollect(Long articleId, Long userId);
    boolean getCollectStatus(Long articleId, Long userId);
    Long getCollectCount(Long articleId);
    java.util.Set<String> getUserLikedArticleIds(Long userId);
    java.util.Set<String> getUserCollectedArticleIds(Long userId);
}