package udacityprojects.com.wonderbeefmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import udacityprojects.com.wonderbeefmovies.R;

/**
 * Created by E811339 on 01/06/2016.
 */
public class TrailerRecyclerAdapter extends RecyclerView.Adapter<TrailerRecyclerAdapter.TrailerViewHolder> implements View.OnClickListener {


    private JSONArray mData;
    private Context mContext;

    private String TAG = "TrailerRecyclerAdapter";



    //Constructor, need context for onclick intent and Picassorita
    public TrailerRecyclerAdapter(JSONArray trailers,  Context context){
        mData = trailers;
        mContext=context;
    }




    //ViewHolder
    public static class TrailerViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.item_trailerButton) ImageButton playButton;
        @Bind(R.id.item_trailerTitle) TextView title;
        @Bind(R.id.item_trailershare) ImageButton shareButton;

        public TrailerViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
  }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trailer, parent, false);

        TrailerViewHolder tv = new TrailerViewHolder(v);
        return tv;
    }


    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        //avoiding global constants here due to small scope

        try{
            final JSONObject trailer = mData.getJSONObject(position);
            holder.title.setText(trailer.getString("name"));
            Log.d("Trailer Name", trailer.getString("name"));
            //Trying something new here: storing data using tags on Views. Safety not guaranteed
            holder.playButton.setTag(R.string.tag_youtubeID, trailer.getString("key"));
            holder.shareButton.setTag(R.string.tag_youtubeID, trailer.getString("key"));
            holder.playButton.setOnClickListener(this);
            holder.shareButton.setOnClickListener(this);


        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }


    }

    @Override
    public void onClick(View v) {
        //Make a URI for playing the trailer
        Uri trailerURI= Uri.parse("http://www.youtube.com/watch?v=" + v.getTag(R.string.tag_youtubeID));
            Log.e("view tag", String.valueOf(v.getId()));

        switch (v.getId()){
            case R.id.item_trailerButton:
                Intent playTrailer = new Intent(Intent.ACTION_VIEW, trailerURI);
                //Broadcast the intent
                mContext.startActivity(playTrailer);
                break;
            case R.id.item_trailershare:

                Intent shareVideo = new Intent(Intent.ACTION_SEND);
                shareVideo.setType("text/plain");
                shareVideo.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.shareTitle));
                shareVideo.putExtra(Intent.EXTRA_TEXT, trailerURI.toString());
                //Broadcast the intent
                mContext.startActivity(shareVideo);
                break;

        }

    }


    @Override
    public int getItemCount() {
        return mData.length();
    }
}
