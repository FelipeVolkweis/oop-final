package com.gui;

/**
 * Representa um jogador de futebol. Esta classe é definida como um record, o que implica
 * que todos os seus campos são finais e privados por padrão, e que métodos de acesso
 * são automaticamente fornecidos.
 */
public record Player(int id, int age, String playerName, String nationality, String clubName) {

    /**
     * Retorna uma representação em string do jogador. Este método sobrescreve o método
     * toString padrão fornecido pelo record para customizar a forma como os dados do jogador
     * são apresentados.
     *
     * @return A representação em string do jogador, formatada para mostrar claramente cada
     *         atributo do jogador.
     */
    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", age=" + age +
                ", playerName='" + playerName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", clubName='" + clubName + '\'' +
                '}';
    }

    /**
     * Retorna o nome do jogador.
     *
     * @return O nome do jogador.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Retorna o nome do clube do jogador.
     *
     * @return O nome do clube do jogador.
     */
    public String getClubName() {
        return clubName;
    }

    /**
     * Retorna a nacionalidade do jogador.
     *
     * @return A nacionalidade do jogador.
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * Retorna a idade do jogador.
     *
     * @return A idade do jogador.
     */
    public int getAge() {
        return age;
    }

    /**
     * Retorna o ID do jogador.
     *
     * @return O ID do jogador.
     */
    public int getId() {
        return id;
    }
}