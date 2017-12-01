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
        LatLng dublin = new LatLng(53.35, -6.266);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dublin,10));
    }

    public void searchRoute(){
        origin = etOrigin.getText().toString();
        destination = etDestination.getText().toString();

        if(origin.isEmpty() || destination.isEmpty()){
            printToast("Please enter your origin/destination");
            return;
        }

        if(polylineList.size() > 0){
            for(Polyline polyline: polylineList){
                polylineList.remove(polyline);
            }
        }
        try {
            new SearchRoute(this, origin, destination).search();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void printToast(String s){
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void setStartLocation(LatLng location){
        this.startLocation = location;
    }

    public void receivePoly(List<List<LatLng>> data){
        polylineList = new ArrayList<>();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15));
        PolylineOptions polylineOptions = new PolylineOptions()
                .geodesic(true)
                .color(Color.BLUE)
                .width(10);

        for(List<LatLng> polylines : data){
            for(int i = 0; i < polylines.size(); i++){
                polylineOptions.add(polylines.get(i));
            }
            polylineList.add(mMap.addPolyline(polylineOptions));
        }
    }
}
