package com.steps.geosms.newConversation;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.conversation.ConversationActivity;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.utils.Constants;
import com.steps.geosms.utils.Utils;

import java.util.ArrayList;



public class NewConversationActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener,AdapterView.OnItemClickListener {


    @SuppressWarnings("unused")
    private static final String TAG = NewConversationActivity.class.getSimpleName();

    private ContactsCursorAdapter contactsAdapter;
    private ChosenContactsAdapter chosenContactsAdapter;

    private ArrayList<Contact> chosenContacts;
    private GridView chosenContactsGV;

    private View unknownNumber;

    private static final int CONTACT_PICKER_RESULT = 10232;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversaton);

        if(getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);

        contactsAdapter = new ContactsCursorAdapter(this, null, 0);
        chosenContacts = new ArrayList<>();

        ListView contactsLV = (ListView)findViewById(R.id.contacts_list);
        contactsLV.setAdapter(contactsAdapter);
        contactsLV.setOnItemClickListener(this);
        contactsAdapter.setSelectedContacts(chosenContacts);

        unknownNumber = findViewById(R.id.unknown_contact);

        unknownNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unknownNumber.setVisibility(View.GONE);
                TextView textView = (TextView) unknownNumber.findViewById(R.id.unknown_contact_num);
                if (TextUtils.isEmpty(textView.getText()))
                    return;
                String phoneNumber = textView.getText().toString();

                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                Cursor c = getContentResolver().query(uri, null, null, null, null);
                if (c == null) return;
                Contact contact;
                if (c.moveToFirst()) {
                    String num = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.NUMBER));
                    String name = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    String photoUri = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI));
                    contact = new Contact(name, photoUri, num, null);

                } else {
                    contact = new Contact(null, null, phoneNumber, null);
                }
                contact.resolveContactImage(NewConversationActivity.this,40);

                if (!Contact.containsByAddress(chosenContacts, contact.getAddress()))
                    selectContact(contact);
                c.close();
            }
        });
        new GridView(this){
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };

        chosenContactsGV = (GridView)findViewById(R.id.gridView);

        chosenContactsAdapter = new ChosenContactsAdapter(getBaseContext(),R.layout.contact_bubble);
        chosenContactsGV.setAdapter(chosenContactsAdapter);
        chosenContactsGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = chosenContactsAdapter.getItem(position);
                createContactRemoveDialog(contact).show();
            }
        });
       // chosenContactsGV.setEmptyView(findViewById(R.id.empty_chosen_contact_view));


        final EditText searchView = (EditText)findViewById(R.id.search);
        final Button removeTextButton = (Button)findViewById(R.id.remove_text_button);
        final ToggleButton changeEdiTextMode = (ToggleButton)findViewById(R.id.change_enter_mode);

        changeEdiTextMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean keyboard_mode) {
                if (!TextUtils.isEmpty(searchView.getText()))
                    searchView.setText("");
                if (keyboard_mode) {
                    searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                } else
                    searchView.setInputType(InputType.TYPE_CLASS_PHONE);
            }
        });
        removeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(searchView.getText()))
                    searchView.setText("");
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) return;
                unknownNumber.setVisibility(View.GONE);
                String str = s.toString();
                if (TextUtils.isEmpty(str)) {
                    removeTextButton.setVisibility(View.GONE);
                    changeEdiTextMode.setVisibility(View.VISIBLE);
                } else {
                    removeTextButton.setVisibility(View.VISIBLE);
                    changeEdiTextMode.setVisibility(View.GONE);
                }
                filter(str);
            }
        });

        ImageButton contactPicker = (ImageButton) findViewById(R.id.get_contact);
        contactPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, CONTACT_PICKER_RESULT);
            }
        });

        getLoaderManager().initLoader(0,null,NewConversationActivity.this).forceLoad();
    }

    private Contact getContactFromProjection(Cursor c){
        String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String photoUri = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

        Contact contact = new Contact(name,photoUri,phoneNumber,null);
        contact.resolveContactImage(getBaseContext(),40);

        return contact;
    }
    /*
     * Get contact picker result and add to selected
     * contacts if contact was selected.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    Uri pickedContact = data.getData();
                    if(pickedContact == null)
                        return;
                    // query contact in database...
                    Cursor c = getContentResolver().query(pickedContact, Constants.contacts_projection,null,null,null);
                    if(c == null)
                        return;
                    if(c.moveToFirst()){
                        Contact contact = getContactFromProjection(c);
                        if(!Contact.containsByAddress(chosenContacts, contact.getAddress()))
                            selectContact(contact);
                    }

                    c.close();
                    break;
            }

        } else {
            Log.w(TAG, "Contact picker failed!");
        }
    }

    private Dialog createContactRemoveDialog(final Contact contact){
        String message = contact.getName() == null ? contact.getAddress() :
                String.format("%s (%s)",contact.getName(),contact.getAddress());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        removeContact(contact);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                })
                .setTitle(R.string.remove_request)
                .setMessage(message);
        return  builder.create();
    }

    static String lastQuery = "";

    boolean filter(String s){
        if(lastQuery.equals(s))
            return false;
        lastQuery = s;

        Bundle bundle = new Bundle();
        bundle.putString("constraint", s);
        getLoaderManager().restartLoader(0, bundle, NewConversationActivity.this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
       return filter(s);
    }
    @Override
    public boolean onQueryTextChange(String s) {
        return filter(s);
    }


    private MenuItem startConversationIcon;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_conversations, menu);

        startConversationIcon = menu.findItem(R.id.start_conversation);
        startConversationIcon.setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        startConversationIcon = menu.findItem(R.id.start_conversation);
        if(startConversationIcon == null)
            return false;
        if(chosenContacts != null && chosenContacts.size() > 0)
            startConversationIcon.setVisible(true);
        else
            startConversationIcon.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.start_conversation:
                if(chosenContacts.size() <= 0)
                    return true;
                // TODO change this to multi grup chat...
                Intent intent = new Intent(NewConversationActivity.this, ConversationActivity.class);
                intent.putExtra(Constants.CONTACT_DATA, chosenContacts);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String constraint = args == null ? null : args.getString("constraint");
        if(constraint != null){
            Uri uri;
            if (TextUtils.isEmpty(constraint))
                uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            else
                uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, constraint);

            if(PhoneNumberUtils.isWellFormedSmsAddress(constraint)){
                TextView numView = (TextView)unknownNumber.findViewById(R.id.unknown_contact_num);
                numView.setText(constraint);
                unknownNumber.setVisibility(View.VISIBLE);
            }

            return new CursorLoader(getBaseContext(),
                    uri, Constants.contacts_projection, null, null,
                    ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
        }else{
            return new CursorLoader(getBaseContext(),
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    Constants.contacts_projection,null,null,
                    ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        contactsAdapter.swapCursor(data);
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        contactsAdapter.swapCursor(null);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("contacts_list", chosenContacts);
    }

    @SuppressWarnings("unchecked cast")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        chosenContacts = (ArrayList<Contact>)savedInstanceState.getSerializable("contacts_list");

        contactsAdapter.setSelectedContacts(chosenContacts);
        chosenContactsAdapter.addAll(chosenContacts);
    }

    private void selectContact(Contact contact){
        chosenContacts.add(contact);
        chosenContactsAdapter.add(contact);

        chosenContactsAdapter.notifyDataSetChanged();
        contactsAdapter.notifyDataSetChanged();
        chosenContactsGV.smoothScrollToPosition(chosenContacts.size() - 1);
        if(!startConversationIcon.isVisible())
            startConversationIcon.setVisible(true);
    }

    private void removeContact(Contact contact){
        chosenContacts.remove(contact);
        chosenContactsAdapter.remove(contact);

        chosenContactsAdapter.notifyDataSetChanged();
        contactsAdapter.notifyDataSetChanged();
        chosenContactsGV.smoothScrollToPosition(chosenContacts.size()-1);
        if(chosenContacts.isEmpty() && startConversationIcon.isVisible())
            startConversationIcon.setVisible(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) contactsAdapter.getItem(position);
        contact.resolveContactImage(NewConversationActivity.this,40);
        if(!Contact.containsByAddress(chosenContacts, contact.getAddress())){
            selectContact(contact);
        }else {
            removeContact(contact);
        }
    }

    public static void expand(final View v) {
        v.measure(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }



}
