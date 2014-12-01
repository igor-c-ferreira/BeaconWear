package com.pogamadores.beaconwear.ui.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.ui.activity.MainActivity;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EstimoteFragment extends Fragment implements BeaconManager.ServiceReadyCallback {

    private static final String TAG = EstimoteFragment.class.getSimpleName();

    // Y positions are relative to height of bg_distance image.
    private static final double RELATIVE_START_POS = 320.0 / 1110.0;
    private static final double RELATIVE_STOP_POS = 885.0 / 1110.0;

    private BeaconManager beaconManager;
    private Beacon beacon;
    private Region region;

    private MainActivity mActivity;

    private View dotView;
    private int startY = -1;
    private int segmentLength = -1;

    public EstimoteFragment() {}

    @Override
    public void onStart() {
        super.onStart();
        beaconManager.connect(this);
    }

    @Override
    public void onStop() {
        beaconManager.disconnect();
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof MainActivity)
            mActivity = (MainActivity)activity;
        if(beaconManager != null)
            beaconManager.connect(this);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        if(beaconManager != null)
            beaconManager.disconnect();
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_estimote, container, false);
        dotView = root.findViewById(R.id.dot);

        region = new Region("regionid", null, null, null);
        beaconManager = new BeaconManager(getActivity());
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                // Note that results are not delivered on UI thread.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Just in case if there are multiple beacons with the same uuid, major, minor.
                        Beacon foundBeacon = null;
                        for (Beacon rangedBeacon : rangedBeacons) {
                            if(beacon == null) {
                                beacon = rangedBeacon;
                                mActivity.dispatchNotification(
                                        beacon.getProximityUUID(),
                                        String.valueOf(beacon.getMajor()),
                                        String.valueOf(beacon.getMinor())
                                );
                            }
                            if (rangedBeacon.getMacAddress().equals(beacon.getMacAddress()))
                                foundBeacon = rangedBeacon;
                            if (foundBeacon != null)
                                updateDistanceView(foundBeacon);
                        }
                    }
                });
            }
        });
        beaconManager.connect(this);
        final View view = root.findViewById(R.id.sonar);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                startY = (int) (RELATIVE_START_POS * view.getMeasuredHeight());
                int stopY = (int) (RELATIVE_STOP_POS * view.getMeasuredHeight());
                segmentLength = stopY - startY;

                dotView.setVisibility(View.VISIBLE);
                dotView.setTranslationY(computeDotPosY(beacon));
            }
        });
        return root;
    }

    @Override
    public void onServiceReady() {
        try {
            beaconManager.startRanging(region);
        } catch (RemoteException e) {
            Toast.makeText(getActivity(),
                    "Cannot start ranging, something terrible happened",
                    Toast.LENGTH_LONG).show();
            Log.e(TAG, "Cannot start ranging", e);
        }
    }

    private void updateDistanceView(Beacon foundBeacon) {
        if (segmentLength == -1)
            return;
        dotView.animate().translationY(computeDotPosY(foundBeacon)).start();
    }

    private int computeDotPosY(Beacon beacon) {
        if(beacon != null) {
            // Let's put dot at the end of the scale when it's further than 6m.
            double distance = Math.min(Utils.computeAccuracy(beacon), 6.0);
            return startY + (int) (segmentLength * (distance / 6.0));
        }
        return startY + (int) (segmentLength * 6.0);
    }

}
