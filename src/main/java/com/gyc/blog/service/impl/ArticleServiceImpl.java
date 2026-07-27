package com.gyc.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyc.blog.entity.Article;
import com.gyc.blog.entity.Category;
import com.gyc.blog.entity.User;
import com.gyc.blog.entity.vo.ArticleVO;
import com.gyc.blog.mapper.ArticleMapper;
import com.gyc.blog.mapper.CategoryMapper;
import com.gyc.blog.mapper.UserMapper;
import com.gyc.blog.service.AiService;
import com.gyc.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AiService aiService;

    @Override
    public boolean publish(Article article, Long authorId) {
        article.setAuthorId(authorId);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        if (article.getViewCount() == null) article.setViewCount(0);
        if (article.getLikeCount() == null) article.setLikeCount(0);
        if (article.getCollectCount() == null) article.setCollectCount(0);
        if (article.getStatus() == null) article.setStatus(1);

        // 未填写摘要时，调用 AI 自动生成
        if (article.getSummary() == null || article.getSummary().isBlank()) {
            String aiSummary = aiService.generateSummary(article.getContent());
            if (aiSummary != null && !aiSummary.isBlank()) {
                article.setSummary(aiSummary);
            }
        }

        return articleMapper.insert(article) > 0;
    }

    @Override
    public IPage<ArticleVO> getArticleList(Page<?> page, Long categoryId, Integer status, String keyword, Long authorId) {
        return articleMapper.selectArticleListWithDetail(page, categoryId, status, keyword, authorId);
    }

    // ===== 新增实现 =====
    @Override
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                article.setAuthorName(author.getNickname());
            }
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.selectById(article.getCategoryId());
                if (category != null) {
                    article.setCategoryName(category.getName());
                }
            }
        }
        return article;
    }

    @Override
    public boolean updateArticle(Article article, Long currentUserId) {
        // 1. 先查询原文章
        Article old = articleMapper.selectById(article.getId());
        if (old == null) {
            return false; // 文章不存在
        }
        // 2. 校验作者身份（防止越权）
        if (!old.getAuthorId().equals(currentUserId)) {
            return false; // 不是自己的文章，拒绝修改
        }
        // 3. 更新（只更新允许修改的字段：标题、内容、摘要、分类、状态）
        // 注意：这里只设置要更新的字段，防止误把作者ID、浏览数等覆盖
        old.setTitle(article.getTitle());
        old.setContent(article.getContent());
        old.setSummary(article.getSummary());
        old.setCoverImage(article.getCoverImage());
        old.setCategoryId(article.getCategoryId());
        old.setStatus(article.getStatus());
        old.setUpdateTime(LocalDateTime.now());
        return articleMapper.updateById(old) > 0;
    }

    @Override
    public boolean deleteArticle(Long id, Long currentUserId) {
        Article old = articleMapper.selectById(id);
        if (old == null) return false;
        if (!old.getAuthorId().equals(currentUserId)) return false;
        return articleMapper.deleteById(id) > 0;
    }

    @Override
    public void incrementViewCount(Long id) {
        articleMapper.incrementViewCount(id);
    }
}