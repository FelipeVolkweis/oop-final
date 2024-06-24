package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PlayerEditDialog extends JDialog {
    private static JTextField ageField;
    private static JTextField playerNameField;
    private static JTextField nationalityField;
    private static JTextField clubNameField;
    private JButton saveButton;
    private final Player originalPlayer;

    public PlayerEditDialog(JFrame parent, Player player) {
        super(parent, "Editar Jogador", true);
        this.originalPlayer = player;
        setupUI(player);
        pack();
        setLocationRelativeTo(parent);
        getRootPane().setBorder(new EmptyBorder(0, 10, 10, 10));
    }

    private void setupUI(Player player) {
        JPanel filterFields = new JPanel(new GridBagLayout());
        setPreferredSize(new Dimension(450, 450));
        filterFields.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel idLabel = new JLabel("Id:");
        filterFields.add(idLabel, gbc);
        gbc.gridy++;
        JTextField idField = new JTextField(30);
        idField.setText(String.valueOf(player.getId()));
        idField.setEditable(false);
        filterFields.add(idField, gbc);

        gbc.gridy++;
        String ageValidate = (player.getAge() <= 0) ? "" : String.valueOf(player.getAge());
        GUIUtils.FieldPanel ageFieldPanel = GUIUtils.inputField("Idade:", 200, 10, true, 'y', ageValidate);
        ageField = ageFieldPanel.textField;
        addDocumentListener(ageField);
        filterFields.add(ageFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel playerNameFieldPanel = GUIUtils.inputField("Nome do Jogador:", 200, 40, false, 'y', player.getPlayerName());
        playerNameField = playerNameFieldPanel.textField;
        addDocumentListener(playerNameField);
        filterFields.add(playerNameFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel nationalityFieldPanel = GUIUtils.inputField("Nacionalidade:", 200, 40, false, 'y', player.getNationality());
        nationalityField = nationalityFieldPanel.textField;
        addDocumentListener(nationalityField);
        filterFields.add(nationalityFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel clubNameFieldPanel = GUIUtils.inputField("Nome do Clube:", 200, 40, false, 'y', player.getClubName());
        clubNameField = clubNameFieldPanel.textField;
        addDocumentListener(clubNameField);
        filterFields.add(clubNameFieldPanel.panel, gbc);

        gbc.gridy++;
        saveButton = new JButton("Salvar");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> {
            String ageText = ageField.getText().trim();
            String playerName = playerNameField.getText().trim();
            String nationality = nationalityField.getText().trim();
            String clubName = clubNameField.getText().trim();

            int age = ageText.isEmpty() ? -1 : Integer.parseInt(ageText);
            playerName = playerName.isEmpty() ? "NULO" : playerName;
            nationality = nationality.isEmpty() ? "NULO" : nationality;
            clubName = clubName.isEmpty() ? "NULO" : clubName;

            MainFrame.updatePlayer(
                    new Player(
                            player.getId(),
                            age,
                            playerName,
                            nationality,
                            clubName
                    ),
                    PlayerTable.table
            );
            dispose();
        });
        filterFields.add(saveButton, gbc);

        gbc.gridy++;
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        filterFields.add(cancelButton, gbc);

        add(filterFields);
    }

    private void addDocumentListener(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkFields();
            }

            public void removeUpdate(DocumentEvent e) {
                checkFields();
            }

            public void changedUpdate(DocumentEvent e) {
                checkFields();
            }
        });
    }

    private void checkFields() {
        saveButton.setEnabled(hasChanges());
    }

    private boolean hasChanges() {
        return !String.valueOf(originalPlayer.getAge()).equals(ageField.getText().trim()) ||
                !originalPlayer.getPlayerName().equals(playerNameField.getText().trim()) ||
                !originalPlayer.getNationality().equals(nationalityField.getText().trim()) ||
                !originalPlayer.getClubName().equals(clubNameField.getText().trim());
    }
}
