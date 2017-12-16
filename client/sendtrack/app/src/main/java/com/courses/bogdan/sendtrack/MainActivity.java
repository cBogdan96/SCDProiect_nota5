package com.courses.bogdan.sendtrack;


import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Executor executor = Executors.newFixedThreadPool(1);
    private volatile Handler msgHandler;

    private static final String STATIC_LOCATION = "{" +
            "\"terminalId\":\"%s\"," +
            "\"latitude\":\"%s\"," +
            "\"longitude\":\"%s\"" +
            "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = (Button)findViewById(R.id.button_send);
        sendButton.setOnClickListener(this);

        msgHandler = new MsgHandler(this);
    }

    public void onClick(View v) {
        executor.execute(new Runnable() {
            public void run() {
                Message msg = msgHandler.obtainMessage();
                msg.arg1 = sendCoordinates("string", "23.25", "45.01") ? 1 : 0;
                msgHandler.sendMessage(msg);
            }
        });
    }

    private boolean sendCoordinates(String terminalId, String lat, String lng) {
        HttpURLConnection con = null;
        try {
            URL obj = new URL("http://192.168.43.138:8085/position/send");
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(String.format(STATIC_LOCATION, terminalId, lat, lng).getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private static class MsgHandler extends Handler {
        private final WeakReference<Activity> sendActivity;

        public MsgHandler(Activity activity) {
            sendActivity = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                Toast.makeText(sendActivity.get().getApplicationContext(),
                        "Success!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(sendActivity.get().getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
