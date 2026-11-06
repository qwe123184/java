package com.fenglin.springboottest.common;

import java.util.Arrays;
import java.util.List;

/**
 * 斩神世界观 · 境界突破链配置（单人闯关「禁墟试炼」的数据源）。
 * 顺序即闯关顺序：凡尘 → 池境 → 盏境 → 川境 → 海境 → 无量 → 克莱因 → 神明。
 * 每关从禁墟题库（t_quiz）随机抽样 questions 道题，答对达到 passThreshold 即突破到下一境界。
 */
public final class RealmStageConfig {

    public static class Realm {
        public final String key;
        public final String name;
        public final int questions;
        public final String desc;

        public Realm(String key, String name, int questions, String desc) {
            this.key = key;
            this.name = name;
            this.questions = questions;
            this.desc = desc;
        }
    }

    /** 从低到高的境界链；顺序不可乱，闯关与进度计算都依赖下标。 */
    public static final List<Realm> REALMS = Arrays.asList(
            new Realm("fanchen",  "凡尘", 3, "尚未觉醒，于万家灯火前立誓：以凡尘之躯，横刀向渊。"),
            new Realm("chi",      "池境", 5, "引灵入池，初窥禁墟之门，迷雾中第一缕光将你唤醒。"),
            new Realm("zhan",     "盏境", 5, "一盏明灯照破迷雾，守夜人之名于黯夜初显。"),
            new Realm("chuan",    "川境", 5, "川流不息，神力如江河奔涌，可载舟亦可覆舟。"),
            new Realm("hai",      "海境", 6, "心如瀚海，可纳诸天之渊，于无声处听惊雷。"),
            new Realm("wuliang",  "无量", 6, "无量之境，掌中即有乾坤，一念可镇一方迷雾。"),
            new Realm("kelain",   "克莱因", 7, "触及克莱因的边界，神性初醒，旧神的低语在耳边回响。"),
            new Realm("shenming", "神明", 8, "于黯夜终临之时，立于万万人前——你已是自己的神明。")
    );

    public static Realm byKey(String key) {
        if (key == null) return null;
        for (Realm r : REALMS) if (r.key.equalsIgnoreCase(key)) return r;
        return null;
    }

    public static int indexOf(String key) {
        if (key == null) return -1;
        for (int i = 0; i < REALMS.size(); i++) if (REALMS.get(i).key.equalsIgnoreCase(key)) return i;
        return -1;
    }

    public static Realm next(String key) {
        int i = indexOf(key);
        if (i < 0 || i + 1 >= REALMS.size()) return null;
        return REALMS.get(i + 1);
    }

    /** 突破所需最少答对题数：题数的 60境（向上取整）即 60%。 */
    public static int passThreshold(int questions) {
        return (int) Math.ceil(questions * 0.6);
    }

    public static String displayName(String key) {
        Realm r = byKey(key);
        return r == null ? key : r.name;
    }
}
