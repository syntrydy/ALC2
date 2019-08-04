package org.gasmyr.travelmantics.util;

import android.content.Context;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class Toaster {

    private Toaster(){

    }

    public static void  info(Context context, String message){
        Toasty.info(context, message, Toast.LENGTH_LONG, true).show();
    }
    public static void  warn(Context context, String message){
        Toasty.warning(context, message, Toast.LENGTH_LONG, true).show();
    }

    public static void  error(Context context, String message){
        Toasty.error(context, message, Toast.LENGTH_LONG, true).show();
    }
}
