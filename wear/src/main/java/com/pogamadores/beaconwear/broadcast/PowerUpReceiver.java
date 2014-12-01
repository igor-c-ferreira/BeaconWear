package com.pogamadores.beaconwear.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pogamadores.beaconwear.util.WearableUtil;

public class PowerUpReceiver extends BroadcastReceiver {
    public PowerUpReceiver() {}
    @Override
    public void onReceive(Context context, Intent intent) {
        WearableUtil.startService(context, intent.getExtras());
    }
}
