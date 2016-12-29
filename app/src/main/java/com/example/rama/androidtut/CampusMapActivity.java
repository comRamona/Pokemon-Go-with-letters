package com.example.rama.androidtut;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.rama.androidtut.UtilityClasses.KxmlParser;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CampusMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult>, GoogleMap.OnInfoWindowClickListener {

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
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    protected static final LatLng DEFAULT_EDINBURGH_LATLNG = new LatLng(55.946233, -3.192473);
    KmlLayer kmlLayer;
    SharedPreferences sharedPref;

    SharedPreferences lastUpdated;
    String currentDay;
    String lastDownload;
    private int[] letterCounts;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private boolean markers_loaded = false;
    private FloatingActionButton fab;
    private PopupWindow pwindo;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private DatabaseReference markersDb;
    private DatabaseReference gamePlayDb;
    private DatabaseReference[] letterRefs;
    private FirebaseUser user;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_map);

        updateValuesFromBundle(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        Context context = this.getApplicationContext();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_letters), Context.MODE_PRIVATE);

        lastUpdated = context.getSharedPreferences(
                ("lastDownload"), Context.MODE_PRIVATE);
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        lastDownload = lastUpdated.getString("lastDownload", "00");
        currentDay = df.format(c.getTime());
        System.out.println(currentDay + " lastDOwnload: " + lastDownload);
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


        database = FirebaseDatabase.getInstance().getReference();

        user = mAuth.getCurrentUser();

        markersDb = database.child("Markers").child(user.getUid());

        gamePlayDb=database.child("GamePlay").child(user.getUid());
        letterCounts=new int[26];
        letterRefs=new DatabaseReference[26];
        for(int i=0;i<26;i++){
            String letter=(char)(i+'A')+"";
            letterRefs[i]=gamePlayDb.child("Letters").child(letter).getRef();
            letterRefs[i].setValue(0);
            final int j=i;
            letterRefs[i].addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    letterCounts[j]= dataSnapshot.getValue(Integer.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        letterRefs[2].setValue(3);






    }

    private void collectOldMarkers(Map<String,Object> mymarkers) {

        if(mymarkers==null) {
            System.out.println("My markers is null");
            loadThings(); return; }
        System.out.println("Size is: "+mymarkers.size());
        for (Map.Entry<String, Object> entry : mymarkers.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            LatLng latLng=new LatLng((double)singleUser.get("lat"),(double)singleUser.get("lng"));
            mMap.addMarker(new MarkerOptions().position(latLng).title((String) singleUser.get("name")).snippet("reloaded"));

        }

        Log.i(TAG,"Numer of Markers: "+mymarkers.size());

    }



    public void showOptionsDialog() {
        try {
// We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_win,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, 800, 800, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
            pwindo.setBackgroundDrawable(new ColorDrawable());
            // layout.getBackground().setAlpha(240);

            FloatingActionButton fab_close = (FloatingActionButton) layout.findViewById(R.id.fab_cancel);
            fab_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();

                }
            });

            FloatingActionButton fab_new = (FloatingActionButton) layout.findViewById(R.id.fab_new);
            fab_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), WordArenaActivity.class);
                    startActivity(intent);
                }
            });

            FloatingActionButton fab_instr = (FloatingActionButton) layout.findViewById(R.id.fab_leaderboard);
            fab_instr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), LeaderboardActivity.class);
                    startActivity(intent);
                }
            });

            FloatingActionButton fab_stats = (FloatingActionButton) layout.findViewById(R.id.fab_stats);
            fab_stats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();
                }
            });

            FloatingActionButton fab_usr = (FloatingActionButton) layout.findViewById(R.id.fab_challenges);
            fab_usr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
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

            if (savedInstanceState.keySet().contains("lastDownloaded")) {
                lastDownload = savedInstanceState.getString("lastDownloaded");
            }

        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

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
            public void onResult(Status status) {
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
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
        mMap.setOnInfoWindowClickListener(this);
        //  mMap.animateCamera(CameraUpdateFactory.zoomTo(8));

        if (!currentDay.equals(lastDownload)) {
            lastDownload = currentDay;
            lastUpdated.edit().putString("lastDownload", lastDownload).commit();
            String name=user.getUid();
            //different day, remove previous stored markers and download new ones
            database.child("Markers").child(name).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    loadThings();
                }
            });
        }
                else {
            System.out.println("We already downloaded this. Repopulate:");
            Log.i("UIIII", "repopulaaate");
            repopulate();
        }


    }

    /**
     * A new letter was collected and it will be added to the user's inventory
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        int numberCollected = sharedPref.getInt(title, 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(title, numberCollected + 1);
        editor.commit();

        String key=(marker.getPosition().latitude+"!"+marker.getPosition().longitude).replaceAll("\\.",",");
        marker.remove();
        markersDb.child(key).removeValue();

    }

    // method to restore game markers, if they have been already downloaded for the day
    public void repopulate() {
        mMap.clear();

        markersDb.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectOldMarkers((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("TAG", "Failed to read value.", error.toException());
                    }
                });

//
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
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

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();


        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

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
    public void onResult(LocationSettingsResult locationSettingsResult) {
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
            public void onResult(Status status) {
            }
        });


    }

    public void loadThings() {
        if (!markers_loaded) {
            try {

                Log.i(TAG, "Downloading map...");
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data

                    String url = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/sunday.kml";
                    new DownloadLetters().execute(url);
                } else {
                    showInternetAlert();
                }
            } catch (Error e) {
                Log.i(TAG, "load things failed");
            }
        }
    }

    public void showInternetAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Could not download data.");

        // Setting Dialog Message
        alertDialog.setMessage("Internet is not enabled. Please try again.");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);


        // on pressing cancel button
        alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (!markers_loaded) loadThings();
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

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        checkLocationSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected()) {
            checkLocationSettings();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "Saving the state");
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString("lastDownloaded", lastDownload);

    }

    private class DownloadLettersTask extends AsyncTask<String, Void, byte[]> {


        @Override
        protected byte[] doInBackground(String... params) {
            try {
                InputStream is = downloadUrl(params[0]);


                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
            }
            return null;
        }


        protected void onPostExecute(byte[] byteArr) {
            try {
                KmlLayer kmlLayer = new KmlLayer(mMap, new ByteArrayInputStream(byteArr),
                        getApplicationContext());
                kmlLayer.addLayerToMap();
                Iterable<KmlPlacemark> placemarks = kmlLayer.getPlacemarks();
                //delete all previous markers


                for (KmlPlacemark kp : placemarks) {
                    // System.out.println(kp.getProperties());


                    // System.out.println(kp.toString());

                }

                // moveCameraToKml(kmlLayer);
            } catch (XmlPullParserException e) {
                Log.i(TAG, e.getMessage());
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
            }
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
            Log.i(TAG, "connected succesfully");
            return conn.getInputStream();
        }


    }

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

            // Displays the HTML string in the UI via a WebView

            //Show entries on map

            // first, erase previous markers

            if (result != null) {
                for (KxmlParser.Placemark e : result) {

                    mMap.addMarker(new MarkerOptions().position(new LatLng(e.getLat(), e.getLng())).title(e.getDescription()).visible(true));
                    //save markers for latter use


                    DatabaseReference newMarker = markersDb.child(e.getAsKey());
                    newMarker.child("lat").setValue(e.getLat());
                    newMarker.child("lng").setValue(e.getLng());
                    newMarker.child("name").setValue(e.getDescription());

                }

            }
            markers_loaded = true;
        }

        // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
        private List<KxmlParser.Placemark> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            KxmlParser stackOverflowXmlParser = new KxmlParser();
            List<KxmlParser.Placemark> entries = null;


            try {
                stream = downloadUrl(urlString);
                entries = stackOverflowXmlParser.parse(stream);
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