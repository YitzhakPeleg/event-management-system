package com.eventmanagement.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.eventmanagement.R;
import com.eventmanagement.api.ApiClient;

import org.json.JSONObject;

/**
 * Login screen — first screen shown when the app starts.
 *
 * The user enters username and password.
 * On success, navigates to EventListActivity.
 * On failure, shows an error message.
 */
public class LoginActivity extends AppCompatActivity {

    // UI components
    private EditText  etUsername;
    private EditText  etPassword;
    private Button    btnLogin;
    private TextView  tvError;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect to UI components defined in the layout XML
        etUsername   = findViewById(R.id.etUsername);
        etPassword   = findViewById(R.id.etPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        tvError      = findViewById(R.id.tvError);
        progressBar  = findViewById(R.id.progressBar);

        // Login button click listener
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Reads the username and password from the input fields and starts the login request.
     */
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic client-side validation
        if (username.isEmpty() || password.isEmpty()) {
            tvError.setText("Please enter username and password.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        tvError.setVisibility(View.GONE);

        // Start login in background (network calls must not run on the main thread)
        new LoginTask().execute(username, password);
    }

    /**
     * AsyncTask that performs the login API call in the background.
     * Updates the UI when done (onPostExecute runs on the main thread).
     */
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // Show loading spinner, hide button
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            try {
                // Send POST request to /api/login
                String body = "username=" + username + "&password=" + password;
                return ApiClient.post("/api/login", body);
            } catch (Exception e) {
                return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            try {
                JSONObject json = new JSONObject(response);

                if (json.getBoolean("success")) {
                    // Login OK — read user info
                    JSONObject data = json.getJSONObject("data");
                    String role = data.getString("role");

                    // Navigate to the events list
                    Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
                    intent.putExtra("role", role);
                    startActivity(intent);
                    finish(); // close login screen so user can't go back to it

                } else {
                    // Login failed — show error
                    tvError.setText(json.optString("error", "Login failed."));
                    tvError.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                tvError.setText("Could not connect to server.");
                tvError.setVisibility(View.VISIBLE);
            }
        }
    }
}
