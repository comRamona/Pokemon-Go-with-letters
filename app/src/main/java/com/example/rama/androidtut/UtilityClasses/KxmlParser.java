package com.example.rama.androidtut.UtilityClasses;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rama on 13/10/16.
 */

public class KxmlParser {
    // We don't use namespaces
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "kml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Placemark")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    // Parses the contents of an entry. If it encounters a name, summary, or description tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Placemark readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Placemark");
        String title = null;
        String summary = null;
        String[] point = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                title = readName(parser);
            } else if (name.equals("description")) {
                summary = readDescription(parser);
            } else if (name.equals("Point")) {

                point = readPoints(parser);

                //  if(point.length<2) skip(parser);
            } else {
                skip(parser);
            }
        }
        return new Placemark(title, summary, point[1], point[0]);
    }

    private String[] readPoints(XmlPullParser parser) throws XmlPullParserException, IOException {
        String[] coord = null;

        parser.require(XmlPullParser.START_TAG, ns, "Point");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("coordinates")) {
                coord = (readCoord(parser));

            } else {
                skip(parser);
            }
        }
        return coord;
    }

    // Processes name tags in the feed.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return title;
    }

    private String[] readPoint(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Point");
        String[] coord = readCoord(parser);
        parser.require(XmlPullParser.END_TAG, ns, "Point");
        return coord;
    }

    private String[] readCoord(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "coordinates");
        String coord = readText(parser);
        String[] latlonh = coord.split(",");
        parser.require(XmlPullParser.END_TAG, ns, "coordinates");
        return latlonh;
    }

    // Processes summary tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return summary;
    }

    // For the tags name and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    public static class Placemark {
        private final String name;
        private final String description;
        private final double lat;
        private final double lng;

        private Placemark(String name, String description, String lat, String lng) {
            this.name = name;
            this.description = description;
            this.lat = Double.valueOf(lat);
            this.lng = Double.valueOf(lng);
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public String getAsKey() {
            return (lat + "!" + lng).replaceAll("\\.",",");
        }
    }

}



