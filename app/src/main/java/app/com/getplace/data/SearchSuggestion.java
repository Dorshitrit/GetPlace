package app.com.getplace.data;

import android.os.Parcel;

public class SearchSuggestion implements com.arlib.floatingsearchview.suggestions.model.SearchSuggestion {

    private String sPlaceId;
    private String sPlaceName;
    private String sPlaceAddress;
    private String sPlaceLat;
    private String sPlaceLon;
    private boolean mIsHistory = false;

    public String getsPlaceId() {
        return sPlaceId;
    }

    public void setsPlaceId(String sPlaceId) {
        this.sPlaceId = sPlaceId;
    }

    public String getsPlaceName() {
        return sPlaceName;
    }

    public void setsPlaceName(String sPlaceName) {
        this.sPlaceName = sPlaceName;
    }

    public String getsPlaceAddress() {
        return sPlaceAddress;
    }

    public void setsPlaceAddress(String sPlaceAddress) {
        this.sPlaceAddress = sPlaceAddress;
    }

    public String getsPlaceLat() {
        return sPlaceLat;
    }

    public void setsPlaceLat(String sPlaceLat) {
        this.sPlaceLat = sPlaceLat;
    }

    public String getsPlaceLon() {
        return sPlaceLon;
    }

    public void setsPlaceLon(String sPlaceLon) {
        this.sPlaceLon = sPlaceLon;
    }

    public boolean ismIsHistory() {
        return mIsHistory;
    }

    public void setmIsHistory(boolean mIsHistory) {
        this.mIsHistory = mIsHistory;
    }

    public SearchSuggestion(String sPlaceId, String sPlaceName, String sPlaceAddress, String sPlaceLat, String sPlaceLon) {
        this.sPlaceId = sPlaceId;
        this.sPlaceName = sPlaceName.toLowerCase();
        this.sPlaceAddress = sPlaceAddress;
        this.sPlaceLat = sPlaceLat;
        this.sPlaceLon = sPlaceLon;

    }

    public SearchSuggestion(Parcel source) {
        this.sPlaceId = source.readString();
        this.sPlaceName = source.readString();
        this.sPlaceAddress = source.readString();
        this.sPlaceLat = source.readString();
        this.sPlaceLon = source.readString();
        this.mIsHistory = source.readInt() != 0;
    }

    public SearchSuggestion() {
    }

    public void setIsHistory(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }


    @Override
    public String getBody() {
        return sPlaceName;
    }

    public static final Creator<SearchSuggestion> CREATOR = new Creator<SearchSuggestion>() {
        @Override
        public SearchSuggestion createFromParcel(Parcel in) {
            return new SearchSuggestion(in);
        }

        @Override
        public SearchSuggestion[] newArray(int size) {
            return new SearchSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sPlaceId);
        dest.writeString(sPlaceName);
        dest.writeString(sPlaceAddress);
        dest.writeString(sPlaceLat);
        dest.writeString(sPlaceLon);
        dest.writeInt(mIsHistory ? 1 : 0);
    }
}