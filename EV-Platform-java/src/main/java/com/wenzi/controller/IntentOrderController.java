package com.wenzi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.common.Result;
import com.wenzi.dto.IntentQueryDTO;
import com.wenzi.dto.IntentStatsDTO;
import com.wenzi.dto.IntentSubmitDTO;
import com.wenzi.entity.IntentOrder;
import com.wenzi.service.IIntentOrderService;
import com.wenzi.service.IMessageService;
import com.wenzi.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/intent")
public class IntentOrderController {

    @Autowired
    private IIntentOrderService intentOrderService;

    @Autowired
    private IMessageService messageService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody IntentSubmitDTO dto, HttpServletRequest request) {
        String token = request.getHeader("token");

        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return Result.error("请先登录后再提交意向订单！");
        }

        try {
            Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());

            intentOrderService.submitIntent(dto, userId);
            return Result.success("提交成功，销售顾问将尽快与您联系！");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    @PostMapping("/my-records")
    public Result<Page<IntentOrder>> myRecords(@RequestBody IntentQueryDTO dto, HttpServletRequest request) {
        String token = request.getHeader("token");

        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return Result.error("请先登录后再查看预约记录！");
        }

        try {
            Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());
            dto.setUserId(userId);

            Page<IntentOrder> pageResult = intentOrderService.pageQuery(dto);
            return Result.success(pageResult);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取预约记录失败：" + e.getMessage());
        }
    }

    @PostMapping("/page")
    public Result<Page<IntentOrder>> pageQuery(@RequestBody IntentQueryDTO dto) {
        Page<IntentOrder> pageResult = intentOrderService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/stats")
    public Result<IntentStatsDTO> getIntentStats() {
        IntentStatsDTO stats = intentOrderService.getIntentStatistics();
        return Result.success(stats);
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody IntentOrder intentOrder) {
        try {
            IntentOrder existingOrder = intentOrderService.getById(intentOrder.getId());
            if (existingOrder == null) {
                return Result.error("更新失败，找不到该线索数据");
            }

            Byte oldStatus = existingOrder.getStatus();
            Byte newStatus = intentOrder.getStatus();

            boolean success = intentOrderService.updateById(intentOrder);
            if (success) {
                if (newStatus != null && !newStatus.equals(oldStatus) && newStatus != 0) {
                    String messageContent = buildAutoMessage(newStatus, existingOrder);
                    messageService.sendMessage(0L, existingOrder.getUserId(), messageContent);
                }
                return Result.success("状态更新成功！");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    private String buildAutoMessage(Byte status, IntentOrder order) {
        switch (status) {
            case 1:
                return "您好！您的预约已有销售顾问跟进，将尽快与您联系，请保持电话畅通！";
            case 2:
                return "恭喜！您的预约已成交，感谢您的信任，期待为您带来更多优质服务！";
            case 3:
                return "您好！您的预约已取消，如有需要可随时重新预约。";
            default:
                return "您好！您的预约状态已更新，请注意查看。";
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean success = intentOrderService.removeById(id);
            if (success) {
                return Result.success("删除成功！");
            } else {
                return Result.error("删除失败，该线索可能已被删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("token");

        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return Result.error("请先登录后再取消预约！");
        }

        try {
            Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());

            IntentOrder existingOrder = intentOrderService.getById(id);
            if (existingOrder == null) {
                return Result.error("取消失败，找不到该预约记录");
            }

            if (!existingOrder.getUserId().equals(userId)) {
                return Result.error("无权取消他人的预约");
            }

            if (existingOrder.getStatus() == 3) {
                return Result.error("该预约已经取消过了");
            }

            if (existingOrder.getStatus() == 2) {
                return Result.error("该预约已成交，无法取消");
            }

            existingOrder.setStatus((byte) 3);
            boolean success = intentOrderService.updateById(existingOrder);

            if (success) {
                String messageContent = buildAutoMessage((byte) 3, existingOrder);
                messageService.sendMessage(0L, existingOrder.getUserId(), messageContent);
                return Result.success("预约已成功取消！");
            } else {
                return Result.error("取消失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }


}