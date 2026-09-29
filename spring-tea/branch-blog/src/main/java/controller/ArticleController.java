//package start.controller;
//
//import common.result.Result;
//import lombok.extern.slf4j.Slf4j;
//import model.entity.ArticleDetail;
//import org.springframework.transaction.annotation.Transactional;
//import service.ArticleDetailService;
//import service.ArticleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 文章管理控制器
// * <p>
// * 负责文章的增删改查、状态管理、分类关联等核心功能，
// * 提供完整的文章管理API接口，支持JWT认证和权限控制。
// * </p>
// * @author Smart-doc
// * @since 1.0.0
// * @version 1.0.0
// */
//@RestController
//@RequestMapping("/article")
//@Transactional(rollbackFor = Exception.class)
//@Slf4j
//public class ArticleController {
//    @Autowired
//    private ArticleService articleService;
//    @Autowired
//    private ArticleDetailService articleDetailService;
//
//    /**
//     * 添加文章
//     *
//     * @param articleDetail 文章信息
//     * @return 结果
//     */
//    @PostMapping
//    public Result createArticle(@RequestBody @Validated ArticleDetail articleDetail) {
//        articleDetailService.save(articleDetail);
//        return Result.success("createArticle::"+ articleDetail.getId());
//    }
//
//    /**
//     * 获取所有文章
//     *
//     * @return 结果
//     */
//    @GetMapping("/list")
//    public Result readArticle() {
//        return Result.success(articleDetailService.list());
//    }
//
//    /**
//     * 获取分页文章列表
//     *
//     * @param pageNum 页码
//     * @param pageSize 每页数量
//     * @return 分页结果包含文章列表
//     */
//    @GetMapping
//    public Result readArticlePage(int pageNum, int pageSize){
//        return Result.success(articleDetailService.queryPublishedPage(pageNum, pageSize));
//    }
//
//    /**
//     * 获取单个文章
//     *
//     * @param id 文章ID
//     * @return 结果
//     */
//    @GetMapping("/{id}")
//    public Result readById(@PathVariable Long id) {
//        ArticleDetail articleDetail = articleDetailService.readCache(id);
//        return Result.success(articleDetail);
//    }
//
//    /**
//     * 更新文章
//     *
//     * @param articleDetail 文章信息
//     * @return 结果
//     */
//    @PutMapping
//    public Result updateArticle(@RequestBody @Validated ArticleDetail articleDetail) {
//        articleDetailService.updateCache(articleDetail);
//        return Result.success("updateArticle::"+ articleDetail.getId());
//    }
//
//    /**
//     * 删除文章
//     *
//     * @param id 文章ID
//     * @return 结果
//     */
//    @DeleteMapping("/{id}")
//    public Result deleteById(@PathVariable Long id) {
//        articleDetailService.deleteCache(id);
//        return Result.success("deleteById::"+id);
//    }
//}