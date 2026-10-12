package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.config.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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

        // 注册成功即签发双令牌（access + refresh），前端可凭 refresh 续期
        String access = jwtUtil.generateAccessToken(user.getId(), username);
        String refresh = jwtUtil.generateRefreshToken(user.getId(), username);

        LoginVO vo = new LoginVO(access, UserVO.fromEntity(user));
        vo.setRefreshToken(refresh);
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

        // 签发双令牌（access + refresh）
        String access = jwtUtil.generateAccessToken(user.getId(), username);
        String refresh = jwtUtil.generateRefreshToken(user.getId(), username);

        LoginVO vo = new LoginVO(access, UserVO.fromEntity(user));
        vo.setRefreshToken(refresh);
        return Result.success("代号「" + username + "」已认证，凡尘神域为你敞开。", vo);
    }

    @Override
    public Result<LoginVO> refresh(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            return Result.fail("刷新令牌无效或格式有误。");
        }
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            return Result.fail("刷新令牌已失效，请重新登录。");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("守夜人信息已消散，请重新登录。");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            return Result.fail("该守夜人已被诸神封印，禁止进入神域。");
        }
        // 轮换双令牌（refresh token 滚动失效，降低泄露风险）
        String access = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refresh = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO(access, UserVO.fromEntity(user));
        vo.setRefreshToken(refresh);
        return Result.success("禁墟之力已续满，神域继续为你敞开。", vo);
    }

    @Override
    public UserVO getUserById(Long id) {
        if (id == null) {
            return null;
        }
        User user = userMapper.selectById(id);
        return user == null ? null : UserVO.fromEntity(user);
    }
}
