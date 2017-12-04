package com.huang.irishtransport.routefinder;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final String TAG = "AutoComplete";
    private Button btnSearch;
    private String originAddress = null;
    private String destinationAddress = null;
    private LatLng startLocation;
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
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRoute();
            }
        });
        PlaceAutocompleteFragment originAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origin);
        originAutocompleteFragment.setHint("Please enter your origin");
        originAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                originAddress = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                this.onError(status);
            }
        });
        PlaceAutocompleteFragment destinationAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);
        destinationAutocompleteFragment.setHint("Please enter your destination");
        destinationAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destinationAddress = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                this.onError(status);
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
        LatLng dublin = new LatLng(53.35, -6.266);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dublin,10));
    }

    //When the button is clicked, start searching process.
    //Place API key to SearchRoute.class, google_maps_api.xml and AndroidManifest.xml
    public void searchRoute(){
        //Check if the user has entered their origin and destination.
        if(originAddress == null || destinationAddress == null){
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
            new SearchRoute(this, originAddress, destinationAddress).search();
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
    public void onError(Status status){
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }
}
