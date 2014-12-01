package com.pogamadores.beaconwear.ui.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.ui.activity.MainActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Collection;

/**
 * A simple {@link Fragment} subclass.
 */
public class AltBeaconFragment extends Fragment implements BeaconConsumer, MonitorNotifier, RangeNotifier {

    private static final String TAG = AltBeaconFragment.class.getSimpleName();
    private BeaconManager beaconManager;
    private Region region;
    private Beacon beacon;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private BackgroundPowerSaver backgroundPowerSaver;
    private TextView txtBeaconInformation;
    private MainActivity mActivity;

    public AltBeaconFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_alt_beacon, container, false);
        backgroundPowerSaver = new BackgroundPowerSaver(getApplicationContext());
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

        //iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        beaconManager.setMonitorNotifier(this);
        beaconManager.setRangeNotifier(this);

        //5 seconds between scans
        beaconManager.setBackgroundBetweenScanPeriod(500);

        region = new Region("regionId", null, null, null);

        txtBeaconInformation = (TextView)root.findViewById(R.id.message_content);
        txtBeaconInformation.setText(getString(R.string.lbl_searching));
        return root;
    }

    @Override
    public void onDetach() {
        try {
            if(beaconManager != null && region != null) {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.unbind(this);
            }
        } catch (Exception exception) {
            Log.e(TAG, Log.getStackTraceString(exception));
        }
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            if(beaconManager != null && region != null)
                beaconManager.bind(this);
        } catch (Exception exception) {
            Log.e(TAG,Log.getStackTraceString(exception));
        }
        if(activity instanceof MainActivity)
            mActivity = (MainActivity)activity;
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (Exception exception) {
            Log.e(TAG,Log.getStackTraceString(exception));
        }
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return getActivity().bindService(intent, serviceConnection, i);
    }

    @Override
    public void didEnterRegion(Region region) {
        updateRegionInformation(region);
    }
    @Override
    public void didExitRegion(Region region) {}
    @Override
    public void didDetermineStateForRegion(int i, Region region) {}

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for(Beacon rangedBeacon : beacons) {
            if(beacon == null) {
                beacon = rangedBeacon;
                mActivity.dispatchNotification(
                        beacon.getId1().toString(),
                        beacon.getId2().toString(),
                        beacon.getId3().toString()
                );
            } else if(rangedBeacon.getId1().equals(beacon.getId1()))
                beacon = rangedBeacon;
            updateBeaconInformation(beacon);
        }
    }

    private void updateBeaconInformation(Beacon beacon) {
        updateInformation(false,
                beacon.getId1().toString(),
                beacon.getId2().toString(),
                beacon.getId3().toString(),
                beacon.getRssi(),
                beacon.getDistance());
    }

    private void updateRegionInformation(Region region) {
        updateInformation(true,
                region.getId1().toString(),
                region.getId2().toString(),
                region.getId3().toString(),
                0,
                0);
    }

    private void updateInformation(boolean region, String uuid, String major, String minor, int rssi, double distance) {
        final StringBuilder information = new StringBuilder();
        information.append((region?"region":"beacon"));
        information.append("\n");
        information.append(String.format("UUID: %s\nMajor:%s\nMinor:%s\n",uuid,major,minor));
        if(!region) {
            information.append(String.format("Distance: %f\n",distance));
            information.append(String.format("RSSI: %d\n",rssi));
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtBeaconInformation.setText(information.toString());
            }
        });
    }
}
