package app.com.getplace.UI;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.util.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.com.getplace.R;
import app.com.getplace.data.SearchSuggestion;
import app.com.getplace.db.DBHandler;


public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnPoiClickListener,
        OnMapReadyCallback, LocationListener,
        AppBarLayout.OnOffsetChangedListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ListFragment listFragment;
    private LikedFragment likedFragment;
    private HistoryFragment historyFragment;
    private FloatingActionButton CurrentLocationFab;
    private int FabColor = 0;
    public MapView mMapView;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    protected Location myLoc, lastLocation, currentLocation;
    protected LatLng latLang, lastKnownLatLng, infoLocation;
    protected LatLng LatLanSearch;
    private DBHandler handler;
    boolean isUserMove = false;
    boolean isListOpen, isLikedOpen, isHistoryOpen = false;
    private String lat, lon, name;
    private final String TAG = "BlankFragment";
    private boolean mIsDarkSearchTheme = false;
    private String mLastQuery = "";
    private static final int REQUEST_CHECK_SETTINGS_LOCATION = 999;
    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 750;
    private FloatingSearchView mSearchView;
    protected HandlerThread mHandlerThread;
    private Handler mThreadHandler;
    private List<app.com.getplace.data.SearchSuggestion> searchResultses;
    private SearchSuggestion SearchSuggestion;
    protected SharedPreferences sharedPreferences;
    boolean isLocationOn = false;
    String poiPlaceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapContainer);
        mMapView.onCreate(savedInstanceState);

        displayLocationSettingsRequest(getContext());

        if (mThreadHandler == null) {
            mHandlerThread = new HandlerThread(MapFragment.class.getSimpleName(), android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        Toast.makeText(getContext(), "Worked!", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        lastLocation = new Location("lastLocation");
        currentLocation = new Location("currentLocation");
        myLoc = new Location("myLoc");

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(2000);

        CurrentLocationFab = (FloatingActionButton) rootView.findViewById(R.id.CurrentLocation);


        SearchSuggestion = new SearchSuggestion();
        handler = new DBHandler(getContext());
        searchResultses = new ArrayList<>();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            lat = bundle.getString("lat");
            lon = bundle.getString("lon");
            name = bundle.getString("name");
        }

        CurrentLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FabColor == 0) {
                    FabColor = 1;
                    CurrentLocationFab.setRippleColor(Color.parseColor("#FF0A0076"));
                    CurrentLocationFab.setImageResource(R.drawable.fab_current_location_on);
                } else {
                    FabColor = 0;
                    CurrentLocationFab.setRippleColor(Color.parseColor("#FFFF00B7"));
                    CurrentLocationFab.setImageResource(R.drawable.fab_current_location_off);
                }

                isUserMove = false;
                if (isLocationOn && latLang != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 18));
                } else {
                    displayLocationSettingsRequest(getContext());
                }
            }
        });
        mMapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchView = (FloatingSearchView) view.findViewById(R.id.floating_search_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchView.setZ(99999);
        }
        setupSearchBar();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap = map;

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnPoiClickListener(this);


        Boolean isNight;
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (isNight = hour < 6 || hour > 18) {
            isNight = true;
        } else {
            isNight = false;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String mapStyle = sharedPreferences.getString("map_style", "-1");
        boolean nightMode = sharedPreferences.getBoolean("night_mode", false);
        boolean AnightMode = sharedPreferences.getBoolean("night_mode_clock", false);


        if (!nightMode) {
            if (AnightMode && isNight) {
                mIsDarkSearchTheme = true;
                mSearchView.setBackgroundColor(Color.parseColor("#2f3031"));
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_night));
            } else {
                int map_type = Integer.valueOf(mapStyle);
                switch (map_type) {
                    case -1:
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_standard));
                        break;
                    case 0:
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_retro));
                        break;
                    case 1:
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_night));
                        break;
                    case 2:
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_grayscale));
                        break;
                    case 3:
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_aubergine));
                        break;
                    default:
                        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_standard));
                        break;
                }
            }
        } else {
            mIsDarkSearchTheme = true;
            mSearchView.setBackgroundColor(Color.parseColor("#2f3031"));
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.mapstyle_night));
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        if (lastLocation != null && lat == null) {
            lastKnownLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 18));
        } else {
            try {
                isUserMove = true;
                infoLocation = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(infoLocation, 18));
                Marker infoMarker = googleMap.addMarker(new MarkerOptions()
                        .position(infoLocation)
                        .snippet("More Info...")
                        .title(name));
                infoMarker.showInfoWindow();
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        getActivity().finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            isUserMove = true;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
//            Toast.makeText(getContext(), "The user tapped something on the map.",
//                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
//            Toast.makeText(getContext(), "The app moved the camera.",
//                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isUserMove) {
            myLoc = location;
            if (location != null) {
                latLang = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 18));
            } else {
                latLang = new LatLng(0, 0);
            }
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 18));
        googleMap.addMarker(new MarkerOptions().position(poi.latLng).title(poi.name).snippet("Click to Enter"));

        poiPlaceId = poi.placeId;

        Marker PoiMarker = googleMap.addMarker(new MarkerOptions()
                .position(poi.latLng)
                .snippet("More Info...")
                .title(poi.name));
        PoiMarker.showInfoWindow();
//        ValueAnimator markerAnimator = ObjectAnimator.ofObject(PoiMarker, poi.name, 1);
//        markerAnimator.setDuration(2000);
//        markerAnimator.start();
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(getContext(), InfoActivity.class);
                intent.putExtra("placeid", poiPlaceId);
                startActivity(intent);
            }
        });
    }


    private void setupSearchBar() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {
                    mSearchView.showProgress();
                    mThreadHandler.removeCallbacksAndMessages(null);

                    mThreadHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPlaceSearch(newQuery);
                        }
                    }, FIND_SUGGESTION_SIMULATED_DELAY);
                }
                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final com.arlib.floatingsearchview.suggestions.model.SearchSuggestion item) {
                SearchSuggestion searchSuggestion = (SearchSuggestion) item;
                Log.e(TAG, "onSuggestionClicked()");
                if (((SearchSuggestion) item).getsPlaceId().equals("1")) {
                    Toast.makeText(getContext(), "No Found any places to show", Toast.LENGTH_LONG).show();
                } else {
                    LatLng serachItemLatlng = new LatLng(Double.valueOf(((SearchSuggestion) item).getsPlaceLat()), Double.valueOf(((SearchSuggestion) item).getsPlaceLon()));
                    if (!handler.checkPlace(((SearchSuggestion) item).getsPlaceId()) && !((SearchSuggestion) item).getsPlaceId().equals("1")) {
                        handler.addPlace(((SearchSuggestion) item).getsPlaceId(),
                                ((SearchSuggestion) item).getsPlaceLat(),
                                ((SearchSuggestion) item).getsPlaceLon(),
                                ((SearchSuggestion) item).getsPlaceName(),
                                ((SearchSuggestion) item).getsPlaceAddress(),
                                null, null, null, null, null, null, String.valueOf("1"),
                                "OFF", String.valueOf("1"), String.valueOf("1"), CalculationByDistance(latLang, serachItemLatlng));
                    }
                    mLastQuery = item.getBody();
                    LatLanSearch = new LatLng(Double.valueOf(((SearchSuggestion) item).getsPlaceLat()), Double.valueOf(((SearchSuggestion) item).getsPlaceLon()));
                    isUserMove = true;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLanSearch, 18));
                    googleMap.addMarker(new MarkerOptions().position(LatLanSearch).title(mLastQuery).snippet("Click to Enter"));

                    Marker melbourne = googleMap.addMarker(new MarkerOptions()
                            .position(LatLanSearch)
                            .snippet("More Info...")
                            .title(mLastQuery));
                    melbourne.showInfoWindow();
                    googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(getContext(), InfoActivity.class);
                            intent.putExtra("placeid", ((SearchSuggestion) item).getsPlaceId());
                            startActivity(intent);
                        }
                    });

                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            Intent intent = new Intent(getContext(), InfoActivity.class);
                            intent.putExtra("placeid", ((SearchSuggestion) item).getsPlaceId());
                            startActivity(intent);
                            return false;
                        }
                    });
                }
            }


            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;
                mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
                    @Override
                    public void onFocus() {
                        mSearchView.setSearchBarTitle(mLastQuery);
                        mSearchView.setSearchText(mLastQuery);
                    }

                    @Override
                    public void onFocusCleared() {
                        mSearchView.hideProgress();
                        mSearchView.setSearchBarTitle(mLastQuery);
                        mSearchView.setSearchText(SearchSuggestion.getBody());
                    }
                });
                Log.d(TAG, "onSearchAction()");
            }
        });


        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                mSearchView.swapSuggestions(handler.getHistoryPlace(2));
                getSearchSuggestionDesign();
                isDisconnected();
                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {
                mSearchView.hideProgress();
                mSearchView.setSearchBarTitle(mLastQuery);
                mSearchView.setSearchText(SearchSuggestion.getBody());
                Log.d(TAG, "onFocusCleared()");
            }
        });

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.action_Open_ListList) {
                    if (!isListOpen) {
                        isDisconnected();
                        openlist();
                    } else {
                        isListOpen = false;
                        closeList();
                    }
                } else if (item.getItemId() == R.id.action_Open_ListHistory) {
                    if (!isHistoryOpen) {
                        isDisconnected();
                        openHistory();
                    } else {
                        isHistoryOpen = false;
                        closeHistory();
                    }
                }
                if (item.getItemId() == R.id.action_Open_liked) {
                    if (!isLikedOpen) {
                        isDisconnected();
                        openLiked();
                    } else {
                        isLikedOpen = false;
                        closeLiked();
                    }
                }
                if (item.getItemId() == R.id.action_Open_Setting) {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                }

            }
        });

        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                mSearchView.hideProgress();
                Log.d(TAG, "onHomeClicked()");
            }
        });

        getSearchSuggestionDesign();
    }


    public void getSearchSuggestionDesign() {
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            @SuppressWarnings("deprecation")
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, com.arlib.floatingsearchview.suggestions.model.SearchSuggestion item, int itemPosition) {
                SearchSuggestion searchSuggestion = (SearchSuggestion) item;
                String textAddressColor = mIsDarkSearchTheme ? "#d0d0d0" : "#000b41";
                String textNewIcon = mIsDarkSearchTheme ? "#ffffff" : "#FF373C41";
                String textColor = mIsDarkSearchTheme ? "#ededed" : "#0761c9";
                String textLight = mIsDarkSearchTheme ? "#ededed" : "#0761c9";

                String check = ((SearchSuggestion) item).getsPlaceId();
                if (handler.checkPlace(check)) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history, null));
                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    if (((SearchSuggestion) item).getsPlaceId().equals("1")) {
                        leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.ic_search_error, null));
                        Util.setIconColor(leftIcon, Color.parseColor(textNewIcon));
                        leftIcon.setAlpha(.36f);
                    } else {
                        leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                                R.drawable.ic_search_new, null));
                        Util.setIconColor(leftIcon, Color.parseColor(textNewIcon));
                        leftIcon.setAlpha(.36f);
                    }
                }
                textView.setTextColor(Color.parseColor(textColor));

                String titleFix = ((SearchSuggestion) item).getsPlaceName();
                titleFix = titleFix.substring(0, 1).toUpperCase() + titleFix.substring(1).toLowerCase();
                String text = item.getBody()
                        .replaceFirst(((SearchSuggestion) item).getsPlaceName(),
                                "<font color=\"" + textLight + "\"><h5>" + titleFix + "</h5></font>");

                String textAddress = ((SearchSuggestion) item).getsPlaceAddress()
                        .replaceFirst(((SearchSuggestion) item).getsPlaceAddress(),
                                "<font color=\"" + textAddressColor + "\">" + ((SearchSuggestion) item).getsPlaceAddress() + "</font>");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
                    textView.append(Html.fromHtml(textAddress, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    textView.setText(Html.fromHtml(text));
                    textView.append(Html.fromHtml(textAddress));
                }
            }
        });
    }


    private void getPlaceSearch(final String place) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                ("https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + Uri.encode(place) + "&location=" + myLoc.getLatitude() + "," + myLoc.getLongitude()
                        + "&radius=10&key=AIzaSyAwX3gpO0eMELEJxJiEk-9rxwJANdLsJu4", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("ZERO_RESULTS")) {
                                searchResultses.clear();
                                searchResultses.add(new SearchSuggestion(
                                        "1", "No Places Found", "Try search another place", null, null
                                ));
                                mSearchView.swapSuggestions(searchResultses);
                                mSearchView.hideProgress();
                                return;
                            }
                            JSONArray arr = response.getJSONArray("results");
                            searchResultses.clear();

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject jsonObject = arr.getJSONObject(i);
                                Log.e("placeid", jsonObject.getString("place_id"));
                                searchResultses.add(new SearchSuggestion(
                                        jsonObject.getString("place_id"),
                                        jsonObject.getString("name"),
                                        jsonObject.getString("formatted_address"),
                                        jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat"),
                                        jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng")
                                ));
                            }
                            mSearchView.clearSuggestions();
                            mSearchView.swapSuggestions(searchResultses);
                            mSearchView.hideProgress();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getActivity()).add(jsonRequest);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mSearchView.setTranslationY(verticalOffset);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    //this func check location en\dis and ask to enable.
    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        isLocationOn = true;
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
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
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        if (googleApiClient.isConnected()) {
//            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
//        }
//        if (lastLocation != null) {
//            lastKnownLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 18));
//
//        }
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
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void openlist() {
        isListOpen = true;
        listFragment = new ListFragment();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down)
                .add(R.id.topFragmentcontainer, listFragment)
                .commit();
    }

    public void closeList() {
        isListOpen = false;
        listFragment.googleApiClient.disconnect();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                .remove(getFragmentManager().findFragmentById(R.id.topFragmentcontainer))
                .commit();
    }

    public void openHistory() {
        isHistoryOpen = true;
        historyFragment = new HistoryFragment();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down)
                .add(R.id.historyFragmentcontainer, historyFragment)
                .commit();
    }

    public void closeHistory() {
        isHistoryOpen = false;
        historyFragment.googleApiClient.disconnect();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                .remove(historyFragment)
                .commit();
    }

    public void openLiked() {
        isLikedOpen = true;
        likedFragment = new LikedFragment();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_down, R.anim.slide_out_down)
                .add(R.id.likedFragmentcontainer, likedFragment)
                .commit();
    }

    public void closeLiked() {
        isLikedOpen = false;
        likedFragment.googleApiClient.disconnect();
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                .remove(likedFragment)
                .commit();
    }

    public boolean isListOpen() {
        return isListOpen;
    }

    public boolean isLikedOpen() {
        return isLikedOpen;
    }

    public boolean isHistoryOpen() {
        return isHistoryOpen;
    }

    public void isDisconnected() {
        if (isListOpen) {
            listFragment.listDisconnected();
            closeList();
        } else if (isLikedOpen) {
            likedFragment.likedDisconnected();
            closeLiked();
        } else if (isHistoryOpen) {
            historyFragment.historyDisconnected();
            closeHistory();
        }
    }


}


