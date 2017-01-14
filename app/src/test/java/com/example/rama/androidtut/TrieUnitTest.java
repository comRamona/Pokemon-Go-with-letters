package com.example.rama.androidtut;

import com.example.rama.androidtut.UtilityClasses.Trie;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test adding and retrieving words and prefixes from dictionary using
 * the implementation of Trie
 */

public class TrieUnitTest {
    private Trie trie;
    @Before
    public void initialize(){
        trie=new Trie();
        trie.add("Seventy");
        trie.add("Seventh");
        trie.add("Cathead");
    }
    @Test
    public void containsWord(){
      assertTrue(trie.contains("Seventy"));
    }

    @Test
    public void doesNotContainWord(){
        assertFalse(trie.contains("Seventq"));
    }

    @Test
    public void findWordsWithPrefix(){
        List<String> expected=new ArrayList<>();
        expected.add("SEVENTY");
        expected.add("SEVENTH");
        List<String> actual=trie.getAllWordsStartingWith("Sev");
        assertTrue(expected.containsAll(actual)&&expected.size()==actual.size());
    }
}
