package com.example.rama.androidtut;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Ramona on 14/01/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivitySignItInstrumentedTest {
    @Rule
    public ActivityTestRule mActivityTestRule=new ActivityTestRule<>(MainActivity.class);

    @Test
    public void signIn(){
        onView(withId(R.id.sign_in_button)).perform(click());
    }
}

