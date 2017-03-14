package app.com.getplace.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

import app.com.getplace.Objects.PlaceModule;
import app.com.getplace.data.SearchSuggestion;

/**
 * Created by DorSh on 10-Dec-16.
 */

public class DBHandler {

    private DBHelper helper;

    public DBHandler(Context context) {
        helper = new DBHelper(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }


// String PlaceID,
// double lat,
// double lon,
// String name,
// String address,
// byte[] byteArray,
// CharSequence phoneNumber,
// Uri websiteUri,
// float rating,
// List placeType,
// int priceLevel,
// int visit,
// int favorite,
// int active,
// int type,
// String distance)


    public void addPlace(String PlaceID, String lat, String lon, String name, String address, String byteArray, CharSequence phoneNumber, String websiteUri,
                         String rating, String placeType, String priceLevel, String visit, String favorite, String active, String type, String distance) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Constants.DB_CLM_NAME_PlaceID, PlaceID); //1
            values.put(Constants.DB_CLM_NAME_lat, lat); //2
            values.put(Constants.DB_CLM_NAME_lon, lon); //3
            values.put(Constants.DB_CLM_NAME_name, name); //4
            values.put(Constants.DB_CLM_NAME_address, address); //5
            values.put(Constants.DB_CLM_NAME_byteArray, byteArray); //6
            values.put(Constants.DB_CLM_NAME_phoneNumber, String.valueOf(phoneNumber)); //7
            values.put(Constants.DB_CLM_NAME_websiteUri, websiteUri); //8
            values.put(Constants.DB_CLM_NAME_rating, rating); //9
            values.put(Constants.DB_CLM_NAME_placeType, placeType); //10
            values.put(Constants.DB_CLM_NAME_priceLevel, priceLevel); //11
            values.put(Constants.DB_CLM_NAME_visit, visit); //12
            values.put(Constants.DB_CLM_NAME_favorite, favorite); //13
            values.put(Constants.DB_CLM_NAME_active, active); //14
            values.put(Constants.DB_CLM_NAME_type, type); //15
            values.put(Constants.DB_CLM_NAME_distance, distance); //16
            db.insert(Constants.DB_TABLE_NAME, null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }


    public List<PlaceModule> getUserPlaces(int select) {
        SQLiteDatabase db = null;
        List<PlaceModule> list = new ArrayList<>();
        String ORDER = null;
        String WHAT = null;
        switch (select) {
            case 1:
                WHAT = "_id=?";
                ORDER = " ORDER BY _id DESC";
                break;
            case 2:
                WHAT = "visit=?";
                ORDER = "1";
                break;
            case 3:
                WHAT = "favorite=?";
                ORDER = "ON";
                break;
            case 4:
                WHAT = "URating>?";
                ORDER = " WHERE URating > 0";
                break;
        }
        try {
            db = helper.getReadableDatabase();
            Cursor cursor = null;
            if (select == 1) {
                cursor = db.rawQuery("SELECT * FROM " + Constants.DB_TABLE_NAME + ORDER, null);
            } else if (select == 2 || select == 3) {
                cursor = db.query(Constants.DB_TABLE_NAME, null, WHAT, new String[]{ORDER}, null, null, null, null);
            } else if (select == 4) {
                cursor = db.rawQuery("SELECT * FROM " + Constants.DB_TABLE_NAME + ORDER, null);
            }
            int id;
            String placeid, lat, lon, name, address, phoneNumber, website, visit, dist, liked;
            while (cursor.moveToNext()) {
                id = cursor.getInt(0);
                placeid = cursor.getString(1);
                lat = cursor.getString(2);
                lon = cursor.getString(3);
                name = cursor.getString(4);
                address = cursor.getString(5);
                phoneNumber = cursor.getString(7);
                website = cursor.getString(8);
                visit = cursor.getString(12);
                liked = cursor.getString(13);
                dist = cursor.getString(16);
                PlaceModule places = new PlaceModule(placeid, Double.valueOf(lat), Double.valueOf(lon), name, address, null, phoneNumber, website, 0, null, 0, Integer.valueOf(visit), liked, 0, 0, dist);
                list.add(places);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
        return list;
    }

    public List<SearchSuggestion> getHistoryPlace(int select) {
        SQLiteDatabase db = null;
        List<SearchSuggestion> list = new ArrayList<>();
        String ORDER = null;
        String WHAT = null;
        switch (select) {
            case 1:
                WHAT = "_id=?";
                ORDER = " ORDER BY _id DESC";
                break;
            case 2:
                WHAT = "visit=?";
                ORDER = "1";
                break;
            case 3:
                WHAT = "favorite=?";
                ORDER = "ON";
                break;
            case 4:
                WHAT = "URating>?";
                ORDER = " WHERE URating > 0";
                break;
        }
        try {
            db = helper.getReadableDatabase();
            Cursor cursor = null;
            if (select == 1) {
                cursor = db.rawQuery("SELECT * FROM " + Constants.DB_TABLE_NAME + ORDER, null);
            } else if (select == 2 || select == 3) {
                cursor = db.query(Constants.DB_TABLE_NAME, null, WHAT, new String[]{ORDER}, null, null, Constants.DB_CLM_NAME_id + " DESC", null);
            } else if (select == 4) {
                cursor = db.rawQuery("SELECT * FROM " + Constants.DB_TABLE_NAME + ORDER, null);
            }
            int id;
            String placeid, lat, lon, name, address, phoneNumber, website, visit, dist, liked;
            while (cursor.moveToNext()) {
                id = cursor.getInt(0);
                placeid = cursor.getString(1);
                lat = cursor.getString(2);
                lon = cursor.getString(3);
                name = cursor.getString(4);
                address = cursor.getString(5);
                phoneNumber = cursor.getString(7);
                website = cursor.getString(8);
                visit = cursor.getString(12);
                liked = cursor.getString(13);
                dist = cursor.getString(16);
                SearchSuggestion placess = new SearchSuggestion(placeid, name, address, lat, lon);
                list.add(placess);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
        return list;
    }

    public boolean checkPlace(String checkPID) {
        SQLiteDatabase db = null;
        boolean check = false;
        try {
            db = helper.getReadableDatabase();
            Cursor cursor = null;
            String WHAT = "PlaceID=?";
            cursor = db.query(Constants.DB_TABLE_NAME, null, WHAT, new String[]{checkPID}, null, null, null, null);
            String sendID;
            while (cursor.moveToNext()) {
                sendID = cursor.getString(1);
                if (sendID.equals(checkPID)) {
                    check = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
        return check;
    }


    public void removeVisit(String MID) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            ContentValues args = new ContentValues();
            db.delete(Constants.DB_TABLE_NAME, "PlaceID=?", new String[]{MID});
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean checkPlaceLike(String checkPID) {
        SQLiteDatabase db = null;
        boolean check = false;
        try {
            db = helper.getReadableDatabase();
            Cursor cursor = null;
            String WHAT = "PlaceID=?";
            cursor = db.query(Constants.DB_TABLE_NAME, null, WHAT, new String[]{checkPID}, null, null, null, null);
            String isLike;
            String result = "ON";
            while (cursor.moveToNext()) {
                isLike = cursor.getString(13);
                if (isLike.equals(result)) {
                    check = true;
                }
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
        return check;
    }


    public void updateLike(String MID, String like) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            ContentValues args = new ContentValues();
            args.put(Constants.DB_CLM_NAME_favorite, like);
            db.update(Constants.DB_TABLE_NAME, args, "PlaceID=?", new String[]{MID});
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public void deleteAllHistory() {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.delete(Constants.DB_TABLE_NAME, "visit=?", new String[]{"1"});
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public void deleteAllFavorite() {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.delete(Constants.DB_TABLE_NAME, "favorite=?", new String[]{"ON"});
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public void PlaceActive(String MID, String active) {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            ContentValues args = new ContentValues();
            args.put(Constants.DB_CLM_NAME_active, active);
            db.update(Constants.DB_TABLE_NAME, args, "PlaceID=?", new String[]{MID});
        } catch (SQLiteException e) {
            e.getMessage();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }


    //
//    public List<PlaceModule> getMoviesListFAV() {
//        SQLiteDatabase db = null;
//        List<PlaceModule> list = new ArrayList<>();
//        try {
//            db = helper.getReadableDatabase();
//            Cursor cursor = null;
//            cursor = db.rawQuery("SELECT * FROM " + Constants.DB_TABLE_NAME + " WHERE", null);
//            int id;
//            String Title, Poster, imdbID, FAV, WATCH;
//            float RAT;
//            while (cursor.moveToNext()) {
//                id = cursor.getInt(0);
//                Title = cursor.getString(1);
//                Poster = cursor.getString(14);
//                imdbID = cursor.getString(18);
//                FAV = cursor.getString(21);
//                WATCH = cursor.getString(22);
//                RAT = cursor.getFloat(23);
//                PlaceModule PlaceModule = new PlaceModule(id, Title, "", "", "", "", "", "", "", "", "", "", "", "", Poster, "", "", "", imdbID, "", "", FAV, WATCH, RAT);
//                list.add(movies);
//            }
//        } catch (SQLiteException e) {
//            e.printStackTrace();
//        } finally {
//            if (db.isOpen()) {
//                db.close();
//            }
//        }
//        return list;
//    }
//

    //
//
//    public Movies getMovieView(String movID) {
//        SQLiteDatabase db = null;
//        Movies movies = null;
//        try {
//            db = helper.getReadableDatabase();
//            Cursor cursor = null;
//            String  WHAT = "_id=?";
//            cursor = db.query(Constants.DB_TABLE_NAME, null,WHAT,new String [] {movID}, null, null, null, null);
//            cursor.moveToFirst();
//            movies = new Movies();
//            String Title, Year, Rated, Released, Runtime, Genre, Director, Writer, Actors, Plot, Language,
//                    Country, Awards, Poster, Metascore, imdbRating, imdbVotes, imdbID, Type, Response, Watched, Favorite;
//            float URating;
//            int id = cursor.getInt(0);
//            Title = cursor.getString(1);
//            Year = cursor.getString(2);
//            Rated = cursor.getString(3);
//            Released = cursor.getString(4);
//            Runtime = cursor.getString(5);
//            Genre = cursor.getString(6);
//            Director = cursor.getString(7);
//            Writer = cursor.getString(8);
//            Actors = cursor.getString(9);
//            Plot = cursor.getString(10);
//            Language = cursor.getString(11);
//            Country = cursor.getString(12);
//            Awards = cursor.getString(13);
//            Poster = cursor.getString(14);
//            Metascore = cursor.getString(15);
//            imdbRating = cursor.getString(16);
//            imdbVotes = cursor.getString(17);
//            imdbID = cursor.getString(18);
//            Type = cursor.getString(19);
//            Response = cursor.getString(20);
//            Watched = cursor.getString(21);
//            Favorite = cursor.getString(22);
//            URating = cursor.getFloat(23);
//
//            movies.setId(id);
//            movies.setTitle(Title);
//            movies.setYear(Year);
//            movies.setRated(Rated);
//            movies.setReleased(Released);
//            movies.setRuntime(Runtime);
//            movies.setGenre(Genre);
//            movies.setDirector(Director);
//            movies.setWriter(Writer);
//            movies.setActors(Actors);
//            movies.setPlot(Plot);
//            movies.setLanguage(Language);
//            movies.setCountry(Country);
//            movies.setAwards(Awards);
//            movies.setPoster(Poster);
//            movies.setMetascore(Metascore);
//            movies.setImdbRating(imdbRating);
//            movies.setImdbVotes(imdbVotes);
//            movies.setImdbID(imdbID);
//            movies.setType(Type);
//            movies.setResponse(Response);
//            movies.setWatched(Watched);
//            movies.setFavorite(Favorite);
//            movies.setURating(URating);
//
//
//        } catch (SQLiteException e) {
//            e.printStackTrace();
//        } finally {
//            if (db.isOpen()) {
//                db.close();
//            }
//        }
//        return movies;
//    }
//
//
//    public void dellAllmovies() {
//        SQLiteDatabase db = null;
//        try{
//            db = helper.getWritableDatabase();
//            db.execSQL("DELETE FROM "+Constants.DB_TABLE_NAME);
//        } catch (SQLiteException e) {
//            e.getMessage();
//        } finally {
//            if(db.isOpen()) {
//                db.close();
//            }
//        }
//    }
//
//    public void dellmovies(String thisMOVIE) {
//        SQLiteDatabase db = null;
//        try{
//            db = helper.getWritableDatabase();
//            db.delete(Constants.DB_TABLE_NAME, "_id=?", new String[] {thisMOVIE});
//        } catch (SQLiteException e) {
//            e.getMessage();
//        } finally {
//            if(db.isOpen()) {
//                db.close();
//            }
//        }
//    }
//
//    public void updateMOVIE(int MID ,String title, String year, String rated, String released, String runtime, String genre, String director, String writer,
//                            String actors, String plot, String lang, String country, String awards, String poster, String metascore, String imdbRating, String imdbVotes,
//                            String imdbID, String type, String response, String Watched, String Favorite, float URating) {
//        SQLiteDatabase db = null;
//        try {
//            db = helper.getWritableDatabase();
//            ContentValues args = new ContentValues();
//            args.put(Constants.DB_CLM_NAME_Title, title);
//            args.put(Constants.DB_CLM_NAME_Year, year);
//            args.put(Constants.DB_CLM_NAME_Rated, rated);
//            args.put(Constants.DB_CLM_NAME_Released, released);
//            args.put(Constants.DB_CLM_NAME_Runtime, runtime);
//            args.put(Constants.DB_CLM_NAME_Genre, genre);
//            args.put(Constants.DB_CLM_NAME_Director, director);
//            args.put(Constants.DB_CLM_NAME_Writer, writer);
//            args.put(Constants.DB_CLM_NAME_Actors, actors);
//            args.put(Constants.DB_CLM_NAME_Plot, plot);
//            args.put(Constants.DB_CLM_NAME_Language, lang);
//            args.put(Constants.DB_CLM_NAME_Country, country);
//            args.put(Constants.DB_CLM_NAME_Awards, awards);
//            args.put(Constants.DB_CLM_NAME_Poster, poster);
//            args.put(Constants.DB_CLM_NAME_Metascore, metascore);
//            args.put(Constants.DB_CLM_NAME_imdbRating, imdbRating);
//            args.put(Constants.DB_CLM_NAME_imdbVotes, imdbVotes);
//            args.put(Constants.DB_CLM_NAME_imdbID, imdbID);
//            args.put(Constants.DB_CLM_NAME_Type, type);
//            args.put(Constants.DB_CLM_NAME_Response, response);
//            args.put(Constants.DB_CLM_NAME_Watched, Watched);
//            args.put(Constants.DB_CLM_NAME_Favorite, Favorite);
//            args.put(Constants.DB_CLM_NAME_URating, URating);
//            db.update(Constants.DB_TABLE_NAME, args, "_id=?", new String[] { String.valueOf(MID) });
//        } catch (SQLiteException e) {
//            e.getMessage();
//        } finally {
//            if(db.isOpen()) {
//                db.close();
//            }
//        }
//    }
//
//

//
//    public void updateRAT(int MID, Float RATING) {
//        SQLiteDatabase db = null;
//        try {
//            db = helper.getWritableDatabase();
//            ContentValues args = new ContentValues();
//            args.put(Constants.DB_CLM_NAME_URating, RATING);
//            db.update(Constants.DB_TABLE_NAME, args, "_id=?", new String[] { String.valueOf(MID) });
//        } catch (SQLiteException e) {
//            e.getMessage();
//        } finally {
//            if(db.isOpen()) {
//                db.close();
//            }
//        }
//    }


}
