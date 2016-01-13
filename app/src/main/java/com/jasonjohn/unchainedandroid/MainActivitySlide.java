package com.jasonjohn.unchainedandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.jasonjohn.unchainedapi.UnchainedAPI;
import com.jasonjohn.unchainedapi.UnchainedAPIException;
import com.jasonjohn.unchainedapi.UnchainedRestaurant;
import com.jasonjohn.unchainedapi.Util;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.nineoldandroids.animation.Animator;
import com.norbsoft.typefacehelper.TypefaceHelper;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class MainActivitySlide extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
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
    private InputMethodManager inputMethodManager;
    private String unchainedLocation, unchainedRestaurantQuery;
    private UnchainedAPI unchainedAPI;
    private LocationManager mLocationManager;
    private Location mLastLocation;
    private boolean onFavoritesList = false;
    private boolean useCurrentLocation = false;
    private EditText editTextQuery, editTextLoc;
    private FancyButton goButton, currentLocationButton;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private ImageView tabIconStar, tabIconSearch;
    private ProgressBar currentLocLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_slide);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        listView = (ListView) findViewById(R.id.listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(this);

        editTextLoc = (EditText) findViewById(R.id.config_loc_field);
        editTextLoc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (editTextLoc.getText().toString().equals("Current Location")) {
                    editTextLoc.setText("");
                    editTextLoc.requestFocusFromTouch();
                    return true;
                }
                return false;
            }
        });
        editTextQuery = (EditText) findViewById(R.id.config_query_field);

        goButton = (FancyButton) findViewById(R.id.goButton);
        currentLocationButton = (FancyButton) findViewById(R.id.currentLocButton);
        currentLocationButton.setEnabled(false);
        currentLocationButton.setBackgroundColor(Color.TRANSPARENT);
        currentLocationButton.setBorderColor(Color.WHITE);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextLoc.setText("Current Location");
            }
        });
        currentLocationButton.setBorderWidth(2);

        currentLocLoading = (ProgressBar) findViewById(R.id.loc_loading);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEditTextEmpty(editTextQuery)) {
                    setErrorUi("Please enter a query");
                } else if (!useCurrentLocation) {
                    if (checkEditTextEmpty(editTextLoc)) {
                        setErrorUi("Enter a location or choose \"Current Location\"");
                    } else {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        unchainedLocation = editTextLoc.getText().toString();
                        unchainedRestaurantQuery = editTextQuery.getText().toString();
                        swipeRefreshLayout.setRefreshing(true);
                        onRefresh();
                    }
                }
            }
        });

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return false;
            }
        });
        unchainedAPI = new UnchainedAPI(YELP_KEY, YELP_SECRET, YELP_TOKEN, YELP_TOKEN_SECRET,
                FOURSQUARE_ID, FOURSQUARE_SECRET, GOOGLE_PLACES_KEY);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
                Log.d("LOC", "Got loc " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
                currentLocationButton.setEnabled(true);
                currentLocationButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                currentLocationButton.setBorderWidth(0);
                currentLocLoading.setVisibility(View.INVISIBLE);
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

        tabIconSearch = (ImageView) findViewById(R.id.search_icon);
        tabIconStar = (ImageView) findViewById(R.id.star_icon);

        tabIconSearch.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_search).colorRes(android.R.color.white).actionBarSize());
        tabIconStar.setImageDrawable(new IconDrawable(this, FontAwesomeIcons.fa_star_o).colorRes(android.R.color.white).actionBarSize());
        tabIconStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                loadFavoriteUCRs();
            }
        });

        unchainedRestaurantQuery = "Sushi";
        unchainedLocation = "Buford, GA";
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
                    if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    } else {
                        Intent i = new Intent(getApplicationContext(), VenueDetailActivity.class);
                        HashMap<String, Object> hashMap = Util.breakdownUCR(ucr);
                        i.putExtra("ucr", hashMap);
                        startActivity(i);
                    }
                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    launchVenueActionContextMenu(MainActivitySlide.this, ucr);
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

    /* launchVenueActionContextMenu(Context, UnchainedRestaurant)
    * Called when a user clicks on a venue. Gives the user an option list to call uber, navigate
    * to the venue, share the venue, or go to the venue website
    */
    private void launchVenueActionContextMenu(final Context mContext, final UnchainedRestaurant ucr) {
        final CharSequence[] items = {"View Details", "Navigate to Venue", "Share Venue", "Go to Website"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivitySlide.this, R.style.Dialog));
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


    private void setErrorUi(String msg) {
        Snackbar snackbar = Snackbar.make(swipeRefreshLayout, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
        snackbar.show();
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

    private boolean checkEditTextEmpty(EditText e) {
        if(e.getText().toString().matches("")) return true;
        else return false;
    }
}
