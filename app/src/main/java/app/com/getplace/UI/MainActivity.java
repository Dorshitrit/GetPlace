package app.com.getplace.UI;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;

import app.com.getplace.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends RuntimePermissionsActivity implements LocationListener {

    BroadcastReceiver receiver;
    MapFragment mapFragment;
    Intent intent;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = new MapFragment();
       // bundle = savedInstanceState;
        intent = getIntent();
            if (intent.getExtras() != null) {
                Bundle bundle2 = new Bundle();
                bundle2.putString("lat", intent.getStringExtra("lat"));
                bundle2.putString("lon", intent.getStringExtra("lon"));
                bundle2.putString("name", intent.getStringExtra("name"));
                mapFragment.setArguments(bundle2);
            }


        if (savedInstanceState == null) {
            getMapListActivity();
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            MainActivity.super.requestAppPermissions(new
                    String[]{ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, R.string.clear, 908);
        }

        if (!isNetworkAvailable(getApplicationContext()))
            Toast.makeText(getApplicationContext(), R.string.info_cant_load_image, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPermissionsGranted(int requestCode) {
        if (requestCode == 908) {
            mapFragment.mMapView.getMapAsync(mapFragment);
        }
    }

    //this func call to Map Fragment
    public void getMapListActivity() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.locationFragmentContainer, mapFragment)
                .commit();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    //this BroadcastReceiver checks if device is charging or not.
    public class PowerConnectionReceiver extends BroadcastReceiver {
        public PowerConnectionReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                Toast.makeText(context, R.string.main_charging, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.main_not_charging, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment fragment = (MapFragment) fragmentManager.getFragments().get(0);

        if (fragment.isListOpen()) {
            fragment.closeList();
        } else if (fragment.isLikedOpen()) {
            fragment.closeLiked();
        } else if (fragment.isHistoryOpen()) {
            fragment.closeHistory();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //this BroadcastReceiver checks if device is charging or not.
        receiver = new PowerConnectionReceiver();
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Intent.ACTION_POWER_CONNECTED);
        ifilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, ifilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        //this BroadcastReceiver checks if device is charging or not.
        unregisterReceiver(receiver);
    }


}
