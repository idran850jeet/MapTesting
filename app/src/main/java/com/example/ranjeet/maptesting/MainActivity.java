package com.example.ranjeet.maptesting;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private  static final int REQUEST_ACCESS_FINE_LOCATION=99;
    LatLng latLngNalanda;
    GoogleMap mgoogleMap;
    EditText placeName;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(googleServiceAvailable())
        {//gogle service is Available
            setContentView(R.layout.activity_main);
            mapInitialise();
            sharedPreferences=this.getSharedPreferences(Manifest.permission.ACCESS_FINE_LOCATION.toString(),MODE_PRIVATE);
             editor=sharedPreferences.edit();

        }
        else
        {   setContentView(R.layout.activity_map_error);
        Toast.makeText(getApplicationContext(),"google play service is not Available",Toast.LENGTH_LONG).show();
        }
    }

    private void mapInitialise() {
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.getMapAsync(this);
    }


    public boolean googleServiceAvailable(){
        GoogleApiAvailability api=GoogleApiAvailability.getInstance();
        int isAvailable=api.isGooglePlayServicesAvailable(this);
        if(isAvailable==ConnectionResult.SUCCESS){
            return true;
        }else if(api.isUserResolvableError(isAvailable)){
            Dialog dialog=api.getErrorDialog(this,isAvailable,0);
            dialog.show();
        }else{
            Toast.makeText(this,"can't connect to playService",Toast.LENGTH_LONG).show();
        }
        return false;
    }
    private void goToLocation(double lat,double log){
        LatLng latLng=new LatLng(lat,log);
        CameraUpdate update=CameraUpdateFactory.newLatLng(latLng);
        mgoogleMap.moveCamera(update);
    }
    private void goToLocationZoom(double lat,double log,float zoom){
        LatLng latLng=new LatLng(lat,log);
        CameraUpdate update=CameraUpdateFactory.newLatLngZoom(latLng,zoom);
        mgoogleMap.moveCamera(update);
    }
    public void geoLocate(View view){
        placeName=findViewById(R.id.placeName);
        String location=placeName.getText().toString();
        Geocoder gc=new Geocoder(this);
        try {
            List<Address> list=gc.getFromLocationName(location,1);
            Address address=list.get(0);
            String locality=address.getLocality();
            String postalCode=address.getPostalCode();
            Toast.makeText(this,locality+postalCode,Toast.LENGTH_LONG).show();
            double lat=address.getLatitude();
            double log=address.getLongitude();
            goToLocationZoom(lat,log,15);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
      mgoogleMap=googleMap;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Loacation Permission Needed..");
                builder.setMessage("This app need to access your device Location...Please Allow");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_ACCESS_FINE_LOCATION);

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                } else if(sharedPreferences.getBoolean("RequestedLocation",true)){
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_ACCESS_FINE_LOCATION);
                editor.putBoolean("RequestedLocation",false);
                editor.commit();
                // REQUEST_ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            else
            {
               Toast.makeText(this,"Please allow location permission in your app Settings",Toast.LENGTH_LONG).show();
                Intent intent=new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri=Uri.fromParts("package",this.getPackageName(),null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            // Permission has already been granted

            // for Map tool disable
            mgoogleMap.getUiSettings().setMapToolbarEnabled(true);;
            mgoogleMap.setMyLocationEnabled(true);
            mgoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mgoogleMap.getUiSettings().setCompassEnabled(true);
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId())
       {
           case R.id.mapTypeNormal: mgoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                   break;
           case R.id.mapTypeTerrain: mgoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                    break;
           case R.id.mapTypeSatellite: mgoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                   break;
           case R.id.mapTypeHybrid: mgoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                   break;
           default:
               break;
       }




        return super.onOptionsItemSelected(item);

    }
}
