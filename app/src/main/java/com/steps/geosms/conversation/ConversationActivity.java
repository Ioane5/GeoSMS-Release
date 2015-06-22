package com.steps.geosms.conversation;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.GeoSmsManager;
import com.steps.geosms.MyPreferencesManager;
import com.steps.geosms.notifications.SmsManagerService;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.Conversation;
import com.steps.geosms.objects.SMS;
import com.steps.geosms.utils.Constants;
import com.steps.geosms.utils.MyActivity;
import com.steps.geosms.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConversationActivity extends MyActivity implements LoaderManager.LoaderCallbacks<Cursor> ,TextWatcher{

    private static final String TAG = ConversationActivity.class.getSimpleName();

    private ConversationCursorAdapter adapter;
    private ToggleButton webUseToggle;

    /** This is our conversation id, this defines in which conversation we are.*/
    private static long thread_id;

    /** Recipients of the conversation */
    private ArrayList<Contact> contacts;

    private static boolean isKeyboardVisible = false;
    private GeoSmsManager smsManager;
    private BroadcastReceiver mNetworkChangeListener;

    /** If user changed toggle, we set as true */
    private boolean userChangedWebToggle = false;
    private ImageButton button;
    private TextView symbolCounter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        smsManager = new GeoSmsManager(getBaseContext());
        webUseToggle = (ToggleButton)findViewById(R.id.use_web_toggle_button);
        button = (ImageButton)findViewById(R.id.send_button);
        button.setEnabled(false);

        final EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        symbolCounter = (TextView)findViewById(R.id.symbol_counter);
        editText.addTextChangedListener(this);

        initConversation();
        // if user entered here without contact we disable
        // everything.
        if(contacts == null){
            editText.setEnabled(false);
            editText.setFocusable(false);
            return;
        }

        button.setOnClickListener(new SendButtonListener());

        setTitle(Utils.getChatHeader(contacts));

        adapter = new ConversationCursorAdapter(getBaseContext(),null,true,contacts.get(0));
        listView = (ListView)findViewById(R.id.conversation_list_view);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                    if(isKeyboardVisible)
                        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
                return false;
            }
        });

        if(!Utils.isDefaultSmsApp(this)){
            editText.setFocusable(false);
            editText.setHint(R.string.set_default_app_to_send);
            webUseToggle.setEnabled(false);
            button.setEnabled(false);
        }else {
            SharedPreferences prefs = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
            String text = prefs.getString(Long.toString(thread_id),null);
            if(!TextUtils.isEmpty(text)) {
                button.setEnabled(true);
                editText.setText(text);
            }
            boolean isChecked = prefs.getBoolean(Constants.TOGGLE_CHECKED,false);
            webUseToggle.setChecked(isChecked);
            webUseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userChangedWebToggle = !userChangedWebToggle;
                }
            });
        }
        mNetworkChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if(isConnected){
                    webUseToggle.setEnabled(true);
                }else
                    webUseToggle.setEnabled(false);
            }
        };
        initCab();
        initKeyboardListener();
        startLoader();
    }

    /*
    * Initializes context action bar.
    */
    private void initCab(){
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle(String.format("%d", listView.getCheckedItemCount()));
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.conversation_cab, menu);
                return true;
            }


            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (listView.getCheckedItemCount() == 1) {
                    // single choice
                    MenuItem item = menu.findItem(R.id.action_details);
                    item.setVisible(true);
                    return true;
                } else {
                    // multi choice.
                    MenuItem item = menu.findItem(R.id.action_details);
                    item.setVisible(false);
                    return true;
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
                final List<SMS> smsList = new ArrayList<>(checkedItemPositions.size());

                Log.i(TAG, "eeh " + checkedItemPositions.size());
                for (int i = 0; i < checkedItemPositions.size(); i++) {
                    int pos = checkedItemPositions.keyAt(i);
                    if (checkedItemPositions.get(pos)) {
                        SMS sms = new SMS((Cursor) adapter.getItem(pos));
                        smsList.add(sms);
                    } else {
                        Log.i(TAG, "WTF");
                    }
                }

                if (smsList.isEmpty())
                    return false;

                switch (item.getItemId()) {
                    case R.id.action_copy:
                        ClipboardManager clipboardManager = (ClipboardManager)
                                getSystemService(CLIPBOARD_SERVICE);

                        StringBuilder strBuilder = new StringBuilder();
                        for (SMS sms : smsList) {
                            strBuilder.append(sms.getText());
                        }
                        ClipData clip = ClipData.newPlainText("sms", strBuilder.toString());
                        clipboardManager.setPrimaryClip(clip);
                        break;
                    case R.id.action_send_as_new:
                        for (SMS sms : smsList) {
                            sms.setDate(new Date());
                            sendMessage(sms);
                        }
                        break;
                    case R.id.action_details:
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConversationActivity.this);
                        alertBuilder.setTitle(R.string.info)
                                .setMessage(smsList.get(0).textReference());
                        alertBuilder.setCancelable(true);
                        alertBuilder.show();
                        break;
                    case R.id.action_delete:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.deleteSmsList(getBaseContext(), smsList);
                            }
                        }).start();
                        break;
                    default:
                        return false;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }
    /**
     * This method inits loader when thread_id is resolved.
     */
    private void startLoader(){
        // if we needn't thread_id resolve we don't create new thread.
        if(thread_id != Constants.THREAD_NONE){
            SmsManagerService.updateThreadId(thread_id);
            getLoaderManager().initLoader(0, null, this);
            return;
        }
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                // this part resolves thread id.
                thread_id = Conversation.getOrCreateThreadId(getBaseContext(),
                        Contact.toAddressArray(contacts));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                SmsManagerService.updateThreadId(thread_id);
                getLoaderManager().initLoader(0, null, ConversationActivity.this);
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNetworkChangeListener != null)
            registerReceiver(mNetworkChangeListener, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        if(!MyPreferencesManager.isWebSmsEnabled(getBaseContext())){
            webUseToggle.setVisibility(View.GONE);
            supportInvalidateOptionsMenu();
        }else {
            webUseToggle.setVisibility(View.VISIBLE);
        }
        if(contacts == null){
            Log.w(TAG, "contact is null");
            return;
        }
        SmsManagerService.updateThreadId(thread_id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SmsManagerService.dismissThreadNotifications(ConversationActivity.this,thread_id);
                markConversationAsRead();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNetworkChangeListener != null)
            unregisterReceiver(mNetworkChangeListener);
        SmsManagerService.updateThreadId(SmsManagerService.THREAD_ID_NONE);
    }

    public void resend(View view){
        final SMS sms = (SMS)view.getTag();
        if(sms == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContentResolver().delete(Constants.URIS.SMS, "_id=?",
                        new String[]{"" + sms.getId()});
            }
        }).start();
        sendMessage(sms);
    }

    /**
     * This method initializes conversation.
     *
     * This means it resolves in which thread we are.
     * Which contacts are recipients.
     */
    @SuppressWarnings("unchecked cast")
    private void initConversation(){
        try{
            Bundle extras = getIntent().getExtras();

            if(extras != null){
                contacts = (ArrayList<Contact>)extras.getSerializable(Constants.CONTACT_DATA);
                if(contacts != null)
                    for(Contact c: contacts)
                        c.resolveContactImage(getBaseContext(),80);

                Long passed_tid = extras.getLong(Constants.THREAD_ID, -1);
                if(passed_tid == -1 &&  contacts != null){
                    thread_id = Conversation.getOrCreateThreadId(getBaseContext(),
                            Contact.toAddressArray(contacts));
                }else
                    thread_id = passed_tid;

            }else{
                Uri uri = getIntent().getData();
                if(uri == null) {
                    Log.w(TAG,"Contact data not provided!");
                    return;
                }
                String scheme = uri.getScheme();
                String schemePart = uri.getSchemeSpecificPart();
                if(scheme == null || schemePart == null) return;
                if(!scheme.contains("sms") && !scheme.contains("smsto")) return;

                contacts = new ArrayList<>(1);
                Contact contact = new Contact(getBaseContext(),schemePart);
                contact.resolveContactImage(getBaseContext(),80);
                contacts.add(contact);
                thread_id = Conversation.getOrCreateThreadId(getBaseContext(),
                        Contact.toAddressArray(contacts));
            }
            if(contacts == null)
                return;
            for(Contact c : contacts){
                if(c.getPhotoUri() != null)
                    c.resolveContactImage(getBaseContext(),50);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void markConversationAsRead(){
        if(thread_id == Constants.THREAD_NONE)
            return;
        new AsyncTask<Long,Void,Void>(){

            @Override
            protected Void doInBackground(Long... params) {
                long threadId = params[0];
                ContentValues cv =  new ContentValues();
                cv.put(Constants.MESSAGE.READ,1);
                getContentResolver().update(Uri.parse("content://sms"),cv , ("thread_id = " + threadId), null);
                return null;
            }
        }.execute(thread_id);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(contacts == null) return;
        EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
        if(!editText.isFocusable()) return; // not save when it's not default
        SharedPreferences drafts = getSharedPreferences(Constants.DRAFTS_FILE,MODE_PRIVATE);
        SharedPreferences.Editor editor = drafts.edit();
        /* if user changed web use toggle , let's save it ,
          to not make user choose on every conversation start...*/
        if(userChangedWebToggle){
            editor.putBoolean(Constants.TOGGLE_CHECKED,webUseToggle.isChecked());
        }
        if(editText.getText() == null || editText.getText().toString().equals("")){
            editor.remove(Long.toString(thread_id));
        }else {
            editor.putString(Long.toString(thread_id), editText.getText().toString());
        }
        editor.apply();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://sms/");
        return new CursorLoader(getBaseContext(),uri,null,"thread_id = ?" ,
                new String[]{Long.toString(thread_id)},"date asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }




    private boolean isSendWeb(){
        return webUseToggle.isChecked()
                && webUseToggle.getVisibility() == View.VISIBLE
                && webUseToggle.isEnabled();
    }

    private void sendMessage(SMS sms){
        for(String address : Contact.toAddressArray(contacts))
            smsManager.sendSms(sms, address ,thread_id, isSendWeb());
    }

    /**
     *
     *    @Override
    public void onClick(View v) {
    if(v == null)
    return;
    final ViewHolder holder = (ViewHolder)v.getTag();
    if(holder == null)
    return;
    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
    builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int id) {}
    })
    .setNegativeButton(R.string.resend, new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int id) {
    SMS sms = new SMS((Cursor) getItem(holder.pos));

    }
    })
    .setTitle(R.string.resend_request)
    .setMessage(R.string.resend_request_body);
    }
     */

    /**
     * Class for managing clicked send button.
     */
    private class SendButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            EditText editText = (EditText)findViewById(R.id.enter_message_edit_text);
            if(TextUtils.isEmpty(editText.getText())) return;

            String message = editText.getText().toString();
            // it's a draft
            if(contacts == null){
                SMS sms = new SMS(message,new Date(System.currentTimeMillis()), SMS.MsgType.DRAFT,true,false,null);
                smsManager.saveDraft(sms);
            }else{
                SMS sms = new SMS(message,new Date(System.currentTimeMillis()), SMS.MsgType.PENDING,true,false,null);
                sendMessage(sms);
            }
            if (editText.length() > 0) {
                editText.setText(null);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(listView != null){
            int pos = listView.getFirstVisiblePosition();
            outState.putInt("scroll_pos",pos);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int pos = savedInstanceState.getInt("scroll_pos");
        Log.i(TAG,"THIS SHIT IS  = " + pos);
        if(pos != 0)
            listView.smoothScrollToPositionFromTop(pos,0,0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private static final int MESSAGE_DEFAULT_SIZE = 160;

    private static final int MESSAGE_UNICODE_SIZE = 70;


    /**
     * StackOverflow community answer.
     */
    private void initKeyboardListener(){
        final View activityRootView = findViewById(R.id.conversation_activity);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                double ratio =  ((double)activityRootView.getHeight())/activityRootView.getRootView().getHeight();
                // if more than 100 pixels, its probably a keyboard...
                isKeyboardVisible = ratio < 0.75;
            }
        });
    }


    boolean isASCII(String str){
        for(int i=0;i<str.length();i++){
            if(str.charAt(i) >= 128)
                return false;
        }
        return true;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || s.length() == 0) {
            button.setEnabled(false);
        }else
            button.setEnabled(true);

        if(symbolCounter == null) return;
        if(s == null){
            symbolCounter.setText("");
            return;
        }

        int maxLen = MESSAGE_DEFAULT_SIZE;
        if(!isASCII(s.toString()))
            maxLen = MESSAGE_UNICODE_SIZE;

        if(s.length() > maxLen/2){
            symbolCounter.setVisibility(View.VISIBLE);
            String str;
            if(s.length()/maxLen == 0){
                str = String.format("%d/%d",s.length()%maxLen,maxLen);
            }else{
                str = String.format("%d/%d (%d)",s.length()%maxLen,maxLen,s.length()/maxLen);
            }
            symbolCounter.setText(str);
        }else{
            symbolCounter.setText("");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(contacts != null && contacts.size() > 1){
            MenuItem item = menu.findItem(R.id.action_call);
            item.setVisible(false);
        }
        if(!MyPreferencesManager.isWebSmsEnabled(getBaseContext())){
            MenuItem item = menu.findItem(R.id.action_show_balance);
            item.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(contacts == null || contacts.size() <= 0)
            return false;
        if(item.getItemId() ==  R.id.action_call){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", contacts.get(0).getAddress(), null));
            startActivity(intent);
            return true;
        }else if(item.getItemId() == R.id.people_in_this_conversation){
            Utils.buildContactsDialog(ConversationActivity.this,contacts).show();
            return true;
        }else if(item.getItemId() == R.id.action_show_balance){
            Utils.showBalanceDialog(this,smsManager);
        }
        return super.onOptionsItemSelected(item);
    }
}
