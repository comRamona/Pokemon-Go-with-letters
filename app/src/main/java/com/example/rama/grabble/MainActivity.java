package com.example.rama.grabble;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rama.grabble.UtilityClasses.Challenge;
import com.example.rama.grabble.UtilityClasses.Score;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Start screen that launches when user initialized the app. If the user has never registered, it
 * will display a sign up form.
 * The user can either launch a help screen or start the game, which will take him to the campus map.
 * Snippets of code from the official Firebase examples where use for performing signup actions.
 * (https://github.com/firebase/quickstart-android/tree/master/auth)
 */

public class MainActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "MainActivity";

    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private PopupWindow pwindo;


    // declare_auth
    private FirebaseAuth mAuth;


    // declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.start_game_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };
        // [END auth_state_listener]

        // display instructions pop up
        FloatingActionButton fab_close = (FloatingActionButton) findViewById(R.id.help);
        fab_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.popup_instr,
                        (ViewGroup) findViewById(R.id.show_instr));
                pwindo = new PopupWindow(layout, 900, 800, true);
                pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
                pwindo.setBackgroundDrawable(new ColorDrawable());
                FloatingActionButton fab_close = (FloatingActionButton) layout.findViewById(R.id.instr_cancel);
                fab_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pwindo.dismiss();

                    }
                });

            }
        });


    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


    }


    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            newUserDatabaseCreation();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }
        if (password.length() <= 5) {
            mPasswordField.setError("Try a longer password.");
        } else {
            mPasswordField.setError(null);
        }
        if (!email.contains("@")) {
            mEmailField.setError("Not a valid email");
        } else {
            mEmailField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));
            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.start_game_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);


            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.start_game_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.start_game_button) {
            //finish();
            Intent intent = new Intent(getApplicationContext(), CampusMapActivity.class);
            startActivity(intent);
        } else if (i == R.id.sign_out_button) {
            signOut();
        }
    }

    /**
     * Method initialize database holding default values for new user
     * Initializes Gameplay, Statistics and Scores databases and populates them with defaut values
     */
    public void newUserDatabaseCreation() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();
        if (uid == null) uid = "";
        DatabaseReference gamePlayDb = database.child("GamePlay").child(uid);
        for (int i = 0; i < 26; i++) {
            String letter = (char) (i + 'A') + "";
            gamePlayDb.child("Letters").child(letter).setValue(0);

            database.child("Statistics").child(uid).child("StartLetters").
                    child(letter).setValue(false);

        }
        gamePlayDb.child("lastDownload").setValue("");
        Score score = new Score(user.getEmail(), 0);
        database.child("Scores").child(uid).setValue(score);
        DatabaseReference challengesDb = database.child("Challenges").child(uid);

        database.child("Statistics").child(uid).child("NumberOfWords").setValue(0);
        database.child("Statistics").child(uid).child("NumberOfHints").setValue(1);
        database.child("Statistics").child(uid).child("NumberOfLetters").setValue(0);

        challengesDb.child("consecdays").setValue(new Challenge("Play the game on consecutive days"));
        challengesDb.child("oneletter").setValue(new Challenge("Collect your first letter"));
        challengesDb.child("100letters").setValue(new Challenge("Collect 100 letters"));
        challengesDb.child("1words").setValue(new Challenge("Discover one word"));
        challengesDb.child("5words").setValue(new Challenge("Discover 5 words"));
        challengesDb.child("1eachletter").setValue(new Challenge("Collect at least one letter of each"));
        challengesDb.child("5eachletter").setValue(new Challenge("Collect at least 5 letters of each"));
        challengesDb.child("eachletterword").setValue(new Challenge("Words starting with each letter"));
        challengesDb.child("score2000").setValue(new Challenge("Score 2000 points"));

    }

    public void onBackPressed() {
        finish();
    }
}