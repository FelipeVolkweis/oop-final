package com.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

/**
 * Representa uma tabela de jogadores.
 * Esta classe é responsável por exibir os jogadores em uma tabela, permitindo a ordenação e edição dos dados.
 */
public class PlayerTable {
    private static JPanel tablePanel; // Painel que contém a tabela e os GIFs
    private final JPanel gifPanel; // Painel específico para mostrar GIFs
    public static JTable table; // A tabela que exibirá os dados dos jogadores

    /**
     * Construtor que inicializa a tabela e os painéis de GIF.
     */
    public PlayerTable() {
        tablePanel = new JPanel(new BorderLayout());

        gifPanel = new JPanel(new GridBagLayout()); // Configura o layout para o painel de GIFs
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        // Adiciona cada GIF ao painel com o ícone redimensionado
        gifPanel.add(new JLabel(resizeGif(new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/gif1.gif"))))), gbc);
        gbc.gridx = 1;
        gifPanel.add(new JLabel(resizeGif(new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/gif2.gif"))))), gbc);
        gbc.gridx = 2;
        gifPanel.add(new JLabel(resizeGif(new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/gif3.gif"))))), gbc);

        tablePanel.add(gifPanel, BorderLayout.CENTER);
    }

    /**
     * Redimensiona uma imagem GIF para as dimensões desejadas.
     *
     * @param icon O ImageIcon contendo a imagem GIF a ser redimensionada.
     * @return Um novo ImageIcon com a imagem GIF redimensionada.
     */
    private ImageIcon resizeGif(ImageIcon icon) {
        Image image = icon.getImage().getScaledInstance(300, 200, Image.SCALE_DEFAULT);
        return new ImageIcon(image);
    }

    /**
     * Retorna o painel que contém a tabela de jogadores.
     *
     * @return O painel que contém a tabela de jogadores.
     */
    public static JPanel getTablePanel() {
        return tablePanel;
    }

    /**
     * Atualiza a tabela de jogadores com base na lista fornecida.
     * 
     * @param players A lista de jogadores a ser exibida na tabela.
     */
    public static void updateTable(List<Player> players) {
        PlayerTableModel model = new PlayerTableModel(players);
        table = new JTable(model);

        TableRowSorter<PlayerTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        sorter.setSortable(5, false);
        sorter.setSortable(6, false);

        ImageIcon editIcon = loadIcon("./resources/edit_icon.png");
        ImageIcon deleteIcon = loadIcon("./resources/delete_icon.png");

        table.getColumnModel().getColumn(5).setCellRenderer(new IconRenderer(editIcon));
        table.getColumnModel().getColumn(6).setCellRenderer(new IconRenderer(deleteIcon));
        table.getColumnModel().getColumn(5).setCellEditor(new IconEditor(editIcon, table));
        table.getColumnModel().getColumn(6).setCellEditor(new IconEditor(deleteIcon, table));

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new SortableHeaderRenderer(header.getDefaultRenderer()));

        JScrollPane scrollPane = new JScrollPane(table);

        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(34, 34, 59));
        table.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            if (columnIndex != 5 && columnIndex != 6) {
                table.getColumnModel().getColumn(columnIndex).setCellRenderer(centerRenderer);
            }
        }

        tablePanel.removeAll();
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    /**
     * Classe interna que implementa a interface TableCellRenderer para renderizar o cabeçalho de uma tabela com capacidade de ordenação.
     */
    static class SortableHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;

        /**
         * Construtor da classe SortableHeaderRenderer.
         * @param delegate O TableCellRenderer delegado para renderizar o cabeçalho da tabela.
         */
        public SortableHeaderRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        /**
         * Método que retorna o componente de renderização para o cabeçalho da tabela.
         * @param table A tabela em que o cabeçalho está sendo renderizado.
         * @param value O valor do cabeçalho.
         * @param isSelected Indica se o cabeçalho está selecionado.
         * @param hasFocus Indica se o cabeçalho tem o foco.
         * @param row O número da linha do cabeçalho.
         * @param column O número da coluna do cabeçalho.
         * @return O componente de renderização para o cabeçalho da tabela.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel label) {
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setForeground(Color.WHITE);

                if (column != 5 && column != 6) {
                    RowSorter<?> sorter = table.getRowSorter();
                    List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
                    boolean isSorted = sortKeys.stream().anyMatch(sk -> sk.getColumn() == column);

                    if (!isSorted) {
                        label.setIcon(getIconForSortOrder(SortOrder.ASCENDING));
                    } else {
                        for (RowSorter.SortKey sortKey : sortKeys) {
                            if (sortKey.getColumn() == column) {
                                SortOrder sortOrder = sortKey.getSortOrder();
                                label.setIcon(getIconForSortOrder(sortOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING));
                                break;
                            }
                        }
                    }
                } else {
                    label.setIcon(null);
                }
            }
            return c;
        }

        /**
         * Método privado que retorna o ícone correspondente à ordem de classificação especificada.
         * @param sortOrder A ordem de classificação.
         * @return O ícone correspondente à ordem de classificação.
         */
        private Icon getIconForSortOrder(SortOrder sortOrder) {
            String iconKey = sortOrder == SortOrder.ASCENDING ? "Table.descendingSortIcon" : "Table.ascendingSortIcon";
            Icon icon = UIManager.getIcon(iconKey);
            BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.setComposite(AlphaComposite.SrcAtop);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
            g.dispose();
            return new ImageIcon(bi);
        }
    }

    /**
     * Exibe os GIFs na tabela.
     */
    public void showGifs() {
        tablePanel.removeAll();
        tablePanel.add(gifPanel, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    /**
     * Carrega um ícone a partir de um caminho específico.
     * 
     * @param path O caminho do ícone a ser carregado.
     * @return O ícone carregado.
     */
    private static ImageIcon loadIcon(String path) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(PlayerTable.class.getResource(path)));
        Image image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * Classe interna estática que representa um renderizador de células de tabela com ícone.
     * Estende a classe DefaultTableCellRenderer.
     */
    static class IconRenderer extends DefaultTableCellRenderer {
        private final ImageIcon icon;

        /**
         * Construtor da classe IconRenderer.
         * @param icon O ícone a ser exibido nas células da tabela.
         */
        public IconRenderer(ImageIcon icon) {
            this.icon = icon;
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
        }

        /**
         * Sobrescreve o método getTableCellRendererComponent da classe pai.
         * Configura o ícone e remove o texto da célula.
         * @param table A tabela em que a célula está sendo renderizada.
         * @param value O valor da célula.
         * @param isSelected Indica se a célula está selecionada.
         * @param hasFocus Indica se a célula possui foco.
         * @param row O índice da linha da célula.
         * @param column O índice da coluna da célula.
         * @return A própria instância do renderizador.
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setIcon(icon);
            setText("");
            return this;
        }
    }

    /**
     * Classe interna que implementa a interface TableCellEditor e serve como editor de células para a tabela de jogadores.
     * Esta classe é responsável por exibir um botão com um ícone em cada célula da tabela, permitindo a edição ou exclusão de um jogador.
     */
    static class IconEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private String currentValue;
        private final ImageIcon icon;

        /**
         * Construtor da classe IconEditor.
         * 
         * @param icon  O ícone a ser exibido no botão.
         * @param table A tabela de jogadores.
         */
        public IconEditor(ImageIcon icon, JTable table) {
            this.icon = icon;
            button.addActionListener(e -> {
                if ("delete".equals(currentValue)) {
                    int row = table.convertRowIndexToModel(table.getEditingRow());
                    Player player = ((PlayerTableModel) table.getModel()).getPlayerAt(row);

                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(table);
                    int confirm = JOptionPane.showConfirmDialog(frame,
                            "Deseja realmente excluir o jogador " + player.getPlayerName() + " do clube " + player.getClubName() + "?",
                            "Confirmar Exclusão",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        MainFrame.deletePlayer(player.getId(), () -> ((PlayerTableModel) table.getModel()).removePlayerById(player.getId()));
                    }
                }
                if ("edit".equals(currentValue)) {
                    int row = table.convertRowIndexToModel(table.getEditingRow());
                    Player player = ((PlayerTableModel) table.getModel()).getPlayerAt(row);

                    MainFrame.onEditButtonClicked(player);
                }
                fireEditingStopped();
            });
        }

        /**
         * Retorna o valor atual do editor de célula.
         * 
         * @return O valor atual do editor de célula.
         */
        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }

        /**
         * Retorna o componente que será usado como editor de célula para a célula especificada.
         * 
         * @param table      A tabela de jogadores.
         * @param value      O valor da célula.
         * @param isSelected Indica se a célula está selecionada.
         * @param row        O índice da linha da célula.
         * @param column     O índice da coluna da célula.
         * @return O componente que será usado como editor de célula.
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentValue = (String) value;
            button.setIcon(icon);
            return button;
        }
    }
}
