package udacityprojects.com.wonderbeefmovies.adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import udacityprojects.com.wonderbeefmovies.App;
import udacityprojects.com.wonderbeefmovies.R;
import udacityprojects.com.wonderbeefmovies.data.MovieContract;

/**
 * Created by Joshua on 12/19/2015.
 */
public class FavoriteRecyclerAdapter extends CursorRecyclerAdapter<FavoriteRecyclerAdapter.FavoriteHolder> implements View.OnClickListener{


    private int mTo[]={};
    private int mFrom[]={};


    private Context mContext;






    //Our custom view holder. Will be pretty similar to MovieRecyclerAdapter
    public static class FavoriteHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.item_movie_image) ImageView image;

        //Constructor
        public FavoriteHolder (View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

    }


    public FavoriteRecyclerAdapter (Cursor c, Context context) {
        super(c);
        mContext= context;
    }


    @Override
    public void onBindViewHolder(FavoriteHolder holder, Cursor cursor) {

        String movieID = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.KEY_M_APIID));

        //Set a Tag so we can retrieve ID on Click.
        holder.itemView.setTag(R.string.tag_MovieID,
                movieID);
        holder.itemView.setOnClickListener(this);

        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        //Where we will be storing images
        File directory = cw.getDir(mContext.getString(R.string.key_path_posters), Context.MODE_PRIVATE);



        File f = new File(directory, movieID + ".jpg");

                Picasso.with(mContext)
                .load(f)
                        .into(holder.image);



        //Picasso to load image

    }


    @Override
    public FavoriteHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new FavoriteHolder(v);
    }


    @Override
    public void onClick(View v) {
        //Extract movieID from View, send out broadcast via EventBus
        String movieID = (String) v.getTag(R.string.tag_MovieID);
        App.getInstance().getEventBus().post(movieID);

    }

}