package com.pogamadores.beaconwear.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;

import com.pogamadores.beaconwear.R;

public class QRCodeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        NotificationManagerCompat.from(this).cancel(1234321);
    }
}
