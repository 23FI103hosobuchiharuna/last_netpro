package last_netpro;

import java.io.*;
import java.net.*;

public class gameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public interface ConnectionListener {
        void onConnectSuccess();

        void onConnectFail(IOException e);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private MessageListener messageListener;
    private ConnectionListener connectionListener;

    public gameClient(String host, int port, MessageListener listener, ConnectionListener connectionListener) {
        this.messageListener = listener;
        this.connectionListener = connectionListener;
        try {
            socket = new Socket(host, port);
            System.out.println("サーバーに接続成功: " + host + ":" + port);
            if (this.connectionListener != null) {
                this.connectionListener.onConnectSuccess();
            }
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        if (this.messageListener != null) {
                            this.messageListener.onMessageReceived(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            if (this.connectionListener != null) {
                this.connectionListener.onConnectFail(e);
            }
            e.printStackTrace();
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
