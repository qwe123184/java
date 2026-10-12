package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;

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
     * 按 ID 查询用户视图（不含密码等敏感字段）；不存在返回 null
     */
    UserVO getUserById(Long id);

    /**
     * 用刷新令牌换发新的访问/刷新令牌对；刷新令牌无效或用户异常时返回失败
     */
    Result<LoginVO> refresh(String refreshToken);
}
