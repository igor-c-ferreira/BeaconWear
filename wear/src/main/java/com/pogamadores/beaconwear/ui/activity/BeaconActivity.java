package com.pogamadores.beaconwear.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.util.WearableUtil;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Collection;

public class BeaconActivity extends Activity implements BeaconConsumer {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTextView;
    private BeaconManager beaconManager;
    private Region region;
    private Beacon beacon;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private BackgroundPowerSaver backgroundPowerSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.beacon_watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        WearableUtil.startService(this, savedInstanceState);
        backgroundPowerSaver = new BackgroundPowerSaver(getApplicationContext());
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

        //iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons != null && beacons.size() > 0) {
                    Beacon ranged = (Beacon)beacons.toArray()[0];
                    if(beacon == null || beacon.getId1().equals(ranged.getId1())) {
                        beacon = ranged;
                        BeaconActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(String.format(
                                        "UUID:%s\nDistance:%f",
                                        beacon.getId1().toString(),
                                        beacon.getDistance()
                                ));
                            }
                        });
                    }

                }
            }
        });

        //5 seconds between scans
        beaconManager.setBackgroundBetweenScanPeriod(500);

        region = new Region("regionId", null, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            if(beaconManager != null && region != null)
                beaconManager.bind(this);
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
    }

    @Override
    protected void onStop() {
        try {
            if(beaconManager != null && region != null) {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.unbind(this);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
        super.onStop();
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            BeaconActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText("Searching...");
                }
            });
        } catch (Exception exception) {
            Log.e(TAG,Log.getStackTraceString(exception));
        }
    }
}
