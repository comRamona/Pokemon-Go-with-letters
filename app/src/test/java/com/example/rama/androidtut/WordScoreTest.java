package com.example.rama.androidtut;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Ramona on 16/01/2017.
 */

public class WordScoreTest {
    @Test
    public void testScore(){
        int sc=WordArenaActivity.calculateScore("SEVENTY");
        assertEquals(sc,54);
    }
}
