package last_netpro;

import java.io.*;
import java.net.*;

public class gameClient {
    public static void main(String[] args) {
        String hostname = "localhost"; // サーバーのホスト名
        int port = 12345; // サーバーのポート番号

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("サーバーに接続しました。");

            // メッセージ受信と送信のためのスレッドを開始
            new Thread(new ReadThread(socket)).start();
            new Thread(new WriteThread(socket)).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // サーバーからメッセージを受信するスレッド
    private static class ReadThread implements Runnable {
        private BufferedReader in;

        public ReadThread(Socket socket) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String message;
            try {
                // サーバーからのメッセージを受信して表示
                while ((message = in.readLine()) != null) {
                    System.out.println("サーバーから: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // サーバーにメッセージを送信するスレッド
    private static class WriteThread implements Runnable {
        private PrintWriter out;
        private BufferedReader stdIn;

        public WriteThread(Socket socket) {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                stdIn = new BufferedReader(new InputStreamReader(System.in));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String userInput;
            try {
                // ユーザーからの入力を読み取り、サーバーに送信
                while ((userInput = stdIn.readLine()) != null) {
                    out.println(userInput);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
