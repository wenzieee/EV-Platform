package com.wenzi.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenzi.common.Result;
import com.wenzi.dto.IntentQueryDTO;
import com.wenzi.dto.IntentSubmitDTO;
import com.wenzi.entity.IntentOrder;
import com.wenzi.service.IIntentOrderService;
import com.wenzi.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/intent")
public class IntentOrderController {

    @Autowired
    private IIntentOrderService intentOrderService;

    @PostMapping("/submit")
    public Result<String> submit(@RequestBody IntentSubmitDTO dto, HttpServletRequest request) {
        // 1. 从请求头中获取 token
        String token = request.getHeader("token");

        // 2. 校验 token 是否合法（未登录或伪造）
        if (StrUtil.isBlank(token) || !JwtUtils.verifyToken(token)) {
            return Result.error("请先登录后再提交意向订单！");
        }

        try {
            // 3. 解析 Token 获取 userId
            Long userId = Long.valueOf(JWTUtil.parseToken(token).getPayload("id").toString());

            // 4. 调用业务逻辑
            intentOrderService.submitIntent(dto, userId);
            return Result.success("提交成功，销售顾问将尽快与您联系！");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询线索列表
     */
    @PostMapping("/page")
    public Result<Page<IntentOrder>> pageQuery(@RequestBody IntentQueryDTO dto) {
        Page<IntentOrder> pageResult = intentOrderService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 更新线索状态 (标记为已处理)
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody IntentOrder intentOrder) {
        try {
            boolean success = intentOrderService.updateById(intentOrder);
            if (success) {
                return Result.success("状态更新成功！");
            } else {
                return Result.error("更新失败，找不到该线索数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 删除线索
     */
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


}