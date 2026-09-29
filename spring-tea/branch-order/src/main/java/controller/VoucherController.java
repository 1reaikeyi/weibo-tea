package controller;

import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import model.dto.VoucherDTO;
import VoucherOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.VoucherOrderService;
import service.VoucherService;

/**
 * 使用 redisLock，异步处理
 */
@RestController
@RequestMapping("/voucher")
@Slf4j
public class VoucherController {
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private VoucherOrderService voucherOrderService;

    @PostMapping("/create")
    public Result createVoucher(@RequestBody VoucherDTO voucherDTO) {
        voucherService.createVoucher(voucherDTO);
        return Result.success("createVoucher");
    }

    @PostMapping("/pay")
    public Result redisLock(@RequestBody VoucherOrder voucherOrder) {
        return voucherOrderService.redisLockPay(voucherOrder);
    }
}
