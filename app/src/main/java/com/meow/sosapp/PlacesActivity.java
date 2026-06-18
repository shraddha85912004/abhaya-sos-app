package com.meow.sosapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlacesActivity extends AppCompatActivity {

    private static final String TAG = "PlacesActivity";
    private static final int PERMISSION_REQUEST_CODE = 101;
    
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 0;
    private double currentLng = 0;
    private boolean locationRetrieved = false;

    private ListView emergencyNumbersList;
    private ProgressBar progressBar;
    private TextView listTitleText;
    private Button btnPolice, btnHospital, btnFire, btnPharmacy;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private static class PlaceItem {
        String name;
        String number;
        double lat;
        double lon;

        public PlaceItem(String name, String number, double lat, double lon) {
            this.name = name;
            this.number = number;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private ArrayAdapter<PlaceItem> adapter;
    private final List<PlaceItem> currentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        setupAdapter();
        loadStaticEmergencyNumbers();
        getLocation();
    }

    private void initViews() {
        btnPolice = findViewById(R.id.btnPolice);
        btnHospital = findViewById(R.id.btnHospital);
        btnFire = findViewById(R.id.btnFire);
        btnPharmacy = findViewById(R.id.btnPharmacy);
        emergencyNumbersList = findViewById(R.id.emergencyNumbersList);
        progressBar = findViewById(R.id.progressBar);
        listTitleText = findViewById(R.id.listTitleText);

        btnPolice.setOnClickListener(v -> fetchNearby("police", "Nearby Police Stations"));
        btnHospital.setOnClickListener(v -> fetchNearby("hospital", "Nearby Hospitals"));
        btnFire.setOnClickListener(v -> fetchNearby("fire_station", "Nearby Fire Stations"));
        btnPharmacy.setOnClickListener(v -> fetchNearby("pharmacy", "Nearby Pharmacies"));
    }

    private void setupAdapter() {
        adapter = new ArrayAdapter<PlaceItem>(this, R.layout.item_emergency_number, R.id.serviceNameText, currentList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                PlaceItem item = getItem(position);
                
                TextView nameView = view.findViewById(R.id.serviceNameText);
                TextView numberView = view.findViewById(R.id.serviceNumberText);
                ImageButton callButton = view.findViewById(R.id.callButton);

                if (item != null) {
                    nameView.setText(item.name);
                    
                    if (item.number != null && !item.number.isEmpty()) {
                        numberView.setText(item.number);
                        numberView.setVisibility(View.VISIBLE);
                        callButton.setVisibility(View.VISIBLE);
                        callButton.setOnClickListener(v -> {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + item.number));
                            startActivity(callIntent);
                        });
                    } else if (item.lat != 0 && item.lon != 0) {
                        numberView.setText("Tap to View on Maps");
                        numberView.setVisibility(View.VISIBLE);
                        callButton.setVisibility(View.GONE);
                    } else {
                        numberView.setVisibility(View.GONE);
                        callButton.setVisibility(View.VISIBLE);
                    }
                    
                    // Click whole item to open maps if it has coordinates
                    view.setOnClickListener(v -> {
                        if (item.lat != 0 && item.lon != 0) {
                            String uri = "geo:" + item.lat + "," + item.lon + "?q=" + item.lat + "," + item.lon + "(" + Uri.encode(item.name) + ")";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                        }
                    });
                }
                return view;
            }
        };

        emergencyNumbersList.setAdapter(adapter);
    }

    private void loadStaticEmergencyNumbers() {
        currentList.clear();
        listTitleText.setText("National Emergency Numbers");
        currentList.add(new PlaceItem("National Emergency", "112", 0, 0));
        currentList.add(new PlaceItem("Police", "100", 0, 0));
        currentList.add(new PlaceItem("Ambulance", "108", 0, 0));
        currentList.add(new PlaceItem("Fire", "101", 0, 0));
        currentList.add(new PlaceItem("Women Helpline", "1091", 0, 0));
        currentList.add(new PlaceItem("Women Helpline (Domestic Abuse)", "181", 0, 0));
        currentList.add(new PlaceItem("Child Helpline", "1098", 0, 0));
        currentList.add(new PlaceItem("Disaster Management", "1078", 0, 0));
        currentList.add(new PlaceItem("Cyber Crime", "1930", 0, 0));
        adapter.notifyDataSetChanged();
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                locationRetrieved = true;
                Log.d(TAG, "Location: " + currentLat + ", " + currentLng);
            } else {
                requestLocationUpdate();
            }
        });
    }

    private void requestLocationUpdate() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                fusedLocationClient.removeLocationUpdates(this);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();
                    locationRetrieved = true;
                    Log.d(TAG, "Updated Location: " + currentLat + ", " + currentLng);
                }
            }
        }, getMainLooper());
    }

    private void fetchNearby(String amenity, String title) {
        if (!locationRetrieved) {
            Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show();
            getLocation();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        emergencyNumbersList.setVisibility(View.GONE);
        listTitleText.setText(title);

        executorService.execute(() -> {
            List<PlaceItem> fetchedPlaces = new ArrayList<>();
            try {
                // Construct Overpass QL
                // e.g., [out:json];nwr(around:5000,lat,lon)[amenity=hospital];out center;
                String query = "[out:json];nwr(around:5000," + currentLat + "," + currentLng + ")[amenity=" + amenity + "];out center;";
                String urlString = "https://overpass-api.de/api/interpreter?data=" + Uri.encode(query);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray elements = jsonResponse.getJSONArray("elements");

                    for (int i = 0; i < elements.length(); i++) {
                        JSONObject element = elements.getJSONObject(i);
                        JSONObject tags = element.optJSONObject("tags");
                        
                        if (tags != null && tags.has("name")) {
                            String name = tags.getString("name");
                            String phone = tags.optString("phone", tags.optString("contact:phone", ""));
                            
                            double lat = 0;
                            double lon = 0;
                            
                            if (element.has("lat") && element.has("lon")) {
                                lat = element.getDouble("lat");
                                lon = element.getDouble("lon");
                            } else if (element.has("center")) {
                                JSONObject center = element.getJSONObject("center");
                                lat = center.getDouble("lat");
                                lon = center.getDouble("lon");
                            }

                            fetchedPlaces.add(new PlaceItem(name, phone, lat, lon));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching from Overpass API", e);
            }

            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                emergencyNumbersList.setVisibility(View.VISIBLE);
                
                currentList.clear();
                if (fetchedPlaces.isEmpty()) {
                    Toast.makeText(this, "No " + title + " found nearby.", Toast.LENGTH_SHORT).show();
                    // Keep the list empty or reload static numbers
                } else {
                    currentList.addAll(fetchedPlaces);
                }
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
