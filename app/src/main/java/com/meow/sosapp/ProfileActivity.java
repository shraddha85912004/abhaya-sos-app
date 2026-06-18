package com.meow.sosapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText editName, editPhone, editBloodGroup, editMedicalConditions, editSosMessage, editWakeword;
    private TextView profileAvatarInitial;
    private Button btnSaveProfile;

    public static final String PREF_NAME = "UserProfile";
    public static final String KEY_NAME = "user_name";
    public static final String KEY_PHONE = "user_phone";
    public static final String KEY_BLOOD_GROUP = "user_blood_group";
    public static final String KEY_MEDICAL = "user_medical";
    public static final String KEY_SOS_MSG = "user_sos_message";
    public static final String KEY_WAKEWORD = "wakeword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadProfileData();

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });
    }

    private void initViews() {
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editBloodGroup = findViewById(R.id.editBloodGroup);
        editMedicalConditions = findViewById(R.id.editMedicalConditions);
        editSosMessage = findViewById(R.id.editSosMessage);
        editWakeword = findViewById(R.id.editWakeword);
        profileAvatarInitial = findViewById(R.id.profileAvatarInitial);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        String name = prefs.getString(KEY_NAME, "");
        editName.setText(name);
        editPhone.setText(prefs.getString(KEY_PHONE, ""));
        editBloodGroup.setText(prefs.getString(KEY_BLOOD_GROUP, ""));
        editMedicalConditions.setText(prefs.getString(KEY_MEDICAL, ""));
        editSosMessage.setText(prefs.getString(KEY_SOS_MSG, "I am in danger, please help me!"));
        editWakeword.setText(prefs.getString(KEY_WAKEWORD, "help"));
        
        if (!name.isEmpty()) {
            profileAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        } else {
            profileAvatarInitial.setText("U");
        }
    }

    private void saveProfileData() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        
        String name = editName.getText().toString().trim();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PHONE, editPhone.getText().toString().trim());
        editor.putString(KEY_BLOOD_GROUP, editBloodGroup.getText().toString().trim());
        editor.putString(KEY_MEDICAL, editMedicalConditions.getText().toString().trim());
        
        String sosMsg = editSosMessage.getText().toString().trim();
        if (sosMsg.isEmpty()) {
            sosMsg = "I am in danger, please help me!";
        }
        editor.putString(KEY_SOS_MSG, sosMsg);
        
        String wakeword = editWakeword.getText().toString().trim();
        if(wakeword.isEmpty()) wakeword = "help";
        editor.putString(KEY_WAKEWORD, wakeword.toLowerCase());
        
        editor.apply();
        
        if (!name.isEmpty()) {
            profileAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
        
        Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
        finish(); // Return to main activity
    }
    
    // Helper method to get the custom SOS message from anywhere
    public static String getCustomSosMessage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String msg = prefs.getString(KEY_SOS_MSG, "I am in danger, please help me!");
        
        // Optionally append medical info if available
        String bloodGroup = prefs.getString(KEY_BLOOD_GROUP, "");
        String medical = prefs.getString(KEY_MEDICAL, "");
        
        if (!bloodGroup.isEmpty() || !medical.isEmpty()) {
            msg += " [Medical: ";
            if (!bloodGroup.isEmpty()) msg += "Blood " + bloodGroup + " ";
            if (!medical.isEmpty()) msg += medical;
            msg += "]";
        }
        
        return msg.trim();
    }
}
