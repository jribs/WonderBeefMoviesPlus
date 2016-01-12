package udacityprojects.com.wonderbeefmovies;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import udacityprojects.com.wonderbeefmovies.data.MovieContract;
import udacityprojects.com.wonderbeefmovies.data.MovieDbHelper;

/**
 * Created by E811339 on 01/06/2016.
 */
public class TestDB extends AndroidTestCase{

    public void setUp(){

    }


    public void testCreateDb() throws Throwable{
        //First delete if it exists
        mContext.deleteDatabase(MovieContract.MovieEntry.TABLE_NAME);
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        db.close();


    }

}
