package com.chaitany.agewell;



import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmergencyContact extends AppCompatActivity implements ContactsAdapter.ContactActionListener {

    private RecyclerView contactsRecyclerView;
    private ContactsAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();
    private SearchView searchView;
    private MaterialButton sendLocationButton;
    private MaterialButton emergencyCallButton;
    private FloatingActionButton addContactFab;

    private DatabaseReference databaseRef;
    private String userMobile;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference contactsRef;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_emergency_contact);



        context=this;
        addContactFab=findViewById(R.id.addContactFab);
        sendLocationButton=findViewById(R.id.sendLocationButton);
        emergencyCallButton=findViewById(R.id.emergencyCallButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

       addContactFab.setOnClickListener(v->showContactDialog(null));
        sendLocationButton.setOnClickListener(v -> sendCurrentLocationToContacts());

        // Initialize Firebase reference
        SharedPreferences prefs = getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        userMobile = prefs.getString("mobile", null); // Get the mobile number from SharedPreferences
        if (userMobile != null) {
            // Initialize the Firebase reference for the current user's emergency contacts
            databaseRef = FirebaseDatabase.getInstance()
                    .getReference("users")                // Root node where users are stored
                    .child(userMobile)                    // Child node with the user's mobile number as key
                    .child("emergency_contacts");         // Child node for emergency contacts
        } else {
            // Handle the case when userMobile is null (e.g., user not logged in)
            showToast("User not logged in");
        }

        emergencyCallButton.setOnClickListener(v -> {
            // Check if the emergency contacts exist in the database
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Check if no contacts exist
                    if (!snapshot.exists()) {
                        showToast("No emergency contacts found!");
                        return;
                    }

                    boolean foundHighPriority = false;
                    String highPriorityPhoneNumber = null;

                    // Iterate over the emergency contacts data
                    for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                        String phone = contactSnapshot.child("phone").getValue(String.class);
                        Boolean highPriority = contactSnapshot.child("highPriority").getValue(Boolean.class);

                        // Check if the contact has high priority
                        if (highPriority != null && highPriority && phone != null) {
                            // Store the high-priority phone number
                            highPriorityPhoneNumber = phone;
                            foundHighPriority = true;
                            break; // Stop after finding the first high-priority contact
                        }
                    }

                    if (foundHighPriority && highPriorityPhoneNumber != null) {
                        // Initiate the call to the high-priority phone number
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + highPriorityPhoneNumber));
                        if (ActivityCompat.checkSelfPermission(EmergencyContact.this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            startActivity(intent);
                        } else {
                            // Request CALL_PHONE permission if not granted
                            ActivityCompat.requestPermissions(EmergencyContact.this, new String[]{android.Manifest.permission.CALL_PHONE}, 2);
                        }
                    } else {
                        showToast("No high-priority contacts found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Failed to retrieve contacts: " + error.getMessage());
                }
            });
        });

        initializeViews();
        setupRecyclerView();
        setupButtons();
        loadContacts();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // This hides the app name
        }

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        searchView = findViewById(R.id.searchView);
        sendLocationButton = findViewById(R.id.sendLocationButton);
        emergencyCallButton = findViewById(R.id.emergencyCallButton);
        addContactFab = findViewById(R.id.addContactFab);
    }

    private void setupRecyclerView() {
        adapter = new ContactsAdapter(contacts, this);
        contactsRecyclerView.setAdapter(adapter);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupButtons() {

    }

    private void showContactDialog(Contact contact) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_contact_form, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Initialize form fields
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        AutoCompleteTextView categoryInput = dialogView.findViewById(R.id.categoryInput);
        AutoCompleteTextView priorityInput = dialogView.findViewById(R.id.priorityInput);

        // Set up dropdowns
        String[] categories = {"Family", "Friend", "Medical", "Other"};
        String[] priorities = {"High Priority", "Medium Priority", "Low Priority"};

        categoryInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories));
        priorityInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, priorities));

        // Pre-fill if editing
        if (contact != null) {
            nameInput.setText(contact.getName());
            phoneInput.setText(contact.getPhone());
            categoryInput.setText(contact.getCategory());
            priorityInput.setText(getPriorityText(contact.getPriority()));
        }

        builder.setView(dialogView)
                .setTitle(contact == null ? "Add Contact" : "Edit Contact")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    String phone = phoneInput.getText().toString();
                    String category = categoryInput.getText().toString();
                    int priority = getPriorityValue(priorityInput.getText().toString());

                    if (validateInput(name, phone)) {
                        if (contact == null) {
                            addContact(name, phone, category, priority);
                        } else {
                            updateContact(contact, name, phone, category, priority);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addContact(String name, String phone, String category, int priority) {
        // Create a new Contact object
        Contact newContact = new Contact(
                UUID.randomUUID().toString(),  // Unique contact ID
                name,                          // Name of the contact
                phone,                         // Phone number of the contact
                category,                      // Category of the contact (e.g., "family", "friend")
                priority                       // Priority level (1, 2, 3, etc.)
        );

        // Add the new contact to Firebase
        addEmergencyContactToFirebase(newContact);
    }

    private void addEmergencyContactToFirebase(Contact newContact) {
        if (databaseRef == null) {
            showToast("Database reference not initialized");
            return;
        }

        // Add contact to Firebase
        String contactId = newContact.getId();
        databaseRef.child(contactId).setValue(newContact)
                .addOnSuccessListener(aVoid -> {
                    showToast("Contact added successfully");
                    loadContacts(); // Refresh contacts list
                })
                .addOnFailureListener(e -> showToast("Failed to add contact"));
    }

    private void updateContact(Contact contact, String name, String phone, String category, int priority) {
        contact.setName(name);
        contact.setPhone(phone);
        contact.setCategory(category);
        contact.setPriority(priority);

        if (databaseRef != null) {
            databaseRef.child(contact.getId()).setValue(contact)
                    .addOnSuccessListener(aVoid -> showToast("Contact updated successfully"))
                    .addOnFailureListener(e -> showToast("Failed to update contact"));
        }
    }

    private void loadContacts() {
        if (databaseRef == null) return;

        // Listen for changes to the contacts data in Firebase
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contacts.clear(); // Clear the current list of contacts

                // Iterate through each child in the Firebase snapshot
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    // Get the Contact object from the snapshot
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact); // Add the contact to the list
                    }
                }

                // Notify the adapter that the data has been updated
                adapter.updateContacts(contacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur while loading data
                Log.e("Firebase", "Failed to load contacts: " + error.getMessage());
                showToast("Failed to load contacts");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getPriorityText(int priority) {
        switch (priority) {
            case 1: return "High Priority";
            case 2: return "Medium Priority";
            case 3: return "Low Priority";
            default: return "Medium Priority";
        }
    }

    private int getPriorityValue(String priorityText) {
        switch (priorityText) {
            case "High Priority": return 1;
            case "Medium Priority": return 2;
            case "Low Priority": return 3;
            default: return 2;
        }
    }

    private boolean validateInput(String name, String phone) {
        if (name.isEmpty() || phone.isEmpty()) {
            showToast("Name and phone are required");
            return false;
        }
        return true;
    }


    @Override
    public void onCallClick(Contact contact) {
        if (contact == null || contact.getPhone() == null || contact.getPhone().isEmpty()) {
            Toast.makeText(context, "Invalid Contact Number", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = contact.getPhone();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        // Check if permission is granted
        if (ActivityCompat.checkSelfPermission(context,android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Ensure context is an Activity before requesting permissions
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
            } else {
                Toast.makeText(context, "Permission required to make calls!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Start call (No need for FLAG_ACTIVITY_NEW_TASK since context is now properly handled)
        context.startActivity(callIntent);
    }




    @Override
    public void onCallClick(Context context, Contact contact) {
        if (contact == null || contact.getPhone() == null || contact.getPhone().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Invalid Contact Number", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = contact.getPhone();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        getApplicationContext().startActivity(callIntent);
    }

    @Override
    public void onEditClick(Contact contact) {
        showContactDialog(contact); // Open the edit contact dialog
    }

    @Override
    public void onDeleteClick(Contact contact) {
        deleteContact(contact); // Delete the contact
    }

    private void deleteContact(Contact contact) {
        // Ensure the context is an AppCompat context
        new androidx.appcompat.app.AlertDialog.Builder(this) // Use 'this' to pass the Activity context
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // If Yes, proceed with the deletion
                    if (databaseRef != null) {
                        databaseRef.child(contact.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> showToast("Contact deleted successfully"))
                                .addOnFailureListener(e -> showToast("Failed to delete contact"));
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // If No, dismiss the dialog and do nothing
                    dialog.dismiss();
                })
                .setCancelable(false) // Optional: prevents dismissing the dialog by tapping outside
                .show();
    }



    private void sendCurrentLocationToContacts() {
        // Check for location and SMS permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.SEND_SMS
            }, 1);
            return;
        }

        // Check if the device's location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // If location is disabled, prompt the user to enable it
            showToast("Location is disabled. Please enable location.");
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }

        // Check if the device has internet connectivity
        if (!isInternetAvailable()) {
            showToast("No internet connection. Please enable it.");
            return;
        }

        showToast("Getting current location...");

        // Get the current location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String latitude = String.valueOf(location.getLatitude());
                            String longitude = String.valueOf(location.getLongitude());

                            // Send the location to contacts
                            sendLocationToContacts(latitude, longitude);
                        } else {
                            showToast("Failed to get current location. Try enabling precise location.");
                        }
                    }
                });
    }


    // Helper method to check internet connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Callback for permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if permissions were granted
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with sending location
                sendCurrentLocationToContacts();
            } else {
                showToast("Permission denied. Cannot send location.");
            }
        }
    }

    // Helper method to check internet connectivity


    private void sendLocationToContacts(String latitude, String longitude) {
        // Fetch emergency contacts from the database
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showToast("No emergency contacts found!");
                    return;
                }

                // Send location via SMS to each contact
                SmsManager smsManager = SmsManager.getDefault();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {

                    String phoneNumber = contactSnapshot.child("phone").getValue(String.class);
                    showToast(phoneNumber+"");
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        String message = " Emergency! My current location: " +
                                "https://www.google.com/maps?q=" + latitude + "," + longitude;
                        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    }
                }

                showToast("Current location sent to emergency contacts.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to retrieve contacts: " + error.getMessage());
            }
        });
    }

    // Callback for permission request




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


}
