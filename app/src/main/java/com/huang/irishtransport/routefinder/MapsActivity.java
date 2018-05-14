package com.huang.irishtransport.routefinder;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Class.SearchRoute;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private EditText etOrigin;
    private EditText etDestination;
    private Button btnSearch;
    private String origin;
    private LatLng startLocation;
    private String destination;
    private List<Polyline> polylineList = new ArrayList<>();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRoute();
            }
        });
    }


    //Initialize the map to view Dublin
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng dublin = new LatLng(53.35, -6.266);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dublin,10));
    }

    //When the button is clicked, start searching process.
    //Place API key to SearchRoute.class, google_maps_api.xml and AndroidManifest.xml
    public void searchRoute(){
        origin = etOrigin.getText().toString();
        destination = etDestination.getText().toString();

        //Check if the user has entered their origin and destination.
        if(origin.isEmpty() || destination.isEmpty()){
            printToast("Please enter your origin/destination");
            return;
        }
        //Clear the polylines from last time.
        if(polylineList.size() > 0){
            for(Polyline polyline: polylineList){
                polylineList.remove(polyline);
            }
        }
        //Start searching process.
        try {
            new SearchRoute(this, origin, destination).search();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //Print toast by entering the message to parameter.
    public void printToast(String s){
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void setStartLocation(LatLng location){
        this.startLocation = location;
    }

    //After JSON data was parsed and decoded, draw the polylines on the map.
    public void receivePoly(List<List<LatLng>> data){
        polylineList = new ArrayList<>();
        //Move the origin to the center of the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15));

        //Polyline style
        PolylineOptions polylineOptions = new PolylineOptions()
                .geodesic(true)
                .color(Color.BLUE)
                .width(12);
        //Draw polylines
        for(List<LatLng> polylines : data){
            for(int i = 0; i < polylines.size(); i++){
                polylineOptions.add(polylines.get(i));
            }
            polylineList.add(mMap.addPolyline(polylineOptions));
        }
    }
}
