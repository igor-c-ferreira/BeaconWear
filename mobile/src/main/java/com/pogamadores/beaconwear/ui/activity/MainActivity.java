package com.pogamadores.beaconwear.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.broadcast.BuyReceiver;
import com.pogamadores.beaconwear.ui.fragment.MainFragment;
import com.pogamadores.beaconwear.util.Util;

import java.util.List;


public class MainActivity extends Activity implements NodeApi.NodeListener {
    private static final int NOTIFICATION_ID = 12345;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isPeerConnected;
    private String nodeId;
    private GoogleApiClient mGoogleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }

        isPeerConnected = false;
        nodeId = null;
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);

                        Wearable.NodeApi.addListener(mGoogleClient, MainActivity.this);
                        Wearable.MessageApi.addListener(mGoogleClient, new MessageApi.MessageListener() {
                            @Override
                            public void onMessageReceived(MessageEvent messageEvent) {
                                if(messageEvent.getPath().equals("/get/image")) {
                                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/get/image");
                                    putDataMapRequest.getDataMap().putLong("timestamp",System.currentTimeMillis());
                                    putDataMapRequest.getDataMap().putString("description",getString(R.string.msg_notification));
                                    putDataMapRequest.getDataMap().putAsset("image", Util.createAssetFromBitmap(
                                            BitmapFactory.decodeResource(getResources(), R.drawable.tardis)
                                    ));
                                    Wearable.DataApi.putDataItem(mGoogleClient, putDataMapRequest.asPutDataRequest());
                                } else
                                    Wearable.MessageApi.sendMessage(mGoogleClient, messageEvent.getSourceNodeId(), "/buy/ok", null);
                            }
                        });

                        // Now you can use the Data Layer API
                        Wearable.NodeApi.getConnectedNodes(mGoogleClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult result) {
                                List<Node> nodes = result.getNodes();
                                if(nodes != null && nodes.size() > 0) {
                                    isPeerConnected = true;
                                    nodeId = nodes.get(0).getId();
                                } else {
                                    isPeerConnected = false;
                                    nodeId = null;
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        mGoogleClient.connect();
    }

    public void changeFragment(Fragment newInstance)
    {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, newInstance)
                .addToBackStack(null)
                .commit();
    }

    public void dispatchNotification(String uuid, String major, String minor)
    {
        if(isPeerConnected)
            dispatchMessage(uuid, major, minor);
        else
            dispatchGlobalNotification(uuid, major, minor);
    }

    public void dispatchMessage(String uuid, String major, String minor)
    {
        Uri.Builder builder = new Uri.Builder()
                .scheme("http")
                .authority("igorcferreira.com")
                .appendQueryParameter("cat","10")
                .appendQueryParameter("uuid",uuid)
                .appendQueryParameter("major",major)
                .appendQueryParameter("minor",minor)
                .appendQueryParameter("message",getString(R.string.msg_notification));

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/notification");
        putDataMapRequest.getDataMap().putLong("DataStamp",System.currentTimeMillis());
        putDataMapRequest.getDataMap().putString("content",builder.toString());
        putDataMapRequest.getDataMap().putAsset("image", Util.createAssetFromBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.tardis)
        ));

        Wearable.DataApi.putDataItem(mGoogleClient, putDataMapRequest.asPutDataRequest());
    }

    public void dispatchGlobalNotification(String uuid, String major, String minor)
    {

        Bundle infoBundle = new Bundle();
        infoBundle.putString("UUID",uuid);
        infoBundle.putString("Major",major);
        infoBundle.putString("Minor",minor);

        PendingIntent buyIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), BuyReceiver.class).putExtras(infoBundle),
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        PendingIntent openIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT,
                infoBundle
        );

        Bitmap item = BitmapFactory.decodeResource(getResources(),R.drawable.tardis);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(item)
                .setContentTitle("Nova promoção!")
                .setContentText("Aproveite essa promoção")
                .setContentIntent(openIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.favicon, "Comprar", buyIntent)
                .setCategory(NotificationCompat.CATEGORY_PROMO)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationCompat.BigPictureStyle imgDetail = new NotificationCompat.BigPictureStyle();
        imgDetail.bigPicture(item);
        Notification imgDetailPage = new NotificationCompat.Builder(getApplicationContext())
                .setStyle(imgDetail)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        NotificationCompat.BigTextStyle textDetail = new NotificationCompat.BigTextStyle();
        textDetail.bigText(getString(R.string.msg_notification));
        Notification textDetailPage = new NotificationCompat.Builder(getApplicationContext())
                .setStyle(textDetail)
                .build();

        Notification fullNotification = new NotificationCompat.WearableExtender()
                .addPage(imgDetailPage)
                .addPage(textDetailPage)
                .extend(builder)
                .build();

        fullNotification.defaults |= Notification.DEFAULT_SOUND;
        fullNotification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(NOTIFICATION_ID,fullNotification);

        item.recycle();
    }

    @Override
    public void onPeerConnected(Node node) {
        if(!isPeerConnected) {
            isPeerConnected = true;
            nodeId = node.getId();
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        if(isPeerConnected && node.getId().equals(nodeId))
        {
            isPeerConnected = false;
            nodeId = null;
        }
    }
}