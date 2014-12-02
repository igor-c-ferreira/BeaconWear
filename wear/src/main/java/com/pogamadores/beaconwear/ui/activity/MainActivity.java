package com.pogamadores.beaconwear.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.pogamadores.beaconwear.R;

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {

    private GoogleApiClient mGoogleClient;
    private ProgressBar progress;
    private ScrollView contentScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = ((ProgressBar) findViewById(R.id.progress));
        contentScroll = (ScrollView)findViewById(R.id.content_card);

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e("TAG",connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();
        Wearable.DataApi.addListener(mGoogleClient, this);
        mGoogleClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(mGoogleClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                if(nodes != null && nodes.size() > 0) {
                    for(Node node : nodes) {
                        Wearable.MessageApi.sendMessage(mGoogleClient, node.getId(), "/get/image", null);
                    }
                }
            }
        });
    }
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for(DataEvent event : dataEvents) {
            if(event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/get/image")) {
                DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());
                final String description = dataItem.getDataMap().getString("description", "Tardis");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.description)).setText(description);
                        progress.setVisibility(View.GONE);
                        contentScroll.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}
