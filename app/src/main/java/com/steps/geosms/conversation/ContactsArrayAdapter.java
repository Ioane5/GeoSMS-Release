package com.steps.geosms.conversation;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioane.sharvadze.geosms.R;
import com.steps.geosms.objects.Contact;

/**
 * Class ContactsArrayAdapter
 * Created by ioane on 5/18/15.
 */
public class ContactsArrayAdapter extends ArrayAdapter<Contact>{

    public ContactsArrayAdapter(Context context, int resource) {
        super(context, resource);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.contact_item, null);
        }
        Contact contact = getItem(position);

        ImageView imageView = (ImageView)view.findViewById(R.id.contact_picture_image_view);
        TextView nameView = (TextView)view.findViewById(R.id.contact_name);
        TextView phoneView = (TextView)view.findViewById(R.id.phone_number_text_view);

        imageView.setImageBitmap(contact.getPhoto());
        if(TextUtils.isEmpty(contact.getName())){
            nameView.setText(contact.getAddress());
            phoneView.setText("");
        }else{
            nameView.setText(contact.getName());
            phoneView.setText(contact.getAddress());
        }

        return view;
    }
}
