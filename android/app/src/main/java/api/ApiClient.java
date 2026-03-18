package com.eventmanagement.api;

import java.io.*;
import java.net.*;

/**
 * HTTP client for communicating with the backend API.
 *
 * All network requests are done using HttpURLConnection (no external libraries needed).
 *
 * IMPORTANT: Never call these methods on the main (UI) thread.
 *            Always call from a background thread (AsyncTask or Thread).
 *
 * Base URL: http://10.0.2.2:8080/EventManagement
 *   - 10.0.2.2 is the Android emulator's address for the host machine's localhost.
 *   - Change this to the real IP if running on a physical device.
 */
public class ApiClient {

    // Base URL of the backend server (Tomcat running on the developer's machine)
    private static final String BASE_URL = "http://10.0.2.2:8888/EventManagement";

    // Connection timeout in milliseconds
    private static final int TIMEOUT_MS = 5000;

    // Shared session cookie (set after login, sent with every request)
    private static String sessionCookie = null;

    /**
     * Sends a GET request and returns the response body as a String.
     *
     * @param endpoint the API path (e.g. "/api/events")
     * @return the response body string (usually JSON)
     * @throws IOException if the network request fails
     */
    public static String get(String endpoint) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        attachCookie(conn);
        return readResponse(conn);
    }

    /**
     * Sends a POST request with form-encoded body and returns the response.
     *
     * @param endpoint the API path (e.g. "/api/login")
     * @param params   URL-encoded parameters (e.g. "username=admin&password=1234")
     * @return the response body string (usually JSON)
     * @throws IOException if the network request fails
     */
    public static String post(String endpoint, String params) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        attachCookie(conn);

        // Write request body
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes());
        }

        // Save session cookie from response
        saveCookie(conn);
        return readResponse(conn);
    }

    /**
     * Sends a DELETE request and returns the response.
     *
     * @param endpoint the API path with query param (e.g. "/api/registrations?id=5")
     * @return the response body string
     * @throws IOException if the network request fails
     */
    public static String delete(String endpoint) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        attachCookie(conn);
        return readResponse(conn);
    }

    /** Attaches the stored session cookie to the request. */
    private static void attachCookie(HttpURLConnection conn) {
        if (sessionCookie != null) {
            conn.setRequestProperty("Cookie", sessionCookie);
        }
    }

    /** Saves the JSESSIONID cookie from the server response. */
    private static void saveCookie(HttpURLConnection conn) {
        String header = conn.getHeaderField("Set-Cookie");
        if (header != null && header.contains("JSESSIONID")) {
            // Extract just the session ID part (before the semicolon)
            sessionCookie = header.split(";")[0];
        }
    }

    /** Reads the response body from the connection. */
    private static String readResponse(HttpURLConnection conn) throws IOException {
        InputStream stream;
        int code = conn.getResponseCode();

        // Use error stream if response code is 4xx or 5xx
        if (code >= 400) {
            stream = conn.getErrorStream();
        } else {
            stream = conn.getInputStream();
        }

        if (stream == null) return "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /** Clears the session cookie (used on logout). */
    public static void clearSession() {
        sessionCookie = null;
    }
}
