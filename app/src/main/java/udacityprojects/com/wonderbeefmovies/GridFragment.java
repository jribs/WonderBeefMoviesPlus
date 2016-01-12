package udacityprojects.com.wonderbeefmovies;


import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import udacityprojects.com.wonderbeefmovies.adapters.FavoriteRecyclerAdapter;
import udacityprojects.com.wonderbeefmovies.adapters.MovieRecyclerViewAdapter;
import udacityprojects.com.wonderbeefmovies.data.MovieContract;
import udacityprojects.com.wonderbeefmovies.events.BeefEvent;

public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    //Views
    private RecyclerView vGrid;
    private GridLayoutManager mGridManager;


    //Constants
    private static final String TAG = "GridFragment";
    private static final String BUNDLE_RECYCLERPOSITION="recyclerposition";

    //Variables
    private Bundle mLayoutArgs;
    private FavoriteRecyclerAdapter mFavAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mLayoutArgs = savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_main_grid, container, false);


        //Instantiate the RecyclerView, set it to Gridlayout
        vGrid=(RecyclerView) v.findViewById(R.id.main_list);
        vGrid.setHasFixedSize(true);


        //TODO use bool from Layout.xml to determine how many columns
        //Depending on orientation, assign 2 or 4 columns for portrait and landscape respectively
        //onCreateView is called on ever orientation change, so we can leave an if statement here
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mGridManager=new GridLayoutManager(getActivity(), 2);

        }
        else{
            mGridManager = new GridLayoutManager(getActivity(), 3);
        }
        vGrid.setLayoutManager(mGridManager);
        //Setting a null adapter here just to get rid of pesky errors, it is reset in AsyncTask
        vGrid.setAdapter(null);
        String sortPref = getSortByPreference();
        if(sortPref.equals(getResources().getStringArray(R.array.values_sortby)[2])){
         getLoaderManager().initLoader(0, null, this);
        } else {
            //Initiate the Async Task
            RetrieveMovieList retrieveMovieList = new RetrieveMovieList();
            retrieveMovieList.execute(getSortByPreference());
        }
        return v;
    }

    //Layout Managers have a build in api for saving scroll position thankfully
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLERPOSITION, mGridManager.onSaveInstanceState());


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

//==================Loader Manager Items==============================
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Dont need arguments or an ID here, just doing a simple SELECT ALL and returning cursor

        CursorLoader loader = new CursorLoader(getActivity(), 	//Context
                MovieContract.MovieEntry.CONTENT_URI, //URI
                MovieContract.MovieEntry.MOVIE_COLUMNS,	   //The fields we want (Must Include ID)
                null,	   //WHERE statement in SQL
                null, //Arguments for Selection statement
                null); // Sort By

        return loader;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFavAdapter=new FavoriteRecyclerAdapter(data, getActivity());
        vGrid.setAdapter(mFavAdapter);
        Log.d("Adapter Count", String.valueOf(vGrid.getAdapter().getItemCount()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader = null;
        mFavAdapter.swapCursor(null);

    }


    /*  Gameplan here:  Retrieve the JSON string.
                        Convert it to a JSONArray
                        Convert JSON objects into MovieItems
                        Return an ArrayList of MovieItems
    */
    private class RetrieveMovieList extends AsyncTask<String, Void, JSONArray> {


        @Override
        protected JSONArray doInBackground(String... params) {



            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String mdbJSONStr = null;

            //Parameters for API
            String apiKey = getString(R.string.url_APIKEY);


            if(apiKey.isEmpty()){
                Log.e(TAG, "Please enter an API Key into the Strings.xml file");
                return null;
            }

            try {
                // Construct the URL for the MovieDB Query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://api.themoviedb.org/3/discover/movie?


                final String MDB_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String PARAM_SORTBY = "sort_by";
                final String PARAM_APIKEY = "api_key";

                Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_SORTBY, params[0])
                        .appendQueryParameter(PARAM_APIKEY, apiKey)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(TAG, "Built URI " + builtUri.toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    //Add new line for readability, derp
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                mdbJSONStr = buffer.toString();

            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            try{
                //Return the ArrayList
                return parseJSONString(mdbJSONStr);

            } catch (JSONException e){
                //If not, something went wonky
                Log.e(TAG, e.getMessage());
                return null;
            }


        }

        @Override
        protected void onPostExecute(JSONArray result){
            super.onPostExecute(result);
            //Set our adapter on the UI thread.
            vGrid.setAdapter(new MovieRecyclerViewAdapter(result, getActivity()));
            //Restore the scroll position
            if(mLayoutArgs!=null){
            Parcelable savedRecyclerLayoutState = mLayoutArgs.getParcelable(BUNDLE_RECYCLERPOSITION);
            vGrid.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            }
        }

        //Method to parse our JSON String and return an Array of MovieItem objects to use in RecyclerView
        //Adapter
        private JSONArray parseJSONString(String JSONstring) throws JSONException{

            try {
                final String MDB_RESULTS = "results";

                JSONObject mdbResponse = new JSONObject(JSONstring);
                JSONArray results = mdbResponse.getJSONArray(MDB_RESULTS);


                //For our JSONObjectArray, make a MovieObject and add it to our ArrayList
                /**for (int i = 0; i < results.length(); i++) {
                 MovieItem item = new MovieItem(results.optJSONObject(i));
                 items.add(item);
                 }**/

                return results;
            } catch (JSONException e){
                Log.e(TAG, "failed to populate arraylist due to JSON error: " + e.getMessage());
                return null;
            }
        }

    }



    //This is just to save some space and add readability.
    private String getSortByPreference(){
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.key_prefs_sortby),
                getString(R.string.default_prefs_sortby));
    }

//Otto event listener that refreshes the movie list
    @Subscribe
    public void handlePrefChanged(BeefEvent.SortByChanged prefChangedEvent){
        //To avoid rereading prefs, we are reading the sortByParams directly
        //from the passed event

        //If it is popular or rating, we will use the API.
        //However, if it is favorited, we will use the LoaderManager and swap out the adapter
        String sortType = prefChangedEvent.getSortType();


        if(!sortType.equals(getResources().getStringArray(R.array.values_sortby)[2])) {
            RetrieveMovieList list = new RetrieveMovieList();
            list.execute(sortType);
        } else {
            getLoaderManager().restartLoader(0,null,this);

        }
    }

    public void refreshGrid(){
        getLoaderManager().restartLoader(0,null,this);
    }

    //For handling unfavorite from tablet view
    @Subscribe
    public void reactToUnfavorite(BeefEvent.ItemUnFavoritedEvent event){
           refreshGrid();
    }


}
