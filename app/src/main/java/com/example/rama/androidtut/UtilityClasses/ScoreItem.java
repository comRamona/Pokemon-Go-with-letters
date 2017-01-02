package com.example.rama.androidtut.UtilityClasses;

/**
 * Created by Ramona on 02/01/2017.
 */


public class ScoreItem {

    private String name;
    private int score;

    public ScoreItem() {
    }

    public ScoreItem(String name, int score) {

        this.name = name;
        this.score = score;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}