//package start.controller;
//
//import common.result.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import service.BlogCommentsService;
//
//@RestController
//@RequestMapping("/comments")
//public class BlogCommentsController {
//    @Autowired
//    private BlogCommentsService blogCommentsService;
//
//    @PostMapping("/0/{id}")
//    public Result createBlog(@PathVariable("id") Long id) {
//        return Result.success();
//    }
//
//    @PostMapping("/1/{id}")
//    public Result createBlogComment(@PathVariable("id") Long id) {
//        return Result.success();
//    }
//
//    @PostMapping("/view")
//    public Result view(@RequestBody Long id) {
//        blogCommentsService.view(id);
//        return Result.success("view+1");
//    }
//
//    @GetMapping("/view/of/{id}")
//    public Result readView(@PathVariable Long id) {
//        return Result.success(blogCommentsService.viewCount(id));
//    }
//}
