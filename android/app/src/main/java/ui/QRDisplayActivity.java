package com.eventmanagement.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.eventmanagement.R;
import com.eventmanagement.util.QRUtil;

/**
 * Displays the participant's QR code after successful registration.
 *
 * The QR code encodes the unique qrCodeToken from the database.
 * At the event entrance, an admin scans this QR code using QRScanActivity.
 *
 * Requires: ZXing library (com.google.zxing:core) in build.gradle
 */
public class QRDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        String qrToken    = getIntent().getStringExtra("qrToken");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        TextView  tvEvent  = findViewById(R.id.tvEventTitle);
        ImageView ivQR     = findViewById(R.id.ivQRCode);
        TextView  tvToken  = findViewById(R.id.tvToken);

        tvEvent.setText("Your ticket for:\n" + eventTitle);
        tvToken.setText("Token: " + qrToken);

        // Generate the QR code bitmap and display it
        Bitmap qrBitmap = QRUtil.generateQRCode(qrToken, 600, 600);
        if (qrBitmap != null) {
            ivQR.setImageBitmap(qrBitmap);
        }

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
