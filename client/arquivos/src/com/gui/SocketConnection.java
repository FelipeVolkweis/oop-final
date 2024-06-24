package com.gui;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class SocketConnection {
    private final String ip;
    private final int port;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public SocketConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect(String selectFile) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                SwingUtilities.invokeLater(() -> {
                    MainFrame.enableComponentsWithConnection();
                    MainFrame.createBinaryFile(selectFile);
                    JOptionPane.showMessageDialog(null, "Conexão bem-sucedida!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Falha na conexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    public void disconnect() {
        new Thread(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Desconexão bem-sucedida!", "Sucesso", JOptionPane.INFORMATION_MESSAGE));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Falha na desconexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            } finally {
                SwingUtilities.invokeLater(MainFrame::disableComponentsWithConnection);
            }
        }).start();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void sendMessage(String message, ResponseCallback callback) {
        new Thread(() -> {
            if (isConnected()) {
                try {
                    byte[] messageBytes = message.getBytes();
                    int messageLength = messageBytes.length;

                    out.writeInt(messageLength);
                    out.write(messageBytes);
                    out.flush();

                    int responseLength = in.readInt();
                    byte[] responseBytes = new byte[responseLength];

                    in.readFully(responseBytes);
                    String response = new String(responseBytes);

                    SwingUtilities.invokeLater(() -> callback.onResponse(response));
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> callback.onFailure(e));
                }
            } else {
                SwingUtilities.invokeLater(() -> callback.onFailure(new IOException("Socket não está conectado")));
            }
        }).start();
    }

    public interface ResponseCallback {
        void onResponse(String response);
        void onFailure(Exception e);
    }
}