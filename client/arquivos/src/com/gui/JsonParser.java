package com.gui;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    public static ResponseDto parseResponse(String json) {
        int payloadStart = json.indexOf("\"payload\":[") + "\"payload\":[".length();
        int payloadEnd = json.indexOf("],\"status\"");
        String payload = json.substring(payloadStart, payloadEnd);

        payload = payload.substring(1, payload.length() - 2);

        List<Player> playerList = parsePayload(payload);

        return new ResponseDto(playerList);
    }

    private static List<Player> parsePayload(String payload) {
        String[] players = payload.split("},\\{");

        List<Player> playerList = new ArrayList<>();

        for (String player : players) {
            String[] splitPlayerData = player.split(",");
            int id = Integer.parseInt(splitPlayerData[0].split(":")[1].trim());
            int idade = checkNullNumber(splitPlayerData[1].split(":")[1].trim());
            String nomeJogador = removeQuote(
                    checkNullString(
                            splitPlayerData[2].split(":")[1].trim()
                    )
            );
            String nacionalidade = removeQuote(
                    checkNullString(
                            splitPlayerData[3].split(":")[1].trim()
                    )
            );
            String nomeClube = removeQuote(
                    checkNullString(
                            splitPlayerData[4].split(":")[1].trim()
                    )
            );
            playerList.add(new Player(id, idade, nomeJogador, nacionalidade, nomeClube));
        }

        return playerList;
    }

    private static String checkNullString(String str) {
        if (str.equals("\"null\"")) return "";
        return str;
    }

    private static int checkNullNumber(String str) {
        if (str.equals("\"null\"")) return 0;
        return Integer.parseInt(str);
    }

    private static String removeQuote(String str) {
        if (str != null && !str.isEmpty()) {
            if (str.startsWith("\"")) {
                str = str.substring(1);
            }
            if (str.endsWith("\"")) {
                str = str.substring(0, str.length() - 1);
            }
        }
        return str;
    }
}

