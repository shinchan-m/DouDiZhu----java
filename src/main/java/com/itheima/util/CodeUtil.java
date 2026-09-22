package com.itheima.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CodeUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private CodeUtil() {
    }

    public static String getCode() {
        List<Character> chars = new ArrayList<>(5);
        for (int i = 0; i < 4; i++) {
            chars.add(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }
        chars.add((char) ('0' + RANDOM.nextInt(10)));
        Collections.shuffle(chars, RANDOM);

        StringBuilder result = new StringBuilder(5);
        for (char c : chars) {
            result.append(c);
        }
        return result.toString();
    }
}
