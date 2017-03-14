package app.com.getplace.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import app.com.getplace.R;
import app.com.getplace.db.DBHandler;

import static com.google.android.gms.internal.zzs.TAG;

public class InfoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    Location lastLocation;
    Location currentLocation;
    LatLng latLng;
    DBHandler handler;
    private LinearLayout layout;
    private ImageView image;
    private String placeID;
    private TextView place_name, place_rating;
    private ImageView info_menu_share, info_menu_search, info_menu_phone;
    private TabLayout tabLayout;
    ViewPager viewPager;
    String oneFrag = "Information";
    String twoFrag = "Opening Hours";
    String threeFrag = "Google Map";
    ScrollView scrollView;

    private int[] tabIcons = {
            R.drawable.ic_info_info,
            R.drawable.ic_info_time,
            R.drawable.ic_info_map,
    };

    public String getPlaceID() {
        return placeID;
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        scrollView = (ScrollView) findViewById(R.id.activity_info);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();


        place_name = (TextView) findViewById(R.id.place_name);
        place_rating = (TextView) findViewById(R.id.place_rating);
        layout = (LinearLayout) findViewById(R.id.imageGallery);

        info_menu_share = (ImageView) findViewById(R.id.info_menu_share);
        info_menu_search = (ImageView) findViewById(R.id.info_menu_search);
        info_menu_phone = (ImageView) findViewById(R.id.info_menu_phone);

        handler = new DBHandler(this);

        lastLocation = new Location("lastLocation");
        currentLocation = new Location("currentLocation");

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .build();
        }
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);


        final Intent intent = getIntent();
        placeID = intent.getStringExtra("placeid");


        Places.GeoDataApi.getPlaceById(googleApiClient, placeID)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            place_name.setText(myPlace.getName());
                            if (myPlace.getRating() == -1) {
                                place_rating.setText(R.string.info_no_available_rating);
                            } else {
                                place_rating.setText(String.valueOf(myPlace.getRating()) + " Rating by Google");
                            }

                            final CharSequence getAddress = myPlace.getAddress();
                            info_menu_share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String shareBody = String.valueOf(getAddress);
                                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                    sharingIntent.setType("text/plain");
                                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, place_name.getText().toString());
                                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                                    startActivity(Intent.createChooser(sharingIntent, getPackageName()));
                                }
                            });
                            if (myPlace.getPhoneNumber().equals("")) {
                                info_menu_phone.setImageResource(R.drawable.ic_info_menu_phone_off);
                            } else {
                                final CharSequence getPhoneNum = myPlace.getPhoneNumber();
                                info_menu_phone.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        askPhonePermission();
                                        Intent goPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getPhoneNum));
                                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        startActivity(goPhone);
                                    }
                                });
                                final CharSequence getWebName = myPlace.getName();
                                info_menu_search.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent goWeb = new Intent(Intent.ACTION_VIEW);
                                        Uri myUri = Uri.parse("https://www.google.com/search?q=" + getWebName);
                                        goWeb.setData(myUri);
                                        startActivity(goWeb);
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "Place not found");
                        }
                        places.release();
                    }
                });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (isNetworkAvailable(getApplicationContext())) {
            Places.GeoDataApi.getPlacePhotos(googleApiClient, intent.getStringExtra("placeid")).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                @Override
                public void onResult(PlacePhotoMetadataResult placePhotoMetadataResult) {
                    if (placePhotoMetadataResult.getStatus().isSuccess()) {
                        PlacePhotoMetadataBuffer photoMetadata = placePhotoMetadataResult.getPhotoMetadata();
                        final int photoCount = photoMetadata.getCount();
                        if (photoCount == 0) {
                            imageSetting();
                            image.setImageResource(R.drawable.infoback);
                            layout.addView(image);
                        } else {
                            for (int i = 0; i < photoCount; i++) {
                                final PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(i);
                                placePhotoMetadata.getScaledPhoto(googleApiClient, 500, 500).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                                    @Override
                                    public void onResult(PlacePhotoResult placePhotoResult) {
                                        if (placePhotoResult.getStatus().isSuccess()) {
                                            imageSetting();
                                            image.setImageBitmap(placePhotoResult.getBitmap());
                                            layout.addView(image);
                                        } else {
                                            image.setImageResource(R.drawable.infoback);
                                            layout.addView(image);
                                        }
                                    }
                                });
                            }
                        }
                        photoMetadata.release();
                    } else {
                        image.setImageResource(R.drawable.infoback);
                        layout.addView(image);
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), R.string.info_cant_load_image, Toast.LENGTH_LONG).show();
            imageSetting();
            image.setImageResource(R.drawable.infoback);
            layout.addView(image);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new InfoPlaceFragment(), oneFrag);
        adapter.addFragment(new InfoOpeningFragment(), twoFrag);
        adapter.addFragment(new InfoMapFragment(), threeFrag);
        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);

        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    public void askPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 909);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 909: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, R.string.info_ask_permission, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    public void imageSetting() {
        final Point pointSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(pointSize);

        image = new ImageView(getApplicationContext());
        image.setLayoutParams(new android.view.ViewGroup.LayoutParams(pointSize.x, pointSize.y / 3));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setMaxHeight(250);
        image.setMaxWidth(500);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            }
        });
        if (lastLocation != null) {
            latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 9000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        }
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
