package com.gui;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

/**
 * Classe utilitária para criação de componentes gráficos.
 */
public class GUIUtils {
    /**
     * Classe interna que representa um painel de campo de entrada.
     * Contém um JPanel e um JTextField, facilitando a manipulação e agrupamento desses componentes.
     */
    public static class FieldPanel {
        public JPanel panel;
        public JTextField textField;

        /**
         * Construtor da classe FieldPanel.
         * 
         * @param panel O painel que contém o campo de entrada.
         * @param textField O campo de entrada.
         */
        public FieldPanel(JPanel panel, JTextField textField) {
            this.panel = panel;
            this.textField = textField;
        }
    }

    /**
     * Cria um campo de entrada com as opções fornecidas, sem valor inicial.
     *
     * @param labelTitle O título do campo de entrada, exibido como um rótulo.
     * @param verticalSize Altura preferencial do campo de entrada.
     * @param maxQtd Máximo de caracteres permitidos no campo de entrada.
     * @param onlyNumbers Restringe a entrada para aceitar apenas números se true.
     * @param layoutAxis Orientação do layout do painel ('x' para horizontal ou 'y' para vertical).
     * @return Um objeto FieldPanel contendo o painel e o campo de entrada criados.
     */
    public static FieldPanel inputField(
            String labelTitle,
            int verticalSize,
            int maxQtd,
            boolean onlyNumbers,
            char layoutAxis
    ) {
        return inputField(labelTitle, verticalSize, maxQtd, onlyNumbers, layoutAxis, "");
    }

    /**
     * Cria um campo de entrada com as opções fornecidas e um valor inicial numérico.
     * Converte o valor inicial para string antes de passá-lo para a sobrecarga do método.
     *
     * @param labelTitle Título do campo.
     * @param verticalSize Altura preferencial do campo.
     * @param maxQtd Máximo de caracteres permitidos.
     * @param onlyNumbers Restringe a entrada para aceitar apenas números.
     * @param layoutAxis Orientação do layout do painel.
     * @param initialValue Valor inicial do campo como um número inteiro.
     * @return Um objeto FieldPanel contendo o painel e o campo de entrada criados.
     */
    public static FieldPanel inputField(
            String labelTitle,
            int verticalSize,
            int maxQtd,
            boolean onlyNumbers,
            char layoutAxis,
            int initialValue
    ) {
        return inputField(labelTitle, verticalSize, maxQtd, onlyNumbers, layoutAxis, String.valueOf(initialValue));
    }

    /**
     * Versão completa do método inputField que configura o campo de entrada e adiciona a um painel com um rótulo.
     * Aplica filtros ao campo de entrada para controlar a entrada de caracteres.
     *
     * @param labelTitle Título do campo.
     * @param verticalSize Altura preferencial do campo.
     * @param maxQtd Máximo de caracteres permitidos.
     * @param onlyNumbers Se true, restringe a entrada para aceitar apenas números.
     * @param layoutAxis Orientação do layout do painel.
     * @param initialValue Valor inicial do campo como string.
     * @return Um objeto FieldPanel contendo o painel e o campo de entrada criados.
     */
    public static FieldPanel inputField(
            String labelTitle,
            int verticalSize,
            int maxQtd,
            boolean onlyNumbers,
            char layoutAxis,
            String initialValue
    ) {
        JPanel panel = new JPanel(); // Cria um novo JPanel

        // Configura o layout do painel
        panel.setLayout(
                new BoxLayout(
                        panel,
                        layoutAxis == 'y' ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS
                )
        );

        // Alinha componentes à esquerda
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Cria e configura o rótulo com o título forneci
        JLabel label = new JLabel(labelTitle);

        JTextField textField = new JTextField(initialValue); // Cria o campo de texto com o valor inicial
        textField.setPreferredSize(new Dimension(verticalSize, 25)); // Define a dimensão preferencial do campo
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25)); // Limita o tamanho máximo do campo

        // Aplica um filtro de documento para controlar a entrada no campo de texto
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new InputFilter(maxQtd, onlyNumbers));

        panel.add(label); // Adiciona o rótulo ao painel

        // Adiciona um espaçamento
        panel.add(
                layoutAxis == 'y' ?
                Box.createVerticalStrut(5) :
                Box.createHorizontalStrut(5)
        );

        panel.add(textField); // Adiciona o campo de texto ao painel

        return new FieldPanel(panel, textField); // Retorna o painel configurado com o campo de texto
    }

    /**
     * Classe interna que representa um filtro de entrada para o campo de texto.
     */
    static class InputFilter extends DocumentFilter {
        private final int maxLength; // Comprimento máximo permitido
        private final boolean onlyNumbers; // Se true, permite apenas a entrada de números

        /**
         * Construtor da classe InputFilter.
         * 
         * @param maxLength O comprimento máximo permitido para o texto.
         * @param onlyNumbers Indica se apenas números são permitidos.
         */
        public InputFilter(int maxLength, boolean onlyNumbers) {
            this.maxLength = maxLength;
            this.onlyNumbers = onlyNumbers;
        }

        /**
         * Insere uma string no documento, respeitando as restrições de filtro e comprimento máximo.
         *
         * @param fb O objeto FilterBypass que permite a inserção da string no documento.
         * @param offset A posição de inserção da string no documento.
         * @param string A string a ser inserida no documento.
         * @param attr Os atributos da string a serem aplicados no documento.
         * @throws BadLocationException Se ocorrer um erro ao inserir a string no documento.
         */
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) {
                return; // Se a string for nula, não insere nada
            }
            if (onlyNumbers && !string.matches("\\d*")) { // Se onlyNumbers for true, permite apenas números
                return;
            }
            if ((fb.getDocument().getLength() + string.length()) <= maxLength) { // Verifica se a inserção não excede o comprimento máximo permitido
                super.insertString(fb, offset, string, attr); // Insere a string no documento
            }
        }

        /**
         * Substitui o texto em um documento filtrado.
         *
         * @param fb O objeto FilterBypass que permite a modificação do documento.
         * @param offset A posição inicial onde o texto deve ser substituído.
         * @param length O comprimento do texto a ser substituído.
         * @param text O novo texto a ser inserido.
         * @param attrs Os atributos do texto a serem aplicados.
         * @throws BadLocationException Se ocorrer um erro ao acessar a posição do documento.
         */
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) {
                return; // Se o texto for nulo, não substitui nada
            }
            if (onlyNumbers && !text.matches("\\d*")) { // Se onlyNumbers for true, permite apenas números
                return;
            }
            if ((fb.getDocument().getLength() + text.length() - length) <= maxLength) { // Verifica se a substituição não excede o comprimento máximo permitido
                super.replace(fb, offset, length, text, attrs); // Substitui o texto no documento
            }
        }
    }
}
