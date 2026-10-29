package com.fenglin.springboottest.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 执政官后台认证接口（管理员登录 / 当前信息 / 用户管理）
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

    /**
     * 后台用户列表（仅 ADMIN 可访问），支持关键词 / 权限 / 状态筛选
     */
    @GetMapping("/users")
    public Result<IPage<UserVO>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer permission,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权查看用户。");
        }
        return userService.listUsers(keyword, permission, status, page, size);
    }

    /**
     * 封禁 / 解封账号（仅 ADMIN 可操作），status：0 正常 / 1 封禁
     */
    @PutMapping("/users/{id}/status")
    public Result<UserVO> setUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权操作。");
        }
        return userService.setUserStatus(id, status);
    }

    /**
     * 调整账号权限（仅 ADMIN 可操作），permission：0 管理员 / 1 默认
     */
    @PutMapping("/users/{id}/permission")
    public Result<UserVO> setUserPermission(
            @PathVariable Long id,
            @RequestParam Integer permission,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权操作。");
        }
        return userService.setUserPermission(id, permission);
    }

    /**
     * 删除账号（仅 ADMIN 可操作）
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        if (!userService.isAdmin(userId)) {
            return Result.fail("非执政官无权操作。");
        }
        return userService.deleteUser(id);
    }
}
