package com.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por realizar a análise e conversão de um JSON em um objeto ResponseDto.
 */
public class JsonParser {
    /**
     * Realiza a análise e conversão de um JSON em um objeto ResponseDto.
     * 
     * @param json O JSON a ser analisado e convertido.
     * @return O objeto ResponseDto resultante da análise e conversão do JSON.
     */
    public static ResponseDto parseResponse(String json) {
        int payloadStart = json.indexOf("\"payload\":[") + "\"payload\":[".length();
        int payloadEnd = json.indexOf("],\"status\"");
        String payload = json.substring(payloadStart, payloadEnd);

        payload = payload.substring(1, payload.length() - 2);

        List<Player> playerList = parsePayload(payload);

        return new ResponseDto(playerList);
    }
    /**
     * Realiza a análise e conversão do payload do JSON em uma lista de objetos Player.
     * 
     * @param payload O payload do JSON a ser analisado e convertido.
     * @return A lista de objetos Player resultante da análise e conversão do payload do JSON.
     */
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
    /**
     * Verifica se uma string é nula e retorna uma string vazia caso seja.
     * 
     * @param str A string a ser verificada.
     * @return A string vazia se a string de entrada for nula, caso contrário, retorna a própria string de entrada.
     */
    private static String checkNullString(String str) {
        if (str.equals("\"null\"")) return "";
        return str;
    }
    /**
     * Verifica se um número é nulo e retorna 0 caso seja.
     * 
     * @param str O número a ser verificado.
     * @return 0 se o número de entrada for nulo, caso contrário, retorna o próprio número de entrada.
     */
    private static int checkNullNumber(String str) {
        if (str.equals("\"null\"")) return 0;
        return Integer.parseInt(str);
    }
    /**
     * Remove as aspas de uma string, se existirem.
     * 
     * @param str A string a ser modificada.
     * @return A string sem as aspas, se existirem.
     */
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

