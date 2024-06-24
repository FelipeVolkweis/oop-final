package com.gui;

import java.util.List;

public class ResponseDto {
    private final List<Player> PlayerList;

    public ResponseDto(List<Player> PlayerList) {
        this.PlayerList = PlayerList;
    }

    public List<Player> getPlayerList() {
        return PlayerList;
    }
}
