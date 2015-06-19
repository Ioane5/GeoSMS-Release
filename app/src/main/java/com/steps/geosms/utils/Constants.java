package com.steps.geosms.utils;

import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Interface Constants that defines constants
 * used in application.
 *
 * Created by Ioane on 2/20/2015.
 */
@SuppressWarnings("unused")
public interface Constants {

    interface TranslatorData{
        String words = "words.dat";
        String shis = "shis.dat";
        String chis = "chis.dat";
        String dzis = "dzis.dat";
        String exceptions = "exceptions.dat";
    }

    String NOTIFICATION_ARRAY = "notification_array";

    interface URIS{
        Uri SMS = Uri.parse("content://sms");
    }

    int IMAGE_SIZE = 90;

    String MAGTIFUN = "Magtifun";
    String GEOCELL = "GeoCell";

    String RECIPIENT_IDS = "recipient_ids";

    String ID = "_id";


    String CONVERSATION_LAST_MESSAGE = "snippet";

    String CONVERSATION_DATE = "date";

    String CONVERSATION_READ = "read";

    String DRAFTS_FILE = "drafts";

    String THREAD_ID = "thread_id";

    String MSG_COUNT = "message_count";

    int THREAD_NONE = -30;

    /**
     * For intent
     */
    String ADDRESS = "address";

    String CONTACT_BUNDLE = "CONTACT";

    String CONTACT_DATA = "CONTACT_DATA";

    int KITKAT_API_LEVEL = 19;
    String TOGGLE_CHECKED = "toggle_checked";


    String[] contacts_projection = new String[]{
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

    /** How many seconds wait before delete */
    int UNDO_TIME = 5;


    interface Actions{
        /** Sms delivered to destination */
        String SMS_DELIVERED = "sms_deliverd_to_Destination";
        String MESSAGE_SENT = "sms_sent";
        String SMS_RECEIVED_OLD= "SMS_RECEIVED";
        String SMS_RECEIVED_NEW = "SMS_DELIVER";
        String RECEIVED_NOTIFICATION_DISMISSED = "received_notification_dismissed";
        String FAILED_NOTIFICATION_DISMISSED = "failed_notification_dismissed";
    }

    interface MESSAGE{


        // Constants for message protocol types.
        int SMS_PROTOCOL = 0;
        int MMS_PROTOCOL = 1;

        String PROTOCOL = "protocol";
        /**
         * The TP-Status value for the message, or -1 if no status has
         * been received
         */
        String STATUS = "status";


        int STATUS_NONE = -1;
        int STATUS_COMPLETE = 0;
        int STATUS_PENDING = 32;
        int STATUS_FAILED = 64;



        String READ = "read";

        String ID = "_id";

        /**
         * The date the message was received.
         * <P>Type: INTEGER (long)</P>
         */
        String DATE = "date";

        /**
         * The date the message was sent.
         * <P>Type: INTEGER (long)</P>
         */
        String DATE_SENT = "date_sent";

        /**
         * The subject of the message, if present
         * <P>Type: TEXT</P>
         */
        String SUBJECT = "subject";

        /**
         * The body of the message
         * <P>Type: TEXT</P>
         */
        String BODY = "body";


        /**
         * The service center (SC) through which to send the message, if present
         * <P>Type: TEXT</P>
         */
        String SERVICE_CENTER = "service_center";

        /**
         * Has the message been locked?
         * <P>Type: INTEGER (boolean)</P>
         */
        String LOCKED = "locked";

        /**
         * Error code associated with sending or receiving this message
         * <P>Type: INTEGER</P>
         */
        String ERROR_CODE = "error_code";

        /** Type of message */
        String TYPE = "type";

        /** Message type: all messages. */
        int MESSAGE_TYPE_ALL    = 0;

        /** Message type: inbox. */
        int MESSAGE_TYPE_INBOX  = 1;

        /** Message type: sent messages. */
        int MESSAGE_TYPE_SENT   = 2;

        /** Message type: drafts. */
        int MESSAGE_TYPE_DRAFT  = 3;

        /** Message type: outbox. */
        int MESSAGE_TYPE_OUTBOX = 4;

        /** Message type: failed outgoing message. */
        int MESSAGE_TYPE_FAILED = 5;

        /** Message type: queued to send later. */
        int MESSAGE_TYPE_QUEUED = 6;


    }

    String NOTIFICATION_TYPE = "notification_type";

    String PREF_THREAD_TRANSLATE = "PREF_THREAD_TRANSLATE";


//    /**
//     * The date the message was received.
//     * <P>Type: INTEGER (long)</P>
//     */java.lang.String
//    public static final String DATE = "date";
//
//    /**
//     * The date the message was sent.
//     * <P>Type: INTEGER (long)</P>
//     */
//    public static final String DATE_SENT = "date_sent";
//
//
//
//    public static final String ADDRESS = "address";
//
//
//    /**
//     * The ID of the sender of the conversation, if present.
//     * <P>Type: INTEGER (reference to item in {@code content://contacts/people})</P>
//     */
//    public static final String PERSON = "person";
//
//    /**
//     * Has the message been seen by the user? The "seen" flag determines
//     * whether we need to show a new message notification.
//     * <P>Type: INTEGER (boolean)</P>
//     */
//    public static final String SEEN = "seen";
//
//
//    /**
//     * The thread ID of the message.
//     * <P>Type: INTEGER</P>
//     */
//    public static final String THREAD_ID = "thread_id";
//
//    /**
//     * The {@code read-status} of the message.
//     * <P>Type: INTEGER</P>
//     */
//    public static final String READ_STATUS = "read_status";
//
//    /**
//     * The body of the message.
//     * <P>Type: TEXT</P>
//     */
//    public static final String BODY = "body";
//
//    /**
//     * {@code TP-Status} value for the message, or -1 if no status has been received.
//     * <P>Type: INTEGER</P>
//     */
//    public static final String STATUS = "status";
//
//
//    /**
//     * Indicates whether all messages of the thread have been read.
//     * <P>Type: INTEGER</P>
//     */
//    public static final String READ = "read";
//
//    /** TP-Status: no status received. */
//    public static final int STATUS_NONE = -1;
//    /** TP-Status: complete. */
//    public static final int STATUS_COMPLETE = 0;
//    /** TP-Status: pending. */
//    public static final int STATUS_PENDING = 32;
//    /** TP-Status: failed. */
//    public static final int STATUS_FAILED = 64;


}
