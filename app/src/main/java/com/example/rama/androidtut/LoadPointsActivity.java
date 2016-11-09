package com.example.rama.androidtut;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

public class LoadPointsActivity extends AppCompatActivity {
    static String TAG="LoadPointsActivity";

    private TextView response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_points);

        response = (TextView) findViewById(R.id.response);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

            String url="http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/sunday.kml";
            new DownloadLettersTask().execute(url);
        } else {
            Toast.makeText(this, "No network connection.", Toast.LENGTH_LONG).show();
        }
    }
    private class DownloadLettersTask extends AsyncTask<String, Void, List<KxmlParser.Placemark>> {
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
            if(result!=null) {
                for (KxmlParser.Placemark e : result) {
                   // Log.i(TAG, e.lat+"");
                }
            }
        }

        // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
        private List<KxmlParser.Placemark> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            KxmlParser stackOverflowXmlParser = new KxmlParser();
            List<KxmlParser.Placemark> entries = null;
            String title = null;
            String url = null;
            String summary = null;
            Calendar rightNow = Calendar.getInstance();


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
