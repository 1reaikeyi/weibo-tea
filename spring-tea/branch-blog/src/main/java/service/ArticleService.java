package service;

import com.baomidou.mybatisplus.extension.service.IService;
import domain.entity.Article;


/**
 * 分类服务接口 - 定义分类相关业务操作
 */
public interface ArticleService extends IService<Article> {
    Article readCache(Long id);
    Boolean updateCache(Article article);
    Boolean deleteCache(Long id);
}