package com.steps.geosms.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.QuickContactBadge;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.conversation.ContactsArrayAdapter;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.Conversation;
import com.steps.geosms.objects.SMS;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Utils
 *
 * Created by Ioane on 2/25/2015.
 */
@SuppressLint("NewApi")
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    private static final String CONTACT_LIST_FILE = "file_contact_list";



    public static AppCompatDialog buildContactsDialog(final Context context, final ArrayList<Contact> contacts){
        AppCompatDialog dialog = new AppCompatDialog(context);
        dialog.setContentView(R.layout.activity_conversation_people);

        dialog.setTitle(R.string.people_in_this_conversation);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        final ContactsArrayAdapter contactsAdapter = new ContactsArrayAdapter(context,R.layout.contact_item_dialog);
        contactsAdapter.addAll(contacts);
        ListView listView = (ListView)dialog.findViewById(R.id.people_list);
        listView.setAdapter(contactsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = contacts.get(position);
                // show contact badge if person was clicked.
                if (contact.getId() > 0) {
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contact.getId()));
                    ContactsContract.QuickContact.showQuickContact(context,
                            ((Activity)context).findViewById(R.id.quick_contact), uri,
                            ContactsContract.QuickContact.MODE_MEDIUM, null);
                } else {
                    QuickContactBadge badge = (QuickContactBadge) ((Activity)context).findViewById(R.id.quick_contact);
                    badge.assignContactFromPhone(contact.getAddress(), true);
                    badge.setMode(ContactsContract.QuickContact.MODE_MEDIUM);
                    badge.performClick();
                }
            }
        });


        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        return dialog;
    }


    /**
     * Saves Conversation list asynchronously
     *
     * @param context necessary to save list.
     * @param conversations to cache conversation
     */
    public static void cacheConversations(Context context, ArrayList<Conversation> conversations){
        new Thread(new AsyncConversationCacher(context,conversations)).start();
    }


    private static class AsyncConversationCacher implements Runnable{

        private Context ctx;
        private ArrayList<Conversation> contactList;

        public AsyncConversationCacher(Context ctx , ArrayList<Conversation> contactList){
            this.ctx = ctx;
            this.contactList = contactList;
        }

        @Override
        public void run() {
            FileOutputStream fos;
            try {
                fos = ctx.openFileOutput(CONTACT_LIST_FILE, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(contactList);
                os.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked cast")
    public static ArrayList<Conversation> loadConversationsFromCache(Context context){
        FileInputStream fis;
        ArrayList<Conversation> obj = null;
        try {
            fis = context.openFileInput(CONTACT_LIST_FILE);
            ObjectInputStream is = new ObjectInputStream(fis);
            obj = (ArrayList<Conversation>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception e){
            e.printStackTrace();
            Log.w(TAG,"serialisation exception");
        }
        return obj;
    }
    public static Bitmap createTextBitmap(String txt,int size,Context context){
        if(txt.length() > 2){
            txt = txt.substring(0,2);
        }
        Bitmap output = Bitmap.createBitmap(size , size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int pixels = Constants.IMAGE_SIZE;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(context.getResources().getColor(R.color.themeAccent));

        Paint textPaint = new Paint();
        Rect textBounds = new Rect();
        textPaint.setTextSize((int)(size/2.5));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.getTextBounds(txt, 0, txt.length(), textBounds);
        textPaint.setColor(Color.WHITE);

        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        canvas.drawText(txt, canvas.getWidth()/2,
                ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2))
                , textPaint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap){
        // create as circle.
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int pixels = Constants.IMAGE_SIZE;
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;

    }

    public static boolean isDefaultSmsApp(Context context){
        final String myPackageName = context.getPackageName();

        if(Build.VERSION.SDK_INT >= Constants.KITKAT_API_LEVEL){
            if(!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName))
                return false;
        }
        return true;
    }


    public static String getOwnersImage(Context context){
        Cursor cursor = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI,
                new String[]{ContactsContract.Profile.PHOTO_URI}, null, null, null);

        if(cursor != null){
            String uri = null;
            if(cursor.moveToFirst()){
                uri = cursor.getString(0);
            }
            cursor.close();
            return uri;
        }

        return null;
    }

    public static Bitmap getPhotoFromURI(String photoURI,Context context,int size) {
        if(photoURI == null) return null;
        try {
            // get image from filesystem
            InputStream input = context.getContentResolver().openInputStream(Uri.parse(photoURI));

            Bitmap bitmap = BitmapFactory.decodeStream(input, null, null);
            Bitmap scaled =  Bitmap.createScaledBitmap(bitmap,size,size,true);

            bitmap.recycle();
            System.gc();
            return scaled;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String removeWhitespaces(String address) {
        return address.replaceAll("\\s+", "");
    }



    /**
     * Returns header from contacts.
     *
     * @param contacts list from which header is cereated.
     * @return header
     */
    public static String getChatHeader(ArrayList<Contact> contacts){
        StringBuilder header = new StringBuilder();
        for(int i=0;i<contacts.size();i++){
            Contact c = contacts.get(i);
            header.append(c.getName() != null ? c.getName() : c.getAddress());
            if(i != contacts.size() -1)
                header.append(", ");
        }
        return header.toString();
    }




    public static void deleteSmsList(Context context,List<SMS> smsList) {
        for(SMS sms:smsList){
            Log.i(TAG,"m " + sms.getId());
            context.getContentResolver().delete(Constants.URIS.SMS,"_id=?",
                    new String[]{""+sms.getId()});
        }
    }


    public static long minuteToMillis(int mins){
        return mins * 60000;
    }

}
