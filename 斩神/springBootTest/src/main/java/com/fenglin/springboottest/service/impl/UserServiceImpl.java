package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 守夜人账户业务实现
 */
@Service
public class UserServiceImpl implements UserService {

    /** 合法的禁墟境界 */
    private static final List<String> REALMS = Arrays.asList("盏境", "池境", "川境", "海境");
    private static final String DEFAULT_REALM = "池境";
    private static final int MIN_PASSWORD_LEN = 4;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 简易内存令牌表：token -> userId（演示用，重启即失效） */
    private final ConcurrentHashMap<String, Long> tokenStore = new ConcurrentHashMap<>();

    @Override
    public Result<LoginVO> register(RegisterRequest req) {
        if (req == null || !StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return Result.fail("守夜人代号与禁墟密钥均不可为空。");
        }
        String username = req.getUsername().trim();
        String password = req.getPassword().trim();

        if (password.length() < MIN_PASSWORD_LEN) {
            return Result.fail("禁墟密钥至少需 " + MIN_PASSWORD_LEN + " 位，横刀之誓不可轻许。");
        }

        // 代号唯一性校验
        Long existed = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existed != null && existed > 0) {
            return Result.fail("该守夜人代号已被觉醒，请另择代号。");
        }

        // 境界校验与兜底
        String realm = req.getRealm();
        if (!StringUtils.hasText(realm) || !REALMS.contains(realm)) {
            realm = DEFAULT_REALM;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setEmail(StringUtils.hasText(req.getEmail()) ? req.getEmail().trim() : null);
        user.setRealm(realm);
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 注册成功即签发会话令牌（与登录保持一致，前端可直接进入神域）
        String token = "zs-" + UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());

        LoginVO vo = new LoginVO(token, UserVO.fromEntity(user));
        return Result.success("守夜人「" + username + "」于【" + realm + "】觉醒，禁墟已铭刻。", vo);
    }

    @Override
    public Result<LoginVO> login(LoginRequest req) {
        if (req == null || !StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return Result.fail("请先留下你的守夜人代号与禁墟密钥。");
        }
        String username = req.getUsername().trim();
        String password = req.getPassword().trim();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.fail("代号未觉醒，请先完成觉醒仪式。");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            return Result.fail("该守夜人已被诸神封印，禁止进入神域。");
        }
        if (!encoder.matches(password, user.getPassword())) {
            return Result.fail("禁墟密钥有误，横刀之誓不可违。");
        }

        // 签发令牌（演示：zs- 前缀 + UUID，与前端原有格式保持一致）
        String token = "zs-" + UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, user.getId());

        LoginVO vo = new LoginVO(token, UserVO.fromEntity(user));
        return Result.success("代号「" + username + "」已认证，凡尘神域为你敞开。", vo);
    }
}
