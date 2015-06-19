package com.steps.geosms;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Mother class
 * Created by Ioane on 6/6/15.
 */
public class MyApplication extends Application{

    @Override
    public void onCreate(){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        prefs.registerOnSharedPreferenceChangeListener(this);
        updateLanguage(this);
        super.onCreate();
    }

    public static void updateLanguage(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String lang = prefs.getString("locale_override", "");
        updateLanguage(ctx, lang);
    }

    public static void updateLanguage(Context ctx, String lang){
        Log.i("APP", " " + lang);
        ctx = ctx.getApplicationContext();
        Configuration cfg = new Configuration();
        if(!TextUtils.isEmpty(lang)){
            cfg.locale = new Locale(lang);
        }
        else{
            cfg.locale = Locale.US;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putString("locale_override",lang).commit();
        ctx.getResources().updateConfiguration(cfg, null);
    }

//    public String translateSMS(String sms){
//        try{
//            if(mTranslator != null)
//                mTranslator.geoToLat(sms);
//            return sms;
//        }catch (Exception e){
//            e.printStackTrace();
//            return sms;
//        }
//    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if("enable_geoTranslate".equals(key)){
//            if(sharedPreferences.getBoolean("enable_geoTranslate",false)){
//                if(mTranslator == null){
//                    try{
//                        mTranslator = Utils.getTranslator(this);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        mTranslator = null;
//                    }
//                }
//            }else {
//                mTranslator = null;
//                }
//        }
//    }
}
