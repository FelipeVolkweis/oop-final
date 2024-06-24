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
     * Cria um campo de entrada com as opções fornecidas.
     * 
     * @param labelTitle O título do campo de entrada.
     * @param verticalSize O tamanho vertical do campo de entrada.
     * @param maxQtd A quantidade máxima de caracteres permitidos no campo de entrada.
     * @param onlyNumbers Indica se apenas números são permitidos no campo de entrada.
     * @param layoutAxis O eixo de layout do painel (x ou y).
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
     * Cria um campo de entrada com as opções fornecidas e um valor inicial.
     * 
     * @param labelTitle O título do campo de entrada.
     * @param verticalSize O tamanho vertical do campo de entrada.
     * @param maxQtd A quantidade máxima de caracteres permitidos no campo de entrada.
     * @param onlyNumbers Indica se apenas números são permitidos no campo de entrada.
     * @param layoutAxis O eixo de layout do painel (x ou y).
     * @param initialValue O valor inicial do campo de entrada.
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
     * Cria um campo de entrada com as opções fornecidas e um valor inicial.
     * 
     * @param labelTitle O título do campo de entrada.
     * @param verticalSize O tamanho vertical do campo de entrada.
     * @param maxQtd A quantidade máxima de caracteres permitidos no campo de entrada.
     * @param onlyNumbers Indica se apenas números são permitidos no campo de entrada.
     * @param layoutAxis O eixo de layout do painel (x ou y).
     * @param initialValue O valor inicial do campo de entrada.
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
        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        layoutAxis == 'y' ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS
                )
        );

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelTitle);

        JTextField textField = new JTextField(initialValue);
        textField.setPreferredSize(new Dimension(verticalSize, 25));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new InputFilter(maxQtd, onlyNumbers));

        panel.add(label);

        panel.add(
                layoutAxis == 'y' ?
                Box.createVerticalStrut(5) :
                Box.createHorizontalStrut(5)
        );

        panel.add(textField);

        return new FieldPanel(panel, textField);
    }

    /**
     * Classe interna que representa um filtro de entrada para o campo de texto.
     */
    static class InputFilter extends DocumentFilter {
        private final int maxLength;
        private final boolean onlyNumbers;

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
                return;
            }
            if (onlyNumbers && !string.matches("\\d*")) {
                return;
            }
            if ((fb.getDocument().getLength() + string.length()) <= maxLength) {
                super.insertString(fb, offset, string, attr);
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
                return;
            }
            if (onlyNumbers && !text.matches("\\d*")) {
                return;
            }
            if ((fb.getDocument().getLength() + text.length() - length) <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
