package com.steps.geosms;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.util.Log;
import android.util.Pair;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.conversation.ConversationActivity;
import com.steps.geosms.conversationsList.ConversationsListActivity;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.SMS;
import com.steps.geosms.utils.Constants;
import com.steps.geosms.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class managing Notifications.
 *
 * Created by Ioane on 3/4/2015.
 */
public class MyNotificationManager extends BroadcastReceiver{

    private static final String GROUP = "GEO_SMS_NOTIF_GROUP";

    private static final String IS_SUMMARY = "is_summary";

    private static final String TAG = MyNotificationManager.class.getSimpleName();

    public static final int ID_SMS_RECEIVED = 1231321;

    public static final int ID_SMS_FAILED = 1222322;

    private static final String UNREAD_MSG_COUNT = "unread_count";

    public static void buildSmsReceiveUsualNotif(Context ctx,Contact contact,SMS sms,NotificationCompat.Builder mBuilder){
        Bitmap photo;
        if(contact.getPhotoUri() == null){
            photo =  BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_sms_received);
        }else {
            photo = Utils.getPhotoFromURI(contact.getPhotoUri(), ctx, 100);
        }
                    mBuilder.setLargeIcon(photo)
                        .setSmallIcon(R.mipmap.ic_sms_received)
                        .setContentTitle(contact.getName() == null ? contact.getAddress() : contact.getName())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(sms.getText()))
                        .setAutoCancel(true)
                        .setGroup(GROUP)
                        .setGroupSummary(true);

        setPriority(mBuilder);

        Intent resultIntent = new Intent(ctx, ConversationActivity.class);
        ArrayList<Contact> data = new ArrayList<>(1);
        data.add(contact);
        resultIntent.putExtra(Constants.CONTACT_DATA, data);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(ConversationActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        int uniqueInt = new Random().nextInt(Integer.MAX_VALUE);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        uniqueInt,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        Intent dismissedPI = new Intent(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED,
                null , ctx, MyNotificationManager.class);

        dismissedPI.putExtra(Constants.CONTACT_BUNDLE, contact.getBundle());
        mBuilder.setDeleteIntent(PendingIntent.getBroadcast(ctx,0,dismissedPI,0));
    }

    public static void buildSmsReceiveSummaryNotif(Context ctx,SMS sms,NotificationCompat.Builder mBuilder,int numUnread){

        Bitmap photo = BitmapFactory.decodeResource(ctx.getResources(),android.R.drawable.stat_notify_chat);

        String format = ctx.getString(R.string.unread_sms_content_format);

                    mBuilder.setLargeIcon(photo)
                        .setSmallIcon(R.mipmap.ic_sms_received)
                        .setContentTitle(ctx.getText(R.string.geosms_unread_sms))
                        .setContentText(String.format(format, numUnread))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setGroup(GROUP)
                        .setGroupSummary(true);
        setPriority(mBuilder);

        Intent resultIntent = new Intent(ctx, ConversationsListActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(ConversationsListActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        Intent dismissedPI = new Intent(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED,
                null , ctx, MyNotificationManager.class);

        dismissedPI.putExtra(IS_SUMMARY, true);
        mBuilder.setDeleteIntent(PendingIntent.getBroadcast(ctx,0,dismissedPI,0));

    }

    public static int getNumUnreadMessages(Context context){
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getInt(UNREAD_MSG_COUNT,0);
    }

    public static void setNumUnreadMessages(Context context,int num){
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(UNREAD_MSG_COUNT,num);
        editor.commit();
    }

    /**
     *
     * @param ctx Context of activity
     * @param contact contact object that summary is directed to.
     * @param sms Sms that must be seen.
     * @param mBuilder Builder where Notification will be assembled.
     * @return true if built notification is summary. False otherwise.
     */
    public static boolean buildSmsReceiveNotification(Context ctx,Contact contact,SMS sms,NotificationCompat.Builder mBuilder){
        // one more notification will be seen

        int numUnread = getNumUnreadMessages(ctx);
        setNumUnreadMessages(ctx, ++numUnread);

        if(numUnread > 1){
            // create and return summary notification.
            buildSmsReceiveSummaryNotif(ctx, sms,mBuilder,numUnread);
            return true;
        }else{
            // create usual notification , for contact.
            buildSmsReceiveUsualNotif(ctx,contact,sms,mBuilder);
            return false;
        }

    }

    public static void buildSmsFailed(Context context,String address,NotificationCompat.Builder mBuilder){
        mBuilder.setContentTitle(context.getString(R.string.sms_sending_faild))
                .setSmallIcon(R.mipmap.ic_sms_failed)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        Intent resultIntent = new Intent(context, ConversationActivity.class);

        resultIntent.setData(Uri.parse("smsto:"+ address));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ConversationsListActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
    }


    @TargetApi(16)
    private static void setPriority(NotificationCompat.Builder builder){
        builder.setPriority(Notification.PRIORITY_HIGH);
    }

    public static void clearNotifications(Context ctx){
        // TODO CLEAR FAILED SMS MESSAGES
        NotificationManager mNotificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(ID_SMS_RECEIVED);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive() :" + action);
        if(action == null) return;

        if(action.equals(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED)){
            setNumUnreadMessages(context,0);
//            if(intent.getBooleanExtra(IS_SUMMARY,false)){
//                // if summary was dismissed we clear all.
//            }else{
//                Contact contact = new Contact(intent.getBundleExtra(Constants.CONTACT_BUNDLE));
//            }
        }else if(action.equals(Constants.Actions.FAILED_NOTIFICATION_DISMISSED)){
            // TODO add failed notification
        }
    }
}


