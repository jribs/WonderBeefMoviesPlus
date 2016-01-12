package udacityprojects.com.wonderbeefmovies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import udacityprojects.com.wonderbeefmovies.adapters.ReviewRecyclerAdapter;
import udacityprojects.com.wonderbeefmovies.adapters.TrailerRecyclerAdapter;
import udacityprojects.com.wonderbeefmovies.data.MovieContract;
import udacityprojects.com.wonderbeefmovies.events.BeefEvent;
import udacityprojects.com.wonderbeefmovies.services.ReviewAsyncTask;
import udacityprojects.com.wonderbeefmovies.services.TrailerAsyncTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{



//Constants
    private static final String TAG = "MovieDetailFragment";
    //Selection String for loading a favorited Movie
    //I read somewhere adding '=?' vs '=' prevents SQL injections; not sure if true
    private static String selection = MovieContract.MovieEntry.KEY_M_APIID + " =? ";
    //For Bundling LayoutManager scroll positions on RecyclerViews
    private static final String BUNDLE_REVIEWPOS="REVIEWPOSITION";
    private static final String BUNDLE_TITLEPOS="TITLEPOSITION";
    private static final String BUNDLE_SCROLLVIEWPOS="SCROLLVIEWPOSITION";

//Variables
    //Used for logic in calling the correct load method
    private boolean mFavoriteMode;
    private String mAPI_MovieID;
    //JSON object representing our movie. Instantiate onCreate
    private String mMovie;
    //Bundle used to store Scroll Positions (to use after data synced
    private Bundle mSavedState;



    //View variables. Used when parsing data
    @Bind(R.id.detail_releaseDate) TextView vReleaseDate;
    @Bind(R.id.detail_description) TextView vSynopsis;
    @Bind(R.id.detail_rating) TextView vRating;
    @Bind(R.id.detail_poster) ImageView vPoster;
    @Bind(R.id.detail_Title) TextView vTitle;
    @Bind(R.id.detail_Favorite) CheckBox vFavorite;
    @Bind(R.id.detail_list_reviews) RecyclerView vListReviews;
    @Bind(R.id.detail_list_trailers) RecyclerView vListTrailers;
    @Bind(R.id.detail_switch) SwitchCompat vSwitch;

//===================Fragment Lifecycle=================================
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Used for async tasks to restore scroll position AFTER load
        mSavedState = savedInstanceState;



    }

    //Here we store the loadedID so we can restore onCreate
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.key_args_favoriteMode), mFavoriteMode);

        if(mFavoriteMode==true) {
            outState.putString(getString(R.string.key_args_movieID), mAPI_MovieID);
        } else {
            outState.putString(getString(R.string.key_args_movieItem), mMovie);
        }

        //Saving UI items here (Scrollview and RecyclerView Positions)
        outState.putParcelable(BUNDLE_REVIEWPOS, vListReviews.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(BUNDLE_TITLEPOS, vListTrailers.getLayoutManager().onSaveInstanceState());


    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, v);

        //Set adapters and layoutmanagers for RecyclerViews to null so Android doesn't have seizure
        vListReviews.setLayoutManager(new LinearLayoutManager(getActivity()));
        vListTrailers.setLayoutManager(new LinearLayoutManager(getActivity()));

        vListReviews.setAdapter(null);
        vListTrailers.setAdapter(null);



        //If we have arguments load them (we get arguments from DetailActivity or saveInstanceState)
        Bundle args = getArguments();
        //If we don't have arguments, set them = to SavedInstanceState IF its available
        if(mSavedState!=null&&args==null){
            args=mSavedState;
        }

        if(args!=null) {
            if (args.containsKey(getString(R.string.key_args_movieItem))) {
                loadJSONMovie(args.getString(getString(R.string.key_args_movieItem)));

                //Check if this is a favorited movie. If so, set the checkbox accordingly



            } else if(args.containsKey(getString(R.string.key_args_movieID))){
                loadFavoritedMovie(args.getString(getString(R.string.key_args_movieID)));
                vFavorite.setChecked(true);
            }
        }

        setUpWidgets();
        loadExtras();
        return  v;
    }

    @Override
    public void onStart(){
        super.onStart();
        //Register event bus on start
        App.getInstance().getEventBus().register(this);
    }
    @Override
    public void onStop(){
        super.onStop();
        //Unregister when destroyed
        App.getInstance().getEventBus().unregister(this);
    }


    //This segment is for actually going back when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }


//======================API Loading=======================================
    //Here we crank out linking JSON fields to their respective Views
    private void parseJSONData(String JSONstring){
        //First some strings representing the fieldnames as marked in MDB API
        final String MDB_TITLE="original_title";
        final String MDB_RATING="vote_average";
        final String MDB_SYNOPSIS = "overview";
        final String MDB_RELEASEDATE="release_date";
        final String MDB_ID="id";

   try {

      JSONObject movie=new JSONObject(JSONstring);
            //Set the movieID just in case we favorite.

            //Image URL acquired by lovely concatenation
            String imageURL = getString(R.string.url_posterpath) +
                    getString(R.string.url_poster_size_185)+
                    movie.get(getString(R.string.api_posterPath));

            vTitle.setText(movie.getString(MDB_TITLE));
            vRating.setText(movie.getString(MDB_RATING));

            vReleaseDate.setText(formatUSDate(movie.getString(MDB_RELEASEDATE)));

            mAPI_MovieID = movie.getString(MDB_ID);

            //Some of the synopses were null, so I am putting a check here so it doesn't look silly
            String synopsis = movie.getString(MDB_SYNOPSIS);
            if(synopsis.matches("null")||synopsis==null) {
                vSynopsis.setText("");
            }else {
                vSynopsis.setText("     " + (synopsis));
            }

            //Load the image using Picasso
            Picasso.with(getActivity())
                    .load(imageURL)
                    .into(vPoster);

        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }

    }

    //For formatting the date and keeping the parseJSON method readable
    private String formatUSDate(String date){
        String formattedDate="";
        SimpleDateFormat fromMDB = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat toUSstandard = new SimpleDateFormat("dd/MM/yyyy");


        try {
            formattedDate= toUSstandard.format(fromMDB.parse(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }


    //Called from Parent Activity to load passed JSON string
    public void loadJSONMovie(String JSONargs) {
        mMovie=JSONargs;
        parseJSONData(mMovie);
        //Set the mode for onSaveInstanceState
        mFavoriteMode=false;
        loadExtras();
    }


//========================Favorites===============================



    /*Logic for Loading a favorited movie:
    * ID and APIMovieID Passed
    * invoke loadermanager with both MovieID and ID parameters
    * onLoadFinished - load text fields from cursor, load image from
    * internal storage using movieID as the file name
    * */
    public void loadFavoritedMovie(String movieID){
        //Take the Movie's SQLID and initialize the Loader with it
        Bundle movieArgs = new Bundle();
        movieArgs.putString(getString(R.string.key_args_movieID), movieID);
        getLoaderManager().initLoader(0, movieArgs, this);

        mAPI_MovieID=movieID;
        //Set the mode for onSaveInstanceState
        mFavoriteMode=true;
        loadExtras();
    }


    private void saveFavorite(){
        //Loading content values. Straight forward, save for image path
        ContentValues cv = new ContentValues();
        cv.put(MovieContract.MovieEntry.KEY_M_TITLE, vTitle.getText().toString());
        cv.put(MovieContract.MovieEntry.KEY_M_AVERAGE, vRating.getText().toString());
        cv.put(MovieContract.MovieEntry.KEY_M_RELEASE, vReleaseDate.getText().toString());
        cv.put(MovieContract.MovieEntry.KEY_M_SYNOPSIS, vSynopsis.getText().toString());
        cv.put(MovieContract.MovieEntry.KEY_M_APIID, mAPI_MovieID);
        //Might have to build cache first. leaving this line of code here just in case

        //Passing a bitmap to saveToInternalStorage was returning null. This was online solution
        //http://stackoverflow.com/questions/2339429/android-view-getdrawingcache-returns-null-only-null

     vPoster.setDrawingCacheEnabled(true);
        vPoster.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        vPoster.layout(0, 0, vPoster.getMeasuredWidth(), vPoster.getMeasuredHeight());


        vPoster.buildDrawingCache(true);
       Bitmap b = Bitmap.createBitmap(vPoster.getDrawingCache());
        //vPoster.setDrawingCacheEnabled(false); // clear drawing cache
        vPoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
                savePosterToInternalStorage(b, mAPI_MovieID);



        getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv);


    }



    /**For saving images.
     * Used http://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
        as reference
     **/
     private String savePosterToInternalStorage(Bitmap bitmapImage, String movieID){
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        //Where we will be storing images
        File directory = cw.getDir(getString(R.string.key_path_posters), Context.MODE_PRIVATE);

        // Create imageDir with Movie SQL ID as image name
        File myPath=new File(directory,movieID + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } finally {

            try {
                fos.close();
            } catch (IOException e){
                Log.e(TAG, e.getMessage());
            }

        }
        return directory.getAbsolutePath();
    }



    //LoaderManager Methods
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {

        //Making sure we actually go the right argument first
        if(!args.containsKey(getString(R.string.key_args_movieID))){
            Log.e(TAG, "LoaderManager did not receive correct key to load movie");
            return null;
        }

        String id = args.getString(getString(R.string.key_args_movieID));

        CursorLoader cLS= new CursorLoader(getActivity(), 	//Context
                MovieContract.MovieEntry.CONTENT_URI, //URI
                MovieContract.MovieEntry.MOVIE_COLUMNS,	   //The fields we want (Must Include ID)
                MovieContract.MovieEntry.KEY_M_APIID + "=?",	   //WHERE statement in SQL
                new String[]{id}, //Arguments for Selection statement
                null); //Sort

        return cLS;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cU) {
        cU.moveToFirst();
        Log.d(TAG, String.valueOf(cU.getCount()));
        if(cU.getCount()>0) {
            //GROAN ZONE. Needs to be more options than statically declaring column numbers and calling this
            vTitle.setText(cU.getString(cU.getColumnIndexOrThrow(MovieContract.MovieEntry.KEY_M_TITLE)));
            vRating.setText(cU.getString(cU.getColumnIndexOrThrow(MovieContract.MovieEntry.KEY_M_AVERAGE)));
            vReleaseDate.setText(
                    formatUSDate(
                            cU.getString(cU.getColumnIndexOrThrow(MovieContract.MovieEntry.KEY_M_RELEASE))));


            //Some of the synopses were null, so I am putting a check here so it doesn't look silly
            String synopsis = cU.getString(cU.getColumnIndexOrThrow(MovieContract.MovieEntry.KEY_M_SYNOPSIS));
            if (synopsis.matches("null") || synopsis == null) {
                vSynopsis.setText("");
            } else {
                vSynopsis.setText("     " + (synopsis));
            }

            //Load the image
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            //Where we will be storing images
            File directory = cw.getDir(getString(R.string.key_path_posters), Context.MODE_PRIVATE);


            File f = new File(directory,
                    cU.getString(cU.getColumnIndexOrThrow(MovieContract.MovieEntry.KEY_M_APIID))+
                            ".jpg");

            Picasso.with(getActivity())
                    .load(f)
                    .into(vPoster);

        } else {
            Log.e(TAG, "Cursor returned 0 results");
        }

        //TODO Load a fricking image from the hahd drhive
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        vTitle.setText(null);
        vRating.setText(null);
        vReleaseDate.setText(null);
        vSynopsis.setText(null);
        vPoster.setImageDrawable(null);
    }







    //===========Subscriptions================
    @Subscribe
    public void TrailersFinished(BeefEvent.TrailerSyncComplete event){
        TrailerRecyclerAdapter trailerRecyclerAdapter =
                new TrailerRecyclerAdapter(event.getTrailers(), getActivity());

            vListTrailers.setAdapter(trailerRecyclerAdapter);
        if(mSavedState!=null) {
            vListTrailers.getLayoutManager().onRestoreInstanceState(mSavedState.getParcelable(BUNDLE_TITLEPOS));
        }
    }

    @Subscribe
    public void ReviewsFinished(BeefEvent.ReviewSyncComplete event){
        ReviewRecyclerAdapter reviewRecyclerAdapter = new ReviewRecyclerAdapter(event.getReviews());

        vListReviews.setAdapter(reviewRecyclerAdapter);
        if(mSavedState!=null) {
        vListReviews.getLayoutManager().onRestoreInstanceState(mSavedState.getParcelable(BUNDLE_REVIEWPOS));
        }
    }


    //============Utilities==========================

    private void setUpWidgets(){
        //Set up Switch to show reviews (Dual RecyclerViews were yielding problems in ScrollView
        vSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    vListTrailers.setVisibility(View.GONE);
                    vListReviews.setVisibility(View.VISIBLE);
                } else {
                    vListTrailers.setVisibility(View.VISIBLE);
                    vListReviews.setVisibility(View.GONE);
                }
            }
        });

        //Set Up Favorite button
        if(mAPI_MovieID!=null) {
            if (checkIfFavorited(mAPI_MovieID)) {
                vFavorite.setChecked(true);
            } else {
                vFavorite.setChecked(false);
            }
        }

        //Checkbox listener
        vFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //if it is checked call the save
                if (isChecked) {
                    saveFavorite();
                    vFavorite.setChecked(true);
                }
                //If not, run delete from ContentProvider
                else {

                    int rowsDeleted = getActivity().getContentResolver().delete(
                            MovieContract.MovieEntry.CONTENT_URI,
                            selection, new String[]{String.valueOf(mAPI_MovieID)});
                    //So grid refreshed
                    getActivity().setResult(Activity.RESULT_OK);
                    App.getInstance().getEventBus().post(new BeefEvent.ItemUnFavoritedEvent());
                    vFavorite.setChecked(false);
                }
            }
        });
    }

    //Loads Trailers and Reviews
    private void loadExtras(){
        if(mAPI_MovieID!=null) {
            if (!mFavoriteMode) {
                String[] params = {mAPI_MovieID, getString(R.string.url_APIKEY)};
                TrailerAsyncTask trailerAsyncTask = new TrailerAsyncTask();
                trailerAsyncTask.execute(params);
                ReviewAsyncTask reviewAsyncTask = new ReviewAsyncTask();
                reviewAsyncTask.execute(params);

            }
        }
    }

    private boolean checkIfFavorited(String API_ID){
        //Since we don't need all the extra data, we are simply concerned with whether or not the APIID exists
        String[] projection = {MovieContract.MovieEntry._ID, MovieContract.MovieEntry.KEY_M_APIID};


        Cursor cU = getActivity().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                projection,             //Projection
                selection,              //Selection
                new String[]{API_ID},   //Selection Args
                null);                  //Sort Order

        if(cU!=null && cU.getCount()>0){
            cU.close();
            return true;
        } else {
            cU.close();
            return false;
        }
    }
}
