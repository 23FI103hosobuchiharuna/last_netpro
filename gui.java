package last_netpro;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class gui extends JFrame {
    // JTextField field = new JTextField();
    int arg1, arg2; // **
    char opeCode; // **
    String arg; // **
    boolean lastTimeNum; // **

    int sum = 0;

    // JLabel timerLabel = new JLabel("20", SwingConstants.CENTER);
    JLabel label = new JLabel();
    int timeLeft = 20;
    Timer timer;

    public static void main(String[] args) {
        gui w = new gui("gui");
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.setSize(500, 500);
        w.setVisible(true);
    }

    public gui(String title) {
        super(title);

        JPanel pane = (JPanel) getContentPane();

        pane.setLayout(new BorderLayout());

        label.setText("   ");
        label.setFont(new Font("Arial", Font.BOLD, 200));
        // タイマーの表示ラベルを上に追加
        // timerLabel.setFont(new Font("Arial", Font.BOLD, 50));
        // pane.add(timerLabel, BorderLayout.NORTH);
        pane.add(label, BorderLayout.NORTH);

        // field を中央の上部に移動
        // field.setPreferredSize(new Dimension(200, 40));
        // pane.add(field, BorderLayout.SOUTH);

        JPanel keyPanel = new JPanel(new GridLayout(4, 4));
        pane.add(keyPanel, BorderLayout.CENTER);

        String[] a = { "", "", "", "",
                "", "", "", "",
                "", "", "", "",
                "", "", "", "" };

        Action[] action = {
                new NumKey(a[0]), new NumKey(a[1]), new NumKey(a[2]), new FuncKey(a[3]),
                new NumKey(a[4]), new NumKey(a[5]), new NumKey(a[6]), new FuncKey(a[7]),
                new NumKey(a[8]), new NumKey(a[9]), new NumKey(a[10]), new FuncKey(a[11]),
                new NumKey(a[12]), new FuncKey(a[13]), new FuncKey(a[14]), new FuncKey(a[15])
        };

        for (int i = 0; i < action.length; i++) {
            keyPanel.add(new JButton(action[i]));
        }

        String[] keyStroke = { "NUMPAD7", "NUMPAD8", "NUMPAD9", "DIVIDE",
                "NUMPAD4", "NUMPAD5", "NUMPAD6", "MULTIPLY",
                "NUMPAD1", "NUMPAD2", "NUMPAD3", "C",
                "NUMPAD0", "ADD", "SUBTRACT", "ENTER" };

        InputMap imap = keyPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap amap = keyPanel.getActionMap();

        for (int i = 0; i < action.length; i++) {
            KeyStroke k = KeyStroke.getKeyStroke(keyStroke[i]);
            imap.put(k, action[i]);
            amap.put(action[i], action[i]);
        }

        arg1 = 0; // **
        opeCode = '+'; // **
        arg = ""; // **
        lastTimeNum = false;

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // if (timeLeft >= 0) {
                // timerLabel.setText(String.valueOf(timeLeft));
                // timeLeft--;
                // } else {
                // timer.stop();
                // timerLabel.setText("Time Up!");
                // }
            }
        });
        timer.start();
    }

    class NumKey extends AbstractAction {
        NumKey(String num) {
            putValue(Action.NAME, num);
        }

        public void actionPerformed(ActionEvent e) {
            sum += 1;
            System.out.println(sum);
        }
    }

    class FuncKey extends AbstractAction {
        FuncKey(String label) {
            // putValue(Action.NAME, label);
        }

        public void actionPerformed(ActionEvent e) {

            sum += 5;
            System.out.println(sum);
        }

    }

}
