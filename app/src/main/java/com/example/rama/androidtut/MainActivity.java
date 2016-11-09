package com.example.rama.androidtut;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.rama.androidtut.MyPlayground.DisplayMessageActivity;


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    static String TAG = "MainActivity";
    static int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button b=(Button) findViewById(R.id.button);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

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
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        score = message.length();

    }

    public void loadPoints(View view) {
        Intent intent = new Intent(this, LoadPointsActivity.class);
        startActivity(intent);
    }

    public void showMap(View view) {
        Intent intent = new Intent(this, CampusMapActivity.class);
        startActivity(intent);
    }
}
