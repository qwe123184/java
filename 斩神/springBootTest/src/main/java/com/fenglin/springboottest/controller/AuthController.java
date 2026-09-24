package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 守夜人认证接口（注册 / 登录）
 * 前端 Vue 通过 /api 代理访问本控制器
 */
@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 觉醒禁墟（注册），返回令牌与用户信息
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterRequest req) {
        return userService.register(req);
    }

    /**
     * 进入神域（登录）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest req) {
        return userService.login(req);
    }
}
