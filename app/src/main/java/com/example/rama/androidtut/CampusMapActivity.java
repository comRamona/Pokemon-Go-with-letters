package com.example.rama.androidtut;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rama.androidtut.UtilityClasses.ChallengeManager;
import com.example.rama.androidtut.UtilityClasses.ConnectivityReceiver;
import com.example.rama.androidtut.UtilityClasses.ConnectivityReceiverListener;
import com.example.rama.androidtut.UtilityClasses.KxmlParser;
import com.example.rama.androidtut.UtilityClasses.MyApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CampusMapActivity extends FragmentActivity implements ConnectivityReceiverListener ,OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult>, GoogleMap.OnMarkerClickListener {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected static final String TAG = "CampusMapActivity";
    protected static final String LOCATION_KEY = "location-key";
    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Maximum distance from a marker for it to be collectable
     */
    protected static final int DISTANCE_THRESHOLD = 25;

    /**
     * Default Edinburgh location
     */
    protected static final LatLng DEFAULT_EDINBURGH_LATLNG = new LatLng(55.946233, -3.192473);
    /**
     * Date format for storing date of marker download, so that new markers are only downloaded
     * on a new day
     */
    private final SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    /**
     * Get current date
     */
    String currentDay;
    /**
     * Store last download
     */
    String lastDownload;
    /**
     * Array of counts for all letters collected by player
     */
    private int[] letterCounts;
    /**
     * Google and gps specific objects
     */
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;

    private boolean markers_loaded = false;
    private PopupWindow pwindo;
    // [[ Start of Firebase specific objects
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private DatabaseReference markersDb;
    private DatabaseReference gamePlayDb;
    private DatabaseReference letterRefs;
    private DatabaseReference lastUpdatedRef;
    private FirebaseUser user;
    // ]] end of Firebase specific objects
    // ChallengeManager that is called every time a new event happens (letter collection)
    private ChallengeManager challengeManager;
    /**
     * Event listener for letter counts
     */
    private ChildEventListener lettersEventListener;
    /**
     * Application context
     */
    private Context context;

    private ConnectivityReceiver connectivityReceiver;

    /**
     * Obtain database connections and manage Google API and location updates.
     * If all requirements are met, markers are loaded on the map.
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_campus_map);

        updateValuesFromBundle(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });


        Calendar c = Calendar.getInstance();
        currentDay = df.format(c.getTime());

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();

        user = mAuth.getCurrentUser();

        markersDb = database.child("Markers").child(user.getUid()).getRef();

        gamePlayDb = database.child("GamePlay").child(user.getUid()).getRef();
        letterCounts = new int[26];
        letterRefs=gamePlayDb.child("Letters").getRef();
        letterRefs.addChildEventListener(lettersEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                int i=key.charAt(0)-'A';
                if(i>=0&&i<26) {
                    letterCounts[i] = dataSnapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                int i=key.charAt(0)-'A';
                if(i>=0&&i<26) {
                    letterCounts[i] = dataSnapshot.getValue(Integer.class);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });

        //determine whether user has played the game on consecutive game (challenge)
        lastUpdatedRef = gamePlayDb.child("lastDownload").getRef();
        lastUpdatedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastDownload = dataSnapshot.getValue(String.class);
                Calendar c = Calendar.getInstance();
                try {
                    Date startDate = df.parse(lastDownload);
                    Date endDate = df.parse(currentDay);
                    c.setTime(endDate);
                    c.add(Calendar.DATE, -1);
                    if (c.getTime().equals(startDate)) {
                        challengeManager.consecdays(context);

                    }

                } catch (Exception e) {

                    Log.e(TAG, e.getMessage());
                }

                // [[ Get markers for the day
                downloadOrPopulateFromDatabase();
                // ]] End of get markers
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error updating db " + databaseError.getMessage());
            }
        });

        challengeManager = ChallengeManager.getInstance();

    }


    /**
     * Connect Google API client and start checking location settings.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        checkLocationSettings();
    }

    /**
     * On Resume add all listeners
     */
    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected()) {
            checkLocationSettings();
        }
        Log.i(TAG, "Resuming and installing listener");
        challengeManager.initializeListeners();
        MyApplication.getInstance().setConnectivityListener(this);

    }

    /**
     * On Pause, remove all listeners
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        challengeManager.removeListeners();
        letterRefs.removeEventListener(lettersEventListener);
        Log.i(TAG, "Unregistered internet receiver");
    }

    /**
     * Disconnect Google API client
     */
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        if(pwindo!=null){
            try{
                pwindo.dismiss();
            }
            catch(Exception e){
                Log.e(TAG,"Can't dismiss pop up");
            }
        }
        Log.i(TAG, "Stopping");

    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "Saving the state");
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);

    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {

            // Update the value of mCurrentLocation from the Bundle
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }


        }
    }


    /**
     * Build google api client
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    /**
     * Create location request
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    /**
     * Build location settings request
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // mMap.getUiSettings().setScrollGesturesEnabled(false);
        // mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.setMyLocationEnabled(true);
        LatLng latLng;
        if (mCurrentLocation != null) {
            latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else latLng = DEFAULT_EDINBURGH_LATLNG;

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.setOnMarkerClickListener(this);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));


    }

    /**
     * Start populating the map with today's letter markers, deciding whether they have already
     * been downloaded and need to be retrieved from database or whether it is the first time
     * playing the game for the day and new markers should be downloaded.
     */
    public void downloadOrPopulateFromDatabase() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
                if (!currentDay.equals(lastDownload)) {
                    lastDownload = currentDay;
                    lastUpdatedRef.setValue(lastDownload);
                    String name = user.getUid();
                    //different day, remove previous stored markers and download new ones
                    database.child("Markers").child(name).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            loadMarkersFromWebsite();
                        }
                    });
                } else {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    repopulateMarkersFromDatabase();
                }
            }
        else {
            showInternetAlert();
        }


    }


    /**
     * Function to define behaviour when users clicks on a marker.
     * Checks distance and if it is within threshold, update user's inventory with new letter.
     *
     * @param marker Marker the user clicked on
     */
    @Override
    public boolean onMarkerClick(Marker marker) {


        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng position = marker.getPosition();
        //store distance in meters from clicked marker
        float[] results = new float[1];
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                position.latitude, position.longitude, results);
        Log.i(TAG, "Distance from marker is " + results[0]);
        if (results[0] <= DISTANCE_THRESHOLD) {
            //replace ! with . since database doesn't allow certain characters in keys
            showDialog("Congratulations!", "You have collected letter " + marker.getTitle() + "!");
            String key = (marker.getPosition().latitude + "!" + marker.getPosition().longitude).replaceAll("\\.", ",");
            marker.remove();
            markersDb.child(key).removeValue();
            String title = marker.getTitle();
            challengeManager.checkLetter(this);
            int i = title.charAt(0) - 'A';
            int oldVal = letterCounts[i];
            letterRefs.child(title.charAt(0)+"").setValue(oldVal+1);
            letterCounts[i]++;
            challengeManager.checkCounts(letterCounts, this);
        } else {
            showDialog("Try again", "Sorry, you are too far away from this letter!");
        }
        return true;

    }

    // method to restore game markers, if they have been already downloaded for the day
    public void repopulateMarkersFromDatabase() {
        Log.i(TAG, "Repopulating from database");
        mMap.clear();

        markersDb.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectOldMarkers((Map<String, Object>) dataSnapshot.getValue());

                        markers_loaded = true;
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });

    }

    /**
     * Display markers retrieved from database
     *
     * @param mymarkers retrieved markers
     */
    private void collectOldMarkers(Map<String, Object> mymarkers) {

        if (mymarkers == null) {

            Log.i(TAG, "No stored markers. Downloading");
            loadMarkersFromWebsite();
            return;
        }

        for (Map.Entry<String, Object> entry : mymarkers.entrySet()) {

            //Get letter markers
            Map letter = (Map) entry.getValue();

            String name = "marker_green" + letter.get("name");
            int id = getResources().getIdentifier(name.toLowerCase(), "drawable", getPackageName());
            LatLng latLng = new LatLng((double) letter.get("lat"), (double) letter.get("lng"));
            mMap.addMarker(new MarkerOptions().position(latLng).title((String) letter.get("name")).icon(BitmapDescriptorFactory.fromResource(id)));

        }

        Log.i(TAG, "Numer of Markers: " + mymarkers.size());

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Change map focus depending on current location
     *
     * @param location current location
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);


    }


    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    // Permission denied, Disable the functionality that depends on this permission.
                    status.startResolutionForResult(CampusMapActivity.this, REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                    Toast.makeText(this, "Location can not be detected.", Toast.LENGTH_LONG).show();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                Toast.makeText(this, "Location can not be detected.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        Toast.makeText(this, "Location can not be detected.", Toast.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
            }
        });


    }

    /**
     * Download kxml file from course webpage and display markers on the map
     */
    public void loadMarkersFromWebsite() {
        if (!markers_loaded) {
            try {

                Log.i(TAG, "Downloading map...");
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    // full name form of the day
                    String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                    String url = getResources().getString(R.string.lettersurl) + day.toLowerCase() + ".kml";
                    // fetch data
                    new DownloadLetters().execute(url);
            } catch (Error e) {
                Log.i(TAG, "Loading markers failed" + e.getMessage());
            }
        }
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }

    }



    /**
     * Display internet connection alert
     */
    public void showInternetAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Could not download data.");

        // Setting Dialog Message
        alertDialog.setMessage("Internet is not enabled. Please try again.");

        // on pressing cancel button
        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    /**
     * Function to display basic alert dialog
     *
     * @param title   of the dialog
     * @param message of the dialog
     */
    public void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Pop up cancel button
     *
     * @param view button
     */
    public void fab_cancel(View view) {
        pwindo.dismiss();
    }

    /**
     * Button to launch Word arena
     *
     * @param view button
     */
    public void fab_new(View view) {
        Toast.makeText(CampusMapActivity.this, "Saving your data..", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, WordArenaActivity.class);
        pwindo.dismiss();
        startActivity(intent);
    }

    /**
     * Button to launch Leaderboard activity
     *
     * @param view button
     */
    public void fab_leaderboard(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        pwindo.dismiss();
        startActivity(intent);
    }

    /**
     * Button to launch Statistics activity
     *
     * @param view button
     */
    public void fab_stats(View view) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        pwindo.dismiss();
        startActivity(intent);

    }

    /**
     * Button to launch Challenges Activity
     *
     * @param view button
     */
    public void fab_challenges(View view) {
        Intent intent = new Intent(this, ChallengesActivity.class);
        pwindo.dismiss();
        startActivity(intent);
    }

    /**
     * Show pop window with buttons to other activities
     */
    public void showOptionsDialog() {
        try {
            // We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_options_menu,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
            pwindo.setBackgroundDrawable(new ColorDrawable());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
//            if(!markers_loaded){
//                downloadOrPopulateFromDatabase();
//            }
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fabs), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
        Log.i(TAG,message);
    }

    /**
     * Asynchronously download markers file and display them on the map
     */
    private class DownloadLetters extends AsyncTask<String, Void, List<KxmlParser.Placemark>> {
        @Override
        protected List<KxmlParser.Placemark> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                Log.i(TAG, "IO error, return null");
                return null;
            } catch (XmlPullParserException e) {
                Log.i(TAG, "Parsing error, return null" + e.getMessage());
                return null;
            }
        }


        @Override
        protected void onPostExecute(List<KxmlParser.Placemark> result) {

            if (result != null) {
                for (KxmlParser.Placemark e : result) {


                    String name = "marker_green" + e.getDescription();
                    int id = getResources().getIdentifier(name.toLowerCase(), "drawable", getPackageName());
                    mMap.addMarker(new MarkerOptions().position(new LatLng(e.getLat(), e.getLng())).title(e.getDescription()).visible(true).icon(BitmapDescriptorFactory.fromResource(id)));
                    //save markers for latter use


                    DatabaseReference newMarker = markersDb.child(e.getAsKey());
                    newMarker.child("lat").setValue(e.getLat());
                    newMarker.child("lng").setValue(e.getLng());
                    newMarker.child("name").setValue(e.getDescription());

                }
                markers_loaded = true;
            }


        }

        // Loads kxml file and parses it
        private List<KxmlParser.Placemark> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            KxmlParser kxmlParser = new KxmlParser();
            List<KxmlParser.Placemark> entries = null;


            try {
                stream = downloadUrl(urlString);
                entries = kxmlParser.parse(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            return entries;
        }

        // Given a string representation of a URL, sets up a connection and gets
        // an input stream.
        private InputStream downloadUrl(String urlString) throws IOException {

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            return conn.getInputStream();
        }
    }

}