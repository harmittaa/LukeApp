package com.luke.lukef.lukeapp.interfaces;

import android.graphics.Bitmap;

/**
 * Interface for receiving a bitmap from auth0
 */
public interface Auth0Responder {

    void receiveBitmapFromAuth0(Bitmap b);
}
