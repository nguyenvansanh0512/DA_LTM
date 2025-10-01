package ui;

import chat.ConnectionManager;
import socket.SocketConnector;

import javax.swing.*;

public class ChatApp {
    public static final int PORT = 12345;

    private ChatWindow chatWindow;
    private SocketConnector socketConnector;
    private ConnectionManager connectionManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatApp().start());
    }

    private void start() {
        chatWindow = new ChatWindow();
        socketConnector = new SocketConnector();
        connectionManager = new ConnectionManager(chatWindow);

        socketConnector.startServer(PORT, connectionManager::setupNewConnection, chatWindow::appendSystemMessage);

        String host = JOptionPane.showInputDialog(chatWindow, "Nhập IP của peer muốn kết nối (bỏ trống nếu chỉ chờ):",
                "Thiết lập Client", JOptionPane.QUESTION_MESSAGE);
        if (host != null && !host.trim().isEmpty()) {
            socketConnector.startClient(host.trim(), PORT, connectionManager::setupNewConnection, chatWindow::appendSystemMessage);
        }
    }
}
