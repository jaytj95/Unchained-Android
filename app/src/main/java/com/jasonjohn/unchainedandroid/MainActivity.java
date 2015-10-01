package com.jasonjohn.unchainedandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jasonjohn.unchainedapi.UnchainedAPI;
import com.jasonjohn.unchainedapi.UnchainedRestaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String GOOGLE_PLACES_KEY = "AIzaSyBNxtP1FnsCQoBz6pOozC-WVRo_2ZoCmzQ";
    public static final String YELP_KEY = "1y6Y9ZQBDOctIKrq5NO7XQ";
    public static final String YELP_SECRET = "852Nfvhn9yd7GnXoOyNsygmT2Ks";
    public static final String YELP_TOKEN = "OHVBilTnx0nmS8_fVQxMJ6s41fcLoZA9";
    public static final String YELP_TOKEN_SECRET = "3bjEG5GcVc-3vJ6UQIt1bgrGD2o";
    public static final String FOURSQUARE_ID = "NVH2HBDEWL00GLGRYWZMDSFK2FUZR00ICNDW0OOGXL13NUFY";
    public static final String FOURSQUARE_SECRET = "TV04OXE1WM32JEHQLJTETFOE35KDHCEPNRHY35YCV5OOAH04";


    private ListView listView;
    private UnchainedAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<UnchainedRestaurant> nonChains;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        new UnchainedAsync().execute("34.0619825,-83.9833599");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class UnchainedAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            UnchainedAPI unchainedAPI = new UnchainedAPI(YELP_KEY, YELP_SECRET, YELP_TOKEN, YELP_TOKEN_SECRET,
                    FOURSQUARE_ID, FOURSQUARE_SECRET, GOOGLE_PLACES_KEY);
            try {
                nonChains = unchainedAPI.getUnchainedRestaurants("sushi", params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("TAG", "Done size: " + nonChains.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listAdapter = new UnchainedAdapter(getApplicationContext(), R.layout.list_item, nonChains);
            listView.setAdapter(listAdapter);
        }
    }

    private class UnchainedAdapter extends ArrayAdapter<UnchainedRestaurant> {
        Context mContext;
        int res;
        public UnchainedAdapter(Context context, int resource, List<UnchainedRestaurant> objects) {
            super(context, resource, objects);
            mContext = context;
            res = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item, null);
            viewHolder.venueName = (TextView) convertView.findViewById(R.id.venueName);


            UnchainedRestaurant ucr = this.getItem(position);

            viewHolder.venueName.setText(ucr.getName());

            return convertView;
        }

        private class ViewHolder {
            TextView venueName, venueAddress, venueRating;
        }
    }
}
