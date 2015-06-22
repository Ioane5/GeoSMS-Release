package com.steps.geosms.notifications;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;
import android.util.Pair;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.MyPreferencesManager;
import com.steps.geosms.conversation.ConversationActivity;
import com.steps.geosms.conversationsList.ConversationsListActivity;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.Conversation;
import com.steps.geosms.objects.SMS;
import com.steps.geosms.utils.Constants;
import com.steps.geosms.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Created by ioane on 6/9/15.
 */
public class SmsManagerService extends IntentService {

    private static final String TAG = SmsManagerService.class.getSimpleName();

    private static final int VIBRATE_LENGTH = 100;

    private static final String GROUP = "GEO_SMS_NOTIF_GROUP";
    /**
     * No thread is shown to user.
     */
    public static final int THREAD_ID_NONE  = -123123;

    private static final String IS_SUMMARY = "is_summary";
    private static final int SUMMARY = -1000;

    private static final String RECEIVED_TAG = "received";
    private static final String FAILED_TAG = "failed";

    private static  AtomicInteger mIntentId;


    /**
     * Saves current shown  threadId that user interacts with.
     */
    private static long currentThreadId = THREAD_ID_NONE;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
\     */
    public SmsManagerService() {
        super("sms_manager");
    }

    public static void updateThreadId(long threadId){
        currentThreadId = threadId;
    }

    private static boolean mIsNotificationsOn = true;

    private int mResultCode;


    @Override
    protected void onHandleIntent(Intent intent) {
        handle(getApplicationContext(),intent);
        SmsReceiver.completeWakefulIntent(intent);
    }

    void handle(Context context,Intent intent){
        mResultCode = intent.getIntExtra("result",-1);
        String action = intent.getAction();
        Log.i(TAG, "handle : " + action);

        // New versions only listen to SMS_DELIVER.
        if(Build.VERSION.SDK_INT >= 19 && action.contains(Constants.Actions.SMS_RECEIVED_OLD)){
            Log.i(TAG, "action dismissed " + action);
            return;
        }

        mIsNotificationsOn = MyPreferencesManager.isNotificationOn(context);

        if(action.equals(Constants.Actions.SMS_DELIVERED)){
            handleSmsDelivered(context, intent);

        }else if(action.equals(Constants.Actions.MESSAGE_SENT)){
            handleSmsSend(context, intent);

        }else if(action.contains(Constants.Actions.SMS_RECEIVED_NEW) ||
                action.contains(Constants.Actions.SMS_RECEIVED_OLD)){
            handleSmsReceive(context, intent);
        }else if(action.contains(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED)){
            handleDismissedNotifications(intent);

        }else{
            Log.w(TAG,"unknown action "+action);

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSubText(action);
            mNotificationManager.notify(1212112, builder.build());
        }
    }

    private void handleSmsReceive(Context ctx,Intent intent){
        Bundle bundle = intent.getExtras();
        if(bundle == null){
            Log.w(TAG,"bundle is empty");
            return;
        }
        ContentValues values = SMS.getContentValuesFromBundle(bundle);
        if(values == null)
            return;
        values.put(Constants.MESSAGE.DATE,System.currentTimeMillis()); // time correction
        Uri smsUri = ctx.getContentResolver().insert(Uri.parse("content://sms/"), values);

        String address = values.getAsString(Constants.ADDRESS);
        Contact contact = new Contact(ctx,address);
        long threadId = Conversation.getOrCreateThreadId(ctx, contact.getAddress());

        SMS sms = new SMS(values);
        sms.setId(Long.parseLong((smsUri.getLastPathSegment())));
        NotificationHelper notificationHelper = new NotificationHelper(ctx);
        if(threadId != currentThreadId) {
            notificationHelper.insertSMS(sms.getId(),threadId,0);
            sendNotification(notificationHelper.getAllReceivedNotifications(), true);
        }else {
            // if user is on this chat , make no notification , just slight vibration
            values = new ContentValues();
            values.put(Constants.MESSAGE.READ, 1); // is read
            // update message as read.
            ctx.getContentResolver().update(smsUri, values,null,null);
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VIBRATE_LENGTH);
        }
    }


    private void handleSmsDelivered(Context ctx, Intent intent){
        Uri deliveredSmsUri = intent.getData();
        if(deliveredSmsUri == null){
            Log.w(TAG, "deliveredSmsUri is null");
            return;
        }

        ContentValues values = new ContentValues();

        values.put(Constants.MESSAGE.STATUS, Constants.MESSAGE.STATUS_COMPLETE);
        try{
            ctx.getContentResolver().update(deliveredSmsUri,values,null,null);
        }catch (Exception e){
            Log.e(TAG,"exception entering delivery report");
            e.printStackTrace();
        }
    }


    private void handleSmsSend(Context ctx, final Intent intent){
        Uri pendingSmsUri = intent.getData();
        if(pendingSmsUri == null){
            Log.w(TAG,"pendingSmsURI is null");
            return;
        }
        Log.i(TAG, pendingSmsUri.toString());

        ContentValues values = new ContentValues();

        long smsId = Long.parseLong(pendingSmsUri.getLastPathSegment());
        String address = intent.getStringExtra(Constants.ADDRESS);
        long threadId = intent.getLongExtra(Constants.THREAD_ID, 0);

        switch (mResultCode){
            case Activity.RESULT_OK:
                Log.d(TAG, "SMS sent");
                values.put(Constants.MESSAGE.TYPE,Constants.MESSAGE.MESSAGE_TYPE_SENT);
                break;
            default:
                values.put(Constants.MESSAGE.TYPE,Constants.MESSAGE.MESSAGE_TYPE_FAILED);
                if(mIsNotificationsOn){
                    NotificationManager notificationManager =
                            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

                    if(address == null){
                        Log.e(TAG,"address was null, SMS_FAILED");
                    }
                    if(currentThreadId != threadId){
                        NotificationHelper helper = new NotificationHelper(ctx);
                        helper.insertSMS(smsId,threadId,1);

                        sendNotification(helper.getAllFailedNotifications(),false);
                    }
                }
                Log.d(TAG,"Message sending failed result Code :" + mResultCode);
        }
        try{
            ctx.getContentResolver().update(pendingSmsUri,values,null,null);
        }catch (Exception e){
            Log.w(TAG, "couldn't update sms! some bug here");
        }
    }


    public static void dismissThreadNotifications(Context context,long threadId){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(RECEIVED_TAG,(int) threadId); // delete all this thread messages.
        notificationManager.cancel(FAILED_TAG,(int) threadId); // delete all this thread messages.

        NotificationHelper helper = new NotificationHelper(context);
        helper.clearAllThreadNotifications(threadId);
        // if summary notifications were also cleared, clear them too.
        if(helper.getAllReceivedNotifications() == null){
            notificationManager.cancel(RECEIVED_TAG,SUMMARY);
        }
        if(helper.getAllFailedNotifications() == null){
            notificationManager.cancel(FAILED_TAG,SUMMARY);
        }
        helper.cleanUp();
    }


    public static void dismissSummaryNotifications(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(RECEIVED_TAG,SUMMARY);
        notificationManager.cancel(FAILED_TAG, SUMMARY);
    }


    public void handleDismissedNotifications(Intent intent){
        boolean isReceivedType = intent.getIntExtra(Constants.NOTIFICATION_TYPE,0) == 0;
        boolean isSummary = intent.getBooleanExtra(IS_SUMMARY,false);
        NotificationHelper helper = new NotificationHelper(getApplicationContext());
        if(isReceivedType){
            if(isSummary){
                helper.clearAllReceivedNotifications();
            }else{
                long threadId = intent.getLongExtra(Constants.THREAD_ID, 0);

                helper.clearThreadReceivedNotifications(threadId);
            }
        }else {
            if(isSummary){
                helper.clearAllFailedNotifications();
            }else{
                long threadId = intent.getLongExtra(Constants.THREAD_ID, 0);
                helper.clearThreadFailedNotifications(threadId);
            }
        }
        helper.cleanUp();
    }



    /**
     * @param notifications list of notification entries
     * @param isReceivedType if true means that notifications is for received sms.
     *                       if false - notification is for failed sms.
     */
    private void sendNotification(List<Pair<Long, Long>> notifications,boolean isReceivedType){
        if(notifications.size() <= 0)
            return;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());

        Uri uri = Uri.parse("content://sms/");
        ContentResolver cv = getContentResolver();
        if(notifications.size() > 1){
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for(Pair<Long,Long> notif : notifications){
                Cursor c = cv.query(uri, null, "_id=?", new String[]{"" + notif.first}, null);
                if(c == null)
                    continue;
                if(!c.moveToFirst()){
                    c.close();
                    Log.i(TAG,"returns null");
                    continue;
                }
                String address = c.getString(c.getColumnIndex("address"));
                String text = c.getString(c.getColumnIndex("body"));
                Contact contact = new Contact(getBaseContext(),address);
                inboxStyle.addLine(Html.fromHtml(String.format("%s : %s", "<b>"+ contact.getDisplayName() + "</b>", text)));
            }

            Bitmap photo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_sms_received);
            String formatContentText = isReceivedType? getString(R.string.unread_sms_content_format) :
                                                        getString(R.string.failed_sms_content_format);

            mBuilder.setStyle(inboxStyle)
                    .setLargeIcon(photo)
                    .setSmallIcon(isReceivedType? R.mipmap.ic_sms_received :
                                                  R.mipmap.ic_sms_failed)
                    .setContentTitle(isReceivedType? getText(R.string.geosms_unread_sms) :
                                                     getText(R.string.geosms_failed_sms))
                    .setContentText(String.format(formatContentText, notifications.size()))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setGroup(GROUP)
                    .setColor(Color.argb(100,20,80,200))
                    .setGroupSummary(true);

            Intent resultIntent = new Intent(this, ConversationsListActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(ConversationsListActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);
            Intent dismissedPI = new Intent(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED,
                    null , this, SmsReceiver.class);

            dismissedPI.putExtra(IS_SUMMARY, true);
            //dismissedPI.putExtra(Constants.NOTIFICATION_ARRAY, notifications.toArray());
            dismissedPI.putExtra(Constants.NOTIFICATION_TYPE, isReceivedType ? 0 : 1);

            int uniqueInt = new Random().nextInt();
            mBuilder.setDeleteIntent(PendingIntent.getBroadcast(this, uniqueInt, dismissedPI, PendingIntent.FLAG_CANCEL_CURRENT));
            notificationManager.notify(isReceivedType? RECEIVED_TAG : FAILED_TAG,
                    SUMMARY, mBuilder.build());
        }else {
            Cursor c = cv.query(uri, null, "_id=?", new String[]{"" + notifications.get(0).first}, null);
            if(c == null)
                return;
            if(!c.moveToFirst()){
                c.close();
                return;
            }
            String address = c.getString(c.getColumnIndex("address"));
            String text = c.getString(c.getColumnIndex("body"));
            Contact contact = new Contact(getBaseContext(),address);

            contact.resolveContactImage(getApplicationContext(),100);
            Bitmap photo = contact.getPhoto();

            mBuilder.setLargeIcon(photo)
                    .setSmallIcon(isReceivedType? R.mipmap.ic_sms_received : R.mipmap.ic_sms_failed)
                    .setContentTitle(isReceivedType? contact.getDisplayName() :
                                                     getString(R.string.sms_sending_faild))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(text)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .setAutoCancel(true)
                    .setGroup(GROUP);

            // now build callback
            Intent resultIntent = new Intent(this, ConversationActivity.class);
            ArrayList<Contact> data = new ArrayList<>(1);
            data.add(contact);
            resultIntent.putExtra(Constants.CONTACT_DATA, data);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(ConversationActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            int uniqueInt = new Random().nextInt();
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            uniqueInt,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            Intent dismissedPI = new Intent(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED,
                    null , this, SmsReceiver.class);

            long threadId = notifications.get(0).second;

            dismissedPI.putExtra(IS_SUMMARY, true);
            dismissedPI.putExtra(Constants.NOTIFICATION_TYPE, isReceivedType? 0 : 1);
            dismissedPI.putExtra(Constants.THREAD_ID,threadId);
            Log.i(TAG,"thread_id " + threadId);
            uniqueInt = new Random().nextInt();
            mBuilder.setDeleteIntent(PendingIntent.getBroadcast(this, uniqueInt, dismissedPI, PendingIntent.FLAG_CANCEL_CURRENT));

            notificationManager.notify(isReceivedType? RECEIVED_TAG : FAILED_TAG,
                    (int)threadId,mBuilder.build());
        }
    }


}
