package com.pogamadores.beaconwear.service;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.pogamadores.beaconwear.util.WearableUtil;

import java.util.concurrent.TimeUnit;

public class ListenerService extends WearableListenerService {

    private static final String TAG = ListenerService.class.getSimpleName();
    private GoogleApiClient mGoogleClient;

    public ListenerService() {}

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        if(mGoogleClient == null) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();

            ConnectionResult connectionResult =
                    googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }
            mGoogleClient = googleApiClient;
        }


        for(DataEvent event : dataEvents)
        {
            if(event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/notification")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset asset = dataMapItem.getDataMap().getAsset("image");
                Uri message = Uri.parse(dataMapItem.getDataMap().getString("content"));

                if(asset != null) {
                    Bitmap icon = WearableUtil.loadBitmapFromAsset(mGoogleClient, asset);
                    WearableUtil.dispatchNotification(getApplicationContext(), message, icon);
                    icon.recycle();
                }

            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
    }
}
