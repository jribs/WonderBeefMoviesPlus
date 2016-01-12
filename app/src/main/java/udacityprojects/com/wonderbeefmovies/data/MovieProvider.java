package udacityprojects.com.wonderbeefmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Joshua on 11/25/2015.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private MovieDbHelper mDBHelper;

    //These are URI MAtch codes for retrieving a fovrited movies for gridview and detailview respectively
    private static final int MOVIES = 937;
    private static final int MOVIE = 951;


    @Override
    public boolean onCreate() {
        //Instantiate dbHelper

        mDBHelper = new MovieDbHelper(getContext());
        Log.d("Provider", getContext().toString());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Get a readable database
        SQLiteDatabase mDB = mDBHelper.getReadableDatabase();
        //No joins or anything crazy in this Provider, so I'm leaving the QueryBuilder internal
        SQLiteQueryBuilder qB = new SQLiteQueryBuilder();
        qB.setTables(MovieContract.MovieEntry.TABLE_NAME);

        int uriType= mUriMatcher.match(uri);

        switch (uriType){
            case MOVIES:
                return qB.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);
            case MOVIE:
                //When given an ID, we need the Where statement to = our passed ID
                qB.appendWhere(MovieContract.MovieEntry._ID + "=" + uri.getLastPathSegment());
                return qB.query(mDB, projection, selection, selectionArgs, null, null, sortOrder);


            //drp
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

    }




    @Override
    public String getType(Uri uri) {
        //Find out what we are working with (single, all movies, or not supported)
        final int uriType = mUriMatcher.match(uri);

        switch (uriType){
            case MOVIE:
                    return MovieContract.MovieEntry.CONTENT_ITEM_TYPE; //One movie
            case MOVIES:
                    return MovieContract.MovieEntry.CONTENT_TYPE; //List of Favorited Movies
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase dB=mDBHelper.getWritableDatabase();
        final int uriType = mUriMatcher.match(uri);

        //ID variable for returning in post-inserted URI
        long id;
        //Returned URI after insert
        Uri contentURI;

        //Switch isn't necessary, but offers expandability
        switch (uriType){
            //Using movies because we won't
            case MOVIES:
                id=dB.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
            //Precaution for erroneous inserts
                if(id>0){
                    contentURI = MovieContract.MovieEntry.buildMovieURI(id);
                    return contentURI;
                } else {
                    //Throw exception taken from Udacity Sunshine App Example
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase dB = mDBHelper.getWritableDatabase();
        int mURIType = mUriMatcher.match(uri);


        switch (mURIType) {

           //Using CONTENT URI here because I store Movie ID as a string and am not looking to simply
            //Appendpath. So I will provide in selectionArgs.
            case MOVIES:

                int rowsDeleted = dB.delete(MovieContract.MovieEntry.TABLE_NAME,
                        selection , selectionArgs);

                if (rowsDeleted > -1) {
                    return rowsDeleted;
                } else {
                    //Taken from Udacity Sunshine App Example
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }


            //The only intention in this app is to delete one record at a time
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
       //I'm not sure why we would update. Perhaps the user rating. But I'm adding the code just
        //to demonstrate understanding
        SQLiteDatabase dB=mDBHelper.getWritableDatabase();

        //If it's not a movie, abort
        switch (mUriMatcher.match(uri)){
            case MOVIE:
                break;
            case MOVIES:

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        int updateCount = dB.update(MovieContract.MovieEntry.TABLE_NAME, values,selection,selectionArgs);

        //Make sure we return a count greater than 0
        if(updateCount>0){
        return updateCount;}
        else {
            //Taken from Udacity Sunshine App Example
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
    }


    //Used for constructing our URIMatcher
    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher= new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        //This will be used to get all favorited movies
        matcher.addURI(authority, MovieContract.PATH_MOVIES , MOVIES);

        //And this for a specific movie
        matcher.addURI(authority, MovieContract.PATH_MOVIES +"/#", MOVIE);

        return matcher;
    }
}
