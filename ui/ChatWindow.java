package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChatWindow extends JFrame {
    JTabbedPane tabbedPane;
    
    // Interface để xử lý sự kiện khi tab Multicast hoặc Broadcast được chọn lần đầu
    public interface MulticastActivationListener {
        void activateMulticast(ChatPanel multicastPanel);
        void activateBroadcast(ChatPanel broadcastPanel); 
    }
    private MulticastActivationListener multicastListener;

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

        tabbedPane.addChangeListener(new ChangeListener() {
            private boolean multicastActive = false;
            private boolean broadcastActive = false; 

            @Override
            public void stateChanged(ChangeEvent e) {
                String selectedTitle = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                Component selectedComponent = tabbedPane.getSelectedComponent();

                if (multicastListener != null && selectedComponent instanceof ChatPanel) {
                    // Kích hoạt Multicast
                    if (!multicastActive && selectedTitle.equals("Multicast (Nhóm)")) {
                        multicastListener.activateMulticast((ChatPanel) selectedComponent);
                        multicastActive = true; 
                    } 
                    // Kích hoạt Broadcast
                    else if (!broadcastActive && selectedTitle.equals("Broadcast (*)") ) {
                        multicastListener.activateBroadcast((ChatPanel) selectedComponent);
                        broadcastActive = true;
                    }
                }
            }
        });

        setVisible(true);
    }

    public void setMulticastActivationListener(MulticastActivationListener listener) {
        this.multicastListener = listener;
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