package com.eventmanagement.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.eventmanagement.R;
import com.eventmanagement.api.ApiClient;
import org.json.JSONObject;

/**
 * Admin-only screen for scanning participant QR codes at the event entrance.
 *
 * Uses the ZXing Android Embedded library (IntentIntegrator) to launch
 * the device camera as a scanner. When a valid token is scanned, it is
 * sent to the server for validation and the participant's name and event
 * are shown on screen.
 *
 * Library: com.journeyapps:zxing-android-embedded (add to build.gradle)
 *
 * ADMIN permission required — enforced by EventListActivity
 * (btnScanQR is hidden for non-admin users).
 */
public class QRScanActivity extends AppCompatActivity {

    private TextView    tvResult;
    private ProgressBar progressBar;
    private Button      btnScanAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        tvResult    = findViewById(R.id.tvResult);
        progressBar = findViewById(R.id.progressBar);
        btnScanAgain = findViewById(R.id.btnScanAgain);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Start scan button — launches the ZXing camera scanner activity
        findViewById(R.id.btnStartScan).setOnClickListener(v -> startScan());

        // Scan Again — clears the previous result and starts a new scan
        btnScanAgain.setOnClickListener(v -> {
            tvResult.setVisibility(View.GONE);
            btnScanAgain.setVisibility(View.GONE);
            startScan();
        });

        // Auto-launch the scanner when this screen opens
        startScan();
    }

    /** Launches the ZXing camera scanner as a new Activity. */
    private void startScan() {
        new IntentIntegrator(this)
                .setPrompt("Scan participant QR code")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
                .initiateScan();
    }

    /**
     * Called when the ZXing scanner Activity returns.
     * rawResult is null if the user pressed Back without scanning.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // User cancelled the scan
                finish();
            } else {
                // Got a token — validate it with the backend
                new ValidateTokenTask().execute(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // ---- AsyncTask: validate the scanned token with the backend ----

    private class ValidateTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String token = params[0];
                return ApiClient.get("/api/registrations?token=" +
                        java.net.URLEncoder.encode(token, "UTF-8"));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);
            btnScanAgain.setVisibility(View.VISIBLE);

            if (response == null) {
                showResult(false, "Connection error. Could not validate.");
                return;
            }

            try {
                JSONObject json = new JSONObject(response);
                if (json.getBoolean("success")) {
                    JSONObject data = json.getJSONObject("data");
                    String name  = data.getString("participantName");
                    String event = data.getString("eventTitle");
                    String date  = data.getString("eventDate");
                    showResult(true,  "✓ VALID\n\nParticipant: " + name +
                                      "\nEvent: " + event +
                                      "\nDate: " + date);
                } else {
                    showResult(false, "✗ INVALID QR CODE\n" +
                            json.optString("error", "Not found."));
                }
            } catch (Exception e) {
                showResult(false, "Error reading response.");
            }
        }
    }

    /** Displays the validation result to the admin. */
    private void showResult(boolean valid, String message) {
        tvResult.setText(message);
        tvResult.setTextColor(valid ? 0xFF27ae60 : 0xFFc0392b);
        tvResult.setVisibility(View.VISIBLE);
    }
}
