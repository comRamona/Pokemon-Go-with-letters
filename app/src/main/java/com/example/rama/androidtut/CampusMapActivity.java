package com.example.rama.androidtut;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;


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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import static java.security.AccessController.getContext;

public class CampusMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult>, GoogleMap.OnInfoWindowClickListener {

    protected static final String TAG = "CampusMapActivity";

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    KmlLayer kmlLayer;
    private boolean markers_loaded=false;
    private FloatingActionButton fab;
    private PopupWindow pwindo;
    SharedPreferences sharedPref;
    SharedPreferences markers;
    SharedPreferences lastUpdated;
    String currentDay;
    String lastDownload;

    protected static final String LOCATION_KEY = "location-key";
    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    protected static final LatLng DEFAULT_EDINBURGH_LATLNG = new LatLng(55.946233, -3.192473);

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
      fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });

        Context context = this.getApplicationContext();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_letters), Context.MODE_PRIVATE);
        markers = context.getSharedPreferences(
                ("saved_markers"), Context.MODE_PRIVATE);
        lastUpdated = context.getSharedPreferences(
                ("lastDownload"), Context.MODE_PRIVATE);
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        lastDownload=lastUpdated.getString("lastDownload","00");
        currentDay = df.format(c.getTime());
        System.out.println(currentDay+" lastDOwnload: "+lastDownload);



    }

    public void showOptionsDialog(){
        try {
// We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_win,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, 800, 800,true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
            pwindo.setBackgroundDrawable(new ColorDrawable());
            layout.getBackground().setAlpha(240);

            FloatingActionButton fab_close=(FloatingActionButton) layout.findViewById(R.id.fab_cancel);
            fab_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();

                }
            });

            FloatingActionButton fab_new=(FloatingActionButton) layout.findViewById(R.id.fab_new);
            fab_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(),NewWordActivity.class);
                    startActivity(intent);
                }
            });

            FloatingActionButton fab_instr=(FloatingActionButton) layout.findViewById(R.id.fab_instr);
            fab_instr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();
                }
            });

            FloatingActionButton fab_stats=(FloatingActionButton) layout.findViewById(R.id.fab_stats);
            fab_stats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();
                }
            });

            FloatingActionButton fab_usr=(FloatingActionButton) layout.findViewById(R.id.fab_usr);
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

            if(savedInstanceState.keySet().contains("lastDownloaded")){
                lastDownload=savedInstanceState.getString("lastDownloaded");
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

        if(!currentDay.equals(lastDownload)) {
            lastDownload=currentDay;
            lastUpdated.edit().putString("lastDownload",lastDownload).commit();
            loadThings();

        }
        else {
            System.out.println("We already downloaded this. Repopulate:");
            Log.i("UIIII","repopulaaate");
            repopulate();
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String title=marker.getTitle();
        int numberCollected = sharedPref.getInt(title,0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(title, numberCollected+1);
        editor.commit();
        SharedPreferences.Editor m=markers.edit();
        LatLng latLng=marker.getPosition();
        m.remove(latLng.latitude+","+latLng.longitude);
        m.commit();
        marker.remove();

    }

    public void repopulate(){
        mMap.clear();
        for(String s:markers.getAll().keySet()){
            String[] ll=s.split(",");
            LatLng latLng=new LatLng(Double.parseDouble(ll[0]),Double.parseDouble(ll[1]));
            mMap.addMarker(new MarkerOptions().position(latLng).title(markers.getString(s,"")));
        }
    }


    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

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
        System.out.println("Saviiiing "+lastDownload);
        savedInstanceState.putString("lastDownloaded",lastDownload);

    }

    public void loadThings() {
        if(!markers_loaded) {
            try {
                System.out.println("Downloading things..");
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
    public void showInternetAlert(){
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
                if(!markers_loaded) loadThings();
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


//    private void moveCameraToKml(KmlLayer kmlLayer) {
//        //Retrieve the first container in the KML layer
//        KmlContainer container = kmlLayer.getContainers().iterator().next();
//        //Retrieve a nested container within the first container
//        container = container.getContainers().iterator().next();
//        //Retrieve the first placemark in the nested container
//        KmlPlacemark placemark = container.getPlacemarks().iterator().next();
//        //Retrieve a polygon object in a placemark
//        KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
//        //Create LatLngBounds of the outer coordinates of the polygon
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (LatLng latLng : polygon.getOuterBoundaryCoordinates()) {
//            builder.include(latLng);
//        }
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 1));
//    }

    private class DownloadLettersTask extends AsyncTask<String, Void, byte[]> {
//

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
               Log.i(TAG,e.getMessage());
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


                for(KmlPlacemark kp:placemarks){
                   // System.out.println(kp.getProperties());


                   // System.out.println(kp.toString());

                }

                // moveCameraToKml(kmlLayer);
            } catch (XmlPullParserException e) {
                Log.i(TAG,e.getMessage());
            } catch (IOException e) {
                Log.i(TAG,e.getMessage());
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
                Log.i(TAG,"IO error, return null");
                return null;
            } catch (XmlPullParserException e) {
                Log.i(TAG,"Parsing error, return null"+e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<KxmlParser.Placemark> result) {

            // Displays the HTML string in the UI via a WebView

            //Show entries on map
            SharedPreferences.Editor edit = markers.edit();
            edit.clear().commit();
            if(result!=null) {
                for (KxmlParser.Placemark e : result) {

                   Marker q= mMap.addMarker(new MarkerOptions().position(new LatLng(e.getLat(),e.getLng())).title(e.getDescription()).visible(true));

                   edit.putString(e.getAll(),e.getDescription());

                }
                edit.commit();
            }
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

    private class DownloadKmlFile extends AsyncTask<String, Void, byte[]> {
        private final String mUrl;

        public DownloadKmlFile(String url) {
            mUrl = url;
        }

        protected byte[] doInBackground(String... params) {
            try {
                InputStream is =  new URL(mUrl).openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(byte[] byteArr) {
            try {
                KmlLayer kmlLayer = new KmlLayer(mMap, new ByteArrayInputStream(byteArr),
                        getApplicationContext());
                kmlLayer.addLayerToMap();

            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

