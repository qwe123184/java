package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 执政官后台认证接口（管理员登录 / 当前信息）
 * 前端 Vue 后台管理模块通过 /api 代理访问本控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    /**
     * 执政官进入后台（管理员登录，仅 ADMIN 角色可登录）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return userService.adminLogin(req);
    }

    /**
     * 查看当前执政官信息（需携带有效访问令牌，由拦截器注入 userId，且必须为 ADMIN 角色）
     */
    @GetMapping("/me")
    public Result<UserVO> me(@RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官身份，无权访问后台。");
        }
        UserVO vo = userService.getUserById(userId);
        return vo == null ? Result.fail("执政官信息已消散。") : Result.success(vo);
    }
}
