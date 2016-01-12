package udacityprojects.com.wonderbeefmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.squareup.otto.Subscribe;

import udacityprojects.com.wonderbeefmovies.events.BeefEvent;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    //Spinner for sorting
    private Spinner vSortBy;


    //Fragments
    private  DetailFragment mDetailFragment;
    private GridFragment mGridFragment;

    //Constants
    private static int LAUNCH_DETAIL =504;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createToolBar();

        //Instantiate Fragments - *NOTE, detailFragment will be null if portrait (we want this)
        FragmentManager fm = getSupportFragmentManager();
        mDetailFragment = (DetailFragment) fm.findFragmentById(R.id.frag_detail);
        mGridFragment = (GridFragment) fm.findFragmentById(R.id.frag_list);

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




    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //First Change the Preferences to selected item
        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(this);
        String sortByVal = getResources().getStringArray(R.array.values_sortby)[position];
        sP.edit().putString(getString(R.string.key_prefs_sortby), sortByVal)
                .apply();

        Log.d("main spinner", sortByVal);
        //Re populate the RecyclerView
        //TODO broadcast prefchange
        App.getInstance().getEventBus().post(new BeefEvent.SortByChanged(sortByVal));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //Created for onCreate readability

    private void createToolBar(){

    //Since we are going MasterDetail, a lot of code was moved from fragments to here

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        //Below is so we don't have to keep calling getSupportActionbar
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowCustomEnabled(true);
        //Disable title
        bar.setDisplayShowTitleEnabled(false);
        //Now we set our Toolbar as a custom view with a spinner
        //Inflate the Spinner
        View vi = LayoutInflater.from(this).inflate(R.layout.spinner_sortby, null);
        vSortBy = (Spinner) vi.findViewById(R.id.grid_spinner);
        vSortBy.setGravity(Gravity.END);
        //Set the adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.labels_sortby, R.layout.item_sortby);
        adapter.setDropDownViewResource(R.layout.item_dropdown_sortby);
        vSortBy.setAdapter(adapter);
        vSortBy.setOnItemSelectedListener(this);
        bar.setCustomView(vi);

        //Set the spinner position to that of our SharedPref value.
        //Using if statement due to differing values of display and saved value in sahred pref

        String sortByPref = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.key_prefs_sortby),
                getString(R.string.default_prefs_sortby));

        String[] values = getResources().getStringArray(R.array.values_sortby);

        if(sortByPref.matches(values[0])){
            vSortBy.setSelection(0);
        } else if(sortByPref.matches(values[1])){
            vSortBy.setSelection(1);
        } else {
            vSortBy.setSelection(2);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LAUNCH_DETAIL) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
               mGridFragment.refreshGrid();
            }
        }
    }

//==================Event Bus Subscriptions=====================

    //Handle an itemGridClick. It is used, but the IDE doesn't play nice with EventBus
    @Subscribe
    public void handleItemClicked(BeefEvent.MDBMovieSelectedEvent event){

        String jsonString = event.getJSONString();
        //If the detail fragment does not exist, we want to launch an activity because Portrait
        //has no detail.
        if(mDetailFragment==null||!mDetailFragment.isInLayout()){
            Intent launchDetailActivity= new Intent(MainActivity.this, DetailActivity.class);
            launchDetailActivity.putExtra(getString(R.string.key_args_movieItem), jsonString);
            startActivity(launchDetailActivity);
        } else {

            mDetailFragment.loadJSONMovie(jsonString);

        }

    }

    @Subscribe
    public void handleFavoriteClicked(String movieID) {
        if (mDetailFragment == null) {
            Intent launchDetailActivity = new Intent(MainActivity.this, DetailActivity.class);
            launchDetailActivity.putExtra(getString(R.string.key_args_movieID), movieID);
            startActivityForResult(launchDetailActivity, LAUNCH_DETAIL);
        } else {
            mDetailFragment.loadFavoritedMovie(movieID);
        }
    }

    }
