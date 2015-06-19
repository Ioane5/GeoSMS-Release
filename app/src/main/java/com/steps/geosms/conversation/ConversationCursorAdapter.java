package com.steps.geosms.conversation;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.SMS;
import com.steps.geosms.utils.Utils;

/**
 * Class ConversationCursorAdapter
 * (custom cursor adapter)
 *
 * Created by Ioane on 2/26/2015.
 */
public class ConversationCursorAdapter extends CursorAdapter {


    @SuppressWarnings("unused")
    private static final  String TAG = ConversationCursorAdapter.class.getSimpleName();

    private Contact contact;

    private Bitmap OWNER_IMAGE;

    private static final int OTHER = 1;
    private static final int ME = 0;
   //private static GeoTranslator mTranslator;

    public ConversationCursorAdapter(Context context, Cursor c, boolean autoRequery ,Contact contact) {
        super(context, c, autoRequery);
        this.contact = contact;

        String ownerPhotoUri = Utils.getOwnersImage(context);


        if(ownerPhotoUri == null){
            OWNER_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_no_image);
            // make it circle like.
            OWNER_IMAGE = Utils.getCircleBitmap(OWNER_IMAGE);

        }else{
            OWNER_IMAGE = Utils.getCircleBitmap(Utils.getPhotoFromURI(ownerPhotoUri,context,60));
        }

        //mTranslator = new GeoTranslator.load();

    }


    private class ViewHolder {
        TextView messageView;
        //TextView nameView;
        TextView deliveryStatusView;
        TextView timeView;
        ImageView photo;
        ImageButton failed;
        ProgressBar sending;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        switch (getItemViewType(cursor)){
            case ME:
                return LayoutInflater.from(context).inflate(R.layout.message_item_me,parent,false);
            case OTHER:
                return LayoutInflater.from(context).inflate(R.layout.message_item_other,parent,false);
            default:
                return null;
        }
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SMS message = new SMS(cursor);

        ViewHolder holder = (ViewHolder)view.getTag();

        if (holder == null) {
            holder = new ViewHolder();
            //holder.nameView = (TextView)view.findViewById(R.id.name_view);
            holder.messageView = (TextView)view.findViewById(R.id.message_text_view);
            holder.deliveryStatusView = (TextView)view.findViewById(R.id.delivery_status_view);
            holder.timeView = (TextView)view.findViewById(R.id.time_view);
            holder.photo = (ImageView)view.findViewById(R.id.message_contact_photo);
            holder.failed = (ImageButton)view.findViewById(R.id.failed_button);
            holder.sending = (ProgressBar)view.findViewById(R.id.sending_progress);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }

        SMS.MsgType type = message.getMsgType();

        SMS nextSms = null;
        if(cursor.moveToPrevious()){
            nextSms = new SMS(cursor);
        }

        holder.photo.setVisibility(View.INVISIBLE);

        if (holder.deliveryStatusView.length() > 0) {
            holder.deliveryStatusView.setText("");
        }
        /*
            if this sms is first , or is received from contact
            let's show header. like photo and name...
         */
        if(type != SMS.MsgType.RECEIVED && (nextSms == null || nextSms.getMsgType() == SMS.MsgType.RECEIVED)){
            holder.photo.setImageBitmap(OWNER_IMAGE);
            holder.photo.setVisibility(View.VISIBLE);
        }
        holder.failed.setVisibility(View.GONE);
        holder.sending.setVisibility(View.GONE);
        holder.deliveryStatusView.setVisibility(View.GONE);

        switch (type){
            case SENT:
                break;
            case PENDING:
                holder.sending.setVisibility(View.VISIBLE);
                break;
            case DRAFT:
                holder.deliveryStatusView.setVisibility(View.VISIBLE);
                holder.deliveryStatusView.setText(R.string.sms_draft);
                holder.deliveryStatusView.setTextColor(Color.GRAY);
                break;
            case FAILED:
                holder.failed.setVisibility(View.VISIBLE);
                break;
            case RECEIVED:
                /*
                    If this is sms from sender or is first, let's show header...
                 */
                if(nextSms == null || nextSms.getMsgType() != SMS.MsgType.RECEIVED){
                    holder.photo.setImageBitmap(contact.getPhoto());
                    holder.photo.setVisibility(View.VISIBLE);
                    break;
                }
        }

        if(message.isDelivered()){
            holder.deliveryStatusView.setVisibility(View.VISIBLE);
            holder.deliveryStatusView.setText(R.string.sms_delivered);
            holder.deliveryStatusView.setTextColor(Color.GRAY);
        }

        holder.messageView.setText(message.getText());
        CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(message.getDate().getTime(),
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        holder.timeView.setText(formattedTime);
    }



    @Override
    public int getItemViewType(int position) {
        return getItemViewType((Cursor)getItem(position));
    }


    private int getItemViewType(Cursor c) {
        if(c == null) return ME;
        SMS message = new SMS(c);
        if(message.getMsgType() == SMS.MsgType.RECEIVED)
            return OTHER;
        return ME;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
