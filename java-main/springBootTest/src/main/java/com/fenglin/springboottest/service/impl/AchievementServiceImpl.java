package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.*;
import com.fenglin.springboottest.entity.UserAchievement;
import com.fenglin.springboottest.entity.UserGameStat;
import com.fenglin.springboottest.entity.UserStage;
import com.fenglin.springboottest.mapper.UserAchievementMapper;
import com.fenglin.springboottest.mapper.UserGameStatMapper;
import com.fenglin.springboottest.mapper.UserStageMapper;
import com.fenglin.springboottest.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 成就系统实现：把玩家当前成长快照与 AchievementConfig 目录逐条比对，
 * 持久化新解锁项，并产出带进度/NEW 角标的视图对象。
 */
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired private UserStageMapper userStageMapper;
    @Autowired private UserGameStatMapper userGameStatMapper;
    @Autowired private UserAchievementMapper userAchievementMapper;

    /** 解锁后多久内展示 NEW 角标 */
    private static final int NEW_WINDOW_HOURS = 24;

    @Override
    public List<AchievementVO> evaluate(Long userId) {
        return doEvaluate(userId, null);
    }

    @Override
    public List<AchievementVO> newlyUnlocked(Long userId) {
        List<AchievementVO> newly = new ArrayList<>();
        doEvaluate(userId, newly);
        return newly;
    }

    private List<AchievementVO> doEvaluate(Long userId, List<AchievementVO> newlyOut) {
        // 1. 构建成长快照
        UserStage st = userStageMapper.selectById(userId);
        UserGameStat stat = userGameStatMapper.selectById(userId);
        int clears = st == null ? 0 : (st.getClears() == null ? 0 : st.getClears());
        int exp = st == null ? 0 : (st.getExp() == null ? 0 : st.getExp());
        int bestStreak = st == null ? 0 : (st.getBestStreak() == null ? 0 : st.getBestStreak());
        int realmIndex = (st != null && st.getMaxRealm() != null)
                ? RealmStageConfig.indexOf(st.getMaxRealm()) : -1;
        int duelWins = stat == null ? 0 : (stat.getDuelWins() == null ? 0 : stat.getDuelWins());
        int quizWins = stat == null ? 0 : (stat.getQuizWins() == null ? 0 : stat.getQuizWins());
        int reactionBestMs = stat == null ? 0 : (stat.getReactionBestMs() == null ? 0 : stat.getReactionBestMs());

        Map<String, Integer> snap = new HashMap<>();
        snap.put(AchievementConfig.Metric.CLEARS.name(), clears);
        snap.put(AchievementConfig.Metric.EXP.name(), exp);
        snap.put(AchievementConfig.Metric.BEST_STREAK.name(), bestStreak);
        snap.put(AchievementConfig.Metric.REALM_INDEX.name(), realmIndex);
        snap.put(AchievementConfig.Metric.DUEL_WINS.name(), duelWins);
        snap.put(AchievementConfig.Metric.QUIZ_WINS.name(), quizWins);
        snap.put(AchievementConfig.Metric.REACTION_BEST_MS.name(), reactionBestMs);

        // 2. 已解锁记录
        List<UserAchievement> recs = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));
        Map<String, LocalDateTime> unlockedMap = new HashMap<>();
        for (UserAchievement r : recs) unlockedMap.put(r.getAchievementKey(), r.getUnlockedAt());

        LocalDateTime now = LocalDateTime.now();
        List<AchievementVO> out = new ArrayList<>();
        for (AchievementConfig.Def d : AchievementConfig.DEFS) {
            int v = snap.getOrDefault(d.metric.name(), 0);
            boolean ok = d.satisfied(v);
            LocalDateTime ua = unlockedMap.get(d.key);
            if (ok && ua == null) {
                userAchievementMapper.insert(new UserAchievement(userId, d.key));
                ua = now;
                if (newlyOut != null) {
                    // 标记：会在下方统一构建，这里仅记录 key 以便加入 newlyOut
                }
            }
            AchievementVO vo = buildVO(d, v, ok, ua, now);
            out.add(vo);
            if (newlyOut != null && ok && ua != null && ua.isAfter(now.minusHours(NEW_WINDOW_HOURS))) {
                // 仅当本次刚插入（ua==now）才计入 newlyOut
                if (ua.equals(now)) newlyOut.add(vo);
            }
        }
        return out;
    }

    private AchievementVO buildVO(AchievementConfig.Def d, int value, boolean unlocked,
                                  LocalDateTime unlockedAt, LocalDateTime now) {
        AchievementVO vo = new AchievementVO();
        vo.setKey(d.key);
        vo.setName(d.name);
        vo.setDesc(d.desc);
        vo.setIcon(d.icon);
        vo.setTier(d.tier.name());
        vo.setGoal(d.desc);
        vo.setUnlocked(unlocked);
        vo.setUnlockedAt(unlocked && unlockedAt != null ? unlockedAt.toString().replace('T', ' ').substring(0, 19) : null);
        vo.setNew(unlocked && unlockedAt != null && unlockedAt.isAfter(now.minusHours(NEW_WINDOW_HOURS)));
        vo.setProgress(buildProgress(d, value, unlocked));
        return vo;
    }

    private String buildProgress(AchievementConfig.Def d, int value, boolean unlocked) {
        if (unlocked) return "已达成";
        switch (d.metric) {
            case REACTION_BEST_MS:
                return value == 0 ? "暂无记录" : "最佳 " + value + "ms（需 ≤ " + d.threshold + "ms）";
            case REALM_INDEX: {
                int v = Math.max(value, 0);
                return "已突破至第 " + (v + 1) + " 境（需第 " + (d.threshold + 1) + " 境）";
            }
            default:
                return "已 " + value + " / " + d.threshold + " " + d.unit;
        }
    }
}
