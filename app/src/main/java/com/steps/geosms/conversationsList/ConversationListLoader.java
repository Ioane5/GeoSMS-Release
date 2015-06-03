package com.steps.geosms.conversationsList;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.Conversation;
import com.steps.geosms.utils.Constants;

import java.util.ArrayList;

/**
 * Class for loading conversations...
 *
 * Created by Ioane on 5/2/15.
 */
public class ConversationListLoader extends AsyncTaskLoader<ArrayList<Conversation>>{

    private ArrayList<Conversation> mConversations;

    private static final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");


    private ConversationsContentObserver mConversationObserver;
    private LongSparseArray<ArrayList<Contact>> mContactCache;


    public ConversationListLoader(Context context){
        super(context);
    }


    @Override
    public ArrayList<Conversation> loadInBackground() {

        ArrayList<Conversation> conversations = new ArrayList<>();
        if(mContactCache == null)
            mContactCache = new LongSparseArray<>();

        Cursor cursor = getContext().getContentResolver().query(uri, null, null,
                null, "date desc");
        if (cursor != null) {
            cursor.getCount();
            while(cursor.moveToNext()){
                int numMsg = cursor.getInt(cursor.getColumnIndex(Constants.MSG_COUNT));
                if(numMsg <= 0)
                    continue; // we don't need empty conversations

                Conversation conversation = new Conversation(getContext(),cursor,mContactCache);
                conversations.add(conversation);

            }
            cursor.close();

        }
        return conversations;
    }

    @Override
    public void deliverResult(ArrayList<Conversation> conversations) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (mConversations != null) {
                onReleaseResources(mConversations);
            }
        }

        ArrayList<Conversation> oldConversations = mConversations;
        mConversations = conversations;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(conversations);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldConversations != null) {
            onReleaseResources(oldConversations);
        }

    }



    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mConversations != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mConversations);
        }

        if (mConversationObserver == null) {
            mConversationObserver = new ConversationsContentObserver(this);
        }

        if (takeContentChanged() || mConversations == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(ArrayList<Conversation> conversations) {
        super.onCanceled(conversations);

        // At this point we can release the resources associated with 'conversations'
        // if needed.
        onReleaseResources(conversations);
    }


    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mConversations != null) {
            onReleaseResources(mConversations);
            mConversations = null;
        }

        if (mConversationObserver != null) {
            mConversationObserver = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    @SuppressWarnings("unused")
    protected void onReleaseResources(ArrayList<Conversation> conversations) {

        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    /**
     * Class for getting sms-mms database change.
     *
     * Created by Ioane on 3/12/2015.
     */
    public static class ConversationsContentObserver extends ContentObserver {

        private AsyncTaskLoader<ArrayList<Conversation>> conversationLoader;


        public ConversationsContentObserver(AsyncTaskLoader<ArrayList<Conversation>> conversationLoader) {
            super(null);
            this.conversationLoader = conversationLoader;
            final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
            conversationLoader.getContext().getContentResolver().registerContentObserver(uri, true, this);
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.i("onChange","changed");
            conversationLoader.onContentChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.i("onChange","changed " + uri);
            super.onChange(selfChange, uri);
        }
    }



}
