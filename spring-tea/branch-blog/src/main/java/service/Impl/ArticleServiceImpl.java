package service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import mapper.ArticleMapper;
import domain.entity.Article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import service.ArticleService;
import org.springframework.stereotype.Service;
import model.wrapper.RedisData;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 分类服务实现类 - 实现分类相关业务逻辑
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
    private final static String KEYS = "category:";
    private final static String LOCK_KEY = "category:lock";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Article readCache(Long id) {
        Article article = logicalCache(id);
        return article;
    }
    private Boolean cacheLocked(){
        return stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "locked", 5, TimeUnit.SECONDS);
    }
    private void cacheUnlock(){
        stringRedisTemplate.delete(LOCK_KEY);
    }

    private Article logicalCache(Long id) {
        String key = KEYS + id;
        //1 直接从缓存中获取数据
        String categoryJson = stringRedisTemplate.opsForValue().get(key);
        //2缓存不存在
        if (StrUtil.isBlank(categoryJson)) {
            Article article = super.getById(id);
            RedisData redisData = new RedisData();
            //没有id
            if (article == null) {
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(30));
                redisData.setData(null);
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
                throw  new RuntimeException("id不存在");
            }
            //有id
            redisData.setData(article);
            redisData.setExpireTime(LocalDateTime.now().plusSeconds(5));
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
            return article;
        }
        //3 缓存存在
        RedisData redisData = JSONUtil.toBean(categoryJson, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //过期
        if (expireTime.isBefore(LocalDateTime.now())) {
            log.info("Category缓存过期");
            Boolean success = cacheLocked();
            if (success) {
                try {
                    Article article = super.getById(id);
                    redisData.setExpireTime(LocalDateTime.now().plusSeconds(5));
                    redisData.setData(article);
                    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (success) {
                        cacheUnlock();
                    }
                }
            }else {
                throw new RuntimeException("服务器繁忙，请稍后重试");
            }
        }
        //正常
        log.info("Category缓存正常");
        Article article = BeanUtil.toBean(redisData.getData(), Article.class);
        return article;
    }

    @Override
    public Boolean updateCache(Article article) {
        boolean result = super.updateById(article);
        stringRedisTemplate.delete(KEYS+ article.getId());
        return result;
    }

    @Override
    public Boolean deleteCache(Long id) {
        boolean result = super.removeById(id);
        stringRedisTemplate.delete(KEYS+id);
        return result;
    }
}