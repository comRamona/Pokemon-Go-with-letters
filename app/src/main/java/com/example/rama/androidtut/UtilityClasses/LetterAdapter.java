package com.example.rama.androidtut.UtilityClasses;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.example.rama.androidtut.R;
import com.google.android.gms.maps.model.LatLng;


/**
 * This is using demo code to accompany the Mobiletuts+ tutorial:
 * - Android SDK: Create a Hangman Game
 *
 * Sue Smith - January 2014
 */

public class LetterAdapter extends BaseAdapter {

    // shared preferences
    //store letters
    private String[] letters;
    //inflater for button layout
    private LayoutInflater letterInf;

    public LetterAdapter(Context context) {
        //instantiate alphabet array
        letters=new String[26];

        for(int a=0; a<letters.length; a++){
            String letter=(char)(a+'A')+"";

            letters[a]=letter+":";
        }

        //specify layout to inflate
        letterInf = LayoutInflater.from(context);
    }

    public void updateCount(int position,int val){
        String text=(char)(position+'A')+":"+val;
        letters[position]=text;
        notifyDataSetChanged();
    }

    public void reset(int[] count){
        for(int i=0;i<26;i++) {
            String text = (char) (i + 'A') + ":" + count[i];
            letters[i] = text;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return letters.length;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //create a button for the letter at this position in the alphabet
        Button letterBtn;
        if (convertView == null) {
            //inflate the button layout
            letterBtn = (Button)letterInf.inflate(R.layout.letter, parent, false);
        } else {
            letterBtn = (Button) convertView;
        }
        //set the text to this letter
        letterBtn.setText(letters[position]);
        return letterBtn;
    }



}