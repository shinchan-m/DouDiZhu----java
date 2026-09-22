package com.itheima.ai;

import java.util.List;
import java.util.Map;

public final class GameSnapshot {
    public final int roundNo;
    public final String myPlayer;
    public final String myRole;
    public final List<String> myHand;
    public final String landlord;
    public final List<String> farmers;
    public final Map<String, Integer> handCounts;
    public final List<ActionRecord> recentActions;
    public final ActionRecord lastPlay;
    public final boolean mustFollow;
    public final boolean isMyLead;
    public final Map<String, Integer> scores;
    public final int multiplier;

    public GameSnapshot(int roundNo, String myPlayer, String myRole,
                        List<String> myHand, String landlord, List<String> farmers,
                        Map<String, Integer> handCounts,
                        List<ActionRecord> recentActions, ActionRecord lastPlay,
                        boolean mustFollow, boolean isMyLead,
                        Map<String, Integer> scores, int multiplier) {
        this.roundNo = roundNo;
        this.myPlayer = myPlayer;
        this.myRole = myRole;
        this.myHand = List.copyOf(myHand);
        this.landlord = landlord;
        this.farmers = List.copyOf(farmers);
        this.handCounts = Map.copyOf(handCounts);
        this.recentActions = List.copyOf(recentActions);
        this.lastPlay = lastPlay;
        this.mustFollow = mustFollow;
        this.isMyLead = isMyLead;
        this.scores = Map.copyOf(scores);
        this.multiplier = multiplier;
    }

    public static final class ActionRecord {
        public final String player;
        public final String action;
        public final List<String> cards;
        public final String type;

        public ActionRecord(String player, String action, List<String> cards, String type) {
            this.player = player;
            this.action = action;
            this.cards = List.copyOf(cards);
            this.type = type;
        }
    }
}
