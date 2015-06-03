package com.steps.geosms.conversationsList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.objects.Contact;
import com.steps.geosms.objects.Conversation;
import com.steps.geosms.utils.AsyncImageDownloader;
import com.steps.geosms.utils.Utils;

import java.util.List;


/**
 * Class ConversationsListAdapter
 * custom list adapter
 *
 * Created by Ioane on 2/23/2015.
 */
public class ConversationsListAdapter extends ArrayAdapter<Conversation> {

    @SuppressWarnings("unused")
    private final String TAG = ConversationsListAdapter.class.getSimpleName();


    private Bitmap DEFAULT_IMAGE;

    private AsyncImageDownloader mImageDownloader;

    private class ViewHolder {
        TextView contactNameView;
        TextView messageView;
        TextView messageDateView;
        ImageView contactImageView;
    }

    public ConversationsListAdapter(Context context, int resource, List<Conversation> objects) {
        super(context, resource, objects);

        DEFAULT_IMAGE = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_no_image);
        // make it circle like.
        DEFAULT_IMAGE = Utils.getCircleBitmap(DEFAULT_IMAGE);
        mImageDownloader = new AsyncImageDownloader(context, 80);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.conversation_item, null);

            holder = new ViewHolder();
            holder.contactNameView = (TextView)view.findViewById(R.id.contact_name_text_view);
            holder.messageDateView = (TextView)view.findViewById(R.id.last_message_date_view);
            holder.messageView = (TextView)view.findViewById(R.id.last_message_text_view);
            holder.contactImageView = (ImageView)view.findViewById(R.id.contact_picture_image_view);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        Conversation conversation = getItem(position);
        if(conversation == null) return view;


        // not to show different values if contact is null...
        holder.contactImageView.setImageBitmap(DEFAULT_IMAGE);
        holder.messageView.setText("");

        Contact contact = conversation.getContact();
        String header = Utils.getChatHeader(conversation.getContacts());

        holder.contactImageView.setTag(contact == null ? null : contact.getPhotoUri());
        holder.contactImageView.setTag(R.string.contact,contact);

        if(contact != null){
            holder.contactNameView.setText(header);
            mImageDownloader.addImage(contact,holder.contactImageView);
        }
        holder.messageView.setText(conversation.getLastMessage());

        if(conversation.getDate() != null){
            CharSequence formattedTime = DateUtils.getRelativeTimeSpanString(conversation.getDate().getTime(),System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
            holder.messageDateView.setText(formattedTime);
        }else{
            holder.messageDateView.setText("");
        }

        if(!conversation.isMessageRead()){
            holder.messageView.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.contactNameView.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.messageDateView.setTypeface(null, Typeface.BOLD_ITALIC);
        }else{
            holder.messageView.setTypeface(null,Typeface.NORMAL);
            holder.contactNameView.setTypeface(null,Typeface.NORMAL);
            holder.messageDateView.setTypeface(null, Typeface.NORMAL);
        }

        return view;
    }
}