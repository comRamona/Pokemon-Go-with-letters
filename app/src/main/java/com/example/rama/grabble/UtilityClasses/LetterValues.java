package com.example.rama.grabble.UtilityClasses;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps all Grabble letters to their in game value.
 */

public class LetterValues {

    private static Map<Character,Integer> map;
    public static int getValue(char c){
        if(map==null) initializeMap();
        return map.get(c);
    }
    private static void initializeMap(){
        map=new HashMap<>();
        map.put('A',3);
        map.put('B',20);
        map.put('C',13);
        map.put('D',10);
        map.put('E',1);
        map.put('F',15);
        map.put('G',18);
        map.put('H',9);
        map.put('I',5);
        map.put('J',25);
        map.put('K',22);
        map.put('L',11);
        map.put('M',14);
        map.put('N',6);
        map.put('O',4);
        map.put('P',4);
        map.put('Q',24);
        map.put('R',8);
        map.put('S',7);
        map.put('T',2);
        map.put('U',12);
        map.put('V',21);
        map.put('W',17);
        map.put('X',23);
        map.put('Y',16);
        map.put('Z',26);
    }


}
