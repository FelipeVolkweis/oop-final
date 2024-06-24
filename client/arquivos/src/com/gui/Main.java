package com.gui;

import javax.swing.SwingUtilities;

/**
 * Classe principal que contém o método de inicialização do programa.
 */
public class Main {
    /**
     * Método principal que inicia a aplicação.
     * 
     * @param args os argumentos de linha de comando (não utilizados neste programa)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}