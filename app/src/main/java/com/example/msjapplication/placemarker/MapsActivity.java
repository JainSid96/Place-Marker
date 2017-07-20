package com.example.msjapplication.placemarker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode ==1){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(last , "YOUR LOCATION");
                }
            }
        }
    }

    public void centerMapOnLocation (Location location , String title){
        LatLng user = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.clear();
        if(title != "YOUR LOCATION") {
            mMap.addMarker(new MarkerOptions().position(user).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 10));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if (intent.getIntExtra("Place Number", 0) == 0) {

            //zoom in on location


            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location, "YOUR LOCATION");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
            if (Build.VERSION.SDK_INT < 23) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(last, "YOUR LOCATION");
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        }else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(StartActivity.locations.get(intent.getIntExtra("Place Number", 0)).latitude);
            placeLocation.setLongitude(StartActivity.locations.get(intent.getIntExtra("Place Number", 0)).longitude);
            centerMapOnLocation(placeLocation , StartActivity.places.get(intent.getIntExtra("Place Number", 0)));

        }
    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String adress ="";
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude , latLng.longitude ,1);
            if (listAddress != null && listAddress.size() > 0){
                if (listAddress.get(0).getSubThoroughfare() != null) {
                    if (listAddress.get(0).getThoroughfare() != null) {
                        adress += listAddress.get(0).getThoroughfare() + " ";
                    }
                    adress+=listAddress.get(0).getThoroughfare();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (adress == ""){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            adress = sdf.format(new Date());
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(adress));
        StartActivity.places.add(adress);
        StartActivity.locations.add(latLng);
        StartActivity.arrayAdapter.notifyDataSetChanged();
        Toast.makeText(this , "LOCATION SAVED" , Toast.LENGTH_LONG).show();
    }
}
