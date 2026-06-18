package com.meow.sosapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT = 1;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 2;

    private EditText contactInput;
    private Button addButton;
    private Button selectContactButton;
    private ListView contactListView;

    private ArrayList<String> number_list_str = new ArrayList<>();
    private String FILE_PATH;
    private ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        FILE_PATH = FileUtil.getExternalStorageDir().concat("/EMERGENCY/CONTACTS.txt");

        contactInput = findViewById(R.id.contactInput);
        addButton = findViewById(R.id.addButton);
        selectContactButton = findViewById(R.id.selectContactButton);
        contactListView = findViewById(R.id.contactListView);

        loadContacts();

        adapter = new ContactsAdapter(this, number_list_str);
        contactListView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            String phone = contactInput.getText().toString().trim();
            if (!phone.isEmpty()) {
                addContact(phone);
                contactInput.setText("");
            } else {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
            }
        });

        selectContactButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
            } else {
                pickContact();
            }
        });
    }

    private void loadContacts() {
        if (FileUtil.isExistFile(FILE_PATH)) {
            String data = FileUtil.readFile(FILE_PATH);
            if (data != null && !data.isEmpty()) {
                number_list_str = new Gson().fromJson(data, new TypeToken<ArrayList<String>>(){}.getType());
                if (number_list_str == null) number_list_str = new ArrayList<>();
            }
        } else {
            FileUtil.writeFile(FILE_PATH, new Gson().toJson(number_list_str));
        }
    }

    private void saveContacts() {
        FileUtil.writeFile(FILE_PATH, new Gson().toJson(number_list_str));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void addContact(String phone) {
        if (!number_list_str.contains(phone)) {
            number_list_str.add(phone);
            saveContacts();
            Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Contact already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeContact(int position) {
        if (position >= 0 && position < number_list_str.size()) {
            number_list_str.remove(position);
            saveContacts();
            Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            
            try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(numberIndex);
                    
                    // Clean up phone number
                    number = number.replaceAll("[^0-9+]", "");
                    addContact(number);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ContactsAdapter extends ArrayAdapter<String> {
        public ContactsAdapter(AppCompatActivity context, ArrayList<String> contacts) {
            super(context, 0, contacts);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_contact, parent, false);
            }

            String phone = getItem(position);
            TextView contactTextView = convertView.findViewById(R.id.contactTextView);
            ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

            contactTextView.setText(phone);
            
            deleteButton.setOnClickListener(v -> removeContact(position));

            return convertView;
        }
    }
}
