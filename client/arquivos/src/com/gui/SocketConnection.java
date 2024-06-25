package com.gui;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * Classe responsável por estabelecer e gerenciar a conexão de socket com um servidor.
 */
public class SocketConnection {
    private final String ip; // Endereço IP do servidor
    private final int port; // Porta do servidor
    private Socket socket; // Socket para a conexão com o servidor
    private DataOutputStream out; // Fluxo de saída para enviar dados ao servidor
    private DataInputStream in; // Fluxo de entrada para receber dados do servidor


    /**
     * Construtor que inicializa a conexão com o servidor especificado pelo endereço IP e porta.
     *
     * @param ip O endereço IP do servidor.
     * @param port A porta do servidor.
     */
    public SocketConnection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Estabelece uma conexão com o servidor por meio de um socket.
     * 
     * @param selectFile O arquivo selecionado para envio ao servidor.
     */
    public void connect(String selectFile) {
        new Thread(() -> {
            try {
                socket = new Socket(ip, port); // Cria um novo socket com o IP e porta especificados
                out = new DataOutputStream(socket.getOutputStream()); // Inicializa o fluxo de saída
                in = new DataInputStream(socket.getInputStream()); // Inicializa o fluxo de entrada

                // Executa ações na thread da interface gráfica após estabelecer a conexão
                SwingUtilities.invokeLater(() -> {
                    MainFrame.enableComponentsWithConnection(); // Habilita componentes da interface que dependem da conexão
                    MainFrame.createBinaryFile(selectFile); // Processa o arquivo selecionado
                    JOptionPane.showMessageDialog(null, "Conexão bem-sucedida!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (IOException e) {
                // Em caso de erro, mostra uma mensagem de erro na interface gráfica
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Falha na conexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    /**
     * Fecha a conexão com o socket.
     * 
     * Esta função fecha a conexão com o socket. Se a conexão estiver aberta, o socket é fechado e uma mensagem de sucesso é exibida.
     * Caso ocorra algum erro durante o fechamento da conexão, uma mensagem de erro é exibida com a descrição do erro.
     * Após o fechamento da conexão, os componentes da interface gráfica relacionados à conexão são desabilitados.
     */
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

    /**
     * Verifica se a conexão com o socket está estabelecida.
     * 
     * @return true se a conexão estiver estabelecida, caso contrário, false.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Envia uma mensagem para o servidor através da conexão de socket.
     * 
     * @param message A mensagem a ser enviada.
     * @param callback O objeto de retorno de chamada para lidar com a resposta do servidor.
     */
    public void sendMessage(String message, ResponseCallback callback) {
        new Thread(() -> {
            if (isConnected()) {
                try {
                    byte[] messageBytes = message.getBytes();
                    int messageLength = messageBytes.length;

                    out.writeInt(messageLength); // Envia o tamanho da mensagem
                    out.write(messageBytes); // Envia a mensagem
                    out.flush(); // Limpa o buffer de saída

                    int responseLength = in.readInt(); // Lê o tamanho da resposta
                    byte[] responseBytes = new byte[responseLength];

                    in.readFully(responseBytes); // Lê a resposta completa
                    String response = new String(responseBytes);

                    // Chama o método de sucesso do callback na thread da interface gráfica
                    SwingUtilities.invokeLater(() -> callback.onResponse(response));
                } catch (IOException e) {
                    // Chama o método de falha do callback na thread da interface gráfica em caso de erro
                    SwingUtilities.invokeLater(() -> callback.onFailure(e));
                }
            } else {
                // Se o socket não estiver conectado, chama o método de falha do callback
                SwingUtilities.invokeLater(() -> callback.onFailure(new IOException("Socket não está conectado")));
            }
        }).start();
    }

    /**
     * Interface que define um callback para receber a resposta de uma operação assíncrona.
     */
    public interface ResponseCallback {
        /**
         * Método chamado quando a resposta é recebida com sucesso.
         *
         * @param response A resposta recebida.
         */
        void onResponse(String response);

        /**
         * Método chamado quando ocorre uma falha na operação.
         *
         * @param e A exceção que ocorreu.
         */
        void onFailure(Exception e);
    }
}