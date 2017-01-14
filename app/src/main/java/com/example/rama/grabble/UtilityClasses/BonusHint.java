package com.example.rama.grabble.UtilityClasses;

/**
 * Created by Ramona on 06/01/2017.
 */

public class BonusHint {
    private String message;
    private int value;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public BonusHint(String message, int value) {
        this.message = message;

        this.value = value;
    }
}
