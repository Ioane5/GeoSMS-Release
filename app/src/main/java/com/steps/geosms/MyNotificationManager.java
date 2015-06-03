package com.steps.geosms;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

    // TODO THIS SHOULDN'T BE STATIC
    private static HashMap<Pair<String,String>,Integer> receivedMessages =  new HashMap<>();

    public static void buildSmsReceiveUsualNotif(Context ctx,Contact contact,SMS sms,NotificationCompat.Builder mBuilder){
        Bitmap photo;
        if(contact.getPhotoUri() == null){
            photo =  BitmapFactory.decodeResource(ctx.getResources(), android.R.drawable.stat_notify_chat);
        }else {
            photo = Utils.getPhotoFromURI(contact.getPhotoUri(), ctx, 100);
        }

                    mBuilder.setLargeIcon(photo)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle(contact.getName() == null ? contact.getAddress() : contact.getName())
                        .setContentText(sms.getText())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setGroup(GROUP);
        setPriority(mBuilder);

        Intent resultIntent = new Intent(ctx, ConversationActivity.class);
        ArrayList<Contact> data = new ArrayList<>(1);
        data.add(contact);
        resultIntent.putExtra(Constants.CONTACT_DATA, data);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(ConversationActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        Intent dismissedPI = new Intent(Constants.Actions.RECEIVED_NOTIFICATION_DISMISSED,
                null , ctx, MyNotificationManager.class);

        dismissedPI.putExtra(Constants.CONTACT_BUNDLE, contact.getBundle());
        mBuilder.setDeleteIntent(PendingIntent.getBroadcast(ctx,0,dismissedPI,0));
    }

    public static void buildSmsReceiveSummaryNotif(Context ctx,SMS sms,NotificationCompat.Builder mBuilder){

        Bitmap photo = BitmapFactory.decodeResource(ctx.getResources(),android.R.drawable.stat_notify_chat);

        StringBuilder contentText = new StringBuilder();
        CharSequence format = ctx.getText(R.string.unread_sms_content_format);
        for(Pair<String,String> contact: receivedMessages.keySet()){
            int numReceived = receivedMessages.get(contact);
            String name = contact.first != null ? contact.first : contact.second;
            contentText.append(String.format(format.toString(), numReceived, name));
        }

                    mBuilder.setLargeIcon(photo)
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle(ctx.getText(R.string.geosms_unread_sms))
                        .setContentText(contentText.toString())
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setGroup(GROUP)
                        .setGroupSummary(true)
                        .setNumber(receivedMessages.size());

        setPriority(mBuilder);

        Intent resultIntent = new Intent(ctx, ConversationsListActivity.class);

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
        Pair<String,String> contactInfo = new Pair<>(contact.getName(),contact.getAddress());
        if(receivedMessages.containsKey(contactInfo)){
            receivedMessages.put(contactInfo, receivedMessages.get(contactInfo)+1);
        }else {
            receivedMessages.put(contactInfo, 1);
        }

        if(receivedMessages.size() > 1){
            // create and return summary notification.
            buildSmsReceiveSummaryNotif(ctx, sms,mBuilder);
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
        receivedMessages.clear();
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
            if(intent.getBooleanExtra(IS_SUMMARY,false)){
                // if summary was dismissed we clear all.
                receivedMessages.clear();
            }else{
                Contact contact = new Contact(intent.getBundleExtra(Constants.CONTACT_BUNDLE));
                receivedMessages.remove(new Pair<>(contact.getName(),contact.getAddress()));
            }
        }else if(action.equals(Constants.Actions.FAILED_NOTIFICATION_DISMISSED)){
            // TODO add failed notification
        }
    }
}


