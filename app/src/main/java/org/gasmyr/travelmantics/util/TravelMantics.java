package org.gasmyr.travelmantics.util;

import android.app.Application;

import es.dmoral.toasty.Toasty;

public class TravelMantics extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Toasty.Config.getInstance()
                .tintIcon(true).setTextSize(20).allowQueue(false).apply();
    }
}
