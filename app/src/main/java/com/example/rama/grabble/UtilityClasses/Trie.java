package com.example.rama.grabble.UtilityClasses;


import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a Trie data structure(prefix tree). You can add words and search for
 * prefixes. The time complexity of searching for a word should be O(length of word), since each TrieNode
 * stores a letter, achieving linear search.
 * All words are stored in upper-case, to remove need for case sensitivity.
 */
class TrieNode {
    protected TrieNode[] children;
    protected boolean isLeaf;

    protected TrieNode() {
        children = new TrieNode[26];
        isLeaf = false;
    }

    protected TrieNode get(Character c) {
        int key = c - 'A';
        if (key >= 26 || key < 0) return null;
        return children[key];
    }

    protected boolean put(Character c, TrieNode node) {
        int key = c - 'A';
        if (key >= 26 || key < 0) return false;
        children[key] = node;
        return true;
    }
}

public class Trie {
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    /**
     * Add word to dictionary
     *
     * @param s
     */
    public void add(String s) {
        s = s.toUpperCase();
        TrieNode node = root;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (node.get(c) == null) node.put(c, new TrieNode());
            node = node.get(c);
        }
        node.isLeaf = true;

    }

    /**
     * Check whether a word appears in dictionary.
     *
     * @param word to search for
     * @return whether it is contained in dictionary
     */
    public boolean contains(String word) {
        word = word.toUpperCase();
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (node.get(ch) == null) {
                return false;
            }
            node = node.get(ch);
        }
        return node.isLeaf;
    }

    /**
     * Get all words that start with a given prefix.
     *
     * @param prefix to search for
     * @return a list of words
     */
    public List<String> getAllWordsStartingWith(String prefix) {
        prefix = prefix.toUpperCase();
        TrieNode node = root;
        List<String> li = new ArrayList<>();
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            if (node.get(ch) == null) {
                return li;
            }
            node = node.get(ch);
        }

        dfs(node, li, prefix);
        return li;
    }

    /**
     * Helper function for getting all words starting with a string.
     * Performs a depth-first search to get all letters from a word, stopping at leafs.
     *
     * @param node    current node in the Trie
     * @param prev    list of all words found so far
     * @param current part of word traversed so far
     */
    private void dfs(TrieNode node, List<String> prev, String current) {
        if (node.isLeaf) {
            prev.add(current);
        }

        for (int i = 0; i < 26; i++) {
            TrieNode child = node.children[i];
            if (child != null) {
                char c = (char) ('A' + i);
                dfs(child, prev, current + c);

            }


        }
    }

}

