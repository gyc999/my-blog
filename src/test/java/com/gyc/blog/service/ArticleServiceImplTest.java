package com.gyc.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gyc.blog.entity.Article;
import com.gyc.blog.entity.User;
import com.gyc.blog.entity.vo.ArticleVO;
import com.gyc.blog.mapper.ArticleMapper;
import com.gyc.blog.mapper.UserMapper;
import com.gyc.blog.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Article testArticle;
    private User testAuthor;

    @BeforeEach
    void setUp() {
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("测试文章");
        testArticle.setContent("这是内容");
        testArticle.setAuthorId(1L);

        testAuthor = new User();
        testAuthor.setId(1L);
        testAuthor.setNickname("博主");
    }

    @Test
    @DisplayName("发布文章 — 自动填充字段")
    void publish_shouldSetDefaults() {
        when(articleMapper.insert(any(Article.class))).thenReturn(1);

        boolean result = articleService.publish(testArticle, 1L);

        assertTrue(result);
        assertEquals(1L, testArticle.getAuthorId());
        assertEquals(0, testArticle.getViewCount());
        assertEquals(0, testArticle.getLikeCount());
        assertEquals(0, testArticle.getCollectCount());
        assertEquals(1, testArticle.getStatus());
        assertNotNull(testArticle.getCreateTime());
        assertNotNull(testArticle.getUpdateTime());
    }

    @Test
    @DisplayName("查询文章详情 — 应填充作者昵称")
    void getArticleById_shouldFillAuthorName() {
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(userMapper.selectById(1L)).thenReturn(testAuthor);

        Article result = articleService.getArticleById(1L);

        assertNotNull(result);
        assertEquals("博主", result.getAuthorName());
    }

    @Test
    @DisplayName("查询文章详情 — 文章不存在")
    void getArticleById_shouldReturnNull_whenNotFound() {
        when(articleMapper.selectById(999L)).thenReturn(null);

        Article result = articleService.getArticleById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("更新文章 — 仅允许作者本人操作")
    void updateArticle_shouldRejectNonOwner() {
        Article update = new Article();
        update.setId(1L);
        update.setTitle("新标题");
        testArticle.setAuthorId(2L); // 原作者是 2，当前用户是 1
        when(articleMapper.selectById(1L)).thenReturn(testArticle);

        boolean result = articleService.updateArticle(update, 1L);

        assertFalse(result);
        verify(articleMapper, never()).updateById(any(Article.class));
    }

    @Test
    @DisplayName("更新文章 — 允许作者本人操作")
    void updateArticle_shouldAllowOwner() {
        Article update = new Article();
        update.setId(1L);
        update.setTitle("新标题");
        update.setContent("新内容");
        update.setCoverImage("/img/test.jpg");
        testArticle.setAuthorId(1L); // 作者是当前用户
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        boolean result = articleService.updateArticle(update, 1L);

        assertTrue(result);
        assertEquals("新标题", testArticle.getTitle());
        verify(articleMapper).updateById(testArticle);
    }

    @Test
    @DisplayName("删除文章 — 仅允许作者本人操作")
    void deleteArticle_shouldRejectNonOwner() {
        testArticle.setAuthorId(2L);
        when(articleMapper.selectById(1L)).thenReturn(testArticle);

        boolean result = articleService.deleteArticle(1L, 1L);

        assertFalse(result);
        verify(articleMapper, never()).deleteById(anyLong());
    }
}
