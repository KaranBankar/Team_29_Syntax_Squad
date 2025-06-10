package com.chaitany.agewell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> contacts;
    private ContactActionListener contactActionListener;

    public ContactsAdapter(List<Contact> contacts, ContactActionListener contactActionListener) {
        this.contacts = contacts;
        this.contactActionListener = contactActionListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhone());
        holder.categoryChip.setText(contact.getCategory());
        holder.priorityChip.setText(contact.getPriorityText());

        // Handle visibility for the priority chip
        if (contact.isHighPriority()) {
            holder.priorityChip.setVisibility(View.VISIBLE);
        } else {
            holder.priorityChip.setVisibility(View.GONE);
        }

        // Set listeners for buttons
        holder.callButton.setOnClickListener(v -> contactActionListener.onCallClick(contact));
        holder.editButton.setOnClickListener(v -> contactActionListener.onEditClick(contact));
        holder.deleteButton.setOnClickListener(v -> contactActionListener.onDeleteClick(contact));

    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(List<Contact> newContacts) {
        contacts = newContacts;
        notifyDataSetChanged();
    }

    public interface ContactActionListener {
        void onCallClick(Contact contact);

        void onCallClick(Context context, Contact contact);

        void onEditClick(Contact contact);
        void onDeleteClick(Contact contact);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        Chip categoryChip;
        Chip priorityChip;
        MaterialButton callButton; // Change to ImageButton
        MaterialButton editButton; // Change to ImageButton
        MaterialButton deleteButton; // Change to ImageButton
        MaterialCardView cardView;

        public ContactViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameText);
            phoneTextView = itemView.findViewById(R.id.phoneText);
            categoryChip = itemView.findViewById(R.id.categoryChip);
            priorityChip = itemView.findViewById(R.id.priorityChip);
            callButton = itemView.findViewById(R.id.callButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

        }
    }

}
