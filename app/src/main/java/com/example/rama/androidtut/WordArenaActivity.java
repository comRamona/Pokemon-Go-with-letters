package com.example.rama.androidtut;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;


import com.example.rama.androidtut.UtilityClasses.ChallengeManager;
import com.example.rama.androidtut.UtilityClasses.LetterAdapter;
import com.example.rama.androidtut.UtilityClasses.LetterValues;
import com.example.rama.androidtut.UtilityClasses.ListItem;
import com.example.rama.androidtut.UtilityClasses.Trie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class WordArenaActivity extends AppCompatActivity {

    static String TAG = "WordArenaActivity";
    private int score;


    SharedPreferences sharedPref;
    //text views for each letter in the answer
    private TextView[] charViews;
    //letter button adapter
    private Button submitButton;
    private LetterAdapter ltrAdapt;
    private DatabaseReference[] letterRefs;
    private int[] letterCounts;
    private DatabaseReference scoreDb;
    private DatabaseReference hintsDb;
    private PopupWindow pwindo;
    private int[] temporaryCount;
    private ChallengeManager challengeManager;
    private int noHints;
    private Trie dictionary;

    private BroadcastReceiver broadcastReceiver;
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
        GridView letters = (GridView) findViewById(R.id.letters);

        ltrAdapt=new LetterAdapter(this);
        letters.setAdapter(ltrAdapt);

        //loop through characters
        for (int c = 0; c < 7; c++) {
            charViews[c] = new TextView(this);
            charViews[c].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            charViews[c].setGravity(Gravity.CENTER);
            charViews[c].setBackgroundResource(R.drawable.letter_bg);
            charViews[c].setTextSize(20);
            //add to display
            wordLayout.addView(charViews[c]);

        }
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        loadDictionary();
        scoreDb= database.child("Scores").child(user.getUid()).getRef();
        scoreDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                score=dataSnapshot.getValue(ListItem.class).getScore();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e(TAG,databaseError.getMessage());
            }
        });

        final Button hintButton=(Button) findViewById(R.id.hints);
        hintsDb=database.child("Statistics").child(user.getUid()).child("NumberOfHints").getRef();

                hintsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noHints=dataSnapshot.getValue(Integer.class);
                String s="Hints: "+noHints;
                hintButton.setText(s);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference gamePlayDb = database.child("GamePlay").child(user.getUid());
        letterCounts=new int[26];
        temporaryCount=new int[26];
        letterRefs=new DatabaseReference[26];
        for(int i=0;i<26;i++){
            final int j=i;
            String letter = (char) (i + 'A') + "";
            letterRefs[i]= gamePlayDb.child("Letters").child(letter).getRef();

            letterRefs[i].addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    letterCounts[j]= dataSnapshot.getValue(Integer.class);
                    temporaryCount[j]=letterCounts[j];
                    ltrAdapt.updateCount(j,letterCounts[j]);
                    ltrAdapt.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,databaseError.getMessage());
                }
            });}

        challengeManager=ChallengeManager.getInstance();
        installListener();


    }


    private void loadDictionary(){
        dictionary =new Trie();
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
        String newText=ltr.charAt(0)+":"+(temporaryCount[ltr.charAt(0)-'A']-1);
        temporaryCount[ltr.charAt(0)-'A']--;
        ((TextView)view).setText(newText);
        char letterChar = ltr.charAt(0);
       // view.setBackgroundResource(R.drawable.letter_down);
        if(chosen<7) {
            charViews[chosen].setText(String.valueOf(letterChar));
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
        textView.setText("Congrats!Yu have discovere a new word! \n SEVENTY");
        ltrAdapt.reset(letterCounts);
        for(int i=0;i<7;i++)
            charViews[i].setText("");
        chosen=0;
        for(int i=0;i<26;i++)
            temporaryCount[i]=letterCounts[i];
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
            challengeManager.checkWord(word,this,scoreToAdd);
            challengeManager.checkScore(newScore,this);
            scoreDb.child("score").setValue(newScore);
            updateCounts(word);

        }
        textView.setTextSize(24);
        textView.setText(response);
        refreshScreen(view);

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
            letterRefs[pos].setValue(t);
        }
    }

    public void giveHint(View view){

        hintsDb.setValue(noHints-1);
        String prefix=getCurrentWord();
        String message="You need to input at least 3 letters to get a hint.";
        if(prefix.length()>=3){
            List<String> res= dictionary.getAllWordsStartingWith(prefix);
            Log.w(TAG,prefix);
            if(res==null||res.size()==0) message="No completion found.";
            else {
                String match=checkMatch(res);
                if(match==null) message="No completion found. With a few more letters you could form "+res.get(0);
                else message="How about "+match+"?";
            }
        }
        android.support.v7.app.AlertDialog alertDialog=getAlertDialog(this);
        alertDialog.setMessage(message);
        alertDialog.setTitle("Hint");
        alertDialog.show();
    }

    public String checkMatch(List<String> res){
        Map<Character,Integer> counter=new HashMap<>();
        for(String s:res){
            counter.clear();
            boolean found=true;
            for(int i=0;i<26;i++){
                counter.put(((char)(i+'A')),temporaryCount[i]);
            }
            for(int i=0;i<res.size();i++){
                if(counter.get(s.charAt(i))>0){
                    counter.put(s.charAt(i),counter.get(s.charAt(i))-1);
                }
                else found=false;
            }
            if(found) {
                return s;
            }
        }
        return null;
    }

    private android.support.v7.app.AlertDialog getAlertDialog(Context context) {
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(context).create();

        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setTitle(("Message"));
        return alertDialog;
    }
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void installListener() {

        if (broadcastReceiver == null) {

            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle extras = intent.getExtras();

                    NetworkInfo info = (NetworkInfo) extras
                            .getParcelable("networkInfo");

                    NetworkInfo.State state = info.getState();

                    if (state == NetworkInfo.State.CONNECTED) {




                    } else {



                            Toast.makeText(getApplicationContext(), "Internet connection is off. Turn it on to save your progress", Toast.LENGTH_LONG).show();

                    }

                }
            };

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }
}
