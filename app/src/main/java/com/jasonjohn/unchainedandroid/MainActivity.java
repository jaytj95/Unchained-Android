package com.jasonjohn.unchainedandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
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
    private RelativeLayout configDropdown, refreshNotification;
    private EditText configTextbox;
    private ImageButton configLocationSubmit, configQuerySubmit;
    private InputMethodManager inputMethodManager;
    private TextView configLocationTv, configQueryTv;
    private ImageView refreshImg1, refreshImg2;

    private String unchainedLocation, unchainedRestaurantQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.unchained_red, R.color.unchained_blue);
        swipeRefreshLayout.setOnRefreshListener(this);
//        swipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//            }
//        });

        //config layout init
        configDropdown = (RelativeLayout) findViewById(R.id.config_drop);

        configLocationSubmit = (ImageButton) findViewById(R.id.config_drop_location_submit);
        configLocationSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!configTextbox.getText().toString().matches("")) {
                    unchainedLocation = configTextbox.getText().toString();
//                    listAdapter.clear();
//                    new UnchainedAsync().execute(unchainedRestaurantQuery, unchainedLocation);
                    toggleRefreshIndicators(1);
                } else {
                    YoYo.with(Techniques.Shake).duration(700).playOn(configTextbox);
                }

                toggleConfigDropdown(0);
                configTextbox.setText("");
                configQueryTv.setText(unchainedRestaurantQuery);
                configLocationTv.setText(unchainedLocation);
            }
        });

        configQuerySubmit = (ImageButton) findViewById(R.id.config_drop_query_submit);
        configQuerySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!configTextbox.getText().toString().matches("")) {
                    unchainedRestaurantQuery = configTextbox.getText().toString();
//                    listAdapter.clear();
//                    new UnchainedAsync().execute(unchainedRestaurantQuery, unchainedLocation);
                    toggleRefreshIndicators(1);

                } else {
                    YoYo.with(Techniques.Shake).duration(700).playOn(configTextbox);
                }

                toggleConfigDropdown(1);
                configTextbox.setText("");
                configQueryTv.setText(unchainedRestaurantQuery);
                configLocationTv.setText(unchainedLocation);
            }
        });

        configTextbox = (EditText) findViewById(R.id.config_drop_location_text);
        configTextbox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(configLocationSubmit.getVisibility() == View.VISIBLE)
                        configLocationSubmit.performClick();
                    else
                        configQuerySubmit.performClick();
                    return true;
                }
                return false;
            }
        });

        configDropdown.setVisibility(View.GONE);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        toolbar.bringToFront();
        unchainedRestaurantQuery = "Sushi Restaurants";
        unchainedLocation = "Mall of GA";

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                YoYo.with(Techniques.SlideOutUp).duration(200).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        configDropdown.setVisibility(View.GONE);
                    }
                }).playOn(configDropdown);
                inputMethodManager.hideSoftInputFromWindow(configTextbox.getWindowToken(), 0);
                return false;
            }
        });

        View listHeader = View.inflate(getApplicationContext(), R.layout.list_header, null);
        configQueryTv = (TextView) listHeader.findViewById(R.id.header_query);
        configQueryTv.setText(unchainedRestaurantQuery);
        configQueryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfigDropdown(1);
            }
        });

        configLocationTv = (TextView) listHeader.findViewById(R.id.header_location);
        configLocationTv.setText(unchainedLocation);
        configLocationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfigDropdown(0);
            }
        });

        refreshImg1 = (ImageView) listHeader.findViewById(R.id.header_refresh1);
        refreshImg2 = (ImageView) listHeader.findViewById(R.id.header_refresh2);

        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int pix = (int) (100 * scale + 0.5f);
        listHeader.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pix));
        listView.addHeaderView(listHeader, null, true);
        listView.setAdapter(null);

        refreshNotification = (RelativeLayout) findViewById(R.id.refresh_notif);
        refreshNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
//        new UnchainedAsync().execute(unchainedRestaurantQuery, unchainedLocation);

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
            toggleConfigDropdown(1);
            return true;
        } else if(id == R.id.action_location) {
            toggleConfigDropdown(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleConfigDropdown(int type) {
        //type = 0 for LOCATION
        //type = 1 for QUERY
        if(configDropdown.getVisibility() == View.GONE) {
            configDropdown.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInDown).duration(200).playOn(configDropdown);
            configTextbox.requestFocus();
            inputMethodManager.showSoftInput(configTextbox, InputMethodManager.SHOW_FORCED);
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
            inputMethodManager.hideSoftInputFromWindow(configTextbox.getWindowToken(), 0);
        }

        if(type == 0) {
            configLocationSubmit.setVisibility(View.VISIBLE);
            configQuerySubmit.setVisibility(View.GONE);
            configTextbox.setHint("Enter your location (eg. Mall of Georgia)");
        } else {
            configLocationSubmit.setVisibility(View.GONE);
            configQuerySubmit.setVisibility(View.VISIBLE);
            configTextbox.setHint("Restaurant? Bar? Sushi? Pizza?");
        }

    }

    @Override
    public void onRefresh() {
        try {
            listAdapter.clear();
        } catch (NullPointerException e) {
            //eat exception
        }
        new UnchainedAsync().execute(unchainedRestaurantQuery, unchainedLocation);
        toggleRefreshIndicators(0);
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
            swipeRefreshLayout.setRefreshing(false);
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


    private void toggleRefreshIndicators(int onOff) {
        if(onOff == 0) {
            YoYo.with(Techniques.FadeOut).duration(350).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    YoYo.with(Techniques.FadeOut).duration(350).playOn(refreshImg2);
                    YoYo.with(Techniques.SlideOutDown).duration(350).playOn(refreshNotification);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    refreshImg1.setVisibility(View.GONE);
                    refreshImg2.setVisibility(View.GONE);
                    refreshNotification.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).playOn(refreshImg1);
        } else {
            refreshImg1.setVisibility(View.VISIBLE);
            refreshImg2.setVisibility(View.VISIBLE);
            refreshNotification.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.SlideInUp).duration(350).playOn(refreshNotification);
            YoYo.with(Techniques.FadeIn).duration(350).playOn(refreshImg1);
            YoYo.with(Techniques.FadeIn).duration(350).playOn(refreshImg2);
        }
    }
}
