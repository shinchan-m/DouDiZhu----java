package com.itheima.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 牌桌玩家信息，保存座位、身份和当前手牌。
 */
public class PlayerInfo {
    private final int seat;
    private final String name;
    private final boolean human;
    private final List<Poker> hand = new ArrayList<>();
    private boolean landlord;

    public PlayerInfo(int seat, String name, boolean human) {
        this.seat = seat;
        this.name = name;
        this.human = human;
    }

    public int getSeat() {
        return seat;
    }

    public String getName() {
        return name;
    }

    public boolean isHuman() {
        return human;
    }

    public List<Poker> getHand() {
        return hand;
    }

    public boolean isLandlord() {
        return landlord;
    }

    public void setLandlord(boolean landlord) {
        this.landlord = landlord;
    }

    public String getRoleName() {
        return landlord ? "地主" : "农民";
    }
}
