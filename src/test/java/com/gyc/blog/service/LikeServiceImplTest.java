package com.gyc.blog.service.impl;

import com.gyc.blog.common.RedisUtil;
import com.gyc.blog.entity.Article;
import com.gyc.blog.mapper.ArticleMapper;
import com.gyc.blog.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private LikeServiceImpl likeService;

    private static final Long ARTICLE_ID = 1L;
    private static final Long USER_ID = 100L;

    @Test
    @DisplayName("点赞 — 未点赞时首次点赞返回 true")
    void toggleLike_shouldReturnTrue_whenNotLiked() {
        when(redisUtil.sIsMember(anyString(), anyString())).thenReturn(false);
        when(redisUtil.sAdd(anyString(), anyString())).thenReturn(1L);

        boolean result = likeService.toggleLike(ARTICLE_ID, USER_ID);

        assertTrue(result);
        verify(redisUtil).sAdd(eq("article:like:1"), eq("100"));
        verify(redisUtil).sAdd(eq("user:like:100"), eq("1"));
        verify(redisUtil).increment(eq("article:like:count"), eq("1"), eq(1L));
    }

    @Test
    @DisplayName("点赞 — 已点赞时取消点赞返回 false")
    void toggleLike_shouldReturnFalse_whenAlreadyLiked() {
        when(redisUtil.sIsMember(anyString(), anyString())).thenReturn(true);
        when(redisUtil.sRemove(anyString(), anyString())).thenReturn(1L);

        boolean result = likeService.toggleLike(ARTICLE_ID, USER_ID);

        assertFalse(result);
        verify(redisUtil).sRemove(eq("article:like:1"), eq("100"));
        verify(redisUtil).sRemove(eq("user:like:100"), eq("1"));
        verify(redisUtil).increment(eq("article:like:count"), eq("1"), eq(-1L));
    }

    @Test
    @DisplayName("点赞 — Redis 异常时降级返回 false")
    void toggleLike_shouldReturnFalse_whenRedisFails() {
        when(redisUtil.sIsMember(anyString(), anyString())).thenThrow(new RuntimeException("Redis down"));

        boolean result = likeService.toggleLike(ARTICLE_ID, USER_ID);

        assertFalse(result); // 静默降级
    }

    @Test
    @DisplayName("获取点赞数 — Redis 有值时直接返回")
    void getLikeCount_shouldReturnRedisValue() {
        when(redisUtil.getHash("article:like:count", "1")).thenReturn(42);

        Long count = likeService.getLikeCount(1L);

        assertEquals(42L, count);
        verify(articleMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("获取点赞数 — Redis 无值时降级读 DB")
    void getLikeCount_shouldFallbackToDb() {
        when(redisUtil.getHash("article:like:count", "1")).thenReturn(null);
        Article article = new Article();
        article.setLikeCount(10);
        when(articleMapper.selectById(1L)).thenReturn(article);

        Long count = likeService.getLikeCount(1L);

        assertEquals(10L, count);
    }

    @Test
    @DisplayName("收藏 — 切换逻辑与点赞一致")
    void toggleCollect_shouldWork() {
        when(redisUtil.sIsMember(eq("article:collect:1"), anyString())).thenReturn(false);
        when(redisUtil.sAdd(anyString(), anyString())).thenReturn(1L);

        boolean result = likeService.toggleCollect(ARTICLE_ID, USER_ID);

        assertTrue(result);
        verify(redisUtil).sAdd(eq("article:collect:1"), eq("100"));
    }

    @Test
    @DisplayName("获取用户点赞文章ID列表")
    void getUserLikedArticleIds_shouldReturnSet() {
        Set<String> ids = new HashSet<>();
        ids.add("1");
        ids.add("3");
        ids.add("5");
        when(redisUtil.sMembers("user:like:100")).thenReturn(ids);

        Set<String> result = likeService.getUserLikedArticleIds(100L);

        assertEquals(3, result.size());
        assertTrue(result.contains("1"));
        assertTrue(result.contains("3"));
    }

    @Test
    @DisplayName("获取用户点赞文章ID列表 — Redis 异常返回空集合")
    void getUserLikedArticleIds_shouldReturnEmpty_whenRedisFails() {
        when(redisUtil.sMembers(anyString())).thenThrow(new RuntimeException("Redis down"));

        Set<String> result = likeService.getUserLikedArticleIds(100L);

        assertTrue(result.isEmpty());
    }
}
