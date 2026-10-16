package com.fenglin.springboottest.config;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：把所有异常统一成前端约定的 {@link Result} JSON 结构，
 * 避免 Spring 默认抛出 HTML 错误页或堆栈信息泄漏。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败（@Valid / Bean Validation） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return ResponseEntity.badRequest().body(Result.fail(msg));
    }

    /** 鉴权失败（拦截器抛出） */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.fail(401, ex.getMessage()));
    }

    /** 请求方法不被允许（如用 GET 打只收 POST 的接口） */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.fail("神域不允此等仪式（请求方法不被允许）。"));
    }

    /** 兜底：其余未捕获异常，返回 500 但不泄漏堆栈 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("神域动荡，禁墟异动：" + ex.getMessage()));
    }
}
