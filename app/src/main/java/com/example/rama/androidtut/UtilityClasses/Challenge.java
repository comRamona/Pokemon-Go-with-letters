package com.example.rama.androidtut.UtilityClasses;

/**
 * Created by Ramona on 04/01/2017.
 */

public class Challenge {
    private String description;
    private boolean completed;
    private int rank;

    public Challenge(){
        this("");
    }

    public Challenge(String description){
        this.description=description;
        this.completed=false;
        this.rank=0;
    }

    public Challenge(boolean completed, String description, int rank) {
        this.completed = completed;
        this.description = description;
        this.rank = rank;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
