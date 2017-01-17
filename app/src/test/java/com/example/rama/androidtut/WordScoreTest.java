package com.example.rama.androidtut;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Calculate score for a word according to letter values
 */

public class WordScoreTest {
    @Test
    public void testScore(){
        int sc=WordArenaActivity.calculateScore("SEVENTY");
        assertEquals(sc,54);
    }
}
