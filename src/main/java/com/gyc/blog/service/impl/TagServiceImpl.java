package com.gyc.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gyc.blog.entity.ArticleTag;
import com.gyc.blog.entity.Tag;
import com.gyc.blog.mapper.ArticleTagMapper;
import com.gyc.blog.mapper.TagMapper;
import com.gyc.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public List<Tag> list() {
        return tagMapper.selectList(null);
    }

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> relations = articleTagMapper.selectList(wrapper);
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> tagIds = relations.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    public boolean add(Tag tag) {
        return tagMapper.insert(tag) > 0;
    }

    @Override
    public boolean delete(Long id) {
        // 删除相关关联
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getTagId, id);
        articleTagMapper.delete(wrapper);
        return tagMapper.deleteById(id) > 0;
    }
}
