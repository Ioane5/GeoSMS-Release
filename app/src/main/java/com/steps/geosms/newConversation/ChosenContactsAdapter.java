package com.steps.geosms.newConversation;

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
 * Class ChosenContactsAdapter for ChosenContacts GridView
 *
 * Created by Ioane on 4/23/2015.
 */
public class ChosenContactsAdapter extends ArrayAdapter<Contact>{

    private LayoutInflater inflater;

    public ChosenContactsAdapter(Context context, int resource) {
        super(context, resource);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    static class ViewHolder{
        ImageView contactImage;
        TextView name;
        TextView number;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if(convertView == null){
            view = inflater.inflate(R.layout.contact_bubble, parent, false);

            holder = new ViewHolder();
            holder.contactImage = (ImageView)view.findViewById(R.id.contact_picture_image_view);
            holder.name = (TextView)view.findViewById(R.id.name);
            holder.number = (TextView)view.findViewById(R.id.number);
            view.setTag(holder);
        }else
            holder = (ViewHolder)view.getTag();

        Contact contact = getItem(position);
        if(contact == null) return view;
        holder.contactImage.setImageBitmap(contact.getPhoto());

        if(TextUtils.isEmpty(contact.getName()))
            holder.name.setVisibility(View.GONE);
        else{
            holder.name.setVisibility(View.VISIBLE);
            holder.name.setText(contact.getName());
        }

        holder.number.setText(contact.getAddress());

        return view;
    }
}
