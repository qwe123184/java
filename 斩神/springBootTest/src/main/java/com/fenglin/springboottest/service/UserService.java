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
}
