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
        // Encontra o índice inicial do payload dentro do JSON
        int payloadStart = json.indexOf("\"payload\":[") + "\"payload\":[".length();
        // Encontra o índice final do payload dentro do JSON
        int payloadEnd = json.indexOf("],\"status\"");
        // Extrai a substring que contém o payload
        String payload = json.substring(payloadStart, payloadEnd);

        // Remove os colchetes iniciais e finais do payload
        payload = payload.substring(1, payload.length() - 2);

        // Analisa o payload para criar a lista de jogadores
        List<Player> playerList = parsePayload(payload);

        // Retorna um objeto ResponseDto com a lista de jogadores
        return new ResponseDto(playerList);
    }
    /**
     * Realiza a análise e conversão do payload do JSON em uma lista de objetos Player.
     * 
     * @param payload O payload do JSON a ser analisado e convertido.
     * @return A lista de objetos Player resultante da análise e conversão do payload do JSON.
     */
    private static List<Player> parsePayload(String payload) {
        // Divide o payload em strings de jogadores individuais
        String[] players = payload.split("},\\{");

        List<Player> playerList = new ArrayList<>();

        // Itera sobre cada string de jogador e converte em um objeto Player
        for (String player : players) {
            // Divide os dados do jogador em atributos individuais
            String[] splitPlayerData = player.split(",");
            // Extrai e converte o ID do jogador
            int id = Integer.parseInt(splitPlayerData[0].split(":")[1].trim());
            // Extrai e converte a idade do jogador, verificando se é nulo
            int idade = checkNullNumber(splitPlayerData[1].split(":")[1].trim());
            // Extrai e processa o nome do jogador, removendo aspas e verificando se é nulo
            String nomeJogador = removeQuote(
                    checkNullString(
                            splitPlayerData[2].split(":")[1].trim()
                    )
            );
            // Extrai e processa a nacionalidade do jogador, removendo aspas e verificando se é nulo
            String nacionalidade = removeQuote(
                    checkNullString(
                            splitPlayerData[3].split(":")[1].trim()
                    )
            );
            // Extrai e processa o nome do clube do jogador, removendo aspas e verificando se é nulo
            String nomeClube = removeQuote(
                    checkNullString(
                            splitPlayerData[4].split(":")[1].trim()
                    )
            );
            // Adiciona o novo objeto Player à lista de jogadores
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

