package app.com.getplace.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class HistoryFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    DBHandler handler;
    RecyclerView recyclerViewHistory;
    UserPlaceAdapter historyAdapter;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    ProgressBar progressBar;
    ImageView noHistorydBG;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_history, container, false);

        handler = new DBHandler(getContext());
        recyclerViewHistory = (RecyclerView) v.findViewById(R.id.recyclerViewhistory);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        noHistorydBG = (ImageView) v.findViewById(R.id.noHistorydBG);

        recyclerViewHistory.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setAutoMeasureEnabled(false);
        recyclerViewHistory.setLayoutManager(linearLayoutManager);


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

        recyclerViewHistory.setVisibility(View.GONE);
        historyAdapter = new UserPlaceAdapter(handler.getUserPlaces(2), getContext(), getActivity(), googleApiClient);
        progressBar.setVisibility(View.GONE);
        recyclerViewHistory.setAdapter(historyAdapter);
        recyclerViewHistory.setVisibility(View.VISIBLE);
        historyAdapter.notifyDataSetChanged();

        if (historyAdapter.getItemCount() == 0) {
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            noHistorydBG.setVisibility(View.GONE);
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
        historyAdapter = new UserPlaceAdapter(handler.getUserPlaces(2), getContext(), getActivity(), googleApiClient);
        recyclerViewHistory.setAdapter(historyAdapter);
        historyAdapter.notifyDataSetChanged();
    }


    public void historyDisconnected() {
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

}
