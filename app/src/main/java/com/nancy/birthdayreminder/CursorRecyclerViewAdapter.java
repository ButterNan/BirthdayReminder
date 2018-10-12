package com.nancy.birthdayreminder;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.CursorRecyclerViewHolder>  {

    private Context mContext;
    private OnItemListener mListener;
    private List<ContactDetails> mContactDetailsList;
    //private Cursor mCursor;

    public CursorRecyclerViewAdapter(Context context,List<ContactDetails> contactDetails, OnItemListener listener) {
        mContext = context;
        mContactDetailsList = contactDetails;
        mListener = listener;
    }

    public class CursorRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
   {

       public TextView contactName;
       public TextView contactEmail;
       public TextView contactPhone;
       public TextView contactBirthday;
       public ImageView contactImage;
       public CheckBox checkBox;
       private OnItemListener itemListener;
       public CursorRecyclerViewHolder(final View itemView, OnItemListener listener) {
           super(itemView);

           itemListener = listener;

           contactName = itemView.findViewById(R.id.name);
           contactEmail = itemView.findViewById(R.id.email);
           contactPhone = itemView.findViewById(R.id.phone);
           contactImage = itemView.findViewById(R.id.image);
           contactBirthday = itemView.findViewById(R.id.birthday);
           checkBox = itemView.findViewById(R.id.checkbox);

           checkBox.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   boolean state = ((CheckBox)v).isChecked();
                   if(state)
                   {
                       itemListener.onCheckBoxChecked(itemView, getAdapterPosition());
                   }
                   else {
                       itemListener.onCheckBoxUnChecked(itemView, getAdapterPosition());
                   }
               }
           });
       }
       @Override
       public void onClick(View v) {
           mListener.onRowClicked(v, getAdapterPosition());
       }

   }

    @NonNull
    @Override
    public CursorRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View v = inflator.inflate(R.layout.contact_list_row, parent, false);
        return new CursorRecyclerViewHolder(v,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CursorRecyclerViewHolder holder, int position) {
        if (mContactDetailsList != null && mContactDetailsList.size() > 0) {
            ContactDetails contacts = mContactDetailsList.get(position);
            holder.contactName.setText(contacts.getContactName());
            holder.contactPhone.setText(contacts.getContactPhone());
            holder.contactBirthday.setText(contacts.getContactBday());
            holder.contactEmail.setText(contacts.getContactEmail());
            if (contacts.isChecked()) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        }
    }


    @Override
    public int getItemCount() {
        return mContactDetailsList.size();
    }

}
