package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.ChangePasswordRequest;
import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.ProfileUpdateRequest;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 守夜人账户业务接口
 */
public interface UserService {

    /**
     * 觉醒禁墟（注册），成功后直接签发会话令牌
     */
    Result<LoginVO> register(RegisterRequest req);

    /**
     * 进入神域（登录）
     */
    Result<LoginVO> login(LoginRequest req);

    /**
     * 执政官进入后台（管理员登录），仅 ADMIN 角色可登录
     */
    Result<LoginVO> adminLogin(LoginRequest req);

    /**
     * 判断指定用户是否为后台管理员（ADMIN）；非 ADMIN 或不存在返回 false
     */
    boolean isAdmin(Long userId);

    /**
     * 按 ID 查询用户视图（不含密码等敏感字段）；不存在返回 null
     */
    UserVO getUserById(Long id);

    /**
     * 用刷新令牌换发新的访问/刷新令牌对；刷新令牌无效或用户异常时返回失败
     */
    Result<LoginVO> refresh(String refreshToken);

    /**
     * 修改当前守夜人个人资料（昵称/头像/手机/简介/邮箱/境界），仅影响自身
     */
    Result<UserVO> updateProfile(Long userId, ProfileUpdateRequest req);

    /**
     * 修改当前守夜人密码（明文比对旧密码，明文写入新密码）
     */
    Result<Void> changePassword(Long userId, ChangePasswordRequest req);

    /**
     * 后台用户列表（仅 ADMIN 可访问），支持关键词 / 权限 / 状态筛选
     */
    Result<IPage<UserVO>> listUsers(String keyword, Integer permission, Integer status, long page, long size);

    /**
     * 后台封禁 / 解封账号（仅 ADMIN 可操作）；status 0 正常 / 1 封禁
     */
    Result<UserVO> setUserStatus(Long id, Integer status);

    /**
     * 后台调整账号权限（仅 ADMIN 可操作）；permission 0 管理员 / 1 默认
     */
    Result<UserVO> setUserPermission(Long id, Integer permission);

    /**
     * 后台删除账号（仅 ADMIN 可操作）
     */
    Result<Void> deleteUser(Long id);
}
