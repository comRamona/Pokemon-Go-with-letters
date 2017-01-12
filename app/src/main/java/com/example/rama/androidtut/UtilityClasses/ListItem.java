package com.example.rama.androidtut.UtilityClasses;

/**
 * Created by Ramona on 02/01/2017.
 */


public class ListItem {

    private String fieldOne;
    private String fieldTwo;

    public ListItem() {
    }

    public ListItem(String fieldOne, String fieldTwo) {

        this.fieldOne = fieldOne;
        this.fieldTwo = fieldTwo;

    }

    public String getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    public String getFieldTwo() {
        return fieldTwo;
    }

    public void setFieldTwo(String fieldTwo) {
        this.fieldTwo = fieldTwo;
    }

}