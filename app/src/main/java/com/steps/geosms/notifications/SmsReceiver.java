package com.steps.geosms.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.steps.geosms.utils.Constants;

import java.util.Iterator;
import java.util.Set;

/**
 * Class receiving sms broadcast
 * Created by Ioane on 6/9/15.
 */
public class SmsReceiver extends WakefulBroadcastReceiver{

    private static final String TAG = SmsReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(TextUtils.isEmpty(action))
            return;
        // we must abort broadcast if we are on lower api.
        if(action.contains(Constants.Actions.SMS_RECEIVED_OLD) && Build.VERSION.SDK_INT < 19)
            abortBroadcast();

        intent.putExtra("result",getResultCode());
        intent.setClass(context, SmsManagerService.class);
        startWakefulService(context, intent);
    }


    public static void dumpIntent(Intent i){

        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(TAG,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e(TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e(TAG,"Dumping Intent end");
        }
    }
}
