package com.example.contactapp;

import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdapterContact extends RecyclerView.Adapter<AdapterContact.ContactViewHolder> {

    private Context context;
    private ArrayList<ModelContact> contactList;
    private DbHelper dbHelper;

    // add constructor
    // alt + ins

    public AdapterContact(Context context, ArrayList<ModelContact> contactList) {
        this.context = context;
        this.contactList = contactList;
        dbHelper = new DbHelper(context);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_contact_item,parent,false);
        ContactViewHolder vh = new ContactViewHolder(view);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ModelContact modelContact = contactList.get(position);

        // Get data
        String id = modelContact.getId();
        String image = modelContact.getImage();
        String name = modelContact.getName();
        String phone = modelContact.getPhone();
        String email = modelContact.getEmail();
        String note = modelContact.getNote();
        String addedTime = modelContact.getAddedTime();
        String updatedTime = modelContact.getUpdatedTime();

        // Set data in view
        holder.contactName.setText(name);

        if (image.isEmpty()) {
            // Default image if no image is available
            holder.contactImage.setImageResource(R.drawable.ic_baseline_person_24);
        } else {
            // Use Glide to load the image
            Glide.with(context)
                    .load(image) // image is a URI or file path
                    .into(holder.contactImage);
        }

        // Handle row click to navigate to ContactDetails
        holder.relativeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContactDetails.class);
            intent.putExtra("contactId", id);  // Pass the contact ID to the ContactDetails activity
            context.startActivity(intent);
        });

        // Handle editBtn click
        holder.contactEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditContact.class);
            intent.putExtra("ID", id);
            intent.putExtra("NAME", name);
            intent.putExtra("PHONE", phone);
            intent.putExtra("EMAIL", email);
            intent.putExtra("NOTE", note);
            intent.putExtra("ADDEDTIME", addedTime);
            intent.putExtra("UPDATEDTIME", updatedTime);
            intent.putExtra("isEditMode", true);



                // Set a default image if needed or handle the case where image is null
                intent.putExtra("IMAGE_URI", ""); // or omit this line if no default is needed



            context.startActivity(intent);
        });

        // Handle delete click
        holder.contactDelete.setOnClickListener(v -> {
            // Remove the item from the list
            contactList.remove(position);
            dbHelper.deleteContact(id);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, contactList.size());

            // Optional: Show a Toast message
            Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show();
        });
    }



    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder{

        //view for row_contact_item
        ImageView contactImage;
        TextView contactName,contactEdit,contactDelete;
        RelativeLayout relativeLayout;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            //init view
            contactImage = itemView.findViewById(R.id.contact_image);

            contactName = itemView.findViewById(R.id.contact_name);
            contactDelete = itemView.findViewById(R.id.contact_delete);
            contactEdit = itemView.findViewById(R.id.contact_edit);
            relativeLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
