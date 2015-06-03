package com.steps.geosms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.steps.geosms.websms.MagtifunWebSms;
import com.steps.geosms.websms.WebSms;


/**
 * Class MyPreferenceManager
 * Created by Ioane on 3/5/2015.
 */
public class MyPreferencesManager {

    private static final String TAG = MyPreferencesManager.class.getSimpleName();
    public static String WEBSMS_NAME = "websms_name";
    public static String WEBSMS_USERNAME = "websms_username";
    public static String WEBSMS_PASSWORD = "websms_password";
    public static String WEBSMS_COOKIE = "websms_cookie";
    public static int MAGTIFUN_ID = 1;
    public static int GEOCELL_ID = 191;

    public static String DELIVERY_REQUEST = "request_delivery";
    public static String NOTIFICATIONS = "notifications";


    private static String WEB_PREFS = "WEB_SMS_PREFS";
    private static String WEB_SMS_ENABLED = "enable_websms";

    public static SharedPreferences getWebSmsPreferences(Context ctx){
        //return (ctx.getApplicationContext()).getSharedPreferences(WEB_PREFS,Context.MODE_PRIVATE);
        return PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
    }

    public static WebSms getWebSmsManager(Context ctx){
        SharedPreferences preferences = getWebSmsPreferences(ctx);

        int webSmsId = Integer.parseInt(preferences.getString(WEBSMS_NAME,"-1"));
        Log.i(TAG,"webSmsId " + webSmsId);
        if(webSmsId == -1) return null; // user hasn't account
        if(webSmsId == MAGTIFUN_ID){
            String username = preferences.getString(WEBSMS_USERNAME,null);
            String password = preferences.getString(WEBSMS_PASSWORD,null);
            String cookie = preferences.getString(WEBSMS_COOKIE,"");

            Log.i(TAG,"userName " + username );
            return new MagtifunWebSms(username,password,cookie,ctx);
        }else if(webSmsId == GEOCELL_ID){
            Log.w(TAG,"geocell websms is not ready");
            return null;
        }
        return null;
    }

    public static void saveCookie(Context context,String cookie) {
        SharedPreferences.Editor editor = getWebSmsPreferences(context).edit();
        editor.putString(WEBSMS_COOKIE,cookie);
        editor.commit();
    }

    public static boolean isDeliveryRequested(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getBoolean(DELIVERY_REQUEST,false);
    }

    public static boolean isNotificationOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getBoolean(NOTIFICATIONS,true);
    }

    public static boolean isWebSmsEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getBoolean(WEB_SMS_ENABLED,true);
    }

}
