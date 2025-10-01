package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketConnector {
    private ServerSocket serverSocket;
    private Thread serverThread;

    public void startServer(int port, Consumer<Socket> onNewConnection, Consumer<String> onLog) {
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                onLog.accept("Server đang lắng nghe trên cổng " + port + "...");
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    onNewConnection.accept(socket);
                }
            } catch (IOException e) {
                onLog.accept("Server lỗi: " + e.getMessage());
            }
        });
        serverThread.start();
    }

    public void startClient(String host, int port, Consumer<Socket> onConnected, Consumer<String> onLog) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);
                onConnected.accept(socket);
            } catch (IOException e) {
                onLog.accept("Không thể kết nối tới " + host + ":" + port + " - " + e.getMessage());
            }
        }).start();
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
    }
}
