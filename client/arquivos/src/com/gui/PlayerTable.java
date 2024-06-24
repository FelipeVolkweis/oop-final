package com.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

public class PlayerTable {
    private static JPanel tablePanel;
    private final JPanel gifPanel;
    public static JTable table;

    public PlayerTable() {
        tablePanel = new JPanel(new BorderLayout());

        gifPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gifPanel.add(new JLabel(resizeGif(new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/gif1.gif"))))), gbc);
        gbc.gridx = 1;
        gifPanel.add(new JLabel(resizeGif(new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/gif2.gif"))))), gbc);
        gbc.gridx = 2;
        gifPanel.add(new JLabel(resizeGif(new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/gif3.gif"))))), gbc);

        tablePanel.add(gifPanel, BorderLayout.CENTER);
    }

    private ImageIcon resizeGif(ImageIcon icon) {
        Image image = icon.getImage().getScaledInstance(300, 200, Image.SCALE_DEFAULT);
        return new ImageIcon(image);
    }

    public static JPanel getTablePanel() {
        return tablePanel;
    }

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

    static class SortableHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;

        public SortableHeaderRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

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

    public void showGifs() {
        tablePanel.removeAll();
        tablePanel.add(gifPanel, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
    }

    private static ImageIcon loadIcon(String path) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(PlayerTable.class.getResource(path)));
        Image image = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    static class IconRenderer extends DefaultTableCellRenderer {
        private final ImageIcon icon;

        public IconRenderer(ImageIcon icon) {
            this.icon = icon;
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setIcon(icon);
            setText("");
            return this;
        }
    }

    static class IconEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton();
        private String currentValue;
        private final ImageIcon icon;

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

        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentValue = (String) value;
            button.setIcon(icon);
            return button;
        }
    }
}
