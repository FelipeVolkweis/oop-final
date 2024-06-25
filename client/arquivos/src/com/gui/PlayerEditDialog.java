package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Esta classe representa um diálogo de edição de jogador.
 * É uma subclasse de JDialog e permite ao usuário editar as informações de um jogador.
 */
public class PlayerEditDialog extends JDialog {
    private static JTextField ageField;
    private static JTextField playerNameField;
    private static JTextField nationalityField;
    private static JTextField clubNameField;
    private JButton saveButton;
    private final Player originalPlayer; // Referência ao jogador original antes das edições

    /**
     * Construtor que configura o diálogo de edição.
     *
     * @param parent A janela principal que será o proprietário deste diálogo.
     * @param player O jogador que será editado.
     */
    public PlayerEditDialog(JFrame parent, Player player) {
        super(parent, "Editar Jogador", true); // Define o título e o modality
        this.originalPlayer = player; // Armazena o jogador original
        setupUI(player); // Configura a interface gráfica
        pack(); // Compacta o layout do diálogo
        setLocationRelativeTo(parent); // Posiciona o diálogo relativo à janela principal
        getRootPane().setBorder(new EmptyBorder(0, 10, 10, 10)); // Define uma borda para o conteúdo do diálogo
    }

    /**
     * Configura a interface do usuário para editar um jogador.
     * 
     * @param player O jogador a ser editado.
     */
    private void setupUI(Player player) {
        JPanel filterFields = new JPanel(new GridBagLayout());
        setPreferredSize(new Dimension(450, 450));
        filterFields.setBorder(new EmptyBorder(10, 10, 10, 10)); // Aplica uma borda interna para espaçamento
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Os componentes ocuparão todo o espaço horizontal
        gbc.insets = new Insets(5, 0, 5, 0); // Define o espaçamento entre os componentes
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Cria e configura campo não editável para o ID do jogador
        JLabel idLabel = new JLabel("Id:");
        filterFields.add(idLabel, gbc);
        gbc.gridy++;
        JTextField idField = new JTextField(30);
        idField.setText(String.valueOf(player.getId()));
        idField.setEditable(false);
        filterFields.add(idField, gbc);

        // Adiciona campo editável para a idade do jogador
        gbc.gridy++;
        String ageValidate = (player.getAge() <= 0) ? "" : String.valueOf(player.getAge());
        GUIUtils.FieldPanel ageFieldPanel = GUIUtils.inputField("Idade:", 200, 10, true, 'y', ageValidate);
        ageField = ageFieldPanel.textField;
        addDocumentListener(ageField);
        filterFields.add(ageFieldPanel.panel, gbc);

        // Adiciona campo editável para o nome do jogador
        gbc.gridy++;
        GUIUtils.FieldPanel playerNameFieldPanel = GUIUtils.inputField("Nome do Jogador:", 200, 40, false, 'y', player.getPlayerName());
        playerNameField = playerNameFieldPanel.textField;
        addDocumentListener(playerNameField);
        filterFields.add(playerNameFieldPanel.panel, gbc);

        // Adiciona campo editável para a nacionalidade do jogador
        gbc.gridy++;
        GUIUtils.FieldPanel nationalityFieldPanel = GUIUtils.inputField("Nacionalidade:", 200, 40, false, 'y', player.getNationality());
        nationalityField = nationalityFieldPanel.textField;
        addDocumentListener(nationalityField);
        filterFields.add(nationalityFieldPanel.panel, gbc);

        // Adiciona campo editável para o nome do clube do jogador
        gbc.gridy++;
        GUIUtils.FieldPanel clubNameFieldPanel = GUIUtils.inputField("Nome do Clube:", 200, 40, false, 'y', player.getClubName());
        clubNameField = clubNameFieldPanel.textField;
        addDocumentListener(clubNameField);
        filterFields.add(clubNameFieldPanel.panel, gbc);

        // Adiciona botão de salvar e configura a ação ao ser clicado
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

    /**
     * Adiciona um DocumentListener ao JTextField fornecido.
     * O DocumentListener é responsável por monitorar as alterações no documento do JTextField.
     * Sempre que houver uma inserção, remoção ou alteração no documento, o método checkFields() será chamado.
     *
     * @param textField O JTextField ao qual o DocumentListener será adicionado.
     */
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

    /**
     * Verifica os campos e habilita o botão de salvar se houver alterações.
     */
    private void checkFields() {
        saveButton.setEnabled(hasChanges());
    }

    /**
     * Verifica se houve alterações nos campos de edição do jogador.
     * 
     * @return true se houver alterações, caso contrário, false.
     */
    private boolean hasChanges() {
        return !String.valueOf(originalPlayer.getAge()).equals(ageField.getText().trim()) ||
                !originalPlayer.getPlayerName().equals(playerNameField.getText().trim()) ||
                !originalPlayer.getNationality().equals(nationalityField.getText().trim()) ||
                !originalPlayer.getClubName().equals(clubNameField.getText().trim());
    }
}
