package com.gui;

public record Player(int id, int age, String playerName, String nationality, String clubName) {

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

    public String getPlayerName() {
        return playerName;
    }

    public String getClubName() {
        return clubName;
    }

    public String getNationality() {
        return nationality;
    }

    public int getAge() {
        return age;
    }

    public int getId() {
        return id;
    }
}