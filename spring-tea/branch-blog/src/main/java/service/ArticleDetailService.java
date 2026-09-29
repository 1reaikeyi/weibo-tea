package service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import domain.entity.ArticleDetail;

/**
 * 文章服务接口 - 定义文章相关业务操作
 */
public interface ArticleDetailService extends IService<ArticleDetail> {
    ArticleDetail readCache(Long id);
    Boolean updateCache(ArticleDetail articleDetail);
    Boolean deleteCache(Long id);
    IPage<ArticleDetail> queryPublishedPage(int pageNum, int pageSize);
}