package com.fenglin.springboottest.common;

/**
 * 鉴权失败异常：由拦截器抛出，统一交给 GlobalExceptionHandler 转换为 401 响应。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
