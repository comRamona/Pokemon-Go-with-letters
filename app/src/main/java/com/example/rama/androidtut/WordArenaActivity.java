package com.example.rama.androidtut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;


import com.example.rama.androidtut.UtilityClasses.LetterAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


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
    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private PopupWindow pwindo;
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
        database = FirebaseDatabase.getInstance().getReference();
        firebaseAuth= FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

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





    public void checkWord(View view) {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_word,
                (ViewGroup) findViewById(R.id.show_word));
        pwindo = new PopupWindow(layout, 800, 800, true);
        pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
        pwindo.setBackgroundDrawable(new ColorDrawable());
        FloatingActionButton fab_close = (FloatingActionButton) layout.findViewById(R.id.welldone_cancel);
        fab_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwindo.dismiss();

            }
        });
        score=20;
        String name=user.getUid();
        database.child("Scores").child(name).setValue(score);



    }
}
