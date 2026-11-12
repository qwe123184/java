package com.fenglin.springboottest.common;

import java.util.Arrays;
import java.util.List;

/**
 * 斩神 · 守夜人成就目录（静态配置，不可序列化，仅在服务端使用）。
 * 每个成就绑定一个成长指标（metric）、达成阈值（threshold）与比较模式（mode）。
 * 评估时由 AchievementServiceImpl 基于玩家当前快照（StatSnapshot）逐条判定。
 */
public final class AchievementConfig {

    public enum Tier { COMMON, RARE, EPIC, LEGEND }

    /** 成长指标，对应 StatSnapshot 的字段键 */
    public enum Metric {
        CLEARS,          // 累计突破次数
        EXP,             // 累计经验
        BEST_STREAK,     // 单次闯关最高连对
        REALM_INDEX,     // 已突破到的最高境界下标（-1 表示尚无一境）
        DUEL_WINS,       // 对决获胜次数
        QUIZ_WINS,       // 答题获胜次数
        REACTION_BEST_MS // 反应试炼最佳毫秒（0 表示尚无记录）
    }

    /** GE：当前值 ≥ 阈值即达成；LE：当前值 ≤ 阈值即达成（仅反应试炼使用） */
    public enum Mode { GE, LE }

    public static class Def {
        public final String key;
        public final String name;
        public final String desc;
        public final String icon;
        public final Tier tier;
        public final Metric metric;
        public final Mode mode;
        public final int threshold;
        /** 进度文案中的目标单位，如 “次 / 境 / 经验 / ms” */
        public final String unit;

        public Def(String key, String name, String desc, String icon, Tier tier,
                   Metric metric, Mode mode, int threshold, String unit) {
            this.key = key;
            this.name = name;
            this.desc = desc;
            this.icon = icon;
            this.tier = tier;
            this.metric = metric;
            this.mode = mode;
            this.threshold = threshold;
            this.unit = unit;
        }

        public boolean satisfied(int value) {
            if (mode == Mode.LE) {
                // 反应试炼：需先有记录（>0）且不超过阈值
                return value > 0 && value <= threshold;
            }
            return value >= threshold;
        }
    }

    /** 成就目录（顺序即展示顺序，按主题分组排列） */
    public static final List<Def> DEFS = Arrays.asList(
            // —— 境界突破 ——
            new Def("first_break", "初窥门径", "首次突破任意境界", "🌱", Tier.COMMON,
                    Metric.CLEARS, Mode.GE, 1, "次"),
            new Def("clears5", "五境连破", "累计突破 5 次", "⚔️", Tier.RARE,
                    Metric.CLEARS, Mode.GE, 5, "次"),
            new Def("streak3", "连星之证", "单次闯关连对 3 题", "✨", Tier.COMMON,
                    Metric.BEST_STREAK, Mode.GE, 3, "连对"),
            new Def("streak6", "不败神心", "单次闯关连对 6 题", "🔥", Tier.RARE,
                    Metric.BEST_STREAK, Mode.GE, 6, "连对"),
            new Def("realm_chi", "引灵入池", "突破至「池境」", "💧", Tier.COMMON,
                    Metric.REALM_INDEX, Mode.GE, 1, "境"),
            new Def("realm_hai", "瀚海之心", "突破至「海境」", "🌊", Tier.RARE,
                    Metric.REALM_INDEX, Mode.GE, 4, "境"),
            new Def("realm_shenming", "登临神明", "突破至「神明」之境", "👑", Tier.LEGEND,
                    Metric.REALM_INDEX, Mode.GE, 7, "境"),

            // —— 成长 ——
            new Def("exp500", "千锤百炼", "累计经验达 500", "💎", Tier.COMMON,
                    Metric.EXP, Mode.GE, 500, "经验"),
            new Def("exp2000", "万法归源", "累计经验达 2000", "🌟", Tier.EPIC,
                    Metric.EXP, Mode.GE, 2000, "经验"),

            // —— 神域对决 ——
            new Def("duel1", "初战告捷", "首次对决获胜", "🗡️", Tier.COMMON,
                    Metric.DUEL_WINS, Mode.GE, 1, "胜"),
            new Def("duel10", "对决宗师", "对决获胜 10 次", "🏆", Tier.EPIC,
                    Metric.DUEL_WINS, Mode.GE, 10, "胜"),

            // —— 禁墟答题 ——
            new Def("quiz1", "学识初显", "首次答题获胜", "📜", Tier.COMMON,
                    Metric.QUIZ_WINS, Mode.GE, 1, "胜"),
            new Def("quiz10", "禁墟通儒", "答题获胜 10 次", "🧠", Tier.EPIC,
                    Metric.QUIZ_WINS, Mode.GE, 10, "胜"),

            // —— 反应试炼 ——
            new Def("reaction300", "疾风之眼", "反应试炼最佳 ≤ 300ms", "⚡", Tier.RARE,
                    Metric.REACTION_BEST_MS, Mode.LE, 300, "ms")
    );

    private AchievementConfig() {
    }
}
