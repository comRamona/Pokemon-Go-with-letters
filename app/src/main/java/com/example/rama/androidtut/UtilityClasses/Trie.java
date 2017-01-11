package com.example.rama.androidtut.UtilityClasses;


import java.util.*;

/**
 * Basic implementation of a Trie data structure. You can add words and search for certain prefixes.
 */
class TrieNode{
    public TrieNode[] children;
    public boolean isLeaf;
    public TrieNode() {
        children=new TrieNode[26];
        isLeaf=false;
    }
    public TrieNode get(Character c){
        c=Character.toUpperCase(c);
        int key=c-'A';
        if(key>=26||key<0) return null;
        return children[key];
    }

    public boolean put(Character c,TrieNode node){
        c=Character.toUpperCase(c);
        int key=c-'A';
        if(key>=26||key<0) return false;
        children[key]=node;
        return true;
    }




}

public class Trie{
    private TrieNode root;


    public Trie() {
        root = new TrieNode();
    }

    public void add(String s){

        TrieNode node=root;
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);
            if(node.get(c)==null) node.put(c,new TrieNode());
            node = node.get(c);
        }
        node.isLeaf=true;

    }

    public boolean startsWith(String prefix) {
        TrieNode node = root;
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            if (node.get(ch)==null) {
                return false;
            }
            node = node.get(ch);
        }
        return true;
    }

    public boolean contains(String word){
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (node.get(ch)==null) {
                return false;
            }
            node = node.get(ch);
        }
        return node.isLeaf;
    }
    public List<String> getAllWordsStartingWith(String prefix){
        TrieNode node = root;
        List<String> li=new ArrayList<>();
        for (int i = 0; i < prefix.length(); i++) {
            char ch = prefix.charAt(i);
            if (node.get(ch)==null) {
                return li;
            }
            node = node.get(ch);
        }

        dfs(node,li,prefix);
        return li;
    }
    private void dfs(TrieNode node,List<String> prev,String current){
        if(node.isLeaf) {
            prev.add(current);
        }

        for(int i=0;i<26;i++){
            TrieNode child=node.children[i];
            if(child!=null){
                char c=(char)('A'+i);
                dfs(child,prev,current+c);

            }



        }
    }

}

