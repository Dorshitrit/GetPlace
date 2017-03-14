package app.com.getplace.Objects;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.List;

/**
 * Created by DorSh on 05-Nov-16.
 */

public class PlaceModule {

    private double lat;
    private double lon;
    String PlaceID;
    private byte[] byteArray;
    private String Name, Address;
    private String WebsiteUri;
    private CharSequence PhoneNumber;
    private float Rating;
    private List PlaceType;
    private int PriceLevel;
    private int visit, Active, Type;
    private String Favorite;
    private String Distance;


    public PlaceModule(String PlaceID, double lat, double lon, String name, String address, byte[] byteArray, CharSequence phoneNumber, String websiteUri,
                       float rating, List placeType, int priceLevel, int visit, String favorite, int active, int type, String distance) {
        this.PlaceID = PlaceID;
        this.lat = lat;
        this.lon = lon;
        Name = name;
        Address = address;
        this.byteArray = byteArray;
        PhoneNumber = phoneNumber;
        WebsiteUri = websiteUri;
        Rating = rating;
        PlaceType = placeType;
        PriceLevel = priceLevel;
        this.visit = visit;
        Favorite = favorite;
        Active = active;
        Type = type;
        Distance = distance;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public CharSequence getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(CharSequence phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getWebsiteUri() {
        return WebsiteUri;
    }

    public void setWebsiteUri(String websiteUri) {
        WebsiteUri = websiteUri;
    }

    public float getRating() {
        return Rating;
    }

    public void setRating(float rating) {
        Rating = rating;
    }

    public List getPlaceType() {
        return PlaceType;
    }

    public void setPlaceType(List placeType) {
        PlaceType = placeType;
    }

    public int getPriceLevel() {
        return PriceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        PriceLevel = priceLevel;
    }

    public int getVisit() {
        return visit;
    }

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public String getFavorite() {
        return Favorite;
    }

    public void setFavorite(String favorite) {
        Favorite = favorite;
    }

    public int getActive() {
        return Active;
    }

    public void setActive(int active) {
        Active = active;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public String getPlaceID() {
        return PlaceID;
    }

    public void setPlaceID(String placeID) {
        PlaceID = placeID;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }
}
