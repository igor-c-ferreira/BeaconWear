package com.pogamadores.beaconwear.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.pogamadores.beaconwear.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BuyActivity extends Activity implements MessageApi.MessageListener {

    @SuppressWarnings("FieldCanBeLocal")
    private GoogleApiClient mGoogleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        NotificationManagerCompat.from(this).cancel(12345);

        final String message;
        if(savedInstanceState != null && savedInstanceState.containsKey("message")) {
            message = "buy:" + savedInstanceState.getString("message","");
        } else {
            message = "buy:";
        }

        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Wearable.MessageApi.addListener(mGoogleClient, BuyActivity.this);
                        Wearable.NodeApi.getConnectedNodes(mGoogleClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                List<Node> nodes = getConnectedNodesResult.getNodes();
                                if(nodes != null && nodes.size() > 0) {
                                    for(Node node : nodes) {
                                        Wearable.MessageApi.sendMessage(mGoogleClient, node.getId(), message ,null);
                                    }
                                }
                            }
                        });
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        ((TextView)findViewById(R.id.txt_information)).setText("Erro na compra.\nPor favor, tente de seu celular");
                    }
                })
                .build();
        mGoogleClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals("/buy/ok")) {
            startActivity(new Intent(this,QRCodeActivity.class));
            finish();
        }
    }
}
