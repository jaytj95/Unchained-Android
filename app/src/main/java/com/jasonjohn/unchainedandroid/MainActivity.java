package com.jasonjohn.unchainedandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.jasonjohn.unchainedapi.UnchainedAPI;
import com.jasonjohn.unchainedapi.UnchainedAPIException;
import com.jasonjohn.unchainedapi.UnchainedRestaurant;
import com.jasonjohn.unchainedapi.Util;
import com.nineoldandroids.animation.Animator;
import com.norbsoft.typefacehelper.TypefaceHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Button configLocationSubmit, configQuerySubmit;
    private InputMethodManager inputMethodManager;
    private TextView configLocationTv, configQueryTv;
    private ImageView refreshImg1, refreshImg2;

    private boolean firstLocSetDone = false, firstQuerySetDone = false;
    private String unchainedLocation, unchainedRestaurantQuery;
    private UnchainedAPI unchainedAPI;

    LocationManager mLocationManager;
    private Location mLastLocation;

    private boolean onFavoritesList = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(this);

//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        //config layout init
        configDropdown = (RelativeLayout) findViewById(R.id.config_drop);

        configLocationSubmit = (Button) findViewById(R.id.config_drop_location_submit);
        configLocationSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!configTextbox.getText().toString().matches("")) {
                    unchainedLocation = configTextbox.getText().toString();
                    firstLocSetDone = true;
                    if(firstQuerySetDone && firstLocSetDone)
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

        configQuerySubmit = (Button) findViewById(R.id.config_drop_query_submit);
        configQuerySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!configTextbox.getText().toString().matches("")) {
                    unchainedRestaurantQuery = configTextbox.getText().toString();
                    firstQuerySetDone = true;
                    if(firstQuerySetDone && firstLocSetDone)
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

        unchainedRestaurantQuery = "Enter Food Query Here";
        unchainedLocation = "Enter Location Here";
//        unchainedRestaurantQuery = "Sushi";
//        unchainedLocation = "Mall of GA";

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

        unchainedAPI = new UnchainedAPI(YELP_KEY, YELP_SECRET, YELP_TOKEN, YELP_TOKEN_SECRET,
                FOURSQUARE_ID, FOURSQUARE_SECRET, GOOGLE_PLACES_KEY);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
                Log.d("LOC", "Got loc" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "Resume");
        if(onFavoritesList) {
            loadFavoriteUCRs();
        }
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

        if (id == R.id.action_search) {
            toggleConfigDropdown(1);
            return true;
        } else if(id == R.id.action_location) {
            toggleConfigDropdown(0);
            return true;
        } else if(id == R.id.action_favs) {
            onFavoritesList = true;
            loadFavoriteUCRs();
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
            configTextbox.setHint("Enter your location (eg. Atlanta, GA)");
            if(isLocationServicesOn()) {
                if (mLastLocation == null) {
                    Snackbar snackbar = Snackbar.make(configTextbox, "Trying to get current location", Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    snackbar.show();
                } else if (!unchainedLocation.equals("Current Location")) {
                    Snackbar snackbar = Snackbar.make(configTextbox, "Click here for current location", Snackbar.LENGTH_INDEFINITE)
                            .setAction("SET", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    unchainedLocation = "Current Location";
                                    firstLocSetDone = true;
                                    configLocationSubmit.performClick();

                                }
                            });
                    snackbar.show();

                }
            }
        } else {
            configLocationSubmit.setVisibility(View.GONE);
            configQuerySubmit.setVisibility(View.VISIBLE);
            configTextbox.setHint("What're you in the mood for?");
        }

    }

    @Override
    public void onRefresh() {
        onFavoritesList = false;
        try {
            listAdapter.clear();
        } catch (NullPointerException e) {
            //eat exception
        }
        new UnchainedAsync().execute(unchainedRestaurantQuery, unchainedLocation);
        toggleRefreshIndicators(0);
    }
    private class UnchainedAsync extends AsyncTask<String, Void, Void> {
        final private int ERROR_GEO = 1;
        final private int ERROR_API = 2;
        final private int ERROR_DATA = 3;
        private int errorCode = 0;
        @Override
        protected Void doInBackground(String... params) {
            String location = params[1];
            if(!checkDataStatus()) {
                setErrorCode(ERROR_DATA);
            }
            if(mLastLocation != null && ((String)params[1]).equals("Current Location")) {
                location = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
            }
            if(getErrorCode() == 0) {
                if (!location.matches("-?[0-9.]*,-?[0-9.]*")) {
                    try {
                        location = Util.getLatLngFromMapsQuery(location);
                    } catch (UnchainedAPIException e) {
                        setErrorCode(ERROR_GEO);
                    }
                }
            }

            if(getErrorCode() == 0) {
                try {
                    nonChains = unchainedAPI.getUnchainedRestaurants(params[0], location);
                } catch (UnchainedAPIException e) {
                    if (getErrorCode() != ERROR_GEO)
                        setErrorCode(ERROR_API);
                }

                if (nonChains.size() == 0 && getErrorCode() != ERROR_GEO) {
                    setErrorCode(ERROR_API);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);
            if(getErrorCode() == 0) {
                listAdapter = new UnchainedAdapter(getApplicationContext(), R.layout.list_item, nonChains);
                listView.setAdapter(listAdapter);
            } else {
                Log.d("UC", "Error code: " + getErrorCode());
                String msg;
                switch(getErrorCode()) {
                    case ERROR_GEO: //GEO ERROR
                        msg = "Error finding location";
                        setErrorUi(msg);
                        break;
                    case ERROR_API:
                        msg = "We can't find any restaurants :(";
                        setErrorUi(msg);
                        break;
                    case ERROR_DATA:
                        msg = "No Data Connection...";
                        setErrorUi(msg);

                }
            }
        }

        private void setErrorCode(int code) {
            errorCode = code;
        }

        private int getErrorCode() {
            return errorCode;
        }
    }

    private boolean checkDataStatus() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected()) {
            return true;
        } else if (mobile.isConnected()) {
            return true;
        } else {
            return false;
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
            viewHolder.venueAddress = (TextView) convertView.findViewById(R.id.venueAddress);
            viewHolder.ratingBar = (ProgressBar) convertView.findViewById(R.id.ratingBar);
            viewHolder.imgView = (ImageView) convertView.findViewById(R.id.venueImg);

            final UnchainedRestaurant ucr = this.getItem(position);

            viewHolder.venueName.setText(ucr.getName());
            viewHolder.venueAddress.setText(ucr.getAddress());
            viewHolder.ratingBar.setProgress((int) (ucr.getRating() * 20));
            ArrayList picUrls = ucr.getPicUrls();
            if(picUrls.size() > 0) {
                Picasso.with(getApplicationContext()).load(ucr.getPicUrls().get(0)).into(viewHolder.imgView);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), VenueDetailActivity.class);
                    HashMap<String, Object> hashMap = Util.breakdownUCR(ucr);
                    i.putExtra("ucr", hashMap);
                    startActivity(i);
                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    launchVenueActionContextMenu(MainActivity.this, ucr);
                    return true;
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView venueName, venueAddress;
            ProgressBar ratingBar;
            ImageView imgView;
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

    private void setErrorUi(String msg) {
        Snackbar snackbar = Snackbar.make(swipeRefreshLayout, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
        snackbar.show();
    }

    /* launchVenueActionContextMenu(Context, UnchainedRestaurant)
    * Called when a user clicks on a venue. Gives the user an option list to call uber, navigate
    * to the venue, share the venue, or go to the venue website
    */
    private void launchVenueActionContextMenu(final Context mContext, final UnchainedRestaurant ucr) {
        final CharSequence[] items = {"View Details", "Navigate to Venue", "Share Venue", "Go to Website"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.Dialog));
        builder.setTitle(ucr.getName());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                //location references for map links
//                final String startLoc = userLocation.getLatitude() + "," + userLocation.getLongitude();
//                final String destLoc = venue.getLat() + "," + venue.getLng();

                //switch on the user's choice (index)
                switch (item) {
                    //view details
                    case 0:
                        Intent i = new Intent(getApplicationContext(), VenueDetailActivity.class);
                        HashMap<String, Object> hashMap = Util.breakdownUCR(ucr);
                        i.putExtra("ucr", hashMap);
                        startActivity(i);
                        break;
                    //Navigate to Venue
                    case 1:
                        if (ucr.getAddress() != null) {
                            Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + Uri.encode(ucr.getAddress()));
                            Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            startActivity(intent);
                        }
                        break;
                    //Share Restaurant
                    case 2:
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        String shareBody = ucr.getName() + " seems like a good place to try out: ("
                                + ucr.getWebsite() + ")\nTry Unchained for Android!";
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share Using"));
                        break;
                    //Go to Website
                    case 3:
                        if (ucr.getWebsite() != null) {
                            String url = ucr.getWebsite();
                            Intent i3 = new Intent(Intent.ACTION_VIEW);
                            i3.setData(Uri.parse(url));
                            startActivity(i3);
                        } else {
                            Toast.makeText(getApplicationContext(), "No Website Data", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void loadFavoriteUCRs() {
//        swipeRefreshLayout.setRefreshing(true);
        nonChains = new ArrayList<UnchainedRestaurant>();

        SharedPreferences prefs = getSharedPreferences("savedunchained", Context.MODE_PRIVATE);
        Map<String, ?> allEntires = prefs.getAll();

        if(allEntires.isEmpty()) {
            setErrorUi("You haven't chosen any favorite Unchained Restaurants");
        }
        for(Map.Entry<String,?> entry : allEntires.entrySet()) {
            Gson gson = new Gson();
            String json = entry.getValue().toString();
            UnchainedRestaurant ucr = gson.fromJson(json, UnchainedRestaurant.class);
            nonChains.add(ucr);
        }
//        swipeRefreshLayout.setRefreshing(false);
        listAdapter = new UnchainedAdapter(getApplicationContext(), R.layout.list_item, nonChains);
        listView.setAdapter(listAdapter);
    }



    private boolean isLocationServicesOn() {
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;
        else return false;
    }

}
