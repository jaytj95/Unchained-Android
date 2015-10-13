package com.jasonjohn.unchainedandroid;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.jasonjohn.unchainedapi.UnchainedRestaurant;
import com.jasonjohn.unchainedapi.Util;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class VenueDetailActivity extends AppCompatActivity {
    private ImageView toolbarImg, contentTest;
    private UnchainedRestaurant ucr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);

        Intent intent = getIntent();
        HashMap<String, Object> hashMap = (HashMap<String, Object>) intent.getSerializableExtra("ucr");
        ucr = Util.buildUCR(hashMap);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(ucr.getName());
        toolbarImg = (ImageView) findViewById(R.id.toolbar_img);
        contentTest = (ImageView) findViewById(R.id.content_img);
        contentTest.setBackgroundColor(Color.RED);
        Picasso.with(getApplicationContext()).load(ucr.getPicUrls().get(0)).into(toolbarImg);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saved this Unchained Restaurant!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
