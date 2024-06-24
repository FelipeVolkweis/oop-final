package com.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Esta classe representa a barra lateral esquerda da interface gráfica.
 * Ela estende a classe JPanel e contém componentes para filtrar e pesquisar jogadores.
 */
public class LeftBar extends JPanel {
    private static JLabel fileStatusLabel;
    private static JButton searchButton;
    private static JButton cleanButton;
    private static JButton loadAllButton;
    private static JTextField idField;
    private static JTextField ageField;
    private static JTextField playerNameField;
    private static JTextField nationalityField;
    private static JTextField clubNameField;

    public LeftBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 20));
        Border rightLine = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK);
        Border padding = new EmptyBorder(10, 20, 20, 20);
        setBorder(new CompoundBorder(rightLine, padding));
        setPreferredSize(new Dimension(300, Integer.MAX_VALUE));

        // aviso de qual arquivo foi carregado
        JPanel loadFile = new JPanel();
        loadFile.setLayout(new BoxLayout(loadFile, BoxLayout.Y_AXIS));
        JLabel loadFileLabel = new JLabel("Arquivo carregado:");

        fileStatusLabel = new JLabel("Nenhum arquivo foi carregado");
        Font font = new Font("Arial", Font.PLAIN, 12);
        fileStatusLabel.setFont(font);

        loadFile.add(loadFileLabel);
        loadFile.add(fileStatusLabel);

        // botao de listar todos jogadores
        JPanel listAllPlayers = new JPanel();
        listAllPlayers.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        loadAllButton = new JButton("Listar todos jogadores");
        listAllPlayers.add(loadAllButton);

        // campos para filtrar
        JPanel filterFields = new JPanel(new GridBagLayout());
        filterFields.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        filterFields.add(Box.createHorizontalStrut(200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;


        GUIUtils.FieldPanel idFieldPanel = GUIUtils.inputField("ID:", 250, 10, true, 'y');
        idField = idFieldPanel.textField;
        filterFields.add(idFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel ageFieldPanel = GUIUtils.inputField("Idade:", 200, 10, true, 'y');
        ageField = ageFieldPanel.textField;
        filterFields.add(ageFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel playerNameFieldPanel = GUIUtils.inputField("Nome do Jogador:", 200, 40, false, 'y');
        playerNameField = playerNameFieldPanel.textField;
        filterFields.add(playerNameFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel nationalityFieldPanel = GUIUtils.inputField("Nacionalidade:", 200, 40, false, 'y');
        nationalityField = nationalityFieldPanel.textField;
        filterFields.add(nationalityFieldPanel.panel, gbc);

        gbc.gridy++;
        GUIUtils.FieldPanel clubNameFieldPanel = GUIUtils.inputField("Nome do Clube:", 200, 40, false, 'y');
        clubNameField = clubNameFieldPanel.textField;
        filterFields.add(clubNameFieldPanel.panel, gbc);

        // botões para pesquisar
        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchButton = new JButton("Pesquisar");
        cleanButton = new JButton("Limpar campos");
        filterButtons.add(searchButton);
        filterButtons.add(Box.createHorizontalStrut(10));
        filterButtons.add(cleanButton);

        add(loadFile);
        add(listAllPlayers);
        add(filterFields);
        add(filterButtons);

        loadAllButton.addActionListener(e -> MainFrame.getAllPlayers(fileStatusLabel.getText()));

        setupFieldListeners();

        searchButton.addActionListener(e -> MainFrame.selectPlayers());
        cleanButton.addActionListener(e -> clearFields());

        setComponentsEnabled(false);
    }

    /**
     * Habilita ou desabilita os componentes da barra lateral esquerda.
     * 
     * @param enabled true para habilitar os componentes, false para desabilitar
     */
    public static void setComponentsEnabled(boolean enabled) {
        loadAllButton.setEnabled(enabled);
        idField.setEnabled(enabled);
        ageField.setEnabled(enabled);
        playerNameField.setEnabled(enabled);
        nationalityField.setEnabled(enabled);
        clubNameField.setEnabled(enabled);
        cleanButton.setEnabled(enabled);
        searchButton.setEnabled(enabled);
    }

    /**
     * Atualiza o nome do arquivo selecionado na barra lateral.
     * 
     * @param selectFileName O nome do arquivo selecionado. Pode ser nulo.
     */
    public static void updateSelectFile(String selectFileName) {
        fileStatusLabel.setText(
                selectFileName != null ? selectFileName : "Nenhum arquivo foi carregado"
        );
    }

    /**
     * Verifica se os campos possuem conteúdo.
     * 
     * @return true se pelo menos um dos campos tiver conteúdo, caso contrário, retorna false.
     */
    public static boolean checkFieldsForContent() {
        return !idField.getText().trim().isEmpty() ||
                !ageField.getText().trim().isEmpty() ||
                !playerNameField.getText().trim().isEmpty() ||
                !nationalityField.getText().trim().isEmpty() ||
                !clubNameField.getText().trim().isEmpty();
    }

    /**
     * Atualiza o estado dos botões.
     * 
     * Verifica o conteúdo dos campos e habilita ou desabilita os botões de acordo com o resultado.
     */
    public static void updateButtonState() {
        searchButton.setEnabled(checkFieldsForContent());
        cleanButton.setEnabled(checkFieldsForContent());
    }

    /**
     * Configura os ouvintes de campo.
     * 
     * Adiciona um DocumentListener aos campos de ID, idade, nome do jogador, nacionalidade e nome do clube.
     * O DocumentListener é responsável por atualizar o estado do botão.
     */
    private void setupFieldListeners() {
        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
        };

        idField.getDocument().addDocumentListener(documentListener);
        ageField.getDocument().addDocumentListener(documentListener);
        playerNameField.getDocument().addDocumentListener(documentListener);
        nationalityField.getDocument().addDocumentListener(documentListener);
        clubNameField.getDocument().addDocumentListener(documentListener);
    }

    /**
     * Limpa os campos de entrada de dados.
     */
    private void clearFields() {
        idField.setText("");
        ageField.setText("");
        playerNameField.setText("");
        nationalityField.setText("");
        clubNameField.setText("");
        updateButtonState();
    }

    /**
     * Constrói uma consulta para um arquivo específico.
     * 
     * @param fileName O nome do arquivo.
     * @return A consulta construída.
     */
    static String buildQuery(String fileName) {
        StringBuilder queryBuilder = new StringBuilder(String.format("3 %s.bin 1\n", fileName));
        int fieldCount = 0;
        StringBuilder fieldsBuilder = new StringBuilder();

        if (!idField.getText().trim().isEmpty()) {
            fieldsBuilder.append("id ").append(idField.getText().trim());
            fieldCount++;
        }
        if (!ageField.getText().trim().isEmpty()) {
            if (fieldCount > 0) {
                fieldsBuilder.append(" ");
            }
            fieldsBuilder.append("idade ").append(ageField.getText().trim());
            fieldCount++;
        }
        if (!playerNameField.getText().trim().isEmpty()) {
            if (fieldCount > 0) {
                fieldsBuilder.append(" ");
            }
            fieldsBuilder.append("nomeJogador \"").append(playerNameField.getText().trim()).append("\"");
            fieldCount++;
        }
        if (!nationalityField.getText().trim().isEmpty()) {
            if (fieldCount > 0) {
                fieldsBuilder.append(" ");
            }
            fieldsBuilder.append("nacionalidade \"").append(nationalityField.getText().trim()).append("\"");
            fieldCount++;
        }
        if (!clubNameField.getText().trim().isEmpty()) {
            if (fieldCount > 0) {
                fieldsBuilder.append(" ");
            }
            fieldsBuilder.append("nomeClube \"").append(clubNameField.getText().trim()).append("\"");
            fieldCount++;
        }

        queryBuilder.append(fieldCount).append(" ").append(fieldsBuilder);

        return queryBuilder.toString();
    }
}
