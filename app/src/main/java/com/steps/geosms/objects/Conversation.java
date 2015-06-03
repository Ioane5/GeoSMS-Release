package com.steps.geosms.objects;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.Log;

import com.steps.geosms.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;


/**
 * Class Conversation.
 *
 * Created by Ioane on 2/21/2015.
 */
public class Conversation implements Serializable{

    private static final String TAG = Conversation.class.getSimpleName();

    private static final String RECIPIENT_IDS = Constants.RECIPIENT_IDS;

    private static final String THREAD_ID = Constants.ID;

    private static final String LAST_MESSAGE = Constants.CONVERSATION_LAST_MESSAGE;

    private static final String DATE = Constants.CONVERSATION_DATE;

    private static final String IS_READ = Constants.CONVERSATION_READ;


    /**
     * Contacts associated with conversation.
     */
    private ArrayList<Contact> contacts;

    /**
     * Recipient Ids.
     *
     * One conversation may have many contacts within it.
     */
    private String recipientIds;


    /**
     * This int represents conversation, (same as thread)
     * id
     */
    private long id;

    /**
     *  Last Message
     */
    private String lastMessage;

    /**
     * Conversation date
     */
    private Date date;

    private  boolean messageRead;


    /**
     * This Constructor creates new Conversation object
     * from Cursor.
     *
     * Queries database for resolving Contact , if resolveContact is true.
     *
     * @param context context to resolve data. Null if resolveContacts is false.
     * @param cursor cursor to get data from.
     * @param resolveContact true if we need to resolve contacts
     */
    public Conversation(Context context,Cursor cursor,boolean resolveContact){
        this.recipientIds = cursor.getString(cursor.getColumnIndex(RECIPIENT_IDS));
        this.lastMessage = cursor.getString(cursor.getColumnIndex(LAST_MESSAGE));
        this.date = new Date(cursor.getLong(cursor.getColumnIndex(DATE)));
        this.messageRead = cursor.getInt(cursor.getColumnIndex(IS_READ)) == 1;
        this.id = cursor.getInt(cursor.getColumnIndex(THREAD_ID));

        if(resolveContact){
            this.contacts = resolveContacts(context,recipientIds);
        }
    }

    /**
     * This constructor is for working with cache.
     *
     * If contact was in cache we won't resolve contact
     * but will get directly from cache.
     *
     * @param context Context in order to use resolve some contact parts. (Like contact)
     * @param cursor cursor to get data from.
     * @param contactCache so save or retrieve contact without querying...
     */
    public Conversation(Context context,Cursor cursor,LongSparseArray<ArrayList<Contact>> contactCache){
        this.recipientIds = cursor.getString(cursor.getColumnIndex(RECIPIENT_IDS));
        this.lastMessage = cursor.getString(cursor.getColumnIndex(LAST_MESSAGE));
        this.date = new Date(cursor.getLong(cursor.getColumnIndex(DATE)));
        this.messageRead = cursor.getInt(cursor.getColumnIndex(IS_READ)) == 1;
        this.id = cursor.getLong(cursor.getColumnIndex(THREAD_ID));

        ArrayList<Contact> cached= contactCache.get(id,null);

        if(cached == null){
            this.contacts = resolveContacts(context,recipientIds);
            // save in cache
            contactCache.append(id,contacts);
        }else{
            this.contacts = cached;
        }

    }


    /**
     * This method resolves contacts from canonical-address
     * by recipient IDs.
     * @param recipientIds id to get contacts
     * @return arrayList
     */
    private ArrayList<Contact> resolveContacts(Context context , String recipientIds){
        ArrayList<Contact> resolved =  new ArrayList<>(1); // for most case we don't have group chat.
        ContentResolver cr = context.getContentResolver();

        // iterate through the recipients...
        // we may have many Group chat.
        if(TextUtils.isEmpty(recipientIds))
            return null;
        StringTokenizer tokenizer = new StringTokenizer(recipientIds," ");
        while(tokenizer.hasMoreTokens()){
            try{
                int rec_id = Integer.parseInt(tokenizer.nextToken());
                Cursor c = cr.query(ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), rec_id),
                null, null, null, null);

                if(c.moveToFirst()){
                    String address = c.getString(0);
                    if(address != null){
                        Contact contact = new Contact(context,address);
                        resolved.add(contact);
                    }
                }

                c.close();
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return resolved;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    public Contact getContact() {
        return contacts == null || contacts.isEmpty() ? null : contacts.get(0);
    }

    public void setContact(Contact contact) {
        if(contacts == null) contacts = new ArrayList<>();
        contacts.add(contact);
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(String recipientIds) {
        this.recipientIds = recipientIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conversation that = (Conversation) o;

        return TextUtils.equals(recipientIds,that.recipientIds);
    }

    @Override
    public int hashCode() {
        return (int)id;
    }


    private static final Uri THREAD_ID_CONTENT_URI = Uri.parse(
            "content://mms-sms/threadID");


    /**
     * {@inheritDoc getOrCreateThreadId}
     * @param context
     * @param address
     * @return
     */
    public static long getOrCreateThreadId(Context context, String address){
        ArrayList<String> list = new ArrayList<>(1);
        list.add(address);
        return getOrCreateThreadId(context, list);
    }

    /**
     * Searches thread_id for these recipients.
     * If thread didn't exist. It creates new and returns.
     *
     * @param context to lookup db.
     * @param recipients recipients in conversation.
     * @return Created or found thread_id
     */
    public static long getOrCreateThreadId(
            Context context, Collection<String> recipients) {
        Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();

        for (String recipient : recipients) {
            uriBuilder.appendQueryParameter("recipient", recipient);
        }

        Uri uri = uriBuilder.build();

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);
                } else {
                    Log.e(TAG, "getOrCreateThreadId returned no rows!");
                }
            } finally {
                cursor.close();
            }
        }

        Log.e(TAG, "getOrCreateThreadId failed with uri " + uri.toString());
        throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
    }

}
