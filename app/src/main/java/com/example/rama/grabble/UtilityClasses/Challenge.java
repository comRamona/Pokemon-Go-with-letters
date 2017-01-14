package com.example.rama.grabble.UtilityClasses;

/**
 * Challenges to be stored in the database, containing a description and complete status
 */

public class Challenge {
    private String description;
    private boolean completed;

    public Challenge(){
        this("");
    }

    public Challenge(String description){
        this.description=description;
        this.completed=false;
    }

    public Challenge(boolean completed, String description) {
        this.completed = completed;
        this.description = description;
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


}