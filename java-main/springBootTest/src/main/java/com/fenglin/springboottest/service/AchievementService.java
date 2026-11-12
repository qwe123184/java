package com.fenglin.springboottest.service;

import com.fenglin.springboottest.common.AchievementVO;

import java.util.List;

/**
 * 成就系统：基于玩家成长数据（闯关 / 对决 / 答题 / 反应）自动评估并持久化解锁成就。
 */
public interface AchievementService {

    /**
     * 评估用户成就并持久化新解锁项，返回全部成就（含解锁状态与进度）。
     */
    List<AchievementVO> evaluate(Long userId);

    /**
     * 触发评估并返回本次新解锁的成就（用于闯关/对局结束时的即时奖励提示）。
     */
    List<AchievementVO> newlyUnlocked(Long userId);
}
