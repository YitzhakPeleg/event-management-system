package com.eventmanagement.util;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating QR code bitmaps.
 *
 * Uses the ZXing library (com.google.zxing:core) which is already
 * included when you add barcode-scanner to build.gradle.
 *
 * Usage: Bitmap qr = QRUtil.generateQRCode("MY-TOKEN", 600, 600);
 */
public class QRUtil {

    // Private constructor — utility class, no instances needed
    private QRUtil() {}

    /**
     * Generates a QR code bitmap from the given text.
     *
     * @param content the text to encode in the QR code (the registration token)
     * @param width   the width of the resulting bitmap in pixels
     * @param height  the height of the resulting bitmap in pixels
     * @return a Bitmap of the QR code, or null if generation fails
     */
    public static Bitmap generateQRCode(String content, int width, int height) {
        try {
            // Configure encoding options
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2); // quiet zone around QR code

            // Generate the QR code bit matrix
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // Convert bit matrix to a black-and-white Bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Black pixel for "on" bits, white for "off" bits
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
