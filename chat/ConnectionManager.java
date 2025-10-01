package chat;

import ui.ChatPanel;
import ui.ChatWindow;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ConnectionManager implements ChatHandler.ConnectionClosedListener, ChatPanel.SendActionListener {
    private final List<ChatHandler> activeHandlers = Collections.synchronizedList(new LinkedList<>());
    private final Map<ChatPanel, ChatHandler> panelToHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private final ChatWindow chatWindow;

    public ConnectionManager(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
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
        ChatHandler handler = panelToHandlerMap.get(sourcePanel);
        if (handler != null) {
            if (handler.sendMessage(message)) {
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
