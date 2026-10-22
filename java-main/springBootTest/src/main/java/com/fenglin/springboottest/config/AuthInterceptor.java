package com.fenglin.springboottest.config;

import com.fenglin.springboottest.common.UnauthorizedException;
import com.fenglin.springboottest.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 守夜人令牌校验拦截器。
 * 除 /api/user/login 与 /api/user/register 外，所有 /api/** 请求必须携带有效 JWT，
 * 否则抛出 UnauthorizedException（由 GlobalExceptionHandler 统一返回 401）。
 * 校验通过后将 userId 写入请求属性，供后续 Controller 使用。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        Long userId = (token != null) ? jwtUtil.getUserIdFromToken(token) : null;
        if (userId == null || !jwtUtil.isAccessToken(token)) {
            throw new UnauthorizedException("未携带有效访问令牌，神域之门已闭。");
        }
        request.setAttribute("userId", userId);
        return true;
    }

    /** 支持「Bearer <token>」头、裸 token 头、以及 ?token= 查询参数 */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header)) {
            if (header.startsWith("Bearer ")) {
                return header.substring(7).trim();
            }
            return header.trim();
        }
        String query = request.getParameter("token");
        return StringUtils.hasText(query) ? query.trim() : null;
    }
}
