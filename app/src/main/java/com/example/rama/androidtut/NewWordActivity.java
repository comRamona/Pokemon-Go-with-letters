package com.example.rama.androidtut;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.maps.android.kml.KmlLayer;

import java.util.Map;


public class NewWordActivity extends AppCompatActivity {

    static String TAG="NewWordActivity";
    static int score;
    GridView grid;

    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_word);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_letters), Context.MODE_PRIVATE);
        String[] letters=new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        int[] counts=new int[26];
        for(int i=0;i<26;i++){
            counts[i]=sharedPref.getInt(letters[i],0);
        }


        CustomGrid adapter = new CustomGrid(this, letters,counts);
        grid=(GridView)findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                System.out.println(position);
            }
        });






    }



    @Override

    protected void onStop() {

        super.onStop();

        Log.i(TAG,"OnStop");

    }

    @Override
    protected void onPause() {

        super.onPause();

        Log.i(TAG,"Paused");

    }

    @Override

    protected void onResume() {

        super.onResume();

        Log.i(TAG,"Resumed");

    }

    @Override

    protected void onDestroy() {

        super.onDestroy();

        Log.i(TAG,"Destroyed");

    }

    @Override

    protected void onStart() {

        super.onStart();

        Log.i(TAG,"Started");

    }
    /**
     * Called when the user presses the Send button
     * @param view
     */


    public void loadPoints(View view){
        Intent intent=new Intent(this,LoadPointsActivity.class);
        startActivity(intent);
    }

    public void showMap(View view){
        Intent intent = new Intent(this,CampusMapActivity.class);
        startActivity(intent);
    }
}
