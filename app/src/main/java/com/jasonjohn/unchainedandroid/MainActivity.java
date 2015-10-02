package com.jasonjohn.unchainedandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.jasonjohn.unchainedapi.UnchainedAPI;
import com.jasonjohn.unchainedapi.UnchainedRestaurant;
import com.jasonjohn.unchainedapi.Util;
import com.nineoldandroids.animation.Animator;

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
    private Toolbar toolbar;
    private RelativeLayout configDropdown;
    private EditText configLocationTextbox;
    private ImageButton configLocationSubmit;
    private InputMethodManager inputMethodManager;

    private String unchainedLocation, unchainedRestaurantQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        //config layout init
        configDropdown = (RelativeLayout) findViewById(R.id.config_drop);

        configLocationSubmit = (ImageButton) findViewById(R.id.config_drop_location_submit);
        configLocationSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!configLocationTextbox.getText().toString().matches("")) {
                    toggleConfigDropdown();
                    listAdapter.clear();
                    unchainedLocation = configLocationTextbox.getText().toString();
                    new UnchainedAsync().execute("restaurant", unchainedLocation);
                } else {
                    YoYo.with(Techniques.Shake).duration(700).playOn(configLocationTextbox);
                }
            }
        });

        configLocationTextbox = (EditText) findViewById(R.id.config_drop_location_text);

        configDropdown.setVisibility(View.GONE);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        toolbar.bringToFront();
        new UnchainedAsync().execute("restaurant", "34.0619825,-83.9833599");



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
        } else if(id == R.id.action_location) {
            toggleConfigDropdown();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleConfigDropdown() {
        if(configDropdown.getVisibility() == View.GONE) {
            configDropdown.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInDown).duration(200).playOn(configDropdown);
            configLocationTextbox.requestFocus();
            inputMethodManager.showSoftInput(configLocationTextbox, InputMethodManager.SHOW_FORCED);
        } else {
            YoYo.with(Techniques.SlideOutUp).duration(200).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    configDropdown.setVisibility(View.GONE);
                }
            }).playOn(configDropdown);
            inputMethodManager.hideSoftInputFromWindow(configLocationTextbox.getWindowToken(), 0);
        }
    }

    private class UnchainedAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String location = params[1];
            if(!location.matches("-?[0-9.]*,-?[0-9.]*")) {
                location = Util.getLatLngFromMapsQuery(location);
            }
            UnchainedAPI unchainedAPI = new UnchainedAPI(YELP_KEY, YELP_SECRET, YELP_TOKEN, YELP_TOKEN_SECRET,
                    FOURSQUARE_ID, FOURSQUARE_SECRET, GOOGLE_PLACES_KEY);
            try {
                nonChains = unchainedAPI.getUnchainedRestaurants(params[0], location);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
