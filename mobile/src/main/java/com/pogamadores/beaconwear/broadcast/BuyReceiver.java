package com.pogamadores.beaconwear.broadcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.pogamadores.beaconwear.R;
import com.pogamadores.beaconwear.ui.activity.QRCodeActivity;

public class BuyReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 12345;

    public BuyReceiver() {}
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bitmap outPutBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.qr_code);
        dispatchNotification(context, outPutBitmap);
        outPutBitmap.recycle();
    }

    private void dispatchNotification(Context context, Bitmap bitmap) {

        PendingIntent openIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, QRCodeActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.BigPictureStyle imgDetail = new NotificationCompat.BigPictureStyle();
        imgDetail.bigPicture(bitmap);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentTitle("Oferta comprada")
                .setContentText("Use este QRCode para buscar sua compra")
                .setContentIntent(openIntent)
                .setStyle(imgDetail)
                .setCategory(NotificationCompat.CATEGORY_PROMO)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification imgDetailPage = new NotificationCompat.Builder(context)
                .setStyle(imgDetail)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        Notification fullNotification = new NotificationCompat.WearableExtender()
                .addPage(imgDetailPage)
                .extend(builder)
                .build();

        fullNotification.defaults |= Notification.DEFAULT_SOUND;
        fullNotification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(NOTIFICATION_ID);
        manager.notify(1234321,fullNotification);
    }
}
