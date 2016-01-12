package udacityprojects.com.wonderbeefmovies.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ReviewRecyclerAdapter extends RecyclerView.Adapter<ReviewRecyclerAdapter.ReviewHolder> {

    private static final String TAG="ReviewRecyclerAdapter";

    private JSONArray mData;

    public static class ReviewHolder extends RecyclerView.ViewHolder{
       @Bind(R.id.item_reviewAuthor) TextView author;
        @Bind(R.id.item_reviewContent) TextView content;

        public ReviewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public ReviewRecyclerAdapter(JSONArray reviews){
        mData = reviews;
    }


    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);

        ReviewHolder holder = new ReviewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        try {
            final JSONObject review = mData.getJSONObject(position);
            Log.d("Author", review.getString("author"));
                holder.author.setText(review.getString("author"));
                holder.content.setText("     " + review.getString("content"));

        } catch(JSONException e){
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public int getItemCount() {
        return mData.length();
    }
}
