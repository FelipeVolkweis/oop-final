package com.gui;

import javax.swing.*;

/**
 * A classe ResponseHandler é responsável por lidar com as respostas recebidas do servidor.
 */
public class ResponseHandler {
    /**
     * O código de status para uma resposta bem-sucedida.
     */
    public static final int STATUS_OK = 200;
    /**
     * O código de status para uma resposta de recurso não encontrado.
     */
    public static final int STATUS_NOT_FOUND = 404;
    /**
     * O código de status para uma resposta de erro interno do servidor.
     */
    public static final int STATUS_INTERNAL_ERROR = 500;

    /**
     * Extrai o código de status de uma resposta.
     *
     * @param response A resposta recebida do servidor.
     * @return O código de status extraído da resposta. Retorna -1 se o código de status não puder ser extraído.
     */
    public static int extractStatus(String response) {
        try {
            int statusIndex = response.indexOf("\"status\":");
            if (statusIndex == -1) {
                return -1;
            }

            int startIndex = statusIndex + 9;
            int endIndex = response.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = response.indexOf("}", startIndex);
            }

            if (endIndex == -1) {
                return -1;
            }

            String statusString = response.substring(startIndex, endIndex).trim();
            return Integer.parseInt(statusString);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Lida com a resposta recebida do servidor.
     *
     * @param response A resposta recebida do servidor.
     */
    public static void handleResponse(String response) {
        int status = extractStatus(response);
        String errorMessage = extractMessage(response);

        switch (status) {
            case STATUS_OK:
                break;
            case STATUS_NOT_FOUND:
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Nenhum registro encontrado. " + errorMessage, "Informação", JOptionPane.INFORMATION_MESSAGE));
                break;
            case STATUS_INTERNAL_ERROR:
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Erro interno do servidor. " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE));
                break;
            default:
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Erro desconhecido: " + status + ". " + errorMessage, "Erro", JOptionPane.ERROR_MESSAGE));
                break;
        }
    }

    /**
     * Extrai a mensagem de uma resposta.
     *
     * @param response A resposta recebida do servidor.
     * @return A mensagem extraída da resposta. Retorna uma mensagem de erro padrão se a mensagem não puder ser extraída.
     */
    public static String extractMessage(String response) {
        try {
            int payloadIndex = response.indexOf("\"payload\":");
            if (payloadIndex == -1) {
                return "Detalhe do erro não disponível.";
            }

            int startQuote = response.indexOf('"', payloadIndex + 10) + 1;
            int endQuote = response.indexOf('"', startQuote);
            return response.substring(startQuote, endQuote);
        } catch (Exception e) {
            return "Erro ao extrair a mensagem.";
        }
    }
}
