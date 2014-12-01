package com.pogamadores.beaconwear.ui.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.ui.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private MainActivity mActivity = null;

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.findViewById(R.id.btn_alt_beacon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.changeFragment(new AltBeaconFragment());
            }
        });
        rootView.findViewById(R.id.btn_estimote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.changeFragment(new EstimoteFragment());
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof MainActivity)
            mActivity = (MainActivity)activity;
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }
}
