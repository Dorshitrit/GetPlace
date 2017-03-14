package app.com.getplace.UI;

import android.Manifest;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import app.com.getplace.Adapter.PlaceListAdapter;
import app.com.getplace.Objects.PlaceModule;
import app.com.getplace.R;

import static com.google.android.gms.internal.zzs.TAG;

public class ListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private List<PlaceModule> placesList;
    private RecyclerView recyclerView;
    private PlaceListAdapter adapter;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;
    Location lastLocation, currentLocation;
    LatLng latLng;
    ProgressBar progressBar;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_list, container, false);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar2);

        lastLocation = new Location("lastLocation");
        currentLocation = new Location("currentLocation");

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addConnectionCallbacks(this)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(getActivity(), this)
                    .build();
        }

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) //5000
                .setFastestInterval(2000); //2000


        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setAutoMeasureEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager);

        placesList = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        recyclerView.setVisibility(View.INVISIBLE);

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                String website = null;
                for (final PlaceLikelihood placeLikelihood : likelyPlaces) {
                    if (placeLikelihood.getPlace().getWebsiteUri() != null) {
                        website = placeLikelihood.getPlace().getWebsiteUri().toString();
                    } else {
                        website = "null";
                    }
                    placesList.add(new PlaceModule(placeLikelihood.getPlace().getId(),
                            placeLikelihood.getPlace().getLatLng().latitude,
                            placeLikelihood.getPlace().getLatLng().longitude,
                            placeLikelihood.getPlace().getName().toString(),
                            placeLikelihood.getPlace().getAddress().toString(),
                            null,
                            placeLikelihood.getPlace().getPhoneNumber().toString(),
                            website,
                            placeLikelihood.getPlace().getRating(),
                            placeLikelihood.getPlace().getPlaceTypes(),
                            placeLikelihood.getPlace().getPriceLevel(),
                            0, "OFF", 0, 0, CalculationByDistance(latLng, placeLikelihood.getPlace().getLatLng())));
                }
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                adapter = new PlaceListAdapter(placesList, getContext(), getActivity(), googleApiClient);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                likelyPlaces.release();
            }
        });

        return v;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
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
                connectionResult.startResolutionForResult(getActivity(), 9000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (googleApiClient != null) {
            googleApiClient.stopAutoManage(getActivity());
            googleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
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


    @Override
    public void onResume() {
        if (googleApiClient.isConnected()) {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        super.onResume();
        adapter = new PlaceListAdapter(placesList, getContext(), getActivity(), googleApiClient);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    public void listDisconnected() {
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();

    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }
}
