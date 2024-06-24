package com.gui;

import java.util.List;

/**
 * Classe que representa o objeto de resposta (DTO) contendo uma lista de jogadores.
 */
public class ResponseDto {
    private final List<Player> PlayerList;

    /**
     * Construtor da classe ResponseDto.
     * 
     * @param PlayerList a lista de jogadores
     */
    public ResponseDto(List<Player> PlayerList) {
        this.PlayerList = PlayerList;
    }

    /**
     * Obtém a lista de jogadores.
     * 
     * @return a lista de jogadores
     */
    public List<Player> getPlayerList() {
        return PlayerList;
    }
}
