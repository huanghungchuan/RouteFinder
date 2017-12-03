package Class;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.huang.irishtransport.routefinder.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by user on 2017/11/29.
 */

public class SearchRoute {
    private final String SEARCH_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private final String KEY="Place Your Key Here";
    String origin;
    String destination;
    MapsActivity ma;

    public SearchRoute(MapsActivity ma, String origin, String destination){
        this.origin = origin;
        this.destination = destination;
        this.ma = ma;
    }

    //Start getting JSON data by entering the url in String as parameter.
    public void search() throws UnsupportedEncodingException {
        new DownloadJson().execute(getUrl());
    }

    //Return whole url in String.
    public String getUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return SEARCH_URL + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + KEY;
    }

    //The class that downloads JSON data, gets all the information needed and decode polyline.
    private class DownloadJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            HttpsURLConnection con = null;
            try {

                //get connection
                URL url1 = new URL(url);
                con = (HttpsURLConnection)url1.openConnection();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sBuilder = new StringBuilder();

                //record every line of JSON data
                String line = null;
                while ((line = bReader.readLine()) != null){
                    sBuilder.append(line + "\n");
                }
                bReader.close();
                return sBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                //After getting every line of JSON data, get the needed data from it.
                parseJSonData(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void parseJSonData(String s) throws JSONException{
            /*
             *  Get geocoder_status to check if there is a route found between origin and destination
             *  geocoded_waypoints for polyline.
             */
            if(s != null){
                JSONObject rawData = new JSONObject(s);
                JSONArray routes = rawData.getJSONArray("routes");
                List<List<LatLng>> listRoutes = new ArrayList<>();
                JSONArray geocoded_waypoints = rawData.getJSONArray("geocoded_waypoints");
                JSONObject geocoded_waypoint = geocoded_waypoints.getJSONObject(0);
                String geocoder_status = geocoded_waypoint.getString("geocoder_status");
                if(!geocoder_status.equals("OK")){
                    ma.printToast("No Result");
                    return;
                }

                for(int i = 0; i < routes.length(); i++){
                    JSONObject route = routes.getJSONObject(i);

                    JSONObject overview_polyline = route.getJSONObject("overview_polyline");
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject originLocation = leg.getJSONObject("start_location");

                    ma.setStartLocation(new LatLng(originLocation.getDouble("lat"), originLocation.getDouble("lng")));

                    listRoutes.add(decodePoly(overview_polyline.getString("points")));
                    ma.receivePoly(listRoutes);
                }
            }
            else
                return;
        }
        //convert overview_polyline into LatLng
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng(lat / 100000d, lng / 100000d);
                poly.add(p);
            }

            return poly;
        }
    }
}
