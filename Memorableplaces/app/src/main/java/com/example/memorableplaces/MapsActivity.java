package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    String title;

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
        int position = intent.getIntExtra("position", 0);

        if (position == 0) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    markOnLocation(location, title);
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

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                getTitles(lastLocation);
                markOnLocation(lastLocation, title);
            }


        } else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locationArrayList.get(intent.getIntExtra("position", 0)).latitude);
            placeLocation.setLongitude(MainActivity.locationArrayList.get(intent.getIntExtra("position", 0)).longitude);
            markOnLocation(placeLocation, MainActivity.arrayList.get(intent.getIntExtra("position", 0)));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                getTitles(lastLocation);
                markOnLocation(lastLocation, title);
            }
        }
    }

    public void markOnLocation(Location location, String title) {
        if (location != null) {
            mMap.clear();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                title = "";
                if (addresses.get(0).getSubThoroughfare() != null) {
                    title += addresses.get(0).getSubThoroughfare() + " ";
                }
                title += addresses.get(0).getThoroughfare();
                if (title.equals("null") && addresses.get(0).getAddressLine(0) !=null) {
                    title=addresses.get(0).getAddressLine(0);
                }
            } else {
                Date currentTime = Calendar.getInstance().getTime();
                title = currentTime.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));

        MainActivity.arrayList.add(title);
        MainActivity.locationArrayList.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitudes=new ArrayList<>();
            ArrayList<String> longitudes=new ArrayList<>();

            for (LatLng coord:MainActivity.locationArrayList) {
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));
            }

            sharedPreferences.edit().putString("places",objectSerializer.serialize(MainActivity.arrayList)).apply();
            sharedPreferences.edit().putString("latitudes",objectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes",objectSerializer.serialize(longitudes)).apply();

        }catch (Exception e){
            e.printStackTrace();
        }

        Toast.makeText(this, "Location saved!", Toast.LENGTH_SHORT).show();
    }

    public void getTitles(Location location) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {

                title = "";
                if (addresses.get(0).getSubThoroughfare() != null) {
                    title += addresses.get(0).getSubThoroughfare() + " ";
                }
                title += addresses.get(0).getThoroughfare();
                if (title.equals("null") && addresses.get(0).getAddressLine(0) !=null) {
                    title=addresses.get(0).getAddressLine(0);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}