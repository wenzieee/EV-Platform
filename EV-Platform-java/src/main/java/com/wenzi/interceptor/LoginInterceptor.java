package com.wenzi.interceptor;

import com.alibaba.fastjson.JSON;
import com.wenzi.common.Result;
import com.wenzi.utils.JwtUtils;
import com.wenzi.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 👇 新增：如果是跨域的 OPTIONS 预检请求，直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 从请求头拿到 token
        String token = request.getHeader("token");

        // 2. 校验 token
        if (token != null && JwtUtils.verifyToken(token)) {
            // 3. 解析出 userId 并存入 ThreadLocal
            Long userId = JwtUtils.getUserId(token);
            if (userId != null) {
                UserContext.setUserId(userId);
                return true; // 放行，允许进入 Controller
            }
        }

        // 4. 如果没 token 或者 token 失效，直接拦截并返回 401 错误给前端
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(Result.error(401, "用户未登录或Token已失效")));
        return false; // 拦截，请求到此为止
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求处理完（甚至报错抛异常）后，必须清理 ThreadLocal，防止内存泄漏
        UserContext.removeUserId();
    }
}