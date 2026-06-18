package com.meow.sosapp;

import static com.meow.sosapp.SpeechRecognitionService.CMD_RECEIVED_ACTION;
import static com.meow.sosapp.SpeechRecognitionService.SEND_ACTION;
import static com.meow.sosapp.SpeechRecognitionService.STOP_LISTENING_ACTION;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import com.meow.handguesture.ShakeDetector;

public class MainActivity extends AppCompatActivity implements ShakeDetector.OnShakeListener {

    public static final String SOS_DETECTED_ACTION = "com.meow.sosapp.SOS_DETECTED";
    private static final int REQUEST_PERMISSIONS_CODE = 100;
    
    // UI Elements
    private Button sendButton;
    private View pulseView;
    private View cardContacts, cardPlaces, cardAiChat, cardModes;
    private TextView profileName, profileInitial, detectionStatusText;
    private ImageButton btnSettings;
    
    // Data
    private ArrayList<String> number_list_str = new ArrayList<>();
    private String FILE_PATH = FileUtil.getExternalStorageDir().concat("/EMERGENCY/CONTACTS.txt");
    private String EMPTY_JSON_STR = "";

    // Sensors & Detection
    private boolean isShakeDetectionEnabled = false;
    private boolean isVoiceDetectionEnabled = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private SharedPreferences switchesPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();

        // Start Background Service
        Intent intent = new Intent(MainActivity.this, SpeechRecognitionService.class);
        startService(intent);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        switchesPrefs = getSharedPreferences("Switches.txt", Activity.MODE_PRIVATE);
        
        initViews();
        setupClickListeners();
        setupShakeDetection();
        startPulseAnimation();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        updateDetectionStatus();
        
        // Re-read contacts
        if (FileUtil.isExistFile(FILE_PATH)) {
            String content = FileUtil.readFile(FILE_PATH);
            if (content != null && !content.isEmpty()) {
                number_list_str = new Gson().fromJson(content, new TypeToken<ArrayList<String>>(){}.getType());
            }
        } else {
            FileUtil.writeFile(FILE_PATH, EMPTY_JSON_STR);
        }

        if (isShakeDetectionEnabled && mSensorManager != null && mAccelerometer != null) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null && mShakeDetector != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    }

    private void initViews() {
        sendButton = findViewById(R.id.sendButton);
        pulseView = findViewById(R.id.pulseView);
        cardContacts = findViewById(R.id.cardContacts);
        cardPlaces = findViewById(R.id.cardPlaces);
        cardAiChat = findViewById(R.id.cardAiChat);
        cardModes = findViewById(R.id.cardModes);
        profileName = findViewById(R.id.profileName);
        profileInitial = findViewById(R.id.profileInitial);
        btnSettings = findViewById(R.id.btnSettings);
        detectionStatusText = findViewById(R.id.detectionStatusText);
    }
    
    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences(ProfileActivity.PREF_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(ProfileActivity.KEY_NAME, "User");
        if (name.isEmpty()) name = "User";
        
        profileName.setText(name);
        profileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
    }

    private void setupShakeDetection() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();
            mShakeDetector.setOnShakeListener(this);
        }
    }

    private void updateDetectionStatus() {
        isVoiceDetectionEnabled = switchesPrefs.getBoolean("voice_switch_on", false);
        isShakeDetectionEnabled = switchesPrefs.getBoolean("hand_switch_on", false);
        
        if (isVoiceDetectionEnabled && isShakeDetectionEnabled) {
            detectionStatusText.setText("Voice & Shake Detection Active");
            detectionStatusText.setTextColor(ContextCompat.getColor(this, R.color.color_success));
        } else if (isVoiceDetectionEnabled) {
            detectionStatusText.setText("Voice Detection Active");
            detectionStatusText.setTextColor(ContextCompat.getColor(this, R.color.color_success));
        } else if (isShakeDetectionEnabled) {
            detectionStatusText.setText("Shake Detection Active");
            detectionStatusText.setTextColor(ContextCompat.getColor(this, R.color.color_success));
        } else {
            detectionStatusText.setText("Background Detection Disabled");
            detectionStatusText.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
        }
    }

    private void setupClickListeners() {
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        
        // Setup Profile clickable container as well
        findViewById(R.id.profileContainer).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        sendButton.setOnClickListener(v -> {
            if (number_list_str == null || number_list_str.isEmpty()) {
                startActivity(new Intent(this, ContactsActivity.class));
                Toast.makeText(this, "Add Contacts First", Toast.LENGTH_SHORT).show();
            } else {
                SendSms();
                Toast.makeText(this, "SOS Triggered!", Toast.LENGTH_SHORT).show();
            }
        });

        cardContacts.setOnClickListener(v -> startActivity(new Intent(this, ContactsActivity.class)));
        cardPlaces.setOnClickListener(v -> startActivity(new Intent(this, PlacesActivity.class)));
        cardAiChat.setOnClickListener(v -> startActivity(new Intent(this, ChatWithAi.class)));
        
        cardModes.setOnClickListener(v -> showModesDialog());
        
        // Legacy customize SOS button has been completely removed.
    }

    private void startPulseAnimation() {
        if (pulseView != null) {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            pulseView.startAnimation(pulse);
        }
    }

    private void showModesDialog() {
        AlertDialog customize_mode_dialog = new AlertDialog.Builder(MainActivity.this).create();
        View inflate2 = getLayoutInflater().inflate(R.layout.cust, null);
        
        LinearLayout customize_mode_linear = inflate2.findViewById(R.id.customize_mode_linear);
        LinearLayout customize_sos_linear1 = inflate2.findViewById(R.id.customize_sos_linear1);
        customize_sos_linear1.setVisibility(View.GONE);
        customize_mode_linear.setVisibility(View.VISIBLE);
        
        Button Save_mode_But = inflate2.findViewById(R.id.Save_mode_But);
        Button Cancel_mode_But = inflate2.findViewById(R.id.Cancel_mode_But);
        Switch voice_switch = inflate2.findViewById(R.id.voice_switch);
        Switch hand_switch = inflate2.findViewById(R.id.hand_switch);

        voice_switch.setChecked(switchesPrefs.getBoolean("voice_switch_on", false));
        hand_switch.setChecked(switchesPrefs.getBoolean("hand_switch_on", false));

        voice_switch.setOnCheckedChangeListener((_param1, isChecked) -> {
            SharedPreferences.Editor editor = switchesPrefs.edit();
            editor.putBoolean("voice_switch_on", isChecked);
            editor.apply();
            
            if (isChecked) StartSpeech();
            else StopSpeech();
        });

        hand_switch.setOnCheckedChangeListener((_param1, isChecked) -> {
            SharedPreferences.Editor editor = switchesPrefs.edit();
            editor.putBoolean("hand_switch_on", isChecked);
            editor.apply();
            
            isShakeDetectionEnabled = isChecked;
            if (isChecked && mSensorManager != null) {
                mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            } else if (!isChecked && mSensorManager != null) {
                mSensorManager.unregisterListener(mShakeDetector);
            }
        });

        Save_mode_But.setOnClickListener(v -> {
            updateDetectionStatus();
            customize_mode_dialog.dismiss();
            Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
        });
        
        Cancel_mode_But.setOnClickListener(v -> customize_mode_dialog.dismiss());

        customize_mode_dialog.setView(inflate2);
        customize_mode_dialog.show();
    }
    
    // showCustomizeSosDialog removed
    private void StartSpeech() {
        Intent startSpeech = new Intent(CMD_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(startSpeech);
    }

    private void StopSpeech() {
        Intent stopSpeech = new Intent(STOP_LISTENING_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(stopSpeech);
    }

    private void SendSms() {
        Intent send = new Intent(SEND_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(send);
    }

    @Override
    public void onShake(int count) {
        if (isShakeDetectionEnabled) {
            Log.d("ShakeApp", "Phone Shaken! Triggering SOS!");
            Toast.makeText(this, "Shake Detected! Sending SOS...", Toast.LENGTH_SHORT).show();
            SendSms();
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };
        
        List<String> ungrantedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ungrantedPermissions.add(permission);
            }
        }
        
        if (!ungrantedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    ungrantedPermissions.toArray(new String[0]),
                    REQUEST_PERMISSIONS_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
