package com.steps.geosms.newConversation;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.utils.AsyncImageDownloader;

import java.util.ArrayList;

/**
 * Class ContactsCursorAdapter
 * Custom cursor adapter for contact item binding.
 *
 * Created by Ioane on 4/14/2015.
 */
public class ContactsCursorAdapter extends CursorAdapter implements SectionIndexer{

    @SuppressWarnings("unused")
    private static final String TAG = ContactsCursorAdapter.class.getSimpleName();

    private AlphabetIndexer indexer;

    private Drawable SELECTED_CONTACT_IMAGE;

    private ArrayList<Contact> mSelectedContacts;

    private AsyncImageDownloader mImageDownloader;

    public ContactsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        SELECTED_CONTACT_IMAGE = context.getResources().getDrawable(R.drawable.selected_contact_image);

        indexer = new AlphabetIndexer(null, 0 , " ABCDEFGHIJKLMNOPQRTSUVWXYZ0123456789");
        mSelectedContacts = null;

        mImageDownloader = new AsyncImageDownloader(context, 60);
    }

    public void setSelectedContacts(ArrayList<Contact> selectedContacts){
        mSelectedContacts = selectedContacts;
    }

    @SuppressWarnings("unused")
    public void addSelectedContact(Contact contact){
        if(mSelectedContacts != null)
            mSelectedContacts.add(contact);
    }

    @SuppressWarnings("unused")
    public void removeSelectedContact(Contact contact){
        if(mSelectedContacts != null)
            mSelectedContacts.remove(contact);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null? "no contact," :
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))+
                        " "+ cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }


    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if(newCursor != null && !newCursor.isClosed()){

            indexer = new AlphabetIndexer(newCursor,
                    newCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    " ABCDEFGHIJKLMNOPQRTSUVWXYZ0123456789");
        }else{
            indexer.setCursor(newCursor);
        }
        return super.swapCursor(newCursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
    }

    @Override
    public Object[] getSections() {
        if(indexer == null) return new String[] {" "};
        return indexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if(indexer == null) return -1;
        return indexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
       if(indexer == null) return -1;
       return indexer.getSectionForPosition(position);
    }

    private boolean isSelected(String address){
        if(mSelectedContacts != null){
            for(int i=0;i<mSelectedContacts.size();i++){
                Contact cmp = mSelectedContacts.get(i);
                if(TextUtils.equals(address,cmp.getAddress()))
                    return true;
            }
        }
        return false;
    }

    private static class ViewHolder{
        TextView nameView;
        TextView phoneKindView;
        TextView phoneNumberView;
        ImageView contactPhotoView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder)view.getTag();

        if (holder == null) {
            holder = new ViewHolder();
            holder.nameView = (TextView)view.findViewById(R.id.contact_name);
            holder.phoneKindView = (TextView)view.findViewById(R.id.phone_kind_text_view);
            holder.phoneNumberView = (TextView)view.findViewById(R.id.phone_number_text_view);
            holder.contactPhotoView = (ImageView)view.findViewById(R.id.contact_picture_image_view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        int type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
        CharSequence phoneType = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, null);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

        holder.nameView.setText(name);
        holder.phoneKindView.setText(phoneType);
        holder.phoneNumberView.setText(phoneNumber);

        //holder.contactPhotoView.setTag(cursor.getPosition());
        holder.contactPhotoView.setTag(photoUri);

        if(isSelected(phoneNumber)){
            holder.contactPhotoView.setImageDrawable(SELECTED_CONTACT_IMAGE);
        }else{
            Contact contact = new Contact(name,photoUri,phoneNumber,null);

            holder.contactPhotoView.setImageBitmap(null);
            mImageDownloader.addImage(contact,holder.contactPhotoView);
        }
    }

    @Override
    public Object getItem(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
        return new Contact(name,photoUri,phoneNumber,null);
    }

}
