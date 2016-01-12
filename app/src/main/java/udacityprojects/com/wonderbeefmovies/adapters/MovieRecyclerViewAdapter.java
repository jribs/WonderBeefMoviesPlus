package udacityprojects.com.wonderbeefmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import udacityprojects.com.wonderbeefmovies.App;
import udacityprojects.com.wonderbeefmovies.R;
import udacityprojects.com.wonderbeefmovies.events.BeefEvent;


/**
 * Created by Joshua on 10/31/2015.
 */
public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.MovieViewHolder> implements OnClickListener{


    private JSONArray data;
    private Context mContext;

    private String TAG = "MovieRecyclerAdapter";


    @Override
    public void onClick(View v) {
        App.getInstance().getEventBus().post(
                new BeefEvent.MDBMovieSelectedEvent((String) v.getTag(R.string.tag_movieObject)));

    }

    //Our little custom viewHolder. All we need here is the imageview we are going to Picasso like a champ
    public static class MovieViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.item_movie_image)ImageView poster;

        public MovieViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

    }




    //Constructor, need context for onclick intent and Picassorita
    public MovieRecyclerViewAdapter(JSONArray movies,  Context context){
        data = movies;
        mContext=context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new MovieViewHolder(v);
    }


    @Override
    public void onBindViewHolder(MovieViewHolder holder, final int position){
        //First parse out the image URL for our position
        try {
            final JSONObject movie = data.getJSONObject(position);
            String imageURL = mContext.getString(R.string.url_posterpath) +
                    mContext.getString(R.string.url_poster_size_185)+
                    movie.getString(mContext.getString(R.string.api_posterPath));
            //Glorious Picasso is binding our image to the ImageView
            Picasso.with(mContext)
                    .load(imageURL)
                    .into(holder.poster);
            holder.itemView.setTag(R.string.tag_movieObject, movie.toString());
            //here we are setting the onClickListener to broadcast our movie o
            holder.itemView.setOnClickListener(this);


        } catch (JSONException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if(data!=null){
        return data.length();}
        else {
            return 0;
        }
    }




}


