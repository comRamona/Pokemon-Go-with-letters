package com.example.rama.androidtut;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.example.rama.androidtut.UtilityClasses.ListItemAdapter;
import com.example.rama.androidtut.UtilityClasses.ListItem;
import com.example.rama.androidtut.UtilityClasses.Score;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to display a leaderboard of users.
 * Part of the code was adapted from an stackoverflow answer:
 * http://stackoverflow.com/questions/34518421/adding-a-scoreboard-to-an-android-studio-application
 */

public class LeaderboardActivity extends AppCompatActivity {

    static String TAG = "LeaderboardActivity";
    private List<ListItem> listItemList = new ArrayList<>();
    private ListView listView;
    private ListItemAdapter adapter;
    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference scoreDb;
    private ValueEventListener valueEventListener;
    private Query queryRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.header_footer);

        listView = (ListView) findViewById(R.id.toplist);
        TextView tv=(TextView) findViewById(R.id.bodytextleft);
        tv.setText("Email");
        TextView tv2=(TextView) findViewById(R.id.bodytextright);
        tv2.setText("Score");
        adapter = new ListItemAdapter(this, listItemList);
        listView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance().getReference();
        firebaseAuth= FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        scoreDb=database.child("Scores").getRef();
        queryRef = scoreDb.orderByChild("score").limitToLast(10);

        queryRef.addValueEventListener(valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Score myScore=null;
                int i=1;
                int p=0;
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    //Score score=postSnapshot.getValue(Score.class);

                    //System.out.println(postSnapshot.getKey()+" hdfhdj "+postSnapshot.getValue());
                    Log.i(TAG,postSnapshot.getValue().toString());
                    Score score =postSnapshot.getValue(Score.class);
                    if(score.getName().equals(user.getEmail())) {
                        myScore = score;
                        myScore.setName("ME");
                        p=i;
                    }

                    else {
                        listItemList.add(new ListItem(i + "." + score.getName(), Integer.toString(score.getScore())));

                    }
                    i++;

                }
                Collections.reverse(listItemList);
                if(myScore!=null) {
                    listItemList.add(0, new ListItem(p+"."+myScore.getName(),Integer.toString(myScore.getScore())));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e(TAG,databaseError.getMessage());
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        queryRef.removeEventListener(valueEventListener);

    }
}
