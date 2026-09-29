//package com.branch.controller;
//
//import com.branch.domain.dto.TimeDTO;
//import com.branch.service.SignService;
//import common.result.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//
//@RestController
//@RequestMapping("/sign")
//public class SignController {
//    @Autowired
//    private SignService signService;
//
//    @PostMapping
//    public Result createSign() {
//        return Result.success(signService.sign());
//    }
//
//    @PutMapping("/back")
//    public Result backSign(@RequestBody TimeDTO time) {
//        return Result.success(signService.backSign(time));
//    }
//
//    @PostMapping("/count/now")
//    public Result CountSign() {
//        return Result.success(signService.countDaySign());
//    }
//
//    @PostMapping("/count")
//    public Result Sign(@RequestBody TimeDTO time) {
//        return Result.success(signService.countMonthSign(time));
//    }
//
//}
