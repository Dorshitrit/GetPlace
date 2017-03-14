package app.com.getplace.UI;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import app.com.getplace.R;

import static com.google.android.gms.internal.zzs.TAG;


public class InfoMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnPoiClickListener {
    MapView mapView;
    GoogleApiClient googleApiClient;
    String placeID;

    String a;

    private OnFragmentInteractionListener mListener;
    private GoogleMap googleMap;

    public InfoMapFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static InfoMapFragment newInstance(String param1, String param2) {
        InfoMapFragment fragment = new InfoMapFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_info_map, container, false);

//        mapView.onCreate(savedInstanceState);

        InfoActivity infoActivity = (InfoActivity) getActivity();

        googleApiClient = infoActivity.getGoogleApiClient();
        placeID = infoActivity.getPlaceID();

        mapView = (MapView) v.findViewById(R.id.infomap);
        mapView.onCreate(savedInstanceState);

        Places.GeoDataApi.getPlaceById(googleApiClient, placeID)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPlace.getLatLng(), 18);
                            googleMap.animateCamera(cameraUpdate);
                            googleMap.addMarker(new MarkerOptions().title(myPlace.getName().toString()).position(myPlace.getLatLng()));

                        } else {
                            Log.e(TAG, "Place not found");
                        }
                        places.release();
                    }
                });


        mapView.getMapAsync(this);
        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnPoiClickListener(this);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
