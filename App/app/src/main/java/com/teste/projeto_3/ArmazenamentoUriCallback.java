package com.teste.projeto_3;

import android.net.Uri;

public class ArmazenamentoUriCallback {
    private static ArmazenamentoUriCallback instance;
    private Uri uri;

    private CameraGaleria.CallbackCameraGaleria callbackCameraGaleria;

    private ArmazenamentoUriCallback() {}

    public static synchronized ArmazenamentoUriCallback getInstance() {
        if (instance == null) {
            instance = new ArmazenamentoUriCallback();
        }
        return instance;
    }

    public Uri getUri() { return uri; }
    public void setUri(Uri uri) { this.uri = uri; }

    public CameraGaleria.CallbackCameraGaleria getCallbackCameraGaleria() { return callbackCameraGaleria; }
    public void setCallbackCameraGaleria(CameraGaleria.CallbackCameraGaleria callbackCameraGaleria) { this.callbackCameraGaleria = callbackCameraGaleria; }
}
