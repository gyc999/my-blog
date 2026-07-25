package com.gyc.blog.service.impl;

import com.gyc.blog.common.RedisUtil;
import com.gyc.blog.entity.Article;
import com.gyc.blog.mapper.ArticleMapper;
import com.gyc.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class LikeServiceImpl implements LikeService {

    private static final String LIKE_KEY_PREFIX = "article:like:";
    private static final String COLLECT_KEY_PREFIX = "article:collect:";
    private static final String LIKE_COUNT_KEY = "article:like:count";
    private static final String COLLECT_COUNT_KEY = "article:collect:count";
    private static final String USER_LIKE_KEY_PREFIX = "user:like:";
    private static final String USER_COLLECT_KEY_PREFIX = "user:collect:";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ArticleMapper articleMapper;

    // ========== 点赞 ==========
    @Override
    public boolean toggleLike(Long articleId, Long userId) {
        try {
            String key = LIKE_KEY_PREFIX + articleId;
            String userKey = String.valueOf(userId);
            String userLikeKey = USER_LIKE_KEY_PREFIX + userId;
            String articleIdStr = String.valueOf(articleId);
            Boolean isLiked = redisUtil.sIsMember(key, userKey);
            if (Boolean.TRUE.equals(isLiked)) {
                redisUtil.sRemove(key, userKey);
                redisUtil.sRemove(userLikeKey, articleIdStr);
                redisUtil.increment(LIKE_COUNT_KEY, String.valueOf(articleId), -1);
                return false;
            } else {
                redisUtil.sAdd(key, userKey);
                redisUtil.sAdd(userLikeKey, articleIdStr);
                redisUtil.increment(LIKE_COUNT_KEY, String.valueOf(articleId), 1);
                return true;
            }
        } catch (Exception e) {
            return false; // Redis 不可用，静默降级
        }
    }

    @Override
    public boolean getLikeStatus(Long articleId, Long userId) {
        try {
            String key = LIKE_KEY_PREFIX + articleId;
            return Boolean.TRUE.equals(redisUtil.sIsMember(key, String.valueOf(userId)));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getLikeCount(Long articleId) {
        try {
            Object count = redisUtil.getHash(LIKE_COUNT_KEY, String.valueOf(articleId));
            if (count != null) return Long.parseLong(count.toString());
        } catch (Exception ignored) {}
        // Redis 不可用时直接从 DB 读取
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            return article.getLikeCount() != null ? article.getLikeCount().longValue() : 0L;
        }
        return 0L;
    }

    // ========== 收藏 ==========
    @Override
    public boolean toggleCollect(Long articleId, Long userId) {
        try {
            String key = COLLECT_KEY_PREFIX + articleId;
            String userKey = String.valueOf(userId);
            String userCollectKey = USER_COLLECT_KEY_PREFIX + userId;
            String articleIdStr = String.valueOf(articleId);
            Boolean isCollected = redisUtil.sIsMember(key, userKey);
            if (Boolean.TRUE.equals(isCollected)) {
                redisUtil.sRemove(key, userKey);
                redisUtil.sRemove(userCollectKey, articleIdStr);
                redisUtil.increment(COLLECT_COUNT_KEY, String.valueOf(articleId), -1);
                return false;
            } else {
                redisUtil.sAdd(key, userKey);
                redisUtil.sAdd(userCollectKey, articleIdStr);
                redisUtil.increment(COLLECT_COUNT_KEY, String.valueOf(articleId), 1);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean getCollectStatus(Long articleId, Long userId) {
        try {
            String key = COLLECT_KEY_PREFIX + articleId;
            return Boolean.TRUE.equals(redisUtil.sIsMember(key, String.valueOf(userId)));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getCollectCount(Long articleId) {
        try {
            Object count = redisUtil.getHash(COLLECT_COUNT_KEY, String.valueOf(articleId));
            if (count != null) return Long.parseLong(count.toString());
        } catch (Exception ignored) {}
        // Redis 不可用时直接从 DB 读取
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            return article.getCollectCount() != null ? article.getCollectCount().longValue() : 0L;
        }
        return 0L;
    }

    // ========== 定时同步 Redis → MySQL ==========
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void syncCountsToDatabase() {
        try {
            Map<Object, Object> likeCounts = redisUtil.getHashEntries(LIKE_COUNT_KEY);
            if (likeCounts != null) {
                for (Map.Entry<Object, Object> entry : likeCounts.entrySet()) {
                    Long articleId = Long.valueOf(entry.getKey().toString());
                    Integer count = Integer.valueOf(entry.getValue().toString());
                    Article article = articleMapper.selectById(articleId);
                    if (article != null) {
                        article.setLikeCount(count);
                        articleMapper.updateById(article);
                    }
                }
            }
            Map<Object, Object> collectCounts = redisUtil.getHashEntries(COLLECT_COUNT_KEY);
            if (collectCounts != null) {
                for (Map.Entry<Object, Object> entry : collectCounts.entrySet()) {
                    Long articleId = Long.valueOf(entry.getKey().toString());
                    Integer count = Integer.valueOf(entry.getValue().toString());
                    Article article = articleMapper.selectById(articleId);
                    if (article != null) {
                        article.setCollectCount(count);
                        articleMapper.updateById(article);
                    }
                }
            }
        } catch (Exception e) {
            // Redis 不可用时静默跳过同步，不影响主业务
        }
    }

    // ========== 用户维度查询 ==========
    @Override
    public Set<String> getUserLikedArticleIds(Long userId) {
        try {
            Set<String> result = redisUtil.sMembers(USER_LIKE_KEY_PREFIX + userId);
            return result != null ? result : Collections.emptySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> getUserCollectedArticleIds(Long userId) {
        try {
            Set<String> result = redisUtil.sMembers(USER_COLLECT_KEY_PREFIX + userId);
            return result != null ? result : Collections.emptySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}