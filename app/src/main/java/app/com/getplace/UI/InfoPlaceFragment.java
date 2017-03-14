package app.com.getplace.UI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

import app.com.getplace.R;
import app.com.getplace.db.DBHandler;

import static com.google.android.gms.internal.zzs.TAG;


public class InfoPlaceFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private String placeID;
    private GoogleApiClient googleApiClient;
    LocationManager locationManager;
    private Location currentBestLocation = null;
    private CardView info_like, Moreinfo_address, Moreinfo_phone, Moreinfo_website;
    private TextView addressLine, websiteLine, phoneLine;
    TextView likedLine, distnaceLine;
    private String website;
    private ImageView info_icLiked;
    private Double lat;
    private Double lon;
    private DBHandler handler;
    public boolean likeSwitch = false;
    private LatLng placeLatln, userLatln;

    public InfoPlaceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_place, container, false);

        locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);

        if(getLastBestLocation() != null) {
            userLatln = new LatLng(getLastBestLocation().getLatitude(), getLastBestLocation().getLongitude());
        } else {
            userLatln = new LatLng(0, 0);
        }

        addressLine = (TextView) v.findViewById(R.id.addressLine);
        websiteLine = (TextView) v.findViewById(R.id.websiteLine);
        phoneLine = (TextView) v.findViewById(R.id.phoneLine);

        info_icLiked = (ImageView) v.findViewById(R.id.info_icLiked);

        likedLine = (TextView) v.findViewById(R.id.likedLine);
        distnaceLine = (TextView) v.findViewById(R.id.distnaceLine);

        info_like = (CardView) v.findViewById(R.id.info_like);
        Moreinfo_address = (CardView) v.findViewById(R.id.Moreinfo_address);
        Moreinfo_phone = (CardView) v.findViewById(R.id.Moreinfo_phone);
        Moreinfo_website = (CardView) v.findViewById(R.id.Moreinfo_website);

        handler = new DBHandler(getContext());

        InfoActivity infoActivity = (InfoActivity) getActivity();
        placeID = infoActivity.getPlaceID();
        googleApiClient = infoActivity.getGoogleApiClient();

        Places.GeoDataApi.getPlaceById(googleApiClient, placeID)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);

                            placeLatln = new LatLng(myPlace.getLatLng().latitude, myPlace.getLatLng().longitude);
                            distnaceLine.setText(CalculationByDistance(userLatln, placeLatln));
                            Log.e("user", String.valueOf(userLatln));
                            Log.e("place", String.valueOf(placeLatln));

                            if (myPlace.getWebsiteUri() == null) {
                                websiteLine.setText(R.string.info_no_available_web);
                            } else {
                                website = myPlace.getWebsiteUri().toString();
                                websiteLine.setText("Open Website");

                                Moreinfo_website.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent goWeb = new Intent(Intent.ACTION_VIEW);
                                        goWeb.setData(Uri.parse(website));
                                        startActivity(goWeb);
                                    }
                                });
                            }

                            if (myPlace.getAddress().equals("")) {
                                addressLine.setText(R.string.info_no_available_address);
                            } else {
                                addressLine.setText("On Map: " + myPlace.getAddress());

                                lat = myPlace.getLatLng().latitude;
                                lon = myPlace.getLatLng().longitude;
                                final CharSequence getNameAd = myPlace.getName();
                                Moreinfo_address.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent goPlace = new Intent(getContext(), MainActivity.class);
                                        goPlace.putExtra("lat", String.valueOf(lat));
                                        goPlace.putExtra("lon", String.valueOf(lon));
                                        goPlace.putExtra("name", getNameAd);
                                        startActivityForResult(goPlace, 101);
                                    }
                                });
                            }

                            if (myPlace.getPhoneNumber().equals("")) {
                                phoneLine.setText(R.string.info_no_available_phone);
                            } else {
                                phoneLine.setText("Call");
                                final CharSequence getPhoneNum = myPlace.getPhoneNumber();
                                Moreinfo_phone.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        askPhonePermission();
                                        Intent goPhone = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getPhoneNum));
                                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        startActivity(goPhone);
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "Place not found");
                        }
                        places.release();
                    }
                });

        if (!handler.checkPlaceLike(placeID)) {
            likeSwitch = false;
            info_icLiked.setImageResource(R.drawable.ic_like_off);
            likedLine.setText(R.string.info_tap_to_like);
        } else {
            likeSwitch = true;
            info_icLiked.setImageResource(R.drawable.ic_like_on);
            likedLine.setText(R.string.info_user_like_place);
        }

        info_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!handler.checkPlaceLike(placeID)) {
                    likeSwitch = true;
                    handler.updateLike(placeID, "ON");
                    likedLine.setText(R.string.info_user_like_place);
                    info_icLiked.setImageResource(R.drawable.ic_like_on);
                } else {
                    likeSwitch = false;
                    handler.updateLike(placeID, "OFF");
                    likedLine.setText(R.string.info_tap_to_like);
                    info_icLiked.setImageResource(R.drawable.ic_like_off);

                }
            }
        });


        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void askPhonePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 909);
        }
    }

    private String CalculationByDistance(LatLng startLatLng, LatLng endLatLng) {
        String dist = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean onMiles = sharedPreferences.getBoolean("distance_miles", false);
        if (onMiles) {
            double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
            double dLat = Math.toRadians(endLatLng.latitude - startLatLng.latitude);
            double dLng = Math.toRadians(endLatLng.longitude - startLatLng.longitude);
            double sindLat = Math.sin(dLat / 2);
            double sindLng = Math.sin(dLng / 2);
            double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                    * Math.cos(Math.toRadians(startLatLng.latitude)) * Math.cos(Math.toRadians(startLatLng.longitude));
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            dist = String.valueOf(earthRadius * c + " Miles");

        } else {
            Location firstLocation = new Location("One");
            firstLocation.setLatitude(startLatLng.latitude);
            firstLocation.setLongitude(startLatLng.longitude);

            Location secondLocation = new Location("Two");
            secondLocation.setLatitude(endLatLng.latitude);
            secondLocation.setLongitude(endLatLng.longitude);

            float distance = firstLocation.distanceTo(secondLocation);
            float check = Float.valueOf(new DecimalFormat("#.#").format(distance));

            dist = check + " M";

            if (check > 1000.0f) {
                check = check / 1000.0f;
                dist = check + " KM";
            }
        }
        return dist;
    }


    private Location getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
}
