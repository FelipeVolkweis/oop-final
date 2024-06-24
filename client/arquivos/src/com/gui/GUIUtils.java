package com.gui;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class GUIUtils {
    public static class FieldPanel {
        public JPanel panel;
        public JTextField textField;

        public FieldPanel(JPanel panel, JTextField textField) {
            this.panel = panel;
            this.textField = textField;
        }
    }

    public static FieldPanel inputField(
            String labelTitle,
            int verticalSize,
            int maxQtd,
            boolean onlyNumbers,
            char layoutAxis
    ) {
        return inputField(labelTitle, verticalSize, maxQtd, onlyNumbers, layoutAxis, "");
    }

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

    static class InputFilter extends DocumentFilter {
        private final int maxLength;
        private final boolean onlyNumbers;

        public InputFilter(int maxLength, boolean onlyNumbers) {
            this.maxLength = maxLength;
            this.onlyNumbers = onlyNumbers;
        }

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
