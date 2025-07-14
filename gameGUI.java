package last_netpro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.*;
import javax.swing.Timer;

public class gameGUI extends JFrame implements gameClient.MessageListener {
    int GRID_SIZE = 4;
    int lightDuration = 5000;
    int maxLights = 20;
    int spawnDelay = 1200;

    JButton[][] buttons;
    JPanel gridPanel;
    long totalReactionTime = 0;
    int litCount = 0;
    int missCount = 0;
    boolean gameEnded = false;
    Random random = new Random();

    JButton readyButton;
    JLabel countdownLabel;
    JLabel titleLabel;
    JLabel opponentLabel;
    JComboBox<String> difficultyBox;

    gameClient client;
    java.util.List<ActivePanel> activePanels = new ArrayList<>();

    boolean isGameStarted = false;
    boolean isReady = false;
    boolean opponentReady = false;
    String opponentDifficulty = null;
    Double opponentScore = null;
    Double myScore = null;
    JLabel resultLabel = null;

    JLayeredPane layeredPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gameClient client = new gameClient("133.14.214.14", 5050, null, null);
            gameGUI gui = new gameGUI("反射神経バトル：ポップ版", client);
            gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gui.setSize(500, 580);
            gui.setLocationRelativeTo(null);
            // gui.setVisible(true);
            client.setMessageListener(gui);

        });
    }

    public void connectToServer(String host, int port) {
        client = new gameClient(host, port, this,
                new gameClient.ConnectionListener() {
                    @Override
                    public void onConnectSuccess() {
                        System.out.println("接続成功イベント");
                        SwingUtilities.invokeLater(() -> {
                            readyButton.setEnabled(true);
                            difficultyBox.setEnabled(true);
                        });
                    }

                    @Override
                    public void onConnectFail(IOException e) {
                        System.err.println("接続失敗イベント: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            readyButton.setEnabled(false);
                            difficultyBox.setEnabled(false);
                        });
                    }
                });
    }

    public gameGUI(String title, gameClient client) {
        super(title);
        this.client = client;
        this.client.setMessageListener(this);
        setLayout(new BorderLayout());

        titleLabel = new JLabel("\uD83C\uDF89 反射神経バトル \uD83C\uDF89", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Meiryo", Font.BOLD, 28));
        titleLabel.setForeground(Color.MAGENTA);

        difficultyBox = new JComboBox<>(new String[] { "Normal", "Hard", "Very Hard" });
        difficultyBox.setFont(new Font("Meiryo", Font.PLAIN, 16));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(difficultyBox, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        Font font = new Font("Meiryo", Font.BOLD, 26);
        buttons = new JButton[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setFont(font);
                button.setFocusPainted(false);
                button.setBackground(Color.PINK);
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
                int x = i, y = j;
                button.addActionListener((ActionEvent e) -> handleButtonClick(x, y));
                buttons[i][j] = button;
                gridPanel.add(button);
            }
        }

        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Meiryo", Font.BOLD, 80));
        countdownLabel.setForeground(Color.BLACK);
        countdownLabel.setOpaque(false);
        countdownLabel.setAlignmentX(0.5f);
        countdownLabel.setAlignmentY(0.5f);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        gridPanel.setAlignmentX(0.5f);
        gridPanel.setAlignmentY(0.5f);
        countdownLabel.setAlignmentX(0.5f);
        countdownLabel.setAlignmentY(0.5f);
        layeredPane.add(countdownLabel, Integer.valueOf(1));
        layeredPane.add(gridPanel, Integer.valueOf(0));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(layeredPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        opponentLabel = new JLabel("", SwingConstants.CENTER);
        opponentLabel.setFont(new Font("Meiryo", Font.PLAIN, 16));

        readyButton = new JButton("準備完了（対戦用）");
        readyButton.setFont(new Font("Meiryo", Font.BOLD, 20));
        readyButton.setBackground(Color.GREEN);
        readyButton.setFocusPainted(false);
        readyButton.setEnabled(false);
        readyButton.addActionListener(e -> {
            if (client != null) {
                String selected = (String) difficultyBox.getSelectedItem();
                client.sendMessage("READY:" + selected);
                readyButton.setEnabled(false);
                countdownLabel.setText("準備完了。");
                isReady = true;
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(readyButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void rebuildGrid() {
        if (layeredPane != null) {
            layeredPane.removeAll();
        }

        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        Font font = new Font("Meiryo", Font.BOLD, 26);
        buttons = new JButton[GRID_SIZE][GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JButton button = new JButton();
                button.setFont(font);
                button.setFocusPainted(false);
                button.setBackground(Color.PINK);
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
                int x = i, y = j;
                button.addActionListener((ActionEvent e) -> handleButtonClick(x, y));
                buttons[i][j] = button;
                gridPanel.add(button);
            }
        }

        gridPanel.setAlignmentX(0.5f);
        gridPanel.setAlignmentY(0.5f);

        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Meiryo", Font.BOLD, 80));
        countdownLabel.setForeground(Color.BLACK);
        countdownLabel.setOpaque(false);
        countdownLabel.setAlignmentX(0.5f);
        countdownLabel.setAlignmentY(0.5f);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        layeredPane.setPreferredSize(new Dimension(500, 500));
        layeredPane.add(countdownLabel, Integer.valueOf(1));
        layeredPane.add(gridPanel, Integer.valueOf(0));

        getContentPane().removeAll();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(difficultyBox, BorderLayout.EAST);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(layeredPane, BorderLayout.CENTER);
        getContentPane().add(centerWrapper, BorderLayout.CENTER);

        JPanel bottomLeftPanel = new JPanel();
        bottomLeftPanel.add(readyButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(bottomLeftPanel, BorderLayout.WEST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void handleButtonClick(int x, int y) {
        if (gameEnded || !isGameStarted)
            return;
        Optional<ActivePanel> match = activePanels.stream()
                .filter(p -> p.button == buttons[x][y])
                .findFirst();
        if (match.isPresent()) {
            long reactionTime = System.currentTimeMillis() - match.get().startTime;
            totalReactionTime += reactionTime;
            buttons[x][y].setText(String.format("%.1fs", reactionTime / 1000.0));
            buttons[x][y].setBackground(Color.PINK);
            buttons[x][y].setFont(new Font("Meiryo", Font.BOLD, 18));
            match.get().timer.stop();
            activePanels.remove(match.get());
            if (client != null)
                client.sendMessage("REACTION:" + (reactionTime / 1000.0));
            checkEnd();
            new Timer(500, evt -> buttons[x][y].setText("")).start();

        } else {
            buttons[x][y].setText("-0.5");
            missCount++;
            if (client != null)
                client.sendMessage("MISS:1");
            new Timer(500, evt -> buttons[x][y].setText("")).start();
        }
    }

    public void applyDifficulty(String level) {
        switch (level) {
            case "Hard":
                GRID_SIZE = 5;
                lightDuration = 4000;
                maxLights = 30;
                spawnDelay = 1500;
                break;
            case "Very Hard":
                GRID_SIZE = 6;
                lightDuration = 3000;
                maxLights = 40;
                spawnDelay = 800;
                break;
            default:
                GRID_SIZE = 4;
                lightDuration = 5000;
                maxLights = 20;
                spawnDelay = 2000;
                break;
        }
    }

    public void countdownCountdown(int count) {
        countdownLabel.setText(String.valueOf(count));
        if (count == 0) {
            countdownLabel.setText("スタート！");
            new Timer(1000, e -> {
                countdownLabel.setText("");
                startGame();
            }) {
                {
                    setRepeats(false);
                }
            }.start();
        } else {
            new Timer(1000, e -> countdownCountdown(count - 1)) {
                {
                    setRepeats(false);
                }
            }.start();
        }
    }

    public void startGame() {
        System.out.println("ゲーム開始");

        totalReactionTime = 0;
        missCount = 0;
        litCount = 0;
        gameEnded = false;
        isGameStarted = true;
        activePanels.clear();
        clearButtons();
        countdownLabel.setText("");

        long gameStartTime = System.currentTimeMillis();

        Timer spawner = new Timer(spawnDelay, null);
        spawner.addActionListener(e -> {
            if (gameEnded || litCount >= maxLights)
                return;
            long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000;
            int newDelay = spawnDelay;
            if (elapsed >= 20)
                newDelay = Math.max(200, spawnDelay / 3);
            else if (elapsed >= 10)
                newDelay = Math.max(400, spawnDelay / 2);
            if (spawner.getDelay() != newDelay)
                spawner.setDelay(newDelay);
            int i = random.nextInt(GRID_SIZE);
            int j = random.nextInt(GRID_SIZE);
            JButton btn = buttons[i][j];
            if (btn.getBackground() == Color.YELLOW)
                return;

            Timer blink = new Timer(150, null);
            final int[] count = { 0 };
            blink.addActionListener(ev -> {
                btn.setBackground((count[0] % 2 == 0) ? Color.YELLOW : Color.ORANGE);
                count[0]++;
                if (count[0] >= 4)
                    blink.stop();
            });
            blink.start();

            btn.setText("今だ！");
            btn.setFont(new Font("Meiryo", Font.BOLD, 14));
            long startTime = System.currentTimeMillis();

            Timer t = new Timer(lightDuration, ev -> {
                btn.setBackground(Color.PINK);
                btn.setText("×");
                activePanels.removeIf(p -> p.button == btn);
                checkEnd();
                new Timer(500, ce -> btn.setText("")).start();
            });
            t.setRepeats(false);
            t.start();

            activePanels.add(new ActivePanel(btn, startTime, t));
            litCount++;
        });
        spawner.start();
    }

    public void checkEnd() {
        if (litCount >= maxLights && activePanels.isEmpty() && !gameEnded) {
            gameEnded = true;
            isGameStarted = false;
            double penalty = missCount * 0.5;
            double totalSec = totalReactionTime / 1000.0;
            double finalScore = totalSec + penalty;
            myScore = finalScore;
            if (client != null)
                client.sendMessage("SCORE:" + finalScore);
            String msg = String.format("スコア: %.2f 秒\n(反応合計: %.2f秒 + ミス: %d回 → ペナルティ %.2f秒)",
                    finalScore, totalSec, missCount, penalty);
            JOptionPane.showMessageDialog(this, msg);
            checkMatchResult();

        }
    }

    public void clearButtons() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                buttons[i][j].setBackground(Color.PINK);
                buttons[i][j].setText("");
            }
        }
    }

    public void showResult(boolean isWin) {
        if (resultLabel != null)
            getLayeredPane().remove(resultLabel);
        resultLabel = new JLabel(isWin ? "勝利！" : "敗北！", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Meiryo", Font.BOLD, 72));
        resultLabel.setForeground(isWin ? Color.BLUE : Color.RED);
        resultLabel.setOpaque(true);
        resultLabel.setBackground(new Color(255, 255, 255, 200));
        resultLabel.setBounds(0, getHeight() / 2 - 100, getWidth(), 200);
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        getLayeredPane().add(resultLabel, JLayeredPane.POPUP_LAYER);
        repaint();
    }

    private void checkMatchResult() {
        if (opponentScore == null || myScore == null)
            return;
        boolean isWin = myScore < opponentScore;
        SwingUtilities.invokeLater(() -> showResult(isWin));
    }

    @Override
    public void onMessageReceived(String message) {
        if (message.startsWith("READY:")) {
            opponentDifficulty = message.split(":")[1];
            opponentReady = true;
            if (isReady && opponentReady) {
                client.sendMessage("START:" + opponentDifficulty);
            }
        } else if (message.startsWith("START:")) {
            String level = message.split(":")[1];
            SwingUtilities.invokeLater(() -> {
                applyDifficulty(level);
                rebuildGrid();
                countdownCountdown(3);
            });
        } else if (message.startsWith("MISS:")) {
            opponentLabel.setText("相手がミスした！");
        } else if (message.startsWith("SCORE:")) {
            opponentScore = Double.parseDouble(message.split(":")[1]);
            checkMatchResult();
        }
    }

    class ActivePanel {
        JButton button;
        long startTime;
        Timer timer;

        public ActivePanel(JButton button, long startTime, Timer timer) {
            this.button = button;
            this.startTime = startTime;
            this.timer = timer;
        }
    }
}
