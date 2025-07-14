package last_netpro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class guiMenu extends JFrame implements gameClient.MessageListener {

    gameClient client;

    private JButton game1Button;
    private JButton game2Button;

    public guiMenu() {
        super("ゲーム選択メニュー");

        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(2, 1));

        game1Button = new JButton("脳を活性化せよ！数字順番ゲーム！");
        game2Button = new JButton("光をたどれ！ランプ早押しゲーム！");

        // 初期は無効にしておく
        game1Button.setEnabled(false);
        game2Button.setEnabled(false);

        game1Button.addActionListener(e -> {
            dispose();

            String input = JOptionPane.showInputDialog(
                    null,
                    "平方数（例：16, 25, 36）を入力してください",
                    "マス数入力",
                    JOptionPane.QUESTION_MESSAGE);

            if (input == null || input.isEmpty()) {
                JOptionPane.showMessageDialog(null, "入力がキャンセルされました");
                return;
            }

            try {
                int totalCells = Integer.parseInt(input.trim());

                int root = (int) Math.sqrt(totalCells);
                if (root * root != totalCells) {
                    JOptionPane.showMessageDialog(null, "平方数を入力してください（4, 9, 16, 25...）");
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    gui2 game = new gui2("Number Order Game", totalCells);
                    game.setSize(400, 400);
                    game.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    game.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            SwingUtilities.invokeLater(() -> {
                                guiMenu menu = new guiMenu();
                                menu.setVisible(true);
                            });
                        }
                    });

                    game.setVisible(true);
                });

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "数値を入力してください");
            }
        });

        game2Button.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                if (client != null) {

                    gameGUI game = new gameGUI("早押し", client); // clientを渡す
                    game.setSize(500, 580);
                    game.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    game.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            SwingUtilities.invokeLater(() -> {
                                guiMenu menu = new guiMenu();
                                menu.setVisible(true);
                            });
                        }
                    });

                    game.setVisible(true);
                    game.connectToServer("localhost", 5050);

                } else {
                    JOptionPane.showMessageDialog(this, "サーバーに接続されていません。");
                }
            });
        });

        panel.add(game1Button);
        panel.add(game2Button);

        add(panel);

        connectToServer("localhost", 5050);
    }

    public void connectToServer(String host, int port) {
        client = new gameClient(host, port, this,
                new gameClient.ConnectionListener() {
                    @Override
                    public void onConnectSuccess() {
                        System.out.println("接続成功イベント");
                        SwingUtilities.invokeLater(() -> {
                            game1Button.setEnabled(true);
                            game2Button.setEnabled(true);
                        });
                    }

                    @Override
                    public void onConnectFail(IOException e) {
                        System.err.println("接続失敗イベント: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            game1Button.setEnabled(false);
                            game2Button.setEnabled(false);
                        });
                    }
                });
    }

    @Override
    public void onMessageReceived(String message) {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            guiMenu menu = new guiMenu();
            menu.setVisible(true);
        });
    }
}
