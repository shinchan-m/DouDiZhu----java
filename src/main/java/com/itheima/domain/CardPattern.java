package com.itheima.domain;

import java.util.List;

/**
 * 一次出牌的牌型描述。mainValue 是参与比较的关键牌值。
 */
public record CardPattern(Type type, int mainValue, int cardCount, List<Integer> keyRanks) {
    public enum Type {
        SINGLE("单张"),
        PAIR("对子"),
        TRIPLE("三张"),
        TRIPLE_ONE("三带一"),
        TRIPLE_TWO("三带二"),
        STRAIGHT("顺子"),
        PAIR_STRAIGHT("连对"),
        PLANE("飞机"),
        PLANE_SINGLE("飞机带单"),
        PLANE_PAIR("飞机带对"),
        BOMB("炸弹"),
        ROCKET("王炸");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public CardPattern {
        keyRanks = List.copyOf(keyRanks);
    }

    public boolean isBomb() {
        return type == Type.BOMB || type == Type.ROCKET;
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }
}
