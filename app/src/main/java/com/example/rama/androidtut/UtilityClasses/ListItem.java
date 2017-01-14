package com.example.rama.androidtut.UtilityClasses;

/**
 * ListItem class for anay items with 2 fields that can be displayed in a table.
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