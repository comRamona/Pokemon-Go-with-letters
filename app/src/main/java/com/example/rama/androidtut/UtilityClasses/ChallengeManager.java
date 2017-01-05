package com.example.rama.androidtut.UtilityClasses;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.example.rama.androidtut.WordArenaActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLOutput;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ramona on 04/01/2017.
 */

public class ChallengeManager {
    private static ChallengeManager challengeManager=new ChallengeManager();
    private DatabaseReference challengeDb;
    private DatabaseReference statisticsDb;
    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference allWords;
    private DatabaseReference numberOfWord;
    int numberOfWords;
    private HashMap<String,Challenge> allChallenges=new HashMap<>();
    private String TAG="ChallengeManager";
    private Context context;
    private ChallengeManager(){
        database = FirebaseDatabase.getInstance().getReference();
        firebaseAuth= FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        challengeDb=database.child("Challenges").child(user.getUid()).getRef();
        statisticsDb=database.child("Statistics").child(user.getUid()).getRef();
        allWords=statisticsDb.child("AllWords").getRef();
        challengeDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Challenge challengeItem=postSnapshot.getValue(Challenge.class);
                    allChallenges.put(postSnapshot.getKey(),challengeItem);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

      statisticsDb.child("TotalWords").addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              numberOfWords=dataSnapshot.getValue(Integer.class);
             Log.i(TAG,"You have created your first word!!");
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });
    }
    public void newWord(String word){
        System.out.println("New word!!!");
        String format = "dd/MM/yyyy";
        DateFormat sdf = new SimpleDateFormat(format);
        Date date=Calendar.getInstance().getTime();
        allWords.child(word).setValue(sdf.format(date));
        statisticsDb.child("TotalWords").setValue(numberOfWords+1);
    }

    public void newLetter(String letter){

    }
    public static ChallengeManager getInstance(Context context){
        return challengeManager;
    }
}
