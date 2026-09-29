package service.Impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import domain.entity.BlogComments;
import framework.security.SecurityContextParam;
import mapper.BlogCommentsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import service.BlogCommentsService;

import java.util.Collection;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements BlogCommentsService {

    private static final String BLOG_VIEW_PREFIX = "blog:view:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void view(Long id) {
        Long userId = SecurityContextParam.getCurrentUserId();
        stringRedisTemplate.opsForHyperLogLog().add(BLOG_VIEW_PREFIX + id, userId.toString());
    }

    @Override
    public Long viewCount(Long id) {
        return stringRedisTemplate.opsForHyperLogLog().size(BLOG_VIEW_PREFIX + id);
    }

    /**
     * 插入（批量）
     *
     * @param entityList 实体对象集合
     */
    @Override
    public boolean saveBatch(Collection<BlogComments> entityList) {
        return super.saveBatch(entityList);
    }

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     */
    @Override
    public boolean saveOrUpdateBatch(Collection<BlogComments> entityList) {
        return super.saveOrUpdateBatch(entityList);
    }
}
