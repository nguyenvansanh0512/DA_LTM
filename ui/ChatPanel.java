package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class ChatPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton sendFileButton;

    public interface SendActionListener {
        void onSend(String message, ChatPanel sourcePanel);
        void onSendFile(File file, ChatPanel sourcePanel);
    }

    private SendActionListener sendListener;
    private String peerName;

    public ChatPanel(String peerName, SendActionListener listener) {
        this.peerName = peerName;
        this.sendListener = listener;

        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Gửi");
        sendFileButton = new JButton("Gửi File");

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));

        buttonPanel.add(sendButton);
        buttonPanel.add(sendFileButton);

        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(buttonPanel, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        ActionListener action = e -> {
            String msg = getInputText();
            if (!msg.isEmpty()) {
                sendListener.onSend(msg, this);
            }
        };
        sendButton.addActionListener(action);
        inputField.addActionListener(action);

        sendFileButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                sendListener.onSendFile(file, this);
            }
        });

        appendMessage("System", "Đã thiết lập chat với " + peerName);
    }

    public void appendMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[" + sender + "]: " + message + "\n");
        });
    }

    public String getInputText() {
        String text = inputField.getText().trim();
        clearInput();
        return text;
    }

    public void clearInput() {
        inputField.setText("");
    }

    public String getPeerName() {
        return peerName;
    }
}
