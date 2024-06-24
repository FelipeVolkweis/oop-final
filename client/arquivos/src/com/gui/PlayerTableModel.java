package com.gui;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class PlayerTableModel extends DefaultTableModel {
    private static final String[] columnNames = {"ID", "Idade", "Nome do Jogador", "Nacionalidade", "Nome do Clube", "Editar", "Excluir"};
    private final List<Player> players;

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

    public void removePlayerById(int playerId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == playerId) {
                players.remove(i);
                removeRow(i);
                break;
            }
        }
    }

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


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5 || columnIndex == 6;
    }

    public Player getPlayerAt(int rowIndex) {
        return players.get(rowIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1 ->
                    Integer.class;
            default -> String.class;
        };
    }

}
