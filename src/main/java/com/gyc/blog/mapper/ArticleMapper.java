package com.gyc.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyc.blog.entity.Article;
import com.gyc.blog.entity.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 分页查询文章列表（连表查询作者昵称和分类名称）
     * @param page 分页对象
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param keyword 标题关键词（可选）
     * @return 分页结果
     */
    IPage<ArticleVO> selectArticleListWithDetail(
            Page<?> page,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("authorId") Long authorId
    );
}