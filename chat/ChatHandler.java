package chat;

import ui.ChatPanel;
import ui.ChatWindow;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ChatHandler extends Thread {
    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private ChatPanel chatPanel;
    private ChatWindow chatWindow;
    private final String peerAddress;

    public interface ConnectionClosedListener {
        void onClosed(ChatHandler handler);
    }

    private ConnectionClosedListener closedListener;

    public ChatHandler(Socket socket, ChatWindow chatWindow, ChatPanel chatPanel, ConnectionClosedListener listener) throws IOException {
        this.socket = socket;
        this.chatWindow = chatWindow;
        this.chatPanel = chatPanel;
        this.peerAddress = socket.getInetAddress().getHostAddress();
        this.closedListener = listener;

        dataIn = new DataInputStream(socket.getInputStream());
        dataOut = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        receiveLoop();
    }

    public boolean sendMessage(String message) {
        try {
            dataOut.writeUTF("TEXT:" + message);
            dataOut.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendFile(File file) {
        try {
            dataOut.writeUTF("FILE:" + file.getName());
            dataOut.writeLong(file.length());

            byte[] buffer = new byte[4096];
            FileInputStream fis = new FileInputStream(file);
            int read;
            while ((read = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, read);
            }
            dataOut.flush();
            fis.close();

            chatPanel.appendMessage("System", "Đã gửi file: " + file.getName());
        } catch (IOException e) {
            chatPanel.appendMessage("System", "Gửi file thất bại!");
        }
    }

    private void receiveLoop() {
        try {
            while (true) {
                String header = dataIn.readUTF();
                if (header.startsWith("TEXT:")) {
                    handleText(header.substring(5));
                } else if (header.startsWith("FILE:")) {
                    handleFile(header);
                }
            }
        } catch (IOException e) {
            chatWindow.appendSystemMessage("Kết nối tới " + peerAddress + " đã bị đóng!");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            if (closedListener != null) {
                closedListener.onClosed(this);
            }
        }
    }

    private void handleText(String message) {
        chatPanel.appendMessage(peerAddress, message);
    }

    private void handleFile(String header) {
        try {
            String fileName = header.substring(5);
            long fileLength = dataIn.readLong();

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(fileName));
            if (chooser.showSaveDialog(chatWindow) == JFileChooser.APPROVE_OPTION) {
                File saveFile = chooser.getSelectedFile();
                FileOutputStream fos = new FileOutputStream(saveFile);

                byte[] buffer = new byte[4096];
                long remaining = fileLength;
                int read;
                while (remaining > 0 && (read = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
                fos.close();
                chatPanel.appendMessage("System", "Đã nhận file: " + saveFile.getName());
            } else {
                byte[] buffer = new byte[4096];
                long remaining = fileLength;
                while (remaining > 0) {
                    int r = dataIn.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (r == -1) break;
                    remaining -= r;
                }
                chatPanel.appendMessage("System", "Đã từ chối nhận file: " + fileName);
            }
        } catch (IOException e) {
            chatPanel.appendMessage("System", "Nhận file thất bại!");
        }
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    public String getPeerAddress() {
        return peerAddress;
    }
}
