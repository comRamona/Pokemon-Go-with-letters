package com.example.rama.androidtut.MyPlayground;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rama.androidtut.MainActivity;
import com.example.rama.androidtut.R;

public class DisplayMessageActivity extends AppCompatActivity {

    static String TAG = "DisplayMessageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
        layout.addView(textView);
    }

    protected void onStop() {

        super.onStop();

        Log.i(TAG, "OnStop");

    }

    @Override
    protected void onPause() {

        super.onStop();

        Log.i(TAG, "Paused");

    }

    @Override

    protected void onResume() {

        super.onStop();

        Log.i(TAG, "Resumed");

    }

    @Override

    protected void onDestroy() {

        super.onStop();

        Log.i(TAG, "Destroyed");

    }

    @Override

    protected void onStart() {

        super.onStop();

        Log.i(TAG, "Started");

    }
}
