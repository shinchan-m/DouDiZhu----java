package com.itheima.ai;

import com.itheima.domain.CardPattern;
import com.itheima.domain.Poker;

import java.util.List;

public final class AIAction {
    public enum Source {
        MODEL,
        REPAIR,
        FALLBACK
    }

    private final boolean pass;
    private final List<Poker> cards;
    private final CardPattern pattern;
    private final Source source;
    private final String error;
    private final boolean retryable;
    private String userMessage;

    private AIAction(boolean pass, List<Poker> cards, CardPattern pattern,
                     Source source, String error, boolean retryable) {
        this.pass = pass;
        this.cards = cards == null ? List.of() : List.copyOf(cards);
        this.pattern = pattern;
        this.source = source;
        this.error = error;
        this.retryable = retryable;
    }

    public static AIAction play(List<Poker> cards, CardPattern pattern, Source source) {
        return new AIAction(false, cards, pattern, source, null, false);
    }

    public static AIAction pass(Source source) {
        return new AIAction(true, List.of(), null, source, null, false);
    }

    public static AIAction invalid(String error, boolean retryable) {
        return new AIAction(false, List.of(), null, Source.FALLBACK, error, retryable);
    }

    public static AIAction fallback(String error) {
        return new AIAction(false, List.of(), null, Source.FALLBACK, error, false);
    }

    public static AIAction fallbackPlay(List<Poker> cards, CardPattern pattern) {
        return new AIAction(false, cards, pattern, Source.FALLBACK, null, false);
    }

    public static AIAction fallbackPass() {
        return new AIAction(true, List.of(), null, Source.FALLBACK, null, false);
    }

    public boolean isPass() {
        return pass;
    }

    public List<Poker> getCards() {
        return cards;
    }

    public CardPattern getPattern() {
        return pattern;
    }

    public Source getSource() {
        return source;
    }

    public String getError() {
        return error;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public boolean isUsable() {
        return source != Source.FALLBACK || (pass && error == null);
    }

    public String getUserMessage() {
        return userMessage;
    }

    public AIAction withUserMessage(String userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public String toNormalizedJson() {
        if (pass) {
            return "{\"action\":\"pass\"}";
        }
        StringBuilder builder = new StringBuilder("{\"action\":\"play\",\"cards\":[");
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"').append(cards.get(i).getRank()).append('"');
        }
        return builder.append("]}").toString();
    }
}
