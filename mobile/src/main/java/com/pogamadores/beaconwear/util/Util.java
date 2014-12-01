package com.pogamadores.beaconwear.util;

import android.graphics.Bitmap;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;

/**
 * Created by igorferreira on 11/30/14.
 */
public class Util
{
    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}
