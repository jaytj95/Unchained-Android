package com.jasonjohn.unchainedandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jasonjohn.unchainedapi.UnchainedRestaurant;
import com.jasonjohn.unchainedapi.Util;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
public class VenueDetailActivity extends AppCompatActivity implements OnMapReadyCallback{
    private ImageView toolbarImg;
    private UnchainedRestaurant ucr;
    private NestedScrollView nestedScrollView;
    private TextView phoneNumber, navAddress;
    private LinearLayout contentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);

        nestedScrollView = (NestedScrollView) findViewById(R.id.nested_scroll);
        ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        nestedScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        nestedScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        nestedScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
        Intent intent = getIntent();
        HashMap<String, Object> hashMap = (HashMap<String, Object>) intent.getSerializableExtra("ucr");
        ucr = Util.buildUCR(hashMap);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(ucr.getName());
        toolbarImg = (ImageView) findViewById(R.id.toolbar_img);
        Picasso.with(getApplicationContext()).load(ucr.getPicUrls().get(0)).into(toolbarImg);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saved this Unchained Restaurant!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        phoneNumber = (TextView) findViewById(R.id.content_phone);
        navAddress = (TextView) findViewById(R.id.navigate_addr);

        phoneNumber.setText(ucr.getTelephone());
        navAddress.setText(ucr.getAddress());

        contentLayout = (LinearLayout) findViewById(R.id.content_linlayout);
        contentLayout.setClickable(true);
        View.OnClickListener contentClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = contentLayout.indexOfChild(v);
                Snackbar.make(v, "Saved this Unchained Restaurant! + " + pos, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                switch(pos) {
                    case 0:
                        //phone
                        break;
                    case 1:
                        //nav
                        break;
                    case 2:
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        String shareBody = ucr.getName() + " seems like a good place to try out: ("
                                + ucr.getWebsite() + ")\nTry Unchained for Android!";
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share Using"));
                        break;
                }
            }
        };
        contentLayout.setOnClickListener(contentClickListener);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng ucrMarker = new LatLng(ucr.getLatlng()[0], ucr.getLatlng()[1]);
        map.addMarker(new MarkerOptions().position(ucrMarker).title(ucr.getName()));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(ucrMarker).zoom(13.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cameraUpdate);
    }
}
