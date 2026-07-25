package com.gyc.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyc.blog.entity.Article;
import com.gyc.blog.entity.vo.ArticleVO;

public interface ArticleService {
    // 发布
    boolean publish(Article article, Long authorId);

    // 分页列表
    IPage<ArticleVO> getArticleList(Page<?> page, Long categoryId, Integer status, String keyword, Long authorId);

    // ===== 新增 =====
    // 根据ID查询文章（供编辑回显，不含敏感信息）
    Article getArticleById(Long id);

    // 编辑文章（需校验作者）
    boolean updateArticle(Article article, Long currentUserId);

    // 删除文章（需校验作者）
    boolean deleteArticle(Long id, Long currentUserId);

    // 浏览量+1
    void incrementViewCount(Long id);
}