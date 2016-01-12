package udacityprojects.com.wonderbeefmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Joshua on 11/27/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //SQL Create statement
        final String SQL_CREATE_MOVIETABLE =
                "CREATE TABLE IF NOT EXISTS " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                        MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.MovieEntry.KEY_M_AVERAGE + " REAL, " +
                        MovieContract.MovieEntry.KEY_M_POSTER + " TEXT, " +
                        MovieContract.MovieEntry.KEY_M_RELEASE + " TEXT, " +
                        MovieContract.MovieEntry.KEY_M_APIID + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.KEY_M_SYNOPSIS + " TEXT, " +
                        MovieContract.MovieEntry.KEY_M_TITLE + " TEXT);";
        db.execSQL(SQL_CREATE_MOVIETABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //There are no plans for adding mor efields since this project isn't continued
        //Using DROP TABLE since need to ALTER will likely not come about

        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);

    }
}
