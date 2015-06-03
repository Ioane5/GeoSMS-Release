package com.steps.geosms.objects;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class Contact
 * Created by Ioane on 2/21/2015.
 */
public class Contact implements Serializable{

    private int id;

    private String name;

    private String photoUri;

    private String address;
    /**
     * We don't want to serialize it. :) because we can't serialize it.
      */
    private transient Bitmap photo;


    private static final String TAG = Contact.class.getSimpleName();

    public Contact(String name, String photoUri,String address,Bitmap photo) {
        this.name = name;
        this.photoUri = photoUri;
        this.address = address;
        this.photo = photo;
    }


    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PHOTO = "photo";
    public static final String PHOTO_URI = "photo_uri";
    public static final String ADDRESS = "address";

    public Contact(Bundle contactData){
        this.id = contactData.getInt(ID);
        this.name = contactData.getString(NAME);
        this.photoUri =  contactData.getString(PHOTO_URI);
        this.address = contactData.getString(ADDRESS);
        try{
            this.photo = contactData.getParcelable(PHOTO);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    public Contact(Cursor c){
        Log.i(TAG, Arrays.asList(c.getColumnNames()).toString());
    }

    public Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putInt(ID,id);
        bundle.putString(NAME, name);
        bundle.putString(PHOTO_URI,photoUri);
        bundle.putString(ADDRESS,address);
        bundle.putParcelable(PHOTO, photo);
        return bundle;
    }

    private void initFromAddress(Context context,String address){
        setAddress(address);
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        Cursor c = context.getContentResolver().query(lookupUri, null, null, null, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            String displayName = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String photoURI = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            int id = c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

            setId(id);
            setName(displayName);
            setPhotoUri(photoURI);

            //setPhoto(getPhotoFromURI(photoURI,context));
        }
        c.close();
    }

    public Contact(Context context,String address){
        if(address != null)
            address = Utils.removeWhitespaces(address);
        initFromAddress(context, address);
    }


    public Contact(Context context,int recipientId){
        setAddress(null);
        setName(null);
        setPhotoUri(null);

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId),
                null, null, null, null);
        if(c.moveToFirst()){
            String address = c.getString(0);
            if(address != null)
                initFromAddress(context,address);
        }

        c.close();

    }


    public String getDisplayName(){
        if(!TextUtils.isEmpty(name))
            return name;
        return address;
    }

    public void resolveContactImage(Context ctx, int size){
        if(!TextUtils.isEmpty(photoUri)){
            this.photo = Utils.getCircleBitmap(Utils.getPhotoFromURI(photoUri, ctx, size));
        }else if(!TextUtils.isEmpty(getDisplayName())){
            this.photo = Utils.createTextBitmap(getDisplayName(),size,ctx);
        }else{
            this.photo = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ic_no_image);
        }

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;

        return id == contact.id && !(address != null && !address.equals(contact.getAddress()));
    }


    /**
     * Contains function that compares contacts
     * by it's  address.
     *
     * @param contactList The list where we search this contact.
     * @param address   contact to find in list.
     * @return true if found, false if not.
     */
    public static boolean containsByAddress(List<Contact> contactList,String address){
        for(Contact cmp : contactList){
            if(TextUtils.equals(cmp.getAddress(),address))
                return true;
        }
        return false;
    }


    /**
     * Returns Address list from contact list.
     *
     * @param contactList contact list
     * @return address array list
     */
    public static List<String> toAddressArray(ArrayList<Contact> contactList){
        ArrayList<String> address = new ArrayList<>(contactList.size());
        for(Contact contact : contactList){
            address.add(contact.getAddress());
        }
        return address;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id= " + id +
                ", name='" + name + '\'' +
                ", photoUri='" + photoUri + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}
