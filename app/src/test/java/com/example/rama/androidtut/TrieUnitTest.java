package com.example.rama.androidtut;

import com.example.rama.androidtut.UtilityClasses.Trie;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    /**
     * Test loading the entire grabble dictionary and retrieving words from it
     */
    @Test
    public void loadFullDictionary(){
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("grabdict");
        Scanner sc=new Scanner(in);
        while (sc.hasNextLine()) {
            String word = sc.nextLine().toUpperCase();
            trie.add(word);
        }
        sc.close();
        assertTrue(trie.contains("MILITIA"));
        assertFalse(trie.contains("QWERTYU"));
    }


}
