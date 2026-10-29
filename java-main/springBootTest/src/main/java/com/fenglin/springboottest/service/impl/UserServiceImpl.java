package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenglin.springboottest.common.ChangePasswordRequest;
import com.fenglin.springboottest.common.LoginRequest;
import com.fenglin.springboottest.common.LoginVO;
import com.fenglin.springboottest.common.ProfileUpdateRequest;
import com.fenglin.springboottest.common.RegisterRequest;
import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.common.UserVO;
import com.fenglin.springboottest.config.JwtUtil;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    /** 权限常量：0 管理员（执政官）/ 1 默认（普通守夜人） */
    private static final int PERMISSION_ADMIN = 0;
    private static final int PERMISSION_DEFAULT = 1;

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
        // 密码以明文存储（按需求不加密）
        user.setPassword(password);
        user.setEmail(StringUtils.hasText(req.getEmail()) ? req.getEmail().trim() : null);
        user.setRealm(realm);
        // 注册用户默认权限（普通守夜人，permission=1）
        user.setPermission(PERMISSION_DEFAULT);
        user.setRole("USER");
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
        // 明文比对（按需求密码不加密）
        if (!password.equals(user.getPassword())) {
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
    public Result<LoginVO> adminLogin(LoginRequest req) {
        if (req == null || !StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return Result.fail("请先留下执政官代号与管理密钥。");
        }
        String username = req.getUsername().trim();
        String password = req.getPassword().trim();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.fail("执政官代号未登记，禁止入内。");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            return Result.fail("该执政官已被诸神封印，禁止进入后台。");
        }
        // 仅权限为管理员（permission=0）的账号可进入后台管理系统
        if (user.getPermission() == null || user.getPermission() != PERMISSION_ADMIN) {
            return Result.fail("你并非神域执政官，无权进入后台管理系统。");
        }
        // 明文比对（按需求密码不加密）
        if (!password.equals(user.getPassword())) {
            return Result.fail("管理密钥有误，禁入后台。");
        }

        // 签发双令牌（access + refresh）
        String access = jwtUtil.generateAccessToken(user.getId(), username);
        String refresh = jwtUtil.generateRefreshToken(user.getId(), username);

        LoginVO vo = new LoginVO(access, UserVO.fromEntity(user));
        vo.setRefreshToken(refresh);
        return Result.success("执政官「" + username + "」已就位，凡尘神域尽在掌握。", vo);
    }

    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        return user != null && user.getPermission() != null && user.getPermission() == PERMISSION_ADMIN;
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

    @Override
    public Result<UserVO> updateProfile(Long userId, ProfileUpdateRequest req) {
        if (userId == null || req == null) {
            return Result.fail("未指定守夜人或资料为空。");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("守夜人信息已消散。");
        }
        // 仅允许修改自身展示字段（账号/密码/权限/状态不可经此接口变更）
        if (req.getNickname() != null) user.setNickname(req.getNickname().trim());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar().trim());
        if (req.getPhone() != null) user.setPhone(req.getPhone().trim());
        if (req.getBio() != null) user.setBio(req.getBio().trim());
        if (req.getEmail() != null) user.setEmail(req.getEmail().trim());
        if (req.getRealm() != null) {
            String realm = req.getRealm().trim();
            user.setRealm(!realm.isEmpty() && REALMS.contains(realm) ? realm : user.getRealm());
        }
        userMapper.updateById(user);
        return Result.success("守夜人资料已更新。", UserVO.fromEntity(user));
    }

    @Override
    public Result<Void> changePassword(Long userId, ChangePasswordRequest req) {
        if (userId == null || req == null) {
            return Result.fail("未指定守夜人或请求为空。");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("守夜人信息已消散。");
        }
        // 明文比对原密码（按需求密码不加密）
        if (!req.getOldPassword().equals(user.getPassword())) {
            return Result.fail("原禁墟密钥有误，横刀之誓不可违。");
        }
        // 明文写入新密码
        user.setPassword(req.getNewPassword());
        userMapper.updateById(user);
        return Result.success("禁墟密钥已重塑，神域重归安宁。", (Void) null);
    }

    @Override
    public Result<IPage<UserVO>> listUsers(String keyword, Integer permission, Integer status, long page, long size) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (permission != null) {
            qw.eq(User::getPermission, permission);
        }
        if (status != null) {
            qw.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            qw.and(w -> w.like(User::getUsername, k)
                    .or().like(User::getNickname, k)
                    .or().like(User::getEmail, k));
        }
        qw.orderByDesc(User::getId);
        Page<User> pg = new Page<>(page, size);
        IPage<User> res = userMapper.selectPage(pg, qw);
        IPage<UserVO> voPage = res.convert(UserVO::fromEntity);
        return Result.success(voPage);
    }

    @Override
    public Result<UserVO> setUserStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return Result.fail("未指定账号或状态。");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("该守夜人信息已消散。");
        }
        user.setStatus(status == 1 ? 1 : 0);
        userMapper.updateById(user);
        return Result.success(status == 1 ? "该守夜人已被诸神封印。" : "该守夜人已重获自由。", UserVO.fromEntity(user));
    }

    @Override
    public Result<UserVO> setUserPermission(Long id, Integer permission) {
        if (id == null || permission == null) {
            return Result.fail("未指定账号或权限。");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("该守夜人信息已消散。");
        }
        user.setPermission(permission == PERMISSION_ADMIN ? PERMISSION_ADMIN : PERMISSION_DEFAULT);
        userMapper.updateById(user);
        return Result.success(permission == PERMISSION_ADMIN ? "已擢升为神域执政官。" : "已降为普通守夜人。", UserVO.fromEntity(user));
    }

    @Override
    public Result<Void> deleteUser(Long id) {
        if (id == null) {
            return Result.fail("未指定账号。");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("该守夜人信息已消散。");
        }
        userMapper.deleteById(id);
        return Result.success("守夜人「" + user.getUsername() + "」已从神域抹除。", (Void) null);
    }
}
