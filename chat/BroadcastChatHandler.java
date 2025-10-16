package chat;

import java.io.IOException;
import java.net.*;
import ui.ChatPanel;
import ui.ChatWindow;

public class BroadcastChatHandler extends Thread {
    
    private static final String BROADCAST_ADDR = "255.255.255.255"; 
    private final DatagramSocket socket; 
    private final ChatWindow chatWindow;
    private final ChatPanel broadcastPanel;
    private final InetAddress broadcastAddress;
    private final int port; 

    // Constructor nhận cổng
    public BroadcastChatHandler(ChatWindow chatWindow, ChatPanel broadcastPanel, int port) throws IOException {
        this.chatWindow = chatWindow;
        this.broadcastPanel = broadcastPanel;
        this.broadcastAddress = InetAddress.getByName(BROADCAST_ADDR);
        this.port = port; // Gán cổng được truyền vào
        
        // Sử dụng cổng được truyền vào
        this.socket = new DatagramSocket(port); 
        this.socket.setBroadcast(true); 
        chatWindow.appendSystemMessage("Đã sẵn sàng Broadcast trên cổng " + port);
    }

    @Override
    public void run() {
        byte[] buf = new byte[1024];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String senderAddress = packet.getAddress().getHostAddress();
                
                // LOGIC BỎ QUA GÓI TIN TỪ CHÍNH MÌNH (Loopback)
                if (socket.getLocalAddress().getHostAddress().equals(senderAddress)) {
                    continue; 
                }

                String received = new String(packet.getData(), 0, packet.getLength());
                String[] parts = received.split(":", 2);
                String message = parts.length > 1 ? parts[1] : received;

                // HIỂN THỊ: [Địa chỉ IP]: Message
                broadcastPanel.appendMessage(senderAddress, message);

            } catch (IOException e) {
                if (!socket.isClosed()) {
                    chatWindow.appendSystemMessage("Lỗi khi nhận Broadcast: " + e.getMessage());
                }
                break;
            }
        }
    }

    public boolean sendBroadcastMessage(String senderName, String message) {
        try {
            String fullMessage = senderName + ":" + message; 
            byte[] buf = fullMessage.getBytes();
            
            // Gửi gói tin sử dụng biến cổng
            DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, port); 
            socket.send(packet);
            
            // TỰ HIỂN THỊ: [Me]: Message
            broadcastPanel.appendMessage("Me", message);
            
            return true;
        } catch (IOException e) {
            chatWindow.appendSystemMessage("Lỗi khi gửi Broadcast: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            chatWindow.appendSystemMessage("Đã đóng Broadcast Socket.");
        }
    }
}