package com.jasonjohn.unchainedandroid;

import android.app.Application;
import android.graphics.Typeface;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.norbsoft.typefacehelper.TypefaceCollection;
import com.norbsoft.typefacehelper.TypefaceHelper;

/**
 * Created by jtjohn on 1/12/2016.
 */
public class UnchainedApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Iconify.with(new FontAwesomeModule());

        TypefaceCollection typeface = new TypefaceCollection.Builder()
                .set(Typeface.NORMAL, Typeface.createFromAsset(getAssets(), "fonts/MontserratRegular.ttf"))
                .create();
        TypefaceHelper.init(typeface);
    }
}
