package com.steps.geosms.notifications;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing and retrieving sms
 * Created by ioane on 6/9/15.
 */
public class NotificationHelper{

    private static final String TAG = NotificationHelper.class.getSimpleName();
    private SQLiteDatabase mDb;

    public NotificationHelper(Context context){
        SQLiteOpenHelper openHelper = new NotificationDbHelper(context);
        mDb = openHelper.getWritableDatabase();
    }

    public void insertSMS(long sms_id, long thread_id, int type){
        Log.i(TAG,"insertSms : sms_id " + sms_id + " thread_id " + thread_id);
        ContentValues cv = new ContentValues();
        cv.put("SMS_ID",sms_id);
        cv.put("THREAD_ID",thread_id);
        cv.put("TYPE", type);
        mDb.insert("NOTIFICATIONS",null,cv);
    }

    public void clearAllReceivedNotifications(){
        Log.i(TAG,"clearAllReceivedNotifications");
        mDb.delete("NOTIFICATIONS", "TYPE=?", new String[]{"0"});
    }

    public void clearAllFailedNotifications(){
        Log.i(TAG,"clearAllReceivedNotifications");

        mDb.delete("NOTIFICATIONS", "TYPE=?", new String[]{"1"});
    }

    public void clearAllThreadNotifications(long threadId){
        Log.i(TAG,"clearAllThreadNotifications " + threadId);

        mDb.delete("NOTIFICATIONS","THREAD_ID=?",new String[]{""+threadId});
    }

    public void clearThreadReceivedNotifications(long threadId){
        Log.i(TAG,"clearThreadReceivedNotifications " +threadId);

        mDb.delete("NOTIFICATIONS","TYPE=? and THREAD_ID=?",new String[]{"0",""+threadId});
    }

    public void clearThreadFailedNotifications(long threadId){
        Log.i(TAG,"clearThreadFailedNotifications " +threadId);

        mDb.delete("NOTIFICATIONS","TYPE=? and THREAD_ID=?",new String[]{"1",""+threadId});
    }

    public void cleanUp(){
        mDb.close();
    }

//    public void clearAllFailedThreadNotifications(int threadId){
//
//    }

    /**
     * Pair<Integer,Integer> first param is sms_id , second one is thread_id
     *
     * if has zero elements , returns null.
     */
    public List<Pair<Long,Long>> getAllFailedNotifications(){
        Cursor c = mDb.query("NOTIFICATIONS", null, "TYPE=?", new String[]{"1"}, null, null, null);
        try{
            if(c == null || !c.moveToFirst())
                return null;

            ArrayList<Pair<Long,Long>> failedNotifs = new ArrayList<>();
            do{
                long smsId = c.getLong(c.getColumnIndex("SMS_ID"));
                long threadId = c.getLong(c.getColumnIndex("THREAD_ID"));
                Pair<Long,Long> notification = new Pair<>(smsId,threadId);
                failedNotifs.add(notification);
            }while (c.moveToNext());

            return failedNotifs;
        }finally {
            if(c != null)
                c.close();
        }
    }


    /**
     * Pair<Integer,Integer> first param is sms_id , second one is thread_id
     *
     * if has zero elements , returns null.
     */
    public List<Pair<Long,Long>> getAllReceivedNotifications(){
        Cursor c = mDb.query("NOTIFICATIONS", null, "TYPE=?", new String[]{"0"}, null, null, null);
        try{
            if(c == null || !c.moveToFirst())
                return null;

            ArrayList<Pair<Long,Long>> receivedNotifs = new ArrayList<>();
            do{
                long smsId = c.getLong(c.getColumnIndex("SMS_ID"));
                long threadId = c.getLong(c.getColumnIndex("THREAD_ID"));
                Pair<Long,Long> notification = new Pair<>(smsId,threadId);
                receivedNotifs.add(notification);
            }while (c.moveToNext());

            return receivedNotifs;
        }finally {
            if(c != null)
                c.close();
        }
    }





    private static class NotificationDbHelper extends SQLiteOpenHelper{
        /**
         * Notifications
         * ---------------------------
         * SMS_ID | THREAD_ID | TYPE |
         * ---------------------------
         * Sms id saves which sms arrived.
         * Thread_id saves just in which thread this sms was.
         * and Type saves just what type this notification is.
         * 0 for received, 1 for failed.
         */
        private static final String SQL_CREATE_NOTIFICATIONS =
                "CREATE TABLE NOTIFICATIONS (SMS_ID INTEGER, THREAD_ID INTEGER,TYPE INTEGER);";


        private static final String SQL_NOTIFICATIONS_DESTROY =
                "DROP TABLE IF EXISTS NOTIFICATIONS";

        public NotificationDbHelper(Context context) {
            super(context, "GeoSms_notifications_database", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_NOTIFICATIONS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_NOTIFICATIONS_DESTROY);
            onCreate(db);
        }
    }

}
