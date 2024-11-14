package com.example.contactapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendSmsTask {

    private String toPhoneNumber;
    private String message;
    private Context context;

    public SendSmsTask(Context context, String toPhoneNumber, String message) {
        this.context = context;
        this.toPhoneNumber = toPhoneNumber;
        this.message = message;
    }

    public void execute() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String result = sendSms();
                // Run on main thread to update the UI
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private String sendSms() {
        try {
            // Twilio API URL for sending SMS
            URL url = new URL("https://api.twilio.com/2010-04-01/Accounts/YOUR_ACCOUNT_SID/Messages.json");

            // Set up the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Twilio credentials (replace these with your own)
            String auth = "AC11174b81d84a5a2bf91bcf6838861622" + ":" + "8f721b31ba72c37bed0c08b77114f222";
            String encodedAuth = "Basic " + android.util.Base64.encodeToString(auth.getBytes(), android.util.Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", encodedAuth);

            // Form data for the SMS
            String data = "To=" + toPhoneNumber +
                    "&From=+12029153949" +
                    "&Body=" + message;

            // Write the data to the output stream
            OutputStream os = connection.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();

            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return "SMS sent successfully!";
            } else {
                return "Failed to send SMS. Response code: " + responseCode;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending SMS: " + e.getMessage();
        }
    }
}
