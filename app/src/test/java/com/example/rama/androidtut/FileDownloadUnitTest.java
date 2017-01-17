package com.example.rama.androidtut;

import com.example.rama.androidtut.UtilityClasses.KxmlParser;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * Test downloading the letter file for current day
 */

public class FileDownloadUnitTest {
    @Test
    public void downloadFile() throws IOException, XmlPullParserException {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        // full name form of the day
        String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
        String urlString = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/" + day.toLowerCase() + ".kml";
        URL url = new URL(urlString);
        //download file
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream is= conn.getInputStream();
        //read file
        Scanner s=new Scanner(is);
        assertTrue(s.hasNext());
       if(s!=null) {
           s.close();
       }
         if(is!=null) {
             is.close();
         }
    }
}
