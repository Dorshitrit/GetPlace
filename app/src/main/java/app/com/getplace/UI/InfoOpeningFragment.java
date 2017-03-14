package app.com.getplace.UI;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import app.com.getplace.R;
import app.com.getplace.data.SearchSuggestion;


public class InfoOpeningFragment extends Fragment {

    String placeID;
    GoogleApiClient googleApiClient;
    OnFragmentInteractionListener mListener;
    ArrayList<String> arrayList;
    ListView lv;
    TextView tisOpen;
    boolean bisOpen;

    public InfoOpeningFragment() {
    }


    public static InfoOpeningFragment newInstance(String param1, String param2) {
        InfoOpeningFragment fragment = new InfoOpeningFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_opening, container, false);


        lv = (ListView) view.findViewById(R.id.openlv);
        tisOpen = (TextView) view.findViewById(R.id.isOpen);
        arrayList = new ArrayList<>();

        InfoActivity infoActivity = (InfoActivity) getActivity();

        googleApiClient = infoActivity.getGoogleApiClient();
        placeID = infoActivity.getPlaceID();


        getPlaceSearch(placeID);


        return view;
    }


    //https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJc7lb11o-HRURm9m4I9XG0BM&key=AIzaSyCgKsjYNr1jQWL2rPhzK7Fr6uZW-D8k6IU

    private void getPlaceSearch(final String place) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                ("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place + "&key=AIzaSyCgKsjYNr1jQWL2rPhzK7Fr6uZW-D8k6IU", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            JSONObject opening_hours = result.getJSONObject("opening_hours");
                            bisOpen = opening_hours.getBoolean("open_now");
                            if (bisOpen) {
                                tisOpen.setTextColor(Color.parseColor("#044f00"));
                                tisOpen.setText("Open");
                            } else {
                                tisOpen.setTextColor(Color.parseColor("#ff0000"));
                                tisOpen.setText("Close");
                            }
                            JSONArray periods = opening_hours.getJSONArray("weekday_text");

                            for (int i = 0; i < periods.length(); i++) {
                                arrayList.add(periods.getString(i));
                            }

                            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, arrayList);
                            lv.setAdapter(adapter);

                        } catch (JSONException e) {
                            tisOpen.setText("Ops... No Result");
                            tisOpen.setTextColor(Color.parseColor("#575757"));
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        tisOpen.setText("No Result");
                    }
                });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(getActivity()).add(jsonRequest);
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
        void onFragmentInteraction(Uri uri);
    }
}
