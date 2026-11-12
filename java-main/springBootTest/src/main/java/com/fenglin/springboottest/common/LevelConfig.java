package com.fenglin.springboottest.common;

/**
 * 守夜人等级体系：把长期闲置的累计经验（exp）转化为可见的成长反馈。
 * 每级固定需要 100 经验；等级与称号随境界主题递增。
 */
public final class LevelConfig {

    /** 每级所需经验 */
    private static final int PER_LEVEL = 100;

    /** 等级称号（下标 = level - 1）；超出则用通用称号 */
    private static final String[] TITLES = {
            "凡人", "守夜学徒", "守夜人", "资深守夜人", "执灯者",
            "破晓者", "弑神者", "神域裁决", "弑神之王"
    };

    private LevelConfig() {
    }

    /** 当前等级（1 起） */
    public static int levelOf(int exp) {
        if (exp < 0) exp = 0;
        return exp / PER_LEVEL + 1;
    }

    /** 升到下一级所需的累计经验门槛 */
    public static int expForLevel(int level) {
        if (level < 1) level = 1;
        return (level - 1) * PER_LEVEL;
    }

    /** 当前等级内的进度百分比 0~99 */
    public static int levelPercent(int exp) {
        int level = levelOf(exp);
        int into = exp - expForLevel(level);
        return Math.max(0, Math.min(99, into));
    }

    /** 距离下一级还差多少经验（已满级时返回 0） */
    public static int expToNext(int exp) {
        int level = levelOf(exp);
        return Math.max(0, expForLevel(level + 1) - exp);
    }

    /** 等级称号 */
    public static String titleOf(int level) {
        if (level < 1) level = 1;
        if (level - 1 < TITLES.length) return TITLES[level - 1];
        return "神明使者 Lv" + level;
    }
}
