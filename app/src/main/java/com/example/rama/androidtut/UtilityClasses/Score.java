package com.example.rama.androidtut.UtilityClasses;

/**
 * Scores and names of users.
 * Can be stored directly in nosql database.
 */

public class Score {
    private String name;
    private int score;

    public Score(){
        this("");
    }

    public Score(String name){
        this.name=name;
        this.score=0;
    }

    public Score(String name,int score) {
        this.score = score;
        this.name = name;
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
        this.score=score;
    }

}