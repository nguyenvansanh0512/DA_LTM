package ui;

import javax.swing.*;
import java.awt.*;

public class ChatWindow extends JFrame {
    private JTabbedPane tabbedPane;

    public ChatWindow() {
        setTitle("P2P Chat (Tab Mode)");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        JTextArea systemArea = new JTextArea("Chào mừng đến với ứng dụng Chat P2P Tab Mode!\nHãy kết nối với Peer đầu tiên.");
        systemArea.setEditable(false);
        tabbedPane.addTab("System Log", new JScrollPane(systemArea));

        setVisible(true);
    }

    public void addChatPanel(String title, ChatPanel panel) {
        tabbedPane.addTab(title, panel);
        tabbedPane.setSelectedComponent(panel);
    }

    public void removeChatPanel(ChatPanel panel) {
        tabbedPane.remove(panel);
    }

    public void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            Component systemLog = tabbedPane.getComponentAt(0);
            if (systemLog instanceof JScrollPane) {
                JViewport viewport = ((JScrollPane) systemLog).getViewport();
                if (viewport.getView() instanceof JTextArea) {
                    ((JTextArea) viewport.getView()).append(message + "\n");
                }
            }
        });
    }
}
