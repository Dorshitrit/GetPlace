package app.com.getplace.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import app.com.getplace.Adapter.UserPlaceAdapter;
import app.com.getplace.R;
import app.com.getplace.db.DBHandler;

public class LikedFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    DBHandler handler;
    RecyclerView recyclerViewLiked;
    UserPlaceAdapter LikeAdapter;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    ProgressBar progressBar;

    public ImageView getNoLikedBG() {
        return noLikedBG;
    }

    public ImageView noLikedBG;
    LikedFragment likedFragment;

    public LikedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_liked, container, false);

        handler = new DBHandler(getContext());
        recyclerViewLiked = (RecyclerView) v.findViewById(R.id.recyclerViewlike);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        noLikedBG = (ImageView) v.findViewById(R.id.noLikedBG);

        recyclerViewLiked.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setAutoMeasureEnabled(false);
        recyclerViewLiked.setLayoutManager(linearLayoutManager);


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

        recyclerViewLiked.setVisibility(View.GONE);
        LikeAdapter = new UserPlaceAdapter(handler.getUserPlaces(3), getContext(), getActivity(), googleApiClient);
        progressBar.setVisibility(View.GONE);
        recyclerViewLiked.setAdapter(LikeAdapter);
        recyclerViewLiked.setVisibility(View.VISIBLE);
        LikeAdapter.notifyDataSetChanged();


        if (LikeAdapter.getItemCount() == 0) {
            recyclerViewLiked.setVisibility(View.GONE);
        } else {
            noLikedBG.setVisibility(View.GONE);
        }
        return v;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
    public void onResume() {
        super.onResume();
        LikeAdapter = new UserPlaceAdapter(handler.getUserPlaces(3), getContext(), getActivity(), googleApiClient);
        recyclerViewLiked.setAdapter(LikeAdapter);
        LikeAdapter.notifyDataSetChanged();
    }


    public void likedDisconnected() {
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

}
