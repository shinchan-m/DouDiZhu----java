package com.itheima.domain;

import com.itheima.util.ResourceUtil;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 扑克牌对象，同时负责显示牌面、处理点击和按牌值排序。
 */
public class Poker extends JLabel implements MouseListener, Comparable<Poker> {
    public static final String[] SUITS = {"♠", "♥", "♣", "♦"};
    public static final String[] RANKS = {
            "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2"
    };

    private final String rank;
    private final String suit;
    private final int suitIndex;
    private final int rankIndex;
    private final int value;
    private boolean faceUp;
    private boolean selected;
    private boolean selectable;
    private Consumer<Poker> clickHandler;

    public Poker(String suit, String rank, int suitIndex, int rankIndex, int value) {
        this.suit = suit;
        this.rank = rank;
        this.suitIndex = suitIndex;
        this.rankIndex = rankIndex;
        this.value = value;
        setSize(71, 96);
        setOpaque(false);
        addMouseListener(this);
        refreshIcon();
    }

    public static List<Poker> createDeck() {
        List<Poker> deck = new ArrayList<>(54);
        for (int suit = 0; suit < SUITS.length; suit++) {
            for (int rank = 0; rank < RANKS.length; rank++) {
                String rankName = RANKS[rank];
                deck.add(new Poker(SUITS[suit], rankName, suit + 1,
                        imageIndexForRank(rankName), rankValue(rankName)));
            }
        }
        deck.add(new Poker("小王", "小王", 5, 1, 16));
        deck.add(new Poker("大王", "大王", 5, 2, 17));
        Collections.shuffle(deck);
        return deck;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        refreshIcon();
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public void setClickHandler(Consumer<Poker> clickHandler) {
        this.clickHandler = clickHandler;
    }

    public String getRank() {
        return rank;
    }

    public String getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public String getFaceImageName() {
        if ("小王".equals(rank)) {
            return "5-1.png";
        }
        if ("大王".equals(rank)) {
            return "5-2.png";
        }
        return suitIndex + "-" + rankIndex + ".png";
    }

    public static int rankValue(String rank) {
        return switch (rank) {
            case "3", "4", "5", "6", "7", "8", "9", "10" -> Integer.parseInt(rank);
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            case "A" -> 14;
            case "2" -> 15;
            case "小王" -> 16;
            case "大王" -> 17;
            default -> throw new IllegalArgumentException("未知牌面: " + rank);
        };
    }

    public static int imageIndexForRank(String rank) {
        return switch (rank) {
            case "A" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            case "8" -> 8;
            case "9" -> 9;
            case "10" -> 10;
            case "J" -> 11;
            case "Q" -> 12;
            case "K" -> 13;
            default -> throw new IllegalArgumentException("未知普通牌面: " + rank);
        };
    }

    public void refreshIcon() {
        if (faceUp) {
            setIcon(ResourceUtil.loadPokerIcon(getFaceImageName()));
        } else {
            setIcon(ResourceUtil.loadPokerIcon("rear.png"));
        }
    }

    @Override
    public int compareTo(Poker other) {
        int valueCompare = Integer.compare(value, other.value);
        if (valueCompare != 0) {
            return valueCompare;
        }
        return Integer.compare(suitIndex, other.suitIndex);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (selectable && faceUp && clickHandler != null) {
            clickHandler.accept(this);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public String toString() {
        return suit + rank;
    }
}
