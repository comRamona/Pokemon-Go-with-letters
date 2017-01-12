package com.example.rama.androidtut.UtilityClasses;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


/**
 * ChallengeManager gets alerted each time a new word is created or a new letter is collected,
 * checking and updating the challenge database and awarding bonus hints.
 */

public class ChallengeManager {
    private static ChallengeManager challengeManager=new ChallengeManager();
    private DatabaseReference challengeDb;
    private DatabaseReference statisticsDb;
    private FirebaseUser user;
    private DatabaseReference allWords;
    private int numberOfWords;
    private int numberOfHints;
    private int numberOfLetters;
    private boolean[] startLetters;
    private HashMap<String,Challenge> allChallenges=new HashMap<>();
    private String TAG="ChallengeManager";

    private ChallengeManager(){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        challengeDb= database.child("Challenges").child(user.getUid()).getRef();
        statisticsDb= database.child("Statistics").child(user.getUid()).getRef();
        allWords=statisticsDb.child("AllWords").getRef();

        startLetters=new boolean[26];
        DatabaseReference[] startRefs = new DatabaseReference[26];
        for(int i=0;i<26;i++) {
            final int j = i;
            String letter = (char) (i + 'A') + "";
            startRefs[i] = statisticsDb.child("StartLetters").child(letter).getRef();
            startRefs[i].addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    startLetters[j] = dataSnapshot.getValue(Boolean.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,"Error updating db "+databaseError.getMessage());                }
            });
        }

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
               Log.e(TAG,"Error updating db "+databaseError.getMessage());

            }
        });

      statisticsDb.child("NumberOfWords").getRef().addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              numberOfWords=dataSnapshot.getValue(Integer.class);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
              Log.e(TAG,"Error updating db "+databaseError.getMessage());
          }
      });
        statisticsDb.child("NumberOfHints").getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numberOfHints=dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG,"Error updating db "+databaseError.getMessage());
            }
        });

        statisticsDb.child("NumberOfLetters").getRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numberOfLetters=dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG,"Error updating db "+databaseError.getMessage());
            }
        });

    }

    public int getNumberOfHints(){
        return numberOfHints;
    }
    private void changeNumberOfHints(int i){
        statisticsDb.child("NumberOfHints").getRef().setValue(numberOfHints+i);
    }
    public void checkWord(String word, Context context, int scoreToAdd){
        int newTotal=numberOfWords+1;
        BonusHint returnHint=new BonusHint("default",0);
        android.support.v7.app.AlertDialog alertDialog=getAlertDialog(context);


        if(newTotal==1) {
            returnHint.setMessage("Congrats! You have discovered your first word! Here is one bonus hint!");
            returnHint.setValue(1);
        }

        if(newTotal==5) {
            returnHint.setMessage("Congrats! You have discovered 5 words! Here is one bonus hint!");
            returnHint.setValue(1);
        }

        if(returnHint.getValue()!=0) {
            alertDialog.setTitle(("Congrats!"));
            alertDialog.setMessage(returnHint.getMessage());
            alertDialog.show();
            changeNumberOfHints(1);
            challengeDb.child(newTotal+"words").child("completed").setValue(true);
        }

        allWords.child(word).getRef().setValue(scoreToAdd);
        statisticsDb.child("NumberOfWords").getRef().setValue(numberOfWords+1);

        if(!allChallenges.get("eachletterword").isCompleted()){
            boolean allLetters=true;
            for(int i=0;i<26;i++)
                if(!startLetters[i]){
                    allLetters=false;
                    break;
                }
            if(allLetters){
                challengeDb.child("eachletterword").child("completed").setValue(true);
                changeNumberOfHints(2);
                android.support.v7.app.AlertDialog alertDialog2 = getAlertDialog(context);
                alertDialog2.setMessage("You have created a word starting with each letter of the" +
                        " alphabet! That's impressive! Here are 2 bonus hints!");
                alertDialog2.show();
            }
        }

    }

    public void consecdays(Context context){
        if(!allChallenges.get("consecdays").isCompleted()){
            challengeDb.child("consecdays").child("completed").getRef().setValue(true);
            android.support.v7.app.AlertDialog alertDialog = getAlertDialog(context);
            alertDialog.setMessage("You have played the game on consecutive days! \n" +
                    "Here are 2 bonus hints for you!");
            alertDialog.show();
            changeNumberOfHints(2);
        }
    }

    public void checkCounts(int[] counts,Context context){
        boolean oneEach=true;
        boolean fiveEach=true;
        for(Integer i:counts){
            if(i<1) {oneEach=false; break; }
        }
        for(Integer i:counts){
            if(i<5) { fiveEach=false; break ;}
        }
        if(oneEach){
            if(!allChallenges.get("1eachletter").isCompleted()) {
                challengeDb.child("1eachletter").child("completed").getRef();
                android.support.v7.app.AlertDialog alertDialog = getAlertDialog(context);
                alertDialog.setMessage("You have collected 1 of each letter! \n" +
                        "Here are bonus hints for you!");
                alertDialog.show();
                changeNumberOfHints(3);
            }
        }
        if(fiveEach){
            if(!allChallenges.get("5eachletter").isCompleted()) {
                challengeDb.child("5eachletter").child("completed").getRef();
                android.support.v7.app.AlertDialog alertDialog = getAlertDialog(context);
                alertDialog.setMessage("You have collected 5 of each letter! \n" +
                        "Here are 3 bonus hints for you!");
                alertDialog.show();
                changeNumberOfHints(3);
            }
        }
    }

    public void checkLetter(Context context){
      if(numberOfLetters+1==100){
          System.out.println("should be 100");
          android.support.v7.app.AlertDialog alertDialog = getAlertDialog(context);
          alertDialog.setMessage("You have collected 100 letters! \n" +
                  "Here is a bonus hint for you!");
          alertDialog.show();
          changeNumberOfHints(1);
          challengeDb.child("100letters").child("completed").getRef().setValue(true);
      }



        statisticsDb.child("NumberOfLetters").getRef().setValue(numberOfLetters+1);

    }

    public void checkScore(int score, Context context){
        if(!allChallenges.get("score2000").isCompleted()&&score>=2000){
            challengeDb.child("score2000").child("completed").getRef().setValue(true);
            android.support.v7.app.AlertDialog alertDialog = getAlertDialog(context);
            alertDialog.setMessage("You have achieved a score of 2000! Here are 5 bonus hints " +
                    "for you");
            alertDialog.show();
            changeNumberOfHints(5);
        }
    }
    public static ChallengeManager getInstance(){
        return challengeManager;
    }


private android.support.v7.app.AlertDialog getAlertDialog(Context context) {
    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(context).create();

    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL, "OK",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
    alertDialog.setTitle(("Congrats!"));
    return alertDialog;
}
}