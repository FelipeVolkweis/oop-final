package com.gui;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Classe que representa o modelo de tabela para exibição dos jogadores.
 */
public class PlayerTableModel extends DefaultTableModel {
    private static final String[] columnNames = {"ID", "Idade", "Nome do Jogador", "Nacionalidade", "Nome do Clube", "Editar", "Excluir"};
    private final List<Player> players;

    /**
     * Construtor da classe PlayerTableModel.
     * 
     * @param players A lista de jogadores a ser exibida na tabela.
     */
    public PlayerTableModel(List<Player> players) {
        super(columnNames, 0);
        this.players = players;
        for (Player player : players) {
            Object[] row = {
                    player.id(),
                    player.age() == -1 || player.age() == 0 ? "" : player.age(),
                    player.playerName(),
                    player.nationality(),
                    player.clubName(),
                    "edit",
                    "delete"
            };
            addRow(row);
        }
    }

    /**
     * Remove um jogador da tabela pelo seu ID.
     * 
     * @param playerId O ID do jogador a ser removido.
     */
    public void removePlayerById(int playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == playerId) {
                players.remove(i);
                removeRow(i);
                break;
            }
        }
    }

    /**
     * Atualiza as informações de um jogador na tabela.
     * 
     * @param updatedPlayer O jogador atualizado.
     */
    public void updatePlayer(Player updatedPlayer) {
        boolean updated = false;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).id() == updatedPlayer.id()) {
                Player playerToUpdate = new Player(
                        updatedPlayer.getId(),
                        updatedPlayer.getAge(),
                        updatedPlayer.getPlayerName().equals("NULO") ? "" : updatedPlayer.getPlayerName(),
                        updatedPlayer.getNationality().equals("NULO") ? "" : updatedPlayer.getNationality(),
                        updatedPlayer.getClubName().equals("NULO") ? "" : updatedPlayer.getClubName()
                );
                players.set(i, playerToUpdate);
                updated = true;
                break;
            }
        }
        if (updated) {
            PlayerTable.updateTable(players);
        }
    }

    /**
     * Verifica se uma célula da tabela é editável.
     * 
     * @param rowIndex    O índice da linha da célula.
     * @param columnIndex O índice da coluna da célula.
     * @return true se a célula for editável, false caso contrário.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5 || columnIndex == 6;
    }

    /**
     * Obtém o jogador em uma determinada linha da tabela.
     * 
     * @param rowIndex O índice da linha.
     * @return O jogador na linha especificada.
     */
    public Player getPlayerAt(int rowIndex) {
        return players.get(rowIndex);
    }

    /**
     * Obtém a classe da coluna especificada.
     * 
     * @param columnIndex O índice da coluna.
     * @return A classe da coluna.
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1 ->
                    Integer.class;
            default -> String.class;
        };
    }
}
