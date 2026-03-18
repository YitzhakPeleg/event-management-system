package com.eventmanagement.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventmanagement.R;
import com.eventmanagement.api.ApiClient;
import com.eventmanagement.model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of all events.
 * Each event shows title, date, hall, and available spots.
 * Tapping an event opens EventDetailActivity for registration.
 *
 * If the logged-in user is ADMIN, a QR scan button is visible.
 */
public class EventListActivity extends AppCompatActivity {

    private RecyclerView   recyclerView;
    private ProgressBar    progressBar;
    private TextView       tvEmpty;
    private Button         btnScanQR;

    private String         userRole;
    private List<Event>    eventList = new ArrayList<>();
    private EventAdapter   adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        userRole    = getIntent().getStringExtra("role");
        recyclerView = findViewById(R.id.recyclerEvents);
        progressBar  = findViewById(R.id.progressBar);
        tvEmpty      = findViewById(R.id.tvEmpty);
        btnScanQR    = findViewById(R.id.btnScanQR);

        // Show QR scan button only for admins
        if ("ADMIN".equals(userRole)) {
            btnScanQR.setVisibility(View.VISIBLE);
            btnScanQR.setOnClickListener(v ->
                startActivity(new Intent(this, QRScanActivity.class)));
        }

        // Set up RecyclerView with the event adapter
        adapter = new EventAdapter(eventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        // Load events from the server
        new LoadEventsTask().execute();
    }

    /** Refreshes the event list when returning from detail screen. */
    @Override
    protected void onResume() {
        super.onResume();
        new LoadEventsTask().execute();
    }

    private void logout() {
        ApiClient.clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // ---- AsyncTask to load events ----

    private class LoadEventsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Void... v) {
            try {
                return ApiClient.get("/api/events");
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            progressBar.setVisibility(View.GONE);

            if (response == null) {
                tvEmpty.setText("Could not connect to server.");
                tvEmpty.setVisibility(View.VISIBLE);
                return;
            }

            try {
                JSONObject json = new JSONObject(response);
                if (json.getBoolean("success")) {
                    JSONArray arr = json.getJSONArray("data");
                    eventList.clear();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        Event e = new Event();
                        e.setId(obj.getInt("id"));
                        e.setTitle(obj.getString("title"));
                        e.setEventDate(obj.getString("eventDate"));
                        e.setEventTime(obj.getString("eventTime"));
                        e.setHallName(obj.getString("hallName"));
                        e.setHallCapacity(obj.getInt("hallCapacity"));
                        e.setRegistrationCount(obj.getInt("registrationCount"));
                        eventList.add(e);
                    }

                    adapter.notifyDataSetChanged();

                    if (eventList.isEmpty()) {
                        tvEmpty.setText("No upcoming events.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                tvEmpty.setText("Error loading events.");
                tvEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    // ---- RecyclerView Adapter ----

    private class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

        private final List<Event> items;

        EventAdapter(List<Event> items) { this.items = items; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Event event = items.get(position);
            holder.tvTitle.setText(event.getTitle());
            holder.tvDate.setText(event.getEventDate() + " at " + event.getEventTime().substring(0, 5));
            holder.tvHall.setText(event.getHallName());
            holder.tvSpots.setText(event.getRegistrationCount() + " / " + event.getHallCapacity());

            boolean hasSpots = event.getRegistrationCount() < event.getHallCapacity();
            holder.tvStatus.setText(hasSpots ? "Open" : "Full");
            holder.tvStatus.setTextColor(hasSpots ? 0xFF27ae60 : 0xFFc0392b);

            // Tap event → open detail screen
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(EventListActivity.this, EventDetailActivity.class);
                intent.putExtra("eventId", event.getId());
                intent.putExtra("eventTitle", event.getTitle());
                intent.putExtra("role", userRole);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvHall, tvSpots, tvStatus;

            ViewHolder(View v) {
                super(v);
                tvTitle  = v.findViewById(R.id.tvTitle);
                tvDate   = v.findViewById(R.id.tvDate);
                tvHall   = v.findViewById(R.id.tvHall);
                tvSpots  = v.findViewById(R.id.tvSpots);
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }
    }
}
