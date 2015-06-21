package com.steps.geosms.websms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.steps.geosms.MyPreferencesManager;

/**
 * Class SyncedWebSms that stays synced.
 * <p/>
 * Pattern by Ioane Sharvadze.
 * <p/>
 * This pattern is good for changing object at runtime. So in this option if user changed
 * web sms in runtime, this object will change inside structure so that webSms will be always
 * up to date.
 * <p/>
 * Created by Ioane on 3/24/2015.
 */
public class SyncedWebSms implements WebSms, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SyncedWebSms.class.getSimpleName();

    private WebSms webSms;

    private Context context;

    public SyncedWebSms(Context context) {
        this.context = context;
        this.webSms = MyPreferencesManager.getWebSmsManager(context);
        SharedPreferences preferences = MyPreferencesManager.getWebSmsPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean authenticate() {
        return webSms != null && webSms.authenticate();
    }

    @Override
    public boolean sendSms(String message, String address) {
        return webSms != null && webSms.sendSms(message, address);
    }

    @Override
    public int getNumMessages() {
        if (webSms == null) return 0;
        return webSms.getNumMessages();
    }

    @Override
    public String getBalance() {
        if (webSms == null)
            return null;
        return webSms.getBalance();
    }

    @Override
    public String getCookie() {
        if (webSms == null) return "";
        return webSms.getCookie();
    }

    @Override
    public void setCookie(String cookie) {
        if (webSms == null) return;
        webSms.setCookie(cookie);
    }

    @Override
    public String getPassword() {
        if (webSms == null) return "";
        return webSms.getPassword();
    }

    @Override
    public void setPassword(String password) {
        if (webSms == null) return;
        webSms.setPassword(password);
    }

    @Override
    public String getUserName() {
        if (webSms == null) return "";
        return webSms.getUserName();
    }

    @Override
    public void setUserName(String userName) {
        if (webSms == null) return;
        webSms.setUserName(userName);
    }

    @Override
    public String getAccountName() {
        if (webSms == null) return "";
        return webSms.getAccountName();
    }

    @Override
    public void setAccountName(String name) {
        if (webSms == null) return;
        webSms.setAccountName(name);
    }

    @Override
    public void updateBalance() {
        if (webSms == null) return;
        webSms.updateBalance();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, key);
        if (MyPreferencesManager.WEBSMS_COOKIE.equals(key) || key == null)
            return;
        // Web sms changed
        if (MyPreferencesManager.WEBSMS_NAME.equals(key)) {
            MyPreferencesManager.saveCookie(context, null);
            this.webSms = MyPreferencesManager.getWebSmsManager(context);
            return;
        }
        if (webSms == null) return;

        // changed username
        if (MyPreferencesManager.WEBSMS_USERNAME.equals(key)) {
            MyPreferencesManager.saveCookie(context, null);
            webSms.setCookie(""); // clear cookie
            this.webSms.setUserName(sharedPreferences.getString(MyPreferencesManager.WEBSMS_USERNAME, ""));
        } else if (MyPreferencesManager.WEBSMS_PASSWORD.equals(key)) {
            MyPreferencesManager.saveCookie(context, null);
            webSms.setCookie(""); // clear cookie
            this.webSms.setPassword(sharedPreferences.getString(MyPreferencesManager.WEBSMS_PASSWORD, ""));
        }
    }
}