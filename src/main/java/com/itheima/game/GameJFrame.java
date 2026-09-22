package com.itheima.game;

import com.itheima.ai.AIAction;
import com.itheima.ai.AIPlayer;
import com.itheima.ai.GameSnapshot;
import com.itheima.domain.CardPattern;
import com.itheima.domain.PlayerInfo;
import com.itheima.domain.Poker;
import com.itheima.rules.CardRules;
import com.itheima.util.ResourceUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 游戏主窗口，负责牌桌显示、回合状态、倒计时、AI 调用和胜负结算。
 */
public class GameJFrame extends JFrame {
    private enum Phase {
        DEALING, BIDDING, PLAYING, FINISHED
    }

    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 720;
    private static final int CARD_WIDTH = 71;
    private static final int CARD_HEIGHT = 96;
    private static final int HUMAN_SEAT = 1;

    private final String loginUsername;
    private final Difficulty difficulty;
    private final PlayerInfo[] players = new PlayerInfo[3];
    private final AIPlayer[] aiPlayers = new AIPlayer[3];
    private final List<Poker> bottomCards = new ArrayList<>();
    private final List<GameSnapshot.ActionRecord> recentActions = new ArrayList<>();
    private final Deque<String> publicHistory = new ArrayDeque<>();
    private final Map<String, Map<String, Integer>> playedRanks = new HashMap<>();
    private final List<Component> centralCardComponents = new ArrayList<>();

    // 底牌区和中央公共出牌区。
    private final JPanel bottomArea = new JPanel(null);
    private final JPanel playArea = new JPanel(null);
    private final JLabel centralTitle = new JLabel("", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel[] playerLabels = new JLabel[3];
    private final JTextField[] timeFields = new JTextField[3];
    private final JButton robButton = new JButton("抢地主");
    private final JButton noRobButton = new JButton("不抢");
    private final JButton playButton = new JButton("出牌");
    private final JButton passButton = new JButton("不要");
    private final JButton replayButton = new JButton("重玩");

    private Phase phase = Phase.DEALING;
    private int currentPlayer = HUMAN_SEAT;
    private int landlordSeat = -1;
    private int decisionNo;
    private int multiplier = 1;
    private int passCount;
    private int lastPlayer = -1;
    private int firstBidSeat;
    private int bidTurns;
    private Boolean[] robChoices = new Boolean[3];
    private CardPattern lastPattern;
    private List<Poker> lastCards = List.of();
    private final int[] scores = new int[3];
    private Timer countdownTimer;
    private int remainingSeconds;
    private Runnable timeoutAction;
    private long turnToken;
    private long roundToken;
    private boolean gameOver;

    public GameJFrame(String loginUsername) {
        this(loginUsername, Difficulty.MEDIUM);
    }

    public GameJFrame(String loginUsername, Difficulty difficulty) {
        this.loginUsername = loginUsername;
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
        players[0] = new PlayerInfo(0, "玩家2", false);
        players[1] = new PlayerInfo(1, loginUsername, true);
        players[2] = new PlayerInfo(2, "玩家3", false);

        initFrame();
        initView();
        setVisible(true);
        SwingUtilities.invokeLater(this::startNewRound);
    }

    private void initFrame() {
        setTitle("斗地主");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(18, 92, 52));
        setIconImage(ResourceUtil.loadPokerIcon("dizhu.png").getImage());
    }

    private void initView() {
        Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        playerLabels[0] = createInfoLabel(20, 130, 150, labelFont);
        playerLabels[1] = createInfoLabel(420, 650, 160, labelFont);
        playerLabels[2] = createInfoLabel(830, 130, 150, labelFont);

        statusLabel.setBounds(285, 205, 430, 30);
        statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        add(statusLabel);

        scoreLabel.setBounds(285, 675, 430, 25);
        scoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        scoreLabel.setForeground(new Color(255, 230, 120));
        add(scoreLabel);

        JLabel bottomTitle = new JLabel("底牌", SwingConstants.CENTER);
        bottomTitle.setFont(labelFont);
        bottomTitle.setForeground(Color.WHITE);
        bottomTitle.setBounds(450, 8, 100, 20);
        add(bottomTitle);

        bottomArea.setOpaque(false);
        bottomArea.setBounds(340, 28, 320, 110);
        add(bottomArea);

        playArea.setOpaque(true);
        playArea.setBackground(new Color(12, 70, 38));
        playArea.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 200), 1));
        playArea.setBounds(330, 250, 340, 185);
        centralTitle.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        centralTitle.setForeground(new Color(255, 235, 150));
        centralTitle.setBounds(0, 2, 340, 24);
        playArea.add(centralTitle);
        add(playArea);

        initButtons();
        initTimeFields(labelFont);
        updatePlayerLabels();
        updateScoreLabel();
    }

    private JLabel createInfoLabel(int x, int y, int width, Font font) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setBounds(x, y, width, 24);
        label.setFont(font);
        label.setForeground(Color.WHITE);
        add(label);
        return label;
    }

    private void initButtons() {
        Font buttonFont = new Font("Microsoft YaHei", Font.BOLD, 15);

        replayButton.setBounds(18, 20, 90, 32);
        replayButton.setFont(buttonFont);
        replayButton.addActionListener(e -> replayGame());
        add(replayButton);

        robButton.setBounds(405, 455, 90, 34);
        noRobButton.setBounds(505, 455, 90, 34);
        playButton.setBounds(405, 455, 90, 34);
        passButton.setBounds(505, 455, 90, 34);

        JButton[] buttons = {robButton, noRobButton, playButton, passButton};
        for (JButton button : buttons) {
            button.setFont(buttonFont);
            button.setVisible(false);
            add(button);
        }

        robButton.addActionListener(e -> recordBid(true));
        noRobButton.addActionListener(e -> recordBid(false));
        playButton.addActionListener(e -> humanPlay());
        passButton.addActionListener(e -> humanPass());
    }

    private void initTimeFields(Font font) {
        int[][] bounds = {{180, 160, 130, 24}, {760, 500, 140, 24}, {690, 160, 130, 24}};
        for (int i = 0; i < timeFields.length; i++) {
            JTextField field = new JTextField();
            field.setEditable(false);
            field.setHorizontalAlignment(JTextField.CENTER);
            field.setFont(font);
            field.setForeground(new Color(230, 40, 40));
            field.setOpaque(false);
            field.setBorder(null);
            field.setBounds(bounds[i][0], bounds[i][1], bounds[i][2], bounds[i][3]);
            field.setVisible(true);
            add(field);
            timeFields[i] = field;
        }
    }

    /**
     * 游戏中随时重玩当前难度。
     */
    private void replayGame() {
        int option = JOptionPane.showConfirmDialog(this,
                "确定重新开始本局吗？", "重玩", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            startNewRound();
        }
    }

    /**
     * 重置并开始一局：准备牌、发牌、动画和抢地主。
     */
    private void startNewRound() {
        long currentRoundToken = ++roundToken;
        turnToken++;
        stopCountdown();
        removeAllCards();
        phase = Phase.DEALING;
        gameOver = false;
        decisionNo = 0;
        multiplier = 1;
        lastPattern = null;
        lastCards = List.of();
        lastPlayer = -1;
        passCount = 0;
        landlordSeat = -1;
        currentPlayer = HUMAN_SEAT;
        recentActions.clear();
        publicHistory.clear();
        playedRanks.clear();
        bottomCards.clear();
        centralTitle.setText("");
        centralCardComponents.clear();
        hideActionButtons();
        updateScoreLabel();

        for (int seat = 0; seat < players.length; seat++) {
            PlayerInfo player = players[seat];
            player.getHand().clear();
            player.setLandlord(false);
            playedRanks.put(player.getName(), new LinkedHashMap<>());
            if (!player.isHuman()) {
                aiPlayers[seat] = new AIPlayer(player.getName(), difficulty);
            }
        }
        updatePlayerLabels();

        List<Poker> deck = Poker.createDeck();
        for (int i = 0; i < deck.size(); i++) {
            Poker poker = deck.get(i);
            poker.setFaceUp(false);
            poker.setSelectable(false);
            poker.setSelected(false);
            poker.setClickHandler(this::toggleHumanCard);
            poker.setLocation(465, 300);
            poker.setVisible(true);
            add(poker);
            if (i < 3) {
                bottomCards.add(poker);
            } else {
                int seat = (i - 3) % 3;
                players[seat].getHand().add(poker);
            }
        }
        getContentPane().setComponentZOrder(playArea, getContentPane().getComponentCount() - 1);
        dealCards(deck, currentRoundToken);
    }

    private void dealCards(List<Poker> deck, long currentRoundToken) {
        statusLabel.setText("正在发牌...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(250);
                Point deckPoint = new Point(465, 300);
                for (Poker poker : deck) {
                    Point target = dealTarget(poker);
                    Common.move(poker, deckPoint, target);
                }
                return null;
            }

            @Override
            protected void done() {
                if (currentRoundToken != roundToken) {
                    return;
                }
                for (PlayerInfo player : players) {
                    Collections.sort(player.getHand());
                }
                positionAllHands();
                startBidding();
            }
        }.execute();
    }

    private Point dealTarget(Poker poker) {
        if (bottomCards.contains(poker)) {
            int index = bottomCards.indexOf(poker);
            return new Point(350 + index * 95, 42);
        }
        for (int seat = 0; seat < players.length; seat++) {
            List<Poker> hand = players[seat].getHand();
            int index = hand.indexOf(poker);
            if (index >= 0) {
                if (seat == HUMAN_SEAT) {
                    return new Point(horizontalStart(hand.size()) + index * 22, 550);
                }
                if (seat == 0) {
                    return new Point(18, verticalStart(hand.size()) + index * 15);
                }
                return new Point(900, verticalStart(hand.size()) + index * 15);
            }
        }
        return new Point(465, 300);
    }

    /**
     * 从随机座位开始，依次询问三名玩家是否抢地主。
     */
    private void startBidding() {
        phase = Phase.BIDDING;
        robChoices = new Boolean[3];
        bidTurns = 0;
        firstBidSeat = ThreadLocalRandom.current().nextInt(3);
        currentPlayer = firstBidSeat;
        updatePlayerLabels();
        startNextBidTurn();
    }

    private void startNextBidTurn() {
        if (phase != Phase.BIDDING) {
            return;
        }
        stopCountdown();
        hideActionButtons();
        if (bidTurns >= 3) {
            finishBidding();
            return;
        }
        currentPlayer = (firstBidSeat + bidTurns) % 3;
        statusLabel.setText(players[currentPlayer].getName() + " 正在决定是否抢地主");
        if (currentPlayer == HUMAN_SEAT) {
            robButton.setVisible(true);
            noRobButton.setVisible(true);
            final long token = ++turnToken;
            startCountdown(30, () -> {
                if (token == turnToken && phase == Phase.BIDDING) {
                    recordBid(false);
                }
            });
        } else {
            final long token = ++turnToken;
            startCountdown(30, () -> {
                if (token == turnToken && phase == Phase.BIDDING) {
                    recordBid(aiBidDecision(currentPlayer));
                }
            });
            Timer delay = new Timer(850, e -> {
                if (token == turnToken && phase == Phase.BIDDING) {
                    recordBid(aiBidDecision(currentPlayer));
                }
            });
            delay.setRepeats(false);
            delay.start();
        }
    }

    private boolean aiBidDecision(int seat) {
        int strength = 0;
        for (Poker poker : players[seat].getHand()) {
            if (poker.getValue() == 16 || poker.getValue() == 17) {
                strength += poker.getValue() == 17 ? 5 : 3;
            } else if (poker.getValue() == 15) {
                strength += 2;
            } else if (poker.getValue() >= 13) {
                strength++;
            }
        }
        return switch (difficulty) {
            case EASY -> strength >= 8 || ThreadLocalRandom.current().nextDouble() < 0.10;
            case MEDIUM -> strength >= 5 || ThreadLocalRandom.current().nextDouble() < 0.28;
            case HARD -> strength >= 3 || ThreadLocalRandom.current().nextDouble() < 0.45;
        };
    }

    private void recordBid(boolean rob) {
        if (phase != Phase.BIDDING || robChoices[currentPlayer] != null) {
            return;
        }
        stopCountdown();
        hideActionButtons();
        robChoices[currentPlayer] = rob;
        bidTurns++;
        publicHistory.addLast(players[currentPlayer].getName() + (rob ? " 抢地主" : " 不抢"));
        statusLabel.setText(players[currentPlayer].getName() + (rob ? " 抢地主" : " 不抢"));
        Timer delay = new Timer(500, e -> startNextBidTurn());
        delay.setRepeats(false);
        delay.start();
    }

    private void finishBidding() {
        List<Integer> robbers = new ArrayList<>();
        for (int seat = 0; seat < robChoices.length; seat++) {
            if (Boolean.TRUE.equals(robChoices[seat])) {
                robbers.add(seat);
            }
        }
        if (robbers.isEmpty()) {
            statusLabel.setText("无人抢地主，重新发牌");
            long currentRoundToken = roundToken;
            Timer delay = new Timer(1400, e -> {
                if (currentRoundToken == roundToken) {
                    startNewRound();
                }
            });
            delay.setRepeats(false);
            delay.start();
            return;
        }

        landlordSeat = robbers.get(ThreadLocalRandom.current().nextInt(robbers.size()));
        for (PlayerInfo player : players) {
            player.setLandlord(player.getSeat() == landlordSeat);
        }
        revealBottomCards();
        positionAllHands();
        updatePlayerLabels();
        statusLabel.setText(players[landlordSeat].getName() + " 成为地主");
        long currentRoundToken = roundToken;
        Timer delay = new Timer(1000, e -> {
            if (currentRoundToken == roundToken) {
                startPlayPhase();
            }
        });
        delay.setRepeats(false);
        delay.start();
    }

    private void revealBottomCards() {
        PlayerInfo landlord = players[landlordSeat];
        bottomArea.removeAll();
        for (int i = 0; i < bottomCards.size(); i++) {
            Poker poker = bottomCards.get(i);
            poker.setFaceUp(true);
            landlord.getHand().add(poker);

            JLabel display = new JLabel(ResourceUtil.loadPokerIcon(poker.getFaceImageName()));
            display.setBounds(i * 100 + 10, 15, CARD_WIDTH, CARD_HEIGHT);
            bottomArea.add(display);
        }
        Collections.sort(landlord.getHand());
        bottomArea.revalidate();
        bottomArea.repaint();
    }

    private void startPlayPhase() {
        phase = Phase.PLAYING;
        gameOver = false;
        currentPlayer = landlordSeat;
        lastPattern = null;
        lastCards = List.of();
        lastPlayer = -1;
        passCount = 0;
        recentActions.clear();
        positionAllHands();
        beginTurn();
    }

    /**
     * 开启当前玩家回合。真人等待按钮，电脑异步请求 AI。
     */
    private void beginTurn() {
        if (phase != Phase.PLAYING || gameOver) {
            return;
        }
        stopCountdown();
        hideActionButtons();
        decisionNo++;
        updatePlayerLabels();
        final long token = ++turnToken;

        if (currentPlayer == HUMAN_SEAT) {
            playButton.setVisible(true);
            passButton.setVisible(true);
            passButton.setEnabled(lastPattern != null);
            statusLabel.setText(lastPattern == null ? "轮到你自由出牌" : "轮到你跟牌");
            setHumanCardsSelectable(true);
            startCountdown(60, () -> {
                if (token == turnToken && phase == Phase.PLAYING) {
                    humanTimeout();
                }
            });
        } else {
            setHumanCardsSelectable(false);
            statusLabel.setText(players[currentPlayer].getName() + " 正在思考...");
            startCountdown(30, () -> {
                if (token == turnToken && phase == Phase.PLAYING) {
                    applyAIAction(currentPlayer, chooseFallback(currentPlayer), token);
                }
            });
            requestAIAction(currentPlayer, token);
        }
    }

    /**
     * AI 请求放在 SwingWorker 中，防止网络等待阻塞 EDT。
     */
    private void requestAIAction(int seat, long token) {
        AIPlayer aiPlayer = aiPlayers[seat];
        if (aiPlayer == null) {
            applyAIAction(seat, chooseFallback(seat), token);
            return;
        }
        aiPlayer.setHistorySummary(buildHistorySummary());
        GameSnapshot snapshot = buildSnapshot(seat);
        List<Poker> handCopy = List.copyOf(players[seat].getHand());
        CardPattern previous = lastPattern;

        new SwingWorker<AIAction, Void>() {
            @Override
            protected AIAction doInBackground() {
                return aiPlayer.decide(snapshot, handCopy, previous);
            }

            @Override
            protected void done() {
                if (token != turnToken || phase != Phase.PLAYING || gameOver) {
                    return;
                }
                AIAction action;
                try {
                    action = get();
                } catch (Exception e) {
                    action = AIAction.fallback(e.getMessage());
                }
                if (action == null || !action.isUsable()
                        || !validateAIAction(seat, action, previous)) {
                    action = chooseFallback(seat);
                }
                applyAIAction(seat, action, token);
            }
        }.execute();
    }

    private boolean validateAIAction(int seat, AIAction action, CardPattern previous) {
        List<Poker> hand = players[seat].getHand();
        if (action.isPass()) {
            return previous != null;
        }
        if (action.getCards().isEmpty() || !hand.containsAll(action.getCards())) {
            return false;
        }
        CardPattern pattern = CardRules.parse(action.getCards());
        return pattern != null && CardRules.canBeat(pattern, previous);
    }

    private void applyAIAction(int seat, AIAction action, long token) {
        if (token != turnToken || seat != currentPlayer || phase != Phase.PLAYING
                || gameOver) {
            return;
        }
        if (!validateAIAction(seat, action, lastPattern)) {
            action = chooseFallback(seat);
        }
        aiPlayers[seat].recordDecision(action);
        if (action.isPass()) {
            applyPass(seat);
        } else {
            applyPlay(seat, action);
        }
    }

    private AIAction chooseFallback(int seat) {
        List<Poker> hand = players[seat].getHand();
        if (lastPattern == null) {
            List<List<Poker>> legal = CardRules.findAllLegalPlays(hand, null);
            if (legal.isEmpty()) {
                return AIAction.fallbackPass();
            }
            List<List<Poker>> nonBombLeads = legal.stream()
                    .filter(cards -> !CardRules.parse(cards).isBomb())
                    .toList();
            if (nonBombLeads.isEmpty()) {
                nonBombLeads = legal;
            }
            List<Poker> lead;
            if (difficulty == Difficulty.EASY && nonBombLeads.size() > 1) {
                int limit = Math.min(3, nonBombLeads.size());
                lead = nonBombLeads.get(ThreadLocalRandom.current().nextInt(limit));
            } else if (difficulty == Difficulty.HARD) {
                lead = nonBombLeads.stream()
                        .max((first, second) -> {
                            int countCompare = Integer.compare(first.size(), second.size());
                            if (countCompare != 0) {
                                return countCompare;
                            }
                            return Integer.compare(CardRules.parse(first).mainValue(),
                                    CardRules.parse(second).mainValue());
                        })
                        .orElse(nonBombLeads.get(0));
            } else {
                lead = nonBombLeads.get(0);
            }
            return AIAction.fallbackPlay(lead, CardRules.parse(lead));
        }

        if (difficulty == Difficulty.EASY
                && ThreadLocalRandom.current().nextDouble() < 0.30) {
            return AIAction.fallbackPass();
        }

        if (isTeammate(seat, lastPlayer)) {
            int opponentMin = minimumOpponentHand(seat);
            if (difficulty == Difficulty.EASY || opponentMin > 1) {
                return AIAction.fallbackPass();
            }
        }

        List<List<Poker>> legal = CardRules.findAllLegalPlays(hand, lastPattern);
        if (legal.isEmpty()) {
            return AIAction.fallbackPass();
        }
        List<Poker> nonBomb = legal.stream()
                .filter(cards -> !CardRules.parse(cards).isBomb())
                .findFirst()
                .orElse(null);
        if (nonBomb != null) {
            return AIAction.fallbackPlay(nonBomb, CardRules.parse(nonBomb));
        }
        int bombThreshold = difficulty == Difficulty.HARD ? 3 : 2;
        if ((difficulty != Difficulty.EASY && minimumOpponentHand(seat) <= bombThreshold)
                || legal.get(0).size() == hand.size()) {
            return AIAction.fallbackPlay(legal.get(0), CardRules.parse(legal.get(0)));
        }
        return AIAction.fallbackPass();
    }

    private boolean isTeammate(int firstSeat, int secondSeat) {
        if (secondSeat < 0 || firstSeat == secondSeat) {
            return false;
        }
        return firstSeat != landlordSeat && secondSeat != landlordSeat;
    }

    private int minimumOpponentHand(int seat) {
        int min = Integer.MAX_VALUE;
        for (int other = 0; other < players.length; other++) {
            if (other != seat && !isTeammate(seat, other)) {
                min = Math.min(min, players[other].getHand().size());
            }
        }
        return min == Integer.MAX_VALUE ? 17 : min;
    }

    private void humanPlay() {
        if (phase != Phase.PLAYING || currentPlayer != HUMAN_SEAT) {
            return;
        }
        List<Poker> selected = players[HUMAN_SEAT].getHand().stream()
                .filter(Poker::isSelected)
                .toList();
        if (selected.isEmpty()) {
            showMessage("请选择要出的牌");
            return;
        }
        CardPattern pattern = CardRules.parse(selected);
        if (pattern == null) {
            showMessage("所选牌型不合法");
            return;
        }
        if (!CardRules.canBeat(pattern, lastPattern)) {
            showMessage("牌太小，不能出");
            return;
        }
        applyPlay(HUMAN_SEAT, AIAction.play(selected, pattern, AIAction.Source.MODEL));
    }

    private void humanPass() {
        if (phase != Phase.PLAYING || currentPlayer != HUMAN_SEAT) {
            return;
        }
        if (lastPattern == null) {
            showMessage("本轮必须出牌");
            return;
        }
        applyPass(HUMAN_SEAT);
    }

    private void humanTimeout() {
        if (lastPattern == null) {
            applyPlay(HUMAN_SEAT, chooseFallback(HUMAN_SEAT));
        } else {
            applyPass(HUMAN_SEAT);
        }
    }

    /**
     * 执行一次合法出牌，并更新中央牌区、上一手、倍数和下一回合。
     */
    private void applyPlay(int seat, AIAction action) {
        if (phase != Phase.PLAYING || gameOver) {
            return;
        }
        stopCountdown();
        List<Poker> cards = new ArrayList<>(action.getCards());
        CardPattern pattern = action.getPattern();
        if (pattern == null || cards.isEmpty() || !players[seat].getHand().containsAll(cards)) {
            return;
        }
        players[seat].getHand().removeAll(cards);
        showCentralPlay(seat, cards, pattern);
        recordPlay(seat, cards, pattern);
        lastPattern = pattern;
        lastCards = List.copyOf(cards);
        lastPlayer = seat;
        passCount = 0;
        if (pattern.isBomb()) {
            multiplier *= 2;
            publicHistory.addLast(players[seat].getName() + " 打出" + pattern.getDisplayName()
                    + "，倍数变为 " + multiplier);
        }
        updateScoreLabel();
        positionAllHands();
        if (players[seat].getHand().isEmpty()) {
            finishGame(seat);
            return;
        }
        currentPlayer = (seat + 1) % players.length;
        beginTurn();
    }

    /**
     * 执行不要。连续两家不要后清空跟牌限制，由上一手玩家重新领出。
     */
    private void applyPass(int seat) {
        if (phase != Phase.PLAYING || gameOver || lastPattern == null) {
            return;
        }
        stopCountdown();
        centralTitle.setText(players[seat].getName() + " 不要");
        addActionRecord(new GameSnapshot.ActionRecord(players[seat].getName(),
                "pass", List.of(), null));
        publicHistory.addLast(players[seat].getName() + " 不要");
        passCount++;
        if (passCount >= 2) {
            int leader = lastPlayer;
            lastPattern = null;
            lastCards = List.of();
            lastPlayer = -1;
            passCount = 0;
            recentActions.clear();
            currentPlayer = leader;
        } else {
            currentPlayer = (seat + 1) % players.length;
        }
        beginTurn();
    }

    private void showCentralPlay(int seat, List<Poker> cards, CardPattern pattern) {
        for (Component component : centralCardComponents) {
            playArea.remove(component);
        }
        centralCardComponents.clear();
        centralTitle.setText(players[seat].getName() + " 出牌  " + pattern.getDisplayName());

        int totalWidth = cards.size() * 22 + CARD_WIDTH - 22;
        int startX = (playArea.getWidth() - totalWidth) / 2;
        for (int i = 0; i < cards.size(); i++) {
            Poker poker = cards.get(i);
            poker.setSelected(false);
            poker.setSelectable(false);
            poker.setFaceUp(true);
            poker.setBounds(startX + i * 22, 34, CARD_WIDTH, CARD_HEIGHT);
            if (poker.getParent() != playArea) {
                if (poker.getParent() != null) {
                    poker.getParent().remove(poker);
                }
                playArea.add(poker);
            }
            playArea.setComponentZOrder(poker, 0);
            centralCardComponents.add(poker);
        }
        playArea.revalidate();
        playArea.repaint();
    }

    private void recordPlay(int seat, List<Poker> cards, CardPattern pattern) {
        List<String> ranks = cards.stream().map(Poker::getRank).toList();
        addActionRecord(new GameSnapshot.ActionRecord(players[seat].getName(),
                "play", ranks, pattern.getDisplayName()));
        Map<String, Integer> counts = playedRanks.computeIfAbsent(
                players[seat].getName(), ignored -> new LinkedHashMap<>());
        for (Poker poker : cards) {
            counts.merge(poker.getRank(), 1, Integer::sum);
        }
        publicHistory.addLast(players[seat].getName() + " 出 " + String.join(" ", ranks));
        while (publicHistory.size() > 12) {
            publicHistory.removeFirst();
        }
    }

    private void addActionRecord(GameSnapshot.ActionRecord record) {
        recentActions.add(record);
        while (recentActions.size() > 2) {
            recentActions.remove(0);
        }
    }

    private GameSnapshot buildSnapshot(int seat) {
        List<String> farmers = new ArrayList<>();
        for (PlayerInfo player : players) {
            if (!player.isLandlord()) {
                farmers.add(player.getName());
            }
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Integer> scoreMap = new LinkedHashMap<>();
        for (PlayerInfo player : players) {
            counts.put(player.getName(), player.getHand().size());
            scoreMap.put(player.getName(), scores[player.getSeat()]);
        }
        List<String> hand = players[seat].getHand().stream().map(Poker::getRank).toList();

        GameSnapshot.ActionRecord last = null;
        if (lastPattern != null && lastPlayer >= 0) {
            List<String> cards = lastCards.stream().map(Poker::getRank).toList();
            last = new GameSnapshot.ActionRecord(players[lastPlayer].getName(),
                    "play", cards, lastPattern.getDisplayName());
        }
        return new GameSnapshot(decisionNo, players[seat].getName(),
                players[seat].getRoleName(), hand, players[landlordSeat].getName(),
                farmers, counts, recentActions, last, lastPattern != null,
                lastPattern == null, scoreMap, multiplier);
    }

    private String buildHistorySummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("已出点数：");
        for (int seat = 0; seat < players.length; seat++) {
            if (seat > 0) {
                builder.append("；");
            }
            builder.append(players[seat].getName()).append('=');
            Map<String, Integer> counts = playedRanks.getOrDefault(
                    players[seat].getName(), Map.of());
            builder.append(counts.isEmpty() ? "无" : counts);
        }
        builder.append('\n').append("关键事件：");
        if (publicHistory.isEmpty()) {
            builder.append("无");
        } else {
            builder.append(String.join("；", publicHistory));
        }
        return builder.toString();
    }

    /**
     * 结算地主与农民得分，并由引擎统一判断胜负。
     */
    private void finishGame(int winnerSeat) {
        phase = Phase.FINISHED;
        gameOver = true;
        stopCountdown();
        hideActionButtons();
        setHumanCardsSelectable(false);

        boolean landlordWon = winnerSeat == landlordSeat;
        if (landlordWon) {
            scores[landlordSeat] += 2 * multiplier;
            for (PlayerInfo player : players) {
                if (!player.isLandlord()) {
                    scores[player.getSeat()] -= multiplier;
                }
            }
        } else {
            scores[landlordSeat] -= 2 * multiplier;
            for (PlayerInfo player : players) {
                if (!player.isLandlord()) {
                    scores[player.getSeat()] += multiplier;
                }
            }
        }
        updateScoreLabel();
        updatePlayerLabels();

        boolean humanWon = players[HUMAN_SEAT].isLandlord() == landlordWon;
        String title = landlordWon ? "地主获胜" : "农民获胜";
        String result = humanWon ? "你赢了" : "你输了";
        if (Boolean.getBoolean("doudizhu.test.autoClose")) {
            System.out.println("AutoGame finished: " + title + ", " + result
                    + ", multiplier=" + multiplier);
            dispose();
            return;
        }
        Object[] options = {"继续下一局", "退出游戏"};
        int option = JOptionPane.showOptionDialog(this,
                title + "\n" + result + "\n本局倍数：" + multiplier
                        + "\n请选择继续下一局，或退出游戏。",
                "本局结束", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[1]);
        if (option == 0) {
            startNewRound();
        } else {
            dispose();
        }
    }

    private void positionAllHands() {
        for (int seat = 0; seat < players.length; seat++) {
            List<Poker> hand = players[seat].getHand();
            Collections.sort(hand);
            if (seat == HUMAN_SEAT) {
                positionHumanHand(hand);
            } else {
                positionVerticalHand(hand, seat);
            }
        }
        updatePlayerLabels();
    }

    private void positionHumanHand(List<Poker> hand) {
        int startX = horizontalStart(hand.size());
        for (int i = 0; i < hand.size(); i++) {
            Poker poker = hand.get(i);
            poker.setFaceUp(true);
            int y = poker.isSelected() ? 535 : 550;
            poker.setBounds(startX + i * 22, y, CARD_WIDTH, CARD_HEIGHT);
            if (poker.getParent() != getContentPane()) {
                if (poker.getParent() != null) {
                    poker.getParent().remove(poker);
                }
                add(poker);
            }
            getContentPane().setComponentZOrder(poker, 0);
        }
    }

    private void positionVerticalHand(List<Poker> hand, int seat) {
        int startY = verticalStart(hand.size());
        int x = seat == 0 ? 18 : 900;
        for (int i = 0; i < hand.size(); i++) {
            Poker poker = hand.get(i);
            poker.setFaceUp(false);
            poker.setBounds(x, startY + i * 15, CARD_WIDTH, CARD_HEIGHT);
            getContentPane().setComponentZOrder(poker, 0);
        }
    }

    private int horizontalStart(int count) {
        int totalWidth = Math.max(CARD_WIDTH, count * 22 + CARD_WIDTH - 22);
        return Math.max(10, (FRAME_WIDTH - totalWidth) / 2);
    }

    private int verticalStart(int count) {
        int totalHeight = Math.max(CARD_HEIGHT, count * 15 + CARD_HEIGHT - 15);
        return Math.max(45, (FRAME_HEIGHT - totalHeight) / 2);
    }

    private void setHumanCardsSelectable(boolean selectable) {
        for (Poker poker : players[HUMAN_SEAT].getHand()) {
            poker.setSelectable(selectable);
            if (!selectable) {
                poker.setSelected(false);
            }
        }
        positionHumanHand(players[HUMAN_SEAT].getHand());
    }

    private void toggleHumanCard(Poker poker) {
        if (phase != Phase.PLAYING || currentPlayer != HUMAN_SEAT
                || !players[HUMAN_SEAT].getHand().contains(poker)) {
            return;
        }
        poker.setSelected(!poker.isSelected());
        positionHumanHand(players[HUMAN_SEAT].getHand());
    }

    private void startCountdown(int seconds, Runnable action) {
        stopCountdown();
        remainingSeconds = seconds;
        timeoutAction = action;
        updateCountdownText();
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                remainingSeconds = 0;
                updateCountdownText();
                Runnable currentAction = timeoutAction;
                stopCountdown();
                if (currentAction != null) {
                    currentAction.run();
                }
            } else {
                updateCountdownText();
            }
        });
        countdownTimer.start();
    }

    private void updateCountdownText() {
        for (int i = 0; i < timeFields.length; i++) {
            timeFields[i].setText(i == currentPlayer ? "倒计时" + remainingSeconds + "秒" : "");
        }
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        timeoutAction = null;
    }

    private void hideActionButtons() {
        robButton.setVisible(false);
        noRobButton.setVisible(false);
        playButton.setVisible(false);
        passButton.setVisible(false);
    }

    private void updatePlayerLabels() {
        for (int seat = 0; seat < players.length; seat++) {
            PlayerInfo player = players[seat];
            String role = landlordSeat < 0 ? "" : " · " + player.getRoleName();
            playerLabels[seat].setText(player.getName() + role
                    + " · " + player.getHand().size() + "张");
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText("比分  " + players[0].getName() + ":" + scores[0]
                + "  " + players[1].getName() + ":" + scores[1]
                + "  " + players[2].getName() + ":" + scores[2]
                + "  倍数:" + multiplier
                + "  难度:" + difficulty.getDisplayName());
    }

    private void removeAllCards() {
        for (Component component : getContentPane().getComponents()) {
            if (component instanceof Poker) {
                getContentPane().remove(component);
            }
        }
        for (Component component : centralCardComponents) {
            playArea.remove(component);
        }
        centralCardComponents.clear();
        bottomArea.removeAll();
        playArea.revalidate();
        playArea.repaint();
        bottomArea.revalidate();
        bottomArea.repaint();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
