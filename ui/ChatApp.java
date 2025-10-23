package ui;

import chat.BroadcastChatHandler;
import chat.ConnectionManager;
import chat.MulticastChatHandler;
import java.io.IOException;
import javax.swing.*;
import socket.SocketConnector;

public class ChatApp implements ChatWindow.MulticastActivationListener {
    public static final int PORT = 12345;

    private ChatWindow chatWindow;
    private SocketConnector socketConnector;
    private ConnectionManager connectionManager;
    private MulticastChatHandler multicastHandler;
    private BroadcastChatHandler broadcastHandler;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatApp().start());
    }

    private void start() {
        chatWindow = new ChatWindow();
        socketConnector = new SocketConnector();
        connectionManager = new ConnectionManager(chatWindow);
        
        // 1. Thiết lập Multicast Panel
        ChatPanel multicastPanel = new ChatPanel("MULTICAST", connectionManager);
        chatWindow.addChatPanel("Multicast (Nhóm)", multicastPanel);
        
        // 2. Thiết lập Broadcast Panel
        ChatPanel broadcastPanel = new ChatPanel("BROADCAST", connectionManager);
        chatWindow.addChatPanel("Broadcast (*)", broadcastPanel);

        // 3. Thiết lập Listener cho ChatWindow
        chatWindow.setMulticastActivationListener(this); 

        // 4. Khởi động Server P2P
        socketConnector.startServer(PORT, connectionManager::setupNewConnection, chatWindow::appendSystemMessage);

        // 5. Thiết lập Client P2P
        String host = JOptionPane.showInputDialog(chatWindow, "Nhập IP của peer muốn kết nối (bỏ trống nếu chỉ chờ):",
                "Thiết lập Client", JOptionPane.QUESTION_MESSAGE);
        if (host != null && !host.trim().isEmpty()) {
            socketConnector.startClient(host.trim(), PORT, connectionManager::setupNewConnection, chatWindow::appendSystemMessage);
        }
    }

    @Override
    public void activateMulticast(ChatPanel multicastPanel) {
        if (multicastHandler != null) return; 
        
        // ĐỊA CHỈ MULTICAST GROUP MẶC ĐỊNH
        final String defaultGroupAddress = "230.0.0.1";
        
        // Hộp thoại chỉ yêu cầu nhập Port
        String portInput = JOptionPane.showInputDialog(chatWindow, 
                "Nhập Port cho Multicast (ví dụ: 4446):",
                "Thiết lập Multicast (Group mặc định: " + defaultGroupAddress + ")", JOptionPane.QUESTION_MESSAGE);

        if (portInput != null && !portInput.trim().isEmpty()) {
            try {
                String groupAddress = defaultGroupAddress; // Cố định địa chỉ
                int groupPort = Integer.parseInt(portInput.trim()); // Chỉ lấy Port từ người dùng

                multicastHandler = new MulticastChatHandler(chatWindow, multicastPanel, groupAddress, groupPort);
                multicastHandler.start();
                connectionManager.setMulticastHandler(multicastHandler);
                
                // Kích hoạt điều khiển sau khi khởi tạo thành công
                multicastPanel.setControlsEnabled(true); 
                
            } catch (NumberFormatException e) {
                chatWindow.appendSystemMessage("Lỗi định dạng Port Multicast. Vui lòng nhập một số nguyên hợp lệ.");
            } catch (IOException e) {
                chatWindow.appendSystemMessage("LỖI KHỞI TẠO MULTICAST: " + e.getMessage());
            }
        } else {
             chatWindow.appendSystemMessage("Khởi tạo Multicast đã bị hủy.");
        }
    }
    
    @Override
    public void activateBroadcast(ChatPanel broadcastPanel) {
        if (broadcastHandler != null) return; 
        
        // BƯỚC 1: Hộp thoại nhập cổng
        String portInput = JOptionPane.showInputDialog(chatWindow,"4447", null );
                
        int broadcastPort;
        if (portInput == null) {
            chatWindow.appendSystemMessage("Khởi tạo Broadcast đã bị hủy.");
            return;
        }

        try {
            broadcastPort = Integer.parseInt(portInput);
        } catch (NumberFormatException e) {
            chatWindow.appendSystemMessage("Lỗi: Cổng phải là số nguyên. Khởi tạo Broadcast bị hủy.");
            return;
        }
        
        // BƯỚC 2: Hộp thoại xác nhận
        int confirm = JOptionPane.showConfirmDialog(chatWindow, 
                "Xác nhận khởi động Broadcast trên cổng " + broadcastPort + "?",
                "Thiết lập Broadcast", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // TRUYỀN CỔNG DO NGƯỜI DÙNG NHẬP VÀO CONSTRUCTOR
                broadcastHandler = new BroadcastChatHandler(chatWindow, broadcastPanel, broadcastPort);
                broadcastHandler.start();
                connectionManager.setBroadcastHandler(broadcastHandler);
                chatWindow.appendSystemMessage("Broadcast đã được khởi động thành công trên cổng " + broadcastPort + ".");
                
                // Kích hoạt điều khiển sau khi khởi tạo thành công
                broadcastPanel.setControlsEnabled(true); 
                
            } catch (IOException e) {
                chatWindow.appendSystemMessage("LỖI KHỞI TẠO BROADCAST: " + e.getMessage());
            }
        } else {
            chatWindow.appendSystemMessage("Khởi tạo Broadcast đã bị hủy.");
        }
    }
}