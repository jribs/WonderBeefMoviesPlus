package udacityprojects.com.wonderbeefmovies.services;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.squareup.otto.Bus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import udacityprojects.com.wonderbeefmovies.App;
import udacityprojects.com.wonderbeefmovies.adapters.TrailerRecyclerAdapter;
import udacityprojects.com.wonderbeefmovies.events.BeefEvent;

/**
 * Created by Joshua on 12/20/2015.
 */

public class TrailerAsyncTask extends AsyncTask<String, Void, JSONArray> {

    private static String TAG = "TrailerAsyncTask";
  /*  private RecyclerView mRecycler;
    private Context mContext;

    public TrailerAsyncTask(RecyclerView recycler, Context context){
        mRecycler=recycler;
        mContext=context;
    }*/


    @Override
    protected JSONArray doInBackground(String... params) {



        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String mdbJSONStr = null;

        //If we have no params, we can't load trailers, so gyit out
        if(params[0]==null || params[1]==null) {
            return null;
        }


        String movieId = params[0];

        //Parameters for API
        String apiKey = params[1];

        if(apiKey.isEmpty()){
            Log.e(TAG, "Please enter an API Key into the Strings.xml file");
            return null;
        }

        try {
            // Construct the URL for the MovieDB Query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://api.themoviedb.org/3/discover/movie?


            final String MDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/" +
                            movieId + "/videos?";

            final String PARAM_APIKEY = "api_key";

            Uri builtUri = Uri.parse(MDB_BASE_URL).buildUpon()

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
       Bus eB = App.getInstance().getEventBus();
     //   eB.register(this);
        eB.post(new BeefEvent.TrailerSyncComplete(result));
    //    eB.unregister(this);
       // mRecycler.setAdapter(new TrailerRecyclerAdapter(result, mContext));
    }

    //Method to parse our JSON String and return an Array of MovieItem objects to use in RecyclerView
    //Adapter
    private JSONArray parseJSONString(String JSONstring) throws JSONException{

        try {
            final String MDB_RESULTS = "results";

            JSONObject mdbResponse = new JSONObject(JSONstring);
            JSONArray results = mdbResponse.getJSONArray(MDB_RESULTS);



            return results;
        } catch (JSONException e){
            Log.e(TAG, "failed to populate arraylist due to JSON error: " + e.getMessage());
            return null;
        }
    }

}