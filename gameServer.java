package last_netpro;

import java.io.*;
import java.net.*;
import java.util.*;

public class gameServer {
    private static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        int port = 5050;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("サーバーがポート " + port + " で起動しました。");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("新しいクライアントが接続しました。");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 全クライアントに送る（送信者も含む）
    public static void broadcastMessage(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.sendMessage(message);
            }
        }
    }

    // 送信者以外に送る
    public static void broadcastMessage(String message, ClientHandler excludeClient) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler != excludeClient) {
                    clientHandler.sendMessage(message);
                }
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("クライアントが切断されました。");
    }

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
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("受信: " + message);

                    if (message.startsWith("START:")) {
                        // STARTメッセージは送信者含め全員に送る
                        gameServer.broadcastMessage(message);
                    } else {
                        // その他のメッセージは送信者を除いて送る
                        gameServer.broadcastMessage(message, this);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gameServer.removeClient(this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
