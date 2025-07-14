package last_netpro;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

public class gui2 extends JFrame {

    private int nextNumber = 1;
    private long startTime = 0;
    private long endTime = 0;
    private static final String RECORD_FILE = "records.txt";
    private ArrayList<Long> recordList = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // ループの外に出す

        while (true) {
            System.out.println("4以上の好きな数字の二乗を入力してください（例：25,36） qで終了：");

            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("q")) {
                System.out.println("終了します。");
                break;
            }

            try {
                int range = Integer.parseInt(input);
                if (range < 4) {
                    System.out.println("4以上の数字を入力してください。");
                    continue;
                }

                SwingUtilities.invokeAndWait(() -> {
                    gui2 w = new gui2("Number Order Game", range);
                    w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // ゲーム画面だけ閉じる
                    w.setSize(400, 400);
                    // w.setVisible(true);
                });

            } catch (NumberFormatException e) {
                System.out.println("数値または q を入力してください。");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scanner.close();
    }

    public gui2(String title, int range) {
        super(title);
        loadRecords();

        JPanel pane = (JPanel) getContentPane();
        pane.setLayout(new BorderLayout());

        int rows = (int) Math.sqrt(range);
        JPanel keyPanel = new JPanel(new GridLayout(rows, rows));
        pane.add(keyPanel, BorderLayout.CENTER);

        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= range; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        Color[] colors = { Color.red, Color.pink, Color.yellow, Color.CYAN, Color.GRAY, Color.GREEN, Color.MAGENTA };

        for (int i = 0; i < range; i++) {
            int num = numbers.get(i);
            JButton button = new JButton(String.valueOf(num));
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setPreferredSize(new Dimension(60, 60));
            Color randomColor = colors[new Random().nextInt(colors.length)];
            button.setBackground(randomColor);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int clicked = Integer.parseInt(button.getText());

                    if (clicked == nextNumber) {
                        button.setBackground(Color.white);
                        button.setEnabled(false);
                        if (nextNumber == 1) {
                            startTime = System.currentTimeMillis();
                        }
                        if (nextNumber == range) {
                            endTime = System.currentTimeMillis();
                            long elapsedTime = (endTime - startTime) / 1000;

                            recordList.add(elapsedTime);
                            Collections.sort(recordList);
                            int rank = recordList.indexOf(elapsedTime) + 1;

                            saveRecords();

                            JOptionPane.showMessageDialog(null,
                                    "Clear! Time: " + elapsedTime + " seconds\nYour Rank: " + rank);

                            dispose(); // ゲームウィンドウを閉じるだけ（mainループは継続）
                        }
                        nextNumber++;
                    }
                }
            });

            keyPanel.add(button);
        }

        revalidate();
        repaint();

        // 「q」で即終了できるショートカット
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q') {
                    System.exit(0);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private void loadRecords() {
        try (Scanner sc = new Scanner(new File(RECORD_FILE))) {
            while (sc.hasNextLong()) {
                recordList.add(sc.nextLong());
            }
        } catch (Exception e) {
            // ファイルがない場合などは何もしない
        }
    }

    private void saveRecords() {
        try (PrintWriter out = new PrintWriter(RECORD_FILE)) {
            for (long t : recordList) {
                out.println(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
