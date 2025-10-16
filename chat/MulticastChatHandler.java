package chat;

import java.io.IOException;
import java.net.*;
import ui.ChatPanel;
import ui.ChatWindow;

public class MulticastChatHandler extends Thread {
    private final MulticastSocket socket;
    private final InetAddress group;
    private final int port;
    private final ChatWindow chatWindow;
    private final ChatPanel multicastPanel; 

    public MulticastChatHandler(ChatWindow chatWindow, ChatPanel multicastPanel, String groupAddress, int port) throws IOException {
        this.chatWindow = chatWindow;
        this.multicastPanel = multicastPanel;
        this.port = port;
        this.group = InetAddress.getByName(groupAddress);
        
        this.socket = new MulticastSocket(port); 
        this.socket.joinGroup(group); 
        this.socket.setLoopbackMode(false);
        chatWindow.appendSystemMessage("Đã tham gia nhóm Multicast: " + groupAddress + ":" + port);
    }

    @Override
    public void run() {
        byte[] buf = new byte[1024];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); 

                String received = new String(packet.getData(), 0, packet.getLength());
                String senderAddress = packet.getAddress().getHostAddress();
                
                // Tin nhắn trên đường truyền là "Tên:Nội dung"
                String[] parts = received.split(":", 2);
                String message = parts.length > 1 ? parts[1] : received;

                // HIỂN THỊ: [Địa chỉ IP]: Message
                multicastPanel.appendMessage(senderAddress, message);

            } catch (IOException e) {
                if (!socket.isClosed()) {
                    chatWindow.appendSystemMessage("Lỗi khi nhận Multicast: " + e.getMessage());
                }
                break;
            }
        }
    }

    public boolean sendMulticastMessage(String senderName, String message) {
        try {
            // Cần TênNgườiGửi:TinNhắn trên đường truyền
            String fullMessage = senderName + ":" + message; 
            byte[] buf = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port); 
            socket.send(packet);
            
            // TỰ HIỂN THỊ: [Me]: Message
            multicastPanel.appendMessage("Me", message);
            
            return true;
        } catch (IOException e) {
            chatWindow.appendSystemMessage("Lỗi khi gửi Multicast: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.leaveGroup(group);
                socket.close();
                chatWindow.appendSystemMessage("Đã rời nhóm Multicast.");
            } catch (IOException ignored) {}
        }
    }
}