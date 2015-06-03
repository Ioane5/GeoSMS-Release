package com.steps.geosms.objects;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.steps.geosms.utils.Constants;
import com.steps.geosms.utils.Constants.MESSAGE;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class Sms
 * <p/>
 * Created by Ioane on 2/21/2015.
 */
public class SMS {

    private static final String TAG = SMS.class.getSimpleName();


    public enum MsgType {SENT, RECEIVED, DRAFT, FAILED, PENDING}

    private String text;

    private int id;

    /**
     * If msg if from me date is date sent.
     * If from other date is date received.
     */
    private Date date;

    private MsgType type;

    private boolean isRead;

    private boolean isDelivered;

    private String serviceCenter;

    private MsgType intToMsgType(int intType) {
        switch (intType) {
            case MESSAGE.MESSAGE_TYPE_ALL:
            case MESSAGE.MESSAGE_TYPE_INBOX:
                return MsgType.RECEIVED;
            case MESSAGE.MESSAGE_TYPE_DRAFT:
                return MsgType.DRAFT;
            case MESSAGE.MESSAGE_TYPE_FAILED:
                return MsgType.FAILED;
            case MESSAGE.MESSAGE_TYPE_OUTBOX:
            case MESSAGE.MESSAGE_TYPE_QUEUED:
                return MsgType.PENDING;
            case MESSAGE.MESSAGE_TYPE_SENT:
                return MsgType.SENT;
            default:
                return MsgType.RECEIVED;
        }
    }

    private int msgTypeToInt(MsgType type) {
        switch (type) {
            case RECEIVED:
                return MESSAGE.MESSAGE_TYPE_INBOX;
            case DRAFT:
                return MESSAGE.MESSAGE_TYPE_DRAFT;
            case FAILED:
                return MESSAGE.MESSAGE_TYPE_FAILED;
            case PENDING:
                return MESSAGE.MESSAGE_TYPE_OUTBOX;
            case SENT:
                return MESSAGE.MESSAGE_TYPE_SENT;
        }
        return MESSAGE.MESSAGE_TYPE_ALL;
    }

    public SMS(String text, Date date, MsgType type, boolean isRead, boolean isDelivered, String serviceCenter) {
        this.text = text;
        this.date = date;
        this.type = type;
        this.isRead = isRead;
        this.isDelivered = isDelivered;
        this.serviceCenter = serviceCenter;
    }

    public SMS(ContentValues cv) {
        this.text = cv.getAsString(MESSAGE.BODY);
        this.date = new Date(cv.getAsLong(MESSAGE.DATE));
        this.type = intToMsgType(cv.getAsInteger(MESSAGE.PROTOCOL));
        this.isRead = cv.getAsInteger(MESSAGE.READ) == 1;
        Integer temp = cv.getAsInteger(MESSAGE.STATUS);
        this.isDelivered = temp != null && temp == MESSAGE.STATUS_COMPLETE;
        this.serviceCenter  = cv.getAsString(MESSAGE.SERVICE_CENTER);
    }

    public SMS(Cursor cursor) {
        int protocol = cursor.getInt(cursor.getColumnIndex(MESSAGE.PROTOCOL));
        if (protocol != MESSAGE.SMS_PROTOCOL) {
            Log.w(TAG, "this message is not SMS");
            return;
        }

        id = cursor.getInt(cursor.getColumnIndex(MESSAGE.ID));
        text = cursor.getString(cursor.getColumnIndex(MESSAGE.BODY));
        date = new Date(cursor.getLong(cursor.getColumnIndex(MESSAGE.DATE)));

        int intType = cursor.getInt(cursor.getColumnIndex(MESSAGE.TYPE));
        type = intToMsgType(intType);

        this.isDelivered = cursor.getInt(cursor.getColumnIndex(MESSAGE.STATUS)) == MESSAGE.STATUS_COMPLETE;

        switch (type) {
            case RECEIVED:
                int readInt = cursor.getInt(cursor.getColumnIndex(MESSAGE.READ));
                isRead = readInt == 1;
                serviceCenter = cursor.getString(cursor.getColumnIndex(MESSAGE.SERVICE_CENTER));
                break;

            case SENT:
                isRead = true;
                serviceCenter = null;
                break;
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public MsgType getMsgType() {
        return type;
    }

    public void setMsgType(MsgType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(MESSAGE.PROTOCOL, MESSAGE.SMS_PROTOCOL);
        values.put(MESSAGE.BODY, this.getText());
        values.put(MESSAGE.DATE, this.getDate().getTime());
        values.put(MESSAGE.READ, this.isRead());
        values.put(MESSAGE.TYPE, msgTypeToInt(this.getMsgType()));

        return values;
    }


    public static ContentValues getContentValuesFromBundle(Bundle bundle) {
        ContentValues cv = new ContentValues();

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length <= 0)
            return null;
        SmsMessage[] msgs = new SmsMessage[pdus.length];
        StringBuilder messageText = new StringBuilder();
        for (int i = 0; i < pdus.length; i++) {
            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            messageText.append(msgs[i].getMessageBody());
        }
        msgs[0].getUserData();

        cv.put(Constants.ADDRESS, msgs[0].getOriginatingAddress());
        cv.put(MESSAGE.BODY, messageText.toString());
        cv.put(MESSAGE.PROTOCOL, msgs[0].getProtocolIdentifier());
        cv.put(MESSAGE.TYPE, MESSAGE.MESSAGE_TYPE_INBOX);
        cv.put(MESSAGE.SUBJECT, msgs[0].getPseudoSubject());
        cv.put(MESSAGE.SERVICE_CENTER, msgs[0].getServiceCenterAddress());
        cv.put(MESSAGE.READ, 0); // isn't read
        cv.put(MESSAGE.DATE, msgs[0].getTimestampMillis());
        cv.put(MESSAGE.ERROR_CODE, msgs[0].getStatus());

        return cv;
    }


    public String textReference() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //called without pattern
        return "service center = " + getServiceCenter() + "\n" +
                "date = " + (getDate() == null ? "NULL" : df.format(getDate()));
    }
}
