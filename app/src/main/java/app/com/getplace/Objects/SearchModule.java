package app.com.getplace.Objects;


import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

//http://www.parcelabler.com/

public class SearchModule implements Parcelable, SearchSuggestion {
    String sp_id, sp_name, sp_Fadress;
    String sp_lat, sp_lon;





    public SearchModule(String sp_id, String sp_name, String sp_Fadress, String sp_lat, String sp_lon) {
        this.sp_id = sp_id;
        this.sp_name = sp_name;
        this.sp_Fadress = sp_Fadress;
        this.sp_lat = sp_lat;
        this.sp_lon = sp_lon;
    }


    protected SearchModule(Parcel in) {
        sp_id = in.readString();
        sp_name = in.readString();
        sp_Fadress = in.readString();
        sp_lat = in.readString();
        sp_lon = in.readString();
    }


    public static final Creator<SearchModule> CREATOR = new Creator<SearchModule>() {
        @Override
        public SearchModule createFromParcel(Parcel in) {
            return new SearchModule(in);
        }

        @Override
        public SearchModule[] newArray(int size) {
            return new SearchModule[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sp_id);
        parcel.writeString(sp_name);
        parcel.writeString(sp_Fadress);
        parcel.writeString(sp_lat);
        parcel.writeString(sp_lon);
    }


    @Override
    public String toString() {
        return sp_name;
    }

    @Override
    public String getBody() {
        return sp_name;
    }
}