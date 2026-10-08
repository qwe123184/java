package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.RefreshRequest;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 守夜人认证接口（注册 / 登录 / 刷新 / 当前信息）
 * 前端 Vue 通过 /api 代理访问本控制器
 */
@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 觉醒禁墟（注册），返回双令牌与用户信息
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest req) {
        return userService.register(req);
    }

    /**
     * 进入神域（登录），返回双令牌与用户信息
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return userService.login(req);
    }

    /**
     * 以刷新令牌换发新的访问/刷新令牌对
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshRequest req) {
        return userService.refresh(req.getRefreshToken());
    }

    /**
     * 查看当前守夜人信息（需携带有效访问令牌，由拦截器注入 userId）
     */
    @GetMapping("/me")
    public Result<UserVO> me(@RequestAttribute Long userId) {
        UserVO vo = userService.getUserById(userId);
        return vo == null ? Result.fail("守夜人信息已消散。") : Result.success(vo);
    }
}
