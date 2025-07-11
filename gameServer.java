package last_netpro;

import java.io.*;
import java.net.*;
import java.util.*;

public class gameServer {
    // 同期されたクライアントハンドラのセット
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        int port = 12345; // サーバーのポート番号
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("サーバーがポート " + port + " で起動しました。");

            while (true) {
                // 新しいクライアントの接続を待機
                Socket clientSocket = serverSocket.accept();
                System.out.println("新しいクライアントが接続しました。");

                // 新しいクライアントハンドラを作成し、スレッドを開始
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // クライアントからのメッセージをブロードキャスト
    public static void broadcastMessage(String message, ClientHandler excludeClient) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler != excludeClient) {
                    clientHandler.sendMessage(message);
                }
            }
        }
    }

    // クライアントが切断されたときに呼び出される
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("クライアントが切断されました。");
    }

    // クライアントハンドラの内部クラス
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // クライアントとの入出力ストリームを初期化
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;
                // クライアントからのメッセージを受信し続ける
                while ((message = in.readLine()) != null) {
                    System.out.println("受信: " + message);
                    // 受信したメッセージを他のクライアントにブロードキャスト
                    gameServer.broadcastMessage(message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // クライアントが切断されたときの処理
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gameServer.removeClient(this);
            }
        }

        // クライアントにメッセージを送信
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
