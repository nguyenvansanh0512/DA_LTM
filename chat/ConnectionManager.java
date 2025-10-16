package chat;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import javax.swing.*;
import ui.ChatPanel;
import ui.ChatWindow;

public class ConnectionManager implements ChatHandler.ConnectionClosedListener, ChatPanel.SendActionListener {
    private final List<ChatHandler> activeHandlers = Collections.synchronizedList(new LinkedList<>());
    private final Map<ChatPanel, ChatHandler> panelToHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final ChatWindow chatWindow;
    private MulticastChatHandler multicastHandler;
    private BroadcastChatHandler broadcastHandler; 

    public ConnectionManager(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }

    public void setMulticastHandler(MulticastChatHandler handler) {
        this.multicastHandler = handler;
    }
    
    public void setBroadcastHandler(BroadcastChatHandler handler) {
        this.broadcastHandler = handler;
    }

    public void setupNewConnection(Socket socket) {
        try {
            String peerAddress = socket.getInetAddress().getHostAddress();

            ChatPanel chatPanel = new ChatPanel(peerAddress, this);
            ChatHandler handler = new ChatHandler(socket, chatWindow, chatPanel, this);

            activeHandlers.add(handler);
            panelToHandlerMap.put(chatPanel, handler);

            SwingUtilities.invokeLater(() -> {
                chatWindow.addChatPanel(peerAddress, chatPanel);
                chatWindow.appendSystemMessage("Đã tạo kết nối mới với: " + peerAddress + ". Tổng kết nối: " + activeHandlers.size());
            });

            handler.start();
        } catch (IOException e) {
            chatWindow.appendSystemMessage("Lỗi khi thiết lập kết nối: " + e.getMessage());
        }
    }

    @Override
    public void onSend(String message, ChatPanel sourcePanel) {
        String peerName = sourcePanel.getPeerName();
        
        // 1. Xử lý gửi tin Broadcast (*)
        if (peerName.equals("BROADCAST")) {
            if (broadcastHandler != null) {
                // Logic hiển thị tin mình gửi (Me) đã nằm trong BroadcastChatHandler
                if (!broadcastHandler.sendBroadcastMessage("Me", message)) {
                    sourcePanel.appendMessage("System", "Gửi Broadcast thất bại!");
                }
            } else {
                sourcePanel.appendMessage("System", "Broadcast chưa khởi động.");
            }
            return;
        }

        // 2. Xử lý gửi tin Multicast (group)
        if (peerName.equals("MULTICAST")) {
            if (multicastHandler != null) {
                // Logic hiển thị tin mình gửi (Me) đã nằm trong MulticastChatHandler
                if (!multicastHandler.sendMulticastMessage("Me", message)) {
                    sourcePanel.appendMessage("System", "Gửi Multicast thất bại!");
                }
            } else {
                sourcePanel.appendMessage("System", "Multicast chưa khởi động.");
            }
            return;
        }

        // 3. Xử lý gửi tin P2P (IP)
        ChatHandler handler = panelToHandlerMap.get(sourcePanel);
        if (handler != null) {
            if (handler.sendMessage(message)) {
                // HIỂN THỊ: [Me]: Message
                sourcePanel.appendMessage("Me", message);
            } else {
                sourcePanel.appendMessage("System", "Gửi thất bại. Kết nối đã đóng.");
            }
        } else {
            sourcePanel.appendMessage("System", "Không tìm thấy kết nối!");
        }
    }

    @Override
    public void onSendFile(File file, ChatPanel sourcePanel) {
        // Ngăn chặn gửi file qua Broadcast và Multicast
        String peerName = sourcePanel.getPeerName();
        if (peerName.equals("BROADCAST") || peerName.equals("MULTICAST")) {
            sourcePanel.appendMessage("System", "Gửi File qua Broadcast/Multicast chưa được hỗ trợ.");
            return;
        }
        
        // Xử lý gửi file P2P 
        ChatHandler handler = panelToHandlerMap.get(sourcePanel);
        if (handler != null) {
            handler.sendFile(file);
        } else {
            sourcePanel.appendMessage("System", "Không tìm thấy kết nối!");
        }
    }

    @Override
    public void onClosed(ChatHandler handler) {
        synchronized (activeHandlers) {
            activeHandlers.remove(handler);
        }

        ChatPanel panelToRemove = handler.getChatPanel();
        if (panelToRemove != null) {
            SwingUtilities.invokeLater(() -> chatWindow.removeChatPanel(panelToRemove));
            panelToHandlerMap.remove(panelToRemove);
        }

        chatWindow.appendSystemMessage("Kết nối " + handler.getPeerAddress() + " đã bị đóng. Tổng số kết nối còn lại: " + activeHandlers.size());
    }
}