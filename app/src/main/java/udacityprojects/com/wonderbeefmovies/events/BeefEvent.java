package udacityprojects.com.wonderbeefmovies.events;

import android.content.ClipData;

import org.json.JSONArray;

/**
 * Created by Joshua on 12/13/2015.
 */
public class BeefEvent {


    //Used to notify of a sortBy preference change
    public static class SortByChanged{
        String params;

        public SortByChanged(String sortByParams){
            params=sortByParams;
        }

        public String getSortType(){
            return  params;
        }
    }


    public static class MDBMovieSelectedEvent {

        private String mJSONString;

        public MDBMovieSelectedEvent(String JSONString){
            mJSONString=JSONString;
        }

        public String getJSONString(){return mJSONString;}
    }

    public static class TrailerSyncComplete{
        private JSONArray mData;

        public TrailerSyncComplete(JSONArray trailers){
            mData=trailers;
        }
        public JSONArray getTrailers(){return mData;}
    }

    public static class ReviewSyncComplete{
        private JSONArray mData;

        public ReviewSyncComplete(JSONArray reviews){
            mData=reviews;
        }

        public JSONArray getReviews(){return mData;}
    }


    public static class ItemUnFavoritedEvent{

        public ItemUnFavoritedEvent(){}
    }

//    public static class MovieItemSelected{
//        private String args;
//
//        public MovieItemSelected(String JSONargs){
//            args=JSONargs;
//        }
//
//        public String getJSONargs(){return args;}
//
//    }


}
