package com.eventmanagement.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.eventmanagement.R;
import com.eventmanagement.api.ApiClient;

import org.json.JSONObject;

/**
 * Shows details for a specific event and allows the user to register.
 *
 * Displayed info: title, date, time, hall name, spots remaining.
 * Register button: only shown if there are available spots.
 */
public class EventDetailActivity extends AppCompatActivity {

    private TextView   tvTitle, tvDate, tvHall, tvSpots, tvStatus;
    private Button     btnRegister;
    private ProgressBar progressBar;

    private int    eventId;
    private String eventTitle;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Get event info passed from EventListActivity
        eventId    = getIntent().getIntExtra("eventId", -1);
        eventTitle = getIntent().getStringExtra("eventTitle");
        userRole   = getIntent().getStringExtra("role");

        tvTitle      = findViewById(R.id.tvTitle);
        tvDate       = findViewById(R.id.tvDate);
        tvHall       = findViewById(R.id.tvHall);
        tvSpots      = findViewById(R.id.tvSpots);
        tvStatus     = findViewById(R.id.tvStatus);
        btnRegister  = findViewById(R.id.btnRegister);
        progressBar  = findViewById(R.id.progressBar);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load fresh event data from the server
        new LoadEventTask().execute();
    }

    // ---- Load full event details ----

    private class LoadEventTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... v) {
            try {
                return ApiClient.get("/api/events?id=" + eventId);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);
            if (response == null) {
                Toast.makeText(EventDetailActivity.this,
                    "Connection error.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(response);
                if (json.getBoolean("success")) {
                    JSONObject data = json.getJSONObject("data");

                    tvTitle.setText(data.getString("title"));
                    tvDate.setText(data.getString("eventDate") + " at " +
                        data.getString("eventTime").substring(0, 5));
                    tvHall.setText(data.getString("hallName"));

                    int cap   = data.getInt("hallCapacity");
                    int count = data.getInt("registrationCount");
                    boolean hasSpots = count < cap;

                    tvSpots.setText(count + " / " + cap + " registered");
                    tvStatus.setText(hasSpots ? "Open for registration" : "Fully Booked");

                    if (hasSpots) {
                        btnRegister.setEnabled(true);
                        btnRegister.setOnClickListener(v -> new RegisterTask().execute());
                    }
                }
            } catch (Exception e) {
                Toast.makeText(EventDetailActivity.this,
                    "Error loading event.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ---- Register for the event ----

    private class RegisterTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            btnRegister.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... v) {
            try {
                return ApiClient.post("/api/registrations", "eventId=" + eventId);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);

            if (response == null) {
                Toast.makeText(EventDetailActivity.this,
                    "Connection error.", Toast.LENGTH_SHORT).show();
                btnRegister.setEnabled(true);
                return;
            }

            try {
                JSONObject json = new JSONObject(response);

                if (json.getBoolean("success")) {
                    // Registration successful — show QR code screen
                    JSONObject data   = json.getJSONObject("data");
                    String qrToken    = data.getString("qrCodeToken");
                    String eventTitle = data.getString("eventTitle");

                    android.content.Intent intent =
                        new android.content.Intent(EventDetailActivity.this, QRDisplayActivity.class);
                    intent.putExtra("qrToken", qrToken);
                    intent.putExtra("eventTitle", eventTitle);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(EventDetailActivity.this,
                        json.optString("error", "Registration failed."),
                        Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(true);
                }

            } catch (Exception e) {
                Toast.makeText(EventDetailActivity.this,
                    "Unexpected error.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
