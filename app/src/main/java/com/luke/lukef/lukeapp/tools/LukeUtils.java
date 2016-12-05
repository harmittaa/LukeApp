package com.luke.lukef.lukeapp.tools;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Daniel on 05/12/2016.
 */

public class LukeUtils {
    public static String bitapToBase64String(Bitmap bmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
