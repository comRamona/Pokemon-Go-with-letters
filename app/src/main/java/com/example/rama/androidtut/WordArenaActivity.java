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
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;


import com.example.rama.androidtut.UtilityClasses.LetterAdapter;
import com.example.rama.androidtut.UtilityClasses.LetterValues;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class WordArenaActivity extends AppCompatActivity {

    static String TAG = "WordArenaActivity";
    private int score;
    GridView grid;

    SharedPreferences sharedPref;
    //text views for each letter in the answer
    private TextView[] charViews;
    //letter button grid
    private GridView letters;
    //letter button adapter
    private Button submitButton;
    private LetterAdapter ltrAdapt;
    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference gamePlayDb;
    private DatabaseReference[] letterRefs;
    private int[] letterCounts;
    private DatabaseReference scoreDb;
    private PopupWindow pwindo;
    private Set<String> dictionary;
    private int[] temporaryCount;
    int chosen=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_arena);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_letters), Context.MODE_PRIVATE);


        submitButton = (Button) findViewById(R.id.yes);
        submitButton.setEnabled(false);
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
            charViews[c].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
            charViews[c].setBackgroundResource(R.drawable.letter_bg);
            charViews[c].setTextSize(16);
            //add to display
            wordLayout.addView(charViews[c]);

        }
        database = FirebaseDatabase.getInstance().getReference();
        firebaseAuth= FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        loadDictionary();
        scoreDb=database.child("Scores").child(user.getUid()).getRef();
        scoreDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                score=dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        gamePlayDb=database.child("GamePlay").child(user.getUid());
        letterCounts=new int[26];
        temporaryCount=new int[26];
        letterRefs=new DatabaseReference[26];
        for(int i=0;i<26;i++){
            final int j=i;
            String letter = (char) (i + 'A') + "";
            letterRefs[i]=gamePlayDb.child("Letters").child(letter).getRef();

            letterRefs[i].addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    letterCounts[j]= dataSnapshot.getValue(Integer.class);
                    temporaryCount[j]=letterCounts[j];
                    ltrAdapt.updateCount(j,letterCounts[j]);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });}
        Log.i(TAG,dictionary.size()+"");
    }


    private void loadDictionary(){
        dictionary=new HashSet<>();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.grabdict);
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().toUpperCase();
                dictionary.add(word);
            }
        }
        catch(Exception e){
            Log.e(TAG,"Could not load dictionary",e);
        }
    }
    public void letterPressed(View view) {

        //user has pressed a letter to guess
        String ltr=((TextView)view).getText().toString();

        //can only press letter if more than 0 count
        if(temporaryCount[ltr.charAt(0)-'A']<=0) return;
        String newText=ltr.charAt(0)+":"+(letterCounts[ltr.charAt(0)-'A']-1);
        temporaryCount[ltr.charAt(0)-'A']--;
        ((TextView)view).setText(newText);
        char letterChar = ltr.charAt(0);
       // view.setBackgroundResource(R.drawable.letter_down);
        if(chosen<7) {
            charViews[chosen].setText(letterChar+"");
            charViews[chosen].setTextColor(Color.BLACK);
            chosen++;
        }
        if(chosen==7) {
            submitButton.setEnabled(true);
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

    public void refreshScreen(View view){
        ltrAdapt.reset(letterCounts);
        for(int i=0;i<7;i++)
            charViews[i].setText("");
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
        TextView textView=(TextView) layout.findViewById(R.id.tvcheckWord);
        String word=getCurrentWord();
        String response=word+" is not a valid word. Try again!";
        if(dictionary.contains(word)){
            response="You have discovered a new word!\n"+word;
            int scoreToAdd=calculateScore(word);
            int newScore=score+scoreToAdd;
            scoreDb.setValue(newScore);
            updateCounts(word);


        }
        textView.setTextSize(18);
        textView.setText(response);


    }

    private int calculateScore(String word){
        int sc=0;
        for(int i=0;i<word.length();i++){
            sc+=LetterValues.getValue(word.charAt(i));
        }
        return sc;
    }
    public String getCurrentWord(){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<7;i++)
            sb.append(charViews[i].getText());
        return sb.toString().toUpperCase();
    }

    public void updateCounts(String word){
        for(int i=0;i<word.length();i++){
            int pos=word.charAt(i)-'A';
            int t=temporaryCount[pos];
            letterRefs[pos].setValue(t-1);
        }
    }
}
