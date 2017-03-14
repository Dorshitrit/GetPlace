package app.com.getplace.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.util.List;

import app.com.getplace.Objects.PlaceModule;
import app.com.getplace.R;
import app.com.getplace.UI.InfoActivity;
import app.com.getplace.db.DBHandler;

import static com.google.android.gms.internal.zzs.TAG;


public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewListHolder> {
    private List<PlaceModule> placesList;
    protected Context context;
    private Activity activity;
    private GoogleApiClient googleApiClient;
    private DBHandler handler;
    public int visitSwitch;
    public String favoriteSwitch;
    private int cardImageSize = 400;

    public PlaceListAdapter(List<PlaceModule> placesList, Context context, Activity activity, GoogleApiClient googleApiClient) {
        this.placesList = placesList;
        this.context = context;
        this.activity = activity;
        this.googleApiClient = googleApiClient;
        handler = new DBHandler(activity);
    }

    public class ViewListHolder extends RecyclerView.ViewHolder {
        CardView pcv;

        private TextView locationTitle, Distance;
        private ImageView cardBackground, btnVISIT, btnLIKE;

        ViewListHolder(View view) {
            super(view);
            pcv = (CardView) view.findViewById(R.id.pcv);
            locationTitle = (TextView) view.findViewById(R.id.locationTitle);
            Distance = (TextView) view.findViewById(R.id.Distance);
            cardBackground = (ImageView) view.findViewById(R.id.cardBackground);
            btnVISIT = (ImageView) view.findViewById(R.id.VISIT);
            btnLIKE = (ImageView) view.findViewById(R.id.LIKE);
        }


    }

    @Override
    public PlaceListAdapter.ViewListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pcv, parent, false);
        return new ViewListHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaceListAdapter.ViewListHolder holder, int position) {
        final Point pointSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(pointSize);


        if (!handler.checkPlaceLike(placesList.get(position).getPlaceID())) {
            favoriteSwitch = "ON";
            holder.btnLIKE.setVisibility(View.GONE);
        } else {
            favoriteSwitch = "OFF";
            holder.btnLIKE.setImageResource(R.drawable.ic_like_off);
        }

        handler.checkPlace(placesList.get(position).getPlaceID());
        if (!handler.checkPlace(placesList.get(position).getPlaceID())) {
            visitSwitch = 0;
            holder.btnVISIT.setVisibility(View.GONE);
        } else {
            visitSwitch = 1;
            holder.btnVISIT.setImageResource(R.drawable.ic_history);
        }

        holder.locationTitle.setText(placesList.get(position).getName());
        holder.Distance.setText(placesList.get(position).getDistance());
        holder.cardBackground.setMaxHeight(cardImageSize);
        holder.cardBackground.setImageResource(R.drawable.infoback);

        Places.GeoDataApi.getPlacePhotos(googleApiClient, placesList.get(position).getPlaceID()).setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
            @Override
            public void onResult(@NonNull PlacePhotoMetadataResult placePhotoMetadataResult) {
                if (!placePhotoMetadataResult.getStatus().isSuccess()) {
                    return;
                }
                PlacePhotoMetadataBuffer photoMetadata = placePhotoMetadataResult.getPhotoMetadata();
                if (photoMetadata.getCount() > 0) {
                    PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(0);
                    final String photoDetail = placePhotoMetadata.toString();
                    placePhotoMetadata.getScaledPhoto(googleApiClient, 500, 500).setResultCallback(new ResultCallback<PlacePhotoResult>() {
                        @Override
                        public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                            if (placePhotoResult.getStatus().isSuccess()) {
                                holder.cardBackground.setMaxHeight(cardImageSize);
                                holder.cardBackground.setMinimumHeight(pointSize.y);
                                holder.cardBackground.setMinimumWidth(pointSize.x);
                                holder.cardBackground.setMaxWidth(pointSize.x);
                                holder.cardBackground.setImageBitmap(placePhotoResult.getBitmap());
                            } else {
                                Log.e(TAG, "Photo " + photoDetail + " failed to load");
                            }
                        }
                    });
                } else {
                    holder.cardBackground.setMaxHeight(cardImageSize);
                    holder.cardBackground.setImageResource(R.drawable.infoback);
                }
                photoMetadata.release();
            }
        });


        holder.pcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, InfoActivity.class);
                intent.putExtra("placeid", placesList.get(holder.getAdapterPosition()).getPlaceID());
                ((Activity) v.getContext()).startActivityForResult(intent, 11);

                if (!handler.checkPlace(placesList.get(holder.getAdapterPosition()).getPlaceID())) {
                    visitSwitch = 1;
                    holder.btnVISIT.setImageResource(R.drawable.ic_history);
                    handler.addPlace(placesList.get(holder.getAdapterPosition()).getPlaceID(),
                            String.valueOf(placesList.get(holder.getAdapterPosition()).getLat()),
                            String.valueOf(placesList.get(holder.getAdapterPosition()).getLon()),
                            placesList.get(holder.getAdapterPosition()).getName(),
                            placesList.get(holder.getAdapterPosition()).getAddress(),
                            null,
                            placesList.get(holder.getAdapterPosition()).getPhoneNumber(),
                            null,
                            String.valueOf(placesList.get(holder.getAdapterPosition()).getRating()),
                            placesList.get(holder.getAdapterPosition()).getPlaceID(),
                            String.valueOf(placesList.get(holder.getAdapterPosition()).getPriceLevel()),
                            String.valueOf("1"),
                            "OFF",
                            String.valueOf("1"),
                            String.valueOf("1"),
                            placesList.get(holder.getAdapterPosition()).getDistance());
                } else {
                    visitSwitch = 0;
                    holder.btnVISIT.setVisibility(View.GONE);
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return placesList.size();
    }

}

