package com.example.rama.androidtut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;


import com.example.rama.androidtut.UtilityClasses.LetterAdapter;


public class WordArenaActivity extends AppCompatActivity {

    static String TAG = "WordArenaActivity";
    static int score;
    GridView grid;

    SharedPreferences sharedPref;
    //text views for each letter in the answer
    private TextView[] charViews;
    //letter button grid
    private GridView letters;
    //letter button adapter
    private LetterAdapter ltrAdapt;
    int chosen=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_arena);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_letters), Context.MODE_PRIVATE);


        //create new array for character text views
        charViews = new TextView[7];

        //remove any existing letters
        LinearLayout wordLayout = (LinearLayout) findViewById(R.id.word);

        //get letter button grid
        letters = (GridView)findViewById(R.id.letters);

        ltrAdapt=new LetterAdapter(this);
        letters.setAdapter(ltrAdapt);

        //loop through characters
        for (int c = 0; c < 7; c++) {
            charViews[c] = new TextView(this);
            //set the current letter
           // charViews[c].setText("_");
            //set layout
            charViews[c].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
           // charViews[c].setTextColor(Color.WHITE);
            charViews[c].setBackgroundResource(R.drawable.letter_bg);
            //add to display
            wordLayout.addView(charViews[c]);

        }
    }


    public void letterPressed(View view) {
        System.out.println("you clickeed");
        //user has pressed a letter to guess
        String ltr=((TextView)view).getText().toString();
        char letterChar = ltr.charAt(0);
        view.setBackgroundResource(R.drawable.letter_down);
        if(chosen<7) {
            charViews[chosen].setText(letterChar+"");
            charViews[chosen].setTextColor(Color.BLACK);
            chosen++;
        }
    }



    @Override

    protected void onStop() {

        super.onStop();

        Log.i(TAG, "OnStop");

    }

    @Override
    protected void onPause() {

        super.onPause();

        Log.i(TAG, "Paused");

    }

    @Override

    protected void onResume() {

        super.onResume();

        Log.i(TAG, "Resumed");

    }

    @Override

    protected void onDestroy() {

        super.onDestroy();

        Log.i(TAG, "Destroyed");

    }

    @Override

    protected void onStart() {

        super.onStart();

        Log.i(TAG, "Started");

    }

    /**
     * Called when the user presses the Send button
     *
     * @param view
     */


    public void loadPoints(View view) {
        Intent intent = new Intent(this, LoadPointsActivity.class);
        startActivity(intent);
    }

    public void showMap(View view) {
        Intent intent = new Intent(this, CampusMapActivity.class);
        startActivity(intent);
    }
}
