package com.steps.geosms.broadcastReceivers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Ioane on 3/1/2015.
 */
public class HeadlessSmsSendService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
