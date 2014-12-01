package com.pogamadores.beaconwear.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;
import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.service.ListenerService;
import com.pogamadores.beaconwear.ui.activity.BuyActivity;
import com.pogamadores.beaconwear.ui.activity.MainActivity;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by igorferreira on 11/30/14.
 */
public class WearableUtil
{
    private static final int NOTIFICATION_ID = 12345;

    public static void startService(Context context, @Nullable Bundle extras)
    {
        Intent backgroundService = new Intent(context, ListenerService.class);
        backgroundService.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(extras != null)
            backgroundService.putExtras(extras);
        context.startService(backgroundService);
    }

    public static void dispatchNotification(Context context, Uri information, Bitmap image)
    {
        Bundle infoBundle = new Bundle();

        String message = information.getQueryParameter("message");

        PendingIntent buyIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, BuyActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT,
                infoBundle
        );

        PendingIntent openIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT,
                infoBundle
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(image)
                .setContentTitle("Nova promoção!")
                .setContentText("Aproveite essa promoção")
                .setAutoCancel(true)
                .addAction(R.drawable.favicon, "Comprar", buyIntent)
                .addAction(R.drawable.ic_launcher, "Visualizar", openIntent)
                .setLocalOnly(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationCompat.BigPictureStyle imgDetail = new NotificationCompat.BigPictureStyle();
        imgDetail.bigPicture(image);
        Notification imgDetailPage = new NotificationCompat.Builder(context)
                .setStyle(imgDetail)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        NotificationCompat.BigTextStyle textDetail = new NotificationCompat.BigTextStyle();
        textDetail.bigText(message);
        Notification textDetailPage = new NotificationCompat.Builder(context)
                .setStyle(textDetail)
                .build();

        Notification fullNotification = new NotificationCompat.WearableExtender()
                .addPage(imgDetailPage)
                .addPage(textDetailPage)
                .extend(builder)
                .build();

        fullNotification.defaults |= Notification.DEFAULT_SOUND;
        fullNotification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID,fullNotification);
    }

    public static Bitmap loadBitmapFromAsset(GoogleApiClient client, Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                client.blockingConnect(300, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                client, asset).await().getInputStream();
        client.disconnect();

        if (assetInputStream == null) {
            Log.w("WearableUtil", "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
