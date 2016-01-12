package udacityprojects.com.wonderbeefmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by Joshua on 12/8/2015.
 */
public class DetailActivity extends AppCompatActivity {


    private static final String TAG = "DetailActivity";

@Override
    public void onCreate(Bundle savedIsntanceState){
    super.onCreate(savedIsntanceState);
    setContentView(R.layout.activity_detail);
    //Set Up the toolbar
    Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
    setSupportActionBar(toolbar);
    //Below is so we don't have to keep calling getSupportActionbar
    ActionBar bar = getSupportActionBar();
    //we want the ability to go back here
    bar.setDisplayHomeAsUpEnabled(true);


    //Find our detail fragment
    DetailFragment fragmentDetail =
            new DetailFragment();

    //Get our arguments. If we received a JSON String, load it
    Bundle args = getIntent().getExtras();

   if (args!=null) {
       if (args.containsKey(getString(R.string.key_args_movieItem))||args.containsKey(getString(R.string.key_args_movieID))) {
           fragmentDetail.setArguments(args);
           //Attach fragment with correct arguments
           getSupportFragmentManager().beginTransaction().add(R.id.detail_fragmentContainer, fragmentDetail).commit();
       } else {
        //Correct arguments were not provided to DetailActivity
           Log.e(TAG, "Neither Movie Item no Movie ID was passed as argument");
       }
   }

}

}
