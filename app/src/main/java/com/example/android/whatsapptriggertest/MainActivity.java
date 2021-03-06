package com.example.android.whatsapptriggertest;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import static android.app.PendingIntent.getService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendNotice = (Button) findViewById(R.id.button_notice);
        Button whatsApp = (Button) findViewById(R.id.button_WhatsApp);
        sendNotice.setOnClickListener(this);
        whatsApp.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        String[] contactNames = {"Fanglin", "Edwin"};
        Intent launchService = new Intent (this, AutoSelectService.class);
        launchService.putExtra("contactNames", contactNames);

        switch (v.getId()) {

            case R.id.button_notice:
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("You just arrived home!")
                        .setContentText("Notify your contacts now!")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setPriority(Notification.PRIORITY_MAX) //为什么不work????
                        .setContentIntent(getService(this, 0, launchService, 0))
                        .setAutoCancel(true)
                        .build();
                manager.notify(1, notification);
                break;

            case R.id.button_WhatsApp:
                startService(launchService);
                break;

            default:
                break;
        }
    }
}