package app.com.getplace.db;


import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String cmd = "CREATE TABLE "+Constants.DB_TABLE_NAME+" ("+
                Constants.DB_CLM_NAME_id+" INTEGER PRIMARY KEY, " +
                Constants.DB_CLM_NAME_PlaceID + " TEXT, " +
                Constants.DB_CLM_NAME_lat + " TEXT, " +
                Constants.DB_CLM_NAME_lon + " TEXT, " +
                Constants.DB_CLM_NAME_name + " TEXT, " +
                Constants.DB_CLM_NAME_address + " TEXT, " +
                Constants.DB_CLM_NAME_byteArray + " TEXT, " +
                Constants.DB_CLM_NAME_phoneNumber + " TEXT, " +
                Constants.DB_CLM_NAME_websiteUri + " TEXT, " +
                Constants.DB_CLM_NAME_rating + " TEXT, " +
                Constants.DB_CLM_NAME_placeType + " TEXT, " +
                Constants.DB_CLM_NAME_priceLevel + " TEXT, " +
                Constants.DB_CLM_NAME_visit + " TEXT, " +
                Constants.DB_CLM_NAME_favorite + " TEXT, " +
                Constants.DB_CLM_NAME_active + " TEXT, " +
                Constants.DB_CLM_NAME_type + " TEXT, " +
                Constants.DB_CLM_NAME_distance + " TEXT )";
        try {
            sqLiteDatabase.execSQL(cmd);
        } catch (SQLiteException e) {
            e.getMessage();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
