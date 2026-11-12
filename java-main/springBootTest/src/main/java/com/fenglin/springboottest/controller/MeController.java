package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.*;
import com.fenglin.springboottest.service.AchievementService;
import com.fenglin.springboottest.service.GameService;
import com.fenglin.springboottest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 个人中心聚合接口（资料 / 试炼 / 战绩 / 等级 / 成就）。
 * 位于 /api/me/**，需携带有效访问令牌（由 AuthInterceptor 校验并注入 userId）。
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    @Autowired
    private UserService userService;
    @Autowired
    private GameService gameService;
    @Autowired
    private AchievementService achievementService;

    /**
     * 成就墙：评估并持久化用户成就，返回全部成就（含解锁状态、进度与 NEW 角标）。
     */
    @GetMapping("/achievements")
    public Result<List<AchievementVO>> achievements(@RequestAttribute Long userId) {
        return Result.success(achievementService.evaluate(userId));
    }

    /**
     * 个人中心一站式总览：一次返回资料 / 试炼 / 战绩 / 等级 / 成就，减少前端多次往返。
     */
    @GetMapping("/overview")
    public Result<MeOverviewVO> overview(@RequestAttribute Long userId) {
        MeOverviewVO vo = new MeOverviewVO();
        vo.setUser(userService.getUserById(userId));

        Result<StageProgressVO> stageRes = gameService.stageProgress(userId);
        StageProgressVO stage = (stageRes != null && stageRes.getData() != null) ? stageRes.getData() : null;
        vo.setStage(stage);

        vo.setStat(gameService.getGameStat(userId));

        int exp = stage != null ? stage.getExp() : 0;
        int level = LevelConfig.levelOf(exp);
        vo.setLevel(level);
        vo.setLevelTitle(LevelConfig.titleOf(level));
        vo.setLevelPercent(LevelConfig.levelPercent(exp));
        vo.setExpToNext(LevelConfig.expToNext(exp));

        List<AchievementVO> ach = achievementService.evaluate(userId);
        vo.setAchievements(ach);
        vo.setUnlockedCount((int) ach.stream().filter(AchievementVO::isUnlocked).count());
        vo.setTotalCount(ach.size());

        return Result.success(vo);
    }
}
