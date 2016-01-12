package udacityprojects.com.wonderbeefmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Joshua on 11/25/2015.
 */
public class MovieContract {

    //Authority - going with traditional app package
    public static final String CONTENT_AUTHORITY = "udacityprojects.com.wonderbeefmovies";


    // Base URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    // Paths for Movies
    public static final String PATH_MOVIES = "movies";




    public static final class MovieEntry implements BaseColumns{
        /*Fields we need as per Rubric
           * Title
           * ReleaseDate
           * Movie Poster
           * Vote Average
           * Plot Synopsis
           * */
        public static final String TABLE_NAME = "Movies";
        public static final String KEY_M_TITLE="title";
        public static final String KEY_M_RELEASE="releasedate";
        public static final String KEY_M_POSTER="poster";
        public static final String KEY_M_AVERAGE="voteaverage";
        public static final String KEY_M_SYNOPSIS="synopsis";
        public static final String KEY_M_APIID="mdbID";

        //Storing columns here because it's way too verbose to do so in DetailFragment
        public static String[] MOVIE_COLUMNS={ TABLE_NAME+"."+_ID,
                KEY_M_AVERAGE, KEY_M_TITLE, KEY_M_POSTER, KEY_M_SYNOPSIS, KEY_M_RELEASE, KEY_M_APIID
        };


        //They will all start with this
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;


        //For returning a URI representing a single movie
        public static Uri buildMovieURI(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }



    }



}
