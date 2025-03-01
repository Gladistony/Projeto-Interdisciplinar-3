package com.teste.projeto_3;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraGaleria {
    private final Context context;
    private Uri imagemUri;
    private final ActivityResultLauncher<Intent> cameraLauncher;
    private final ActivityResultLauncher<Intent> galeriaLauncher;
    private final ActivityResultLauncher<String> requestPermissionGalleryLauncher;
    private final ActivityResultLauncher<String> requestPermissionCameraLauncher;

    public CameraGaleria(Context context,
                         ActivityResultLauncher<Intent> cameraLauncher,
                         ActivityResultLauncher<Intent> galeriaLauncher,
                         ActivityResultLauncher<String> requestPermissionGalleryLauncher,
                         ActivityResultLauncher<String> requestPermissionCameraLauncher) {
        this.context = context;
        this.cameraLauncher = cameraLauncher;
        this.galeriaLauncher = galeriaLauncher;
        this.requestPermissionGalleryLauncher = requestPermissionGalleryLauncher;
        this.requestPermissionCameraLauncher = requestPermissionCameraLauncher;
    }

    public void pedirPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionCameraLauncher.launch(Manifest.permission.CAMERA);
        } else {
            abrirCamera();
        }
    }

    public void pedirPermissaoGaleria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                requestPermissionGalleryLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else { // Android 12 ou inferior
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                requestPermissionGalleryLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    public void abrirCamera() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentCamera.resolveActivity(context.getPackageManager()) != null) {
            File imagem = null;
            try {
                imagem = criarImagem();
            } catch (IOException ex) {
                Log.d("Erro CameraGaleria", "Erro ao criar a imagem.");
            }

            if (imagem != null) {
                imagemUri = FileProvider.getUriForFile(context, "com.teste.projeto_3", imagem);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imagemUri);
                cameraLauncher.launch(intentCamera);
            }
        }
    }

    public void abrirGaleria() {
        Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intentGaleria);
    }

    private File criarImagem() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nomeArquivoImagem = "JPEG_" + timeStamp + "_";
        File diretorio = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nomeArquivoImagem, ".jpg", diretorio);
    }

    public Uri getImagemUri() {
        return imagemUri;
    }

    public String converterUriParaBase64(Uri imageUri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // Verifica o tamanho da imagem
            int tamanhoImagem = inputStream.available();

            // Carrega a imagem como Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            // Corrige a orientação da imagem
            bitmap = rotacionarImagemCheck(context, bitmap, imageUri);

            // Se a imagem for maior que 1 MB, redimensiona
            if (tamanhoImagem > 1048576) { // 1 MB = 1048576 bytes
                bitmap = resizeBitmap(bitmap, 1048576);
            }

            // Converte o Bitmap para Base64
            return bitmapParaBase64(bitmap);
        } catch (IOException e) {
            Log.d("Erro CameraGaleria", "Erro ao converter a imagem para Base64.");
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.d("Erro CameraGaleria", "Erro ao fechar o InputStream.");
                }
            }
        }
    }

    public Bitmap rotacionarImagemCheck(Context context, Bitmap bitmap, Uri imageUri) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return bitmap;
            }

            // Verifica a orientação da imagem
            ExifInterface exifInterface = new ExifInterface(inputStream);
            int orientacao = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );

            // Aplica a rotação se for necessário
            switch (orientacao) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotacionarBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotacionarBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotacionarBitmap(bitmap, 270);
                default:
                    return bitmap;
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.d("Erro CameraGaleria", "Erro ao fechar o InputStream.");
                }
            }
        }
    }

    public Bitmap rotacionarBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap resizeBitmap(Bitmap bitmap, long tamanhoMaximoBytes) {
        int largura = bitmap.getWidth();
        int altura = bitmap.getHeight();
        float bitmapRatio = (float) largura / (float) altura;
        long tamanhoAtual = getBitmapSize(bitmap);

        while (tamanhoAtual > tamanhoMaximoBytes) {
            largura = (int) (largura / 1.5);
            altura = (int) (largura / bitmapRatio);
            bitmap = Bitmap.createScaledBitmap(bitmap, largura, altura, true);
            tamanhoAtual = getBitmapSize(bitmap);
        }

        return bitmap;
    }

    public long getBitmapSize(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            Log.d("Erro CameraGaleria", "Erro ao fechar o ByteArrayOutputStream.");
        }
        return byteArray.length;
    }

    public String bitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int qualidade = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Verifica se o tamanho da string Base64 irá possuir mais de um milhão de caracteres.
        // Se tiver, diminui a qualidade até que seja menor.
        while ((byteArray.length * 4 / 3) > 1000000 && qualidade > 70) {
            byteArrayOutputStream.reset();
            qualidade -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();
        }

        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            Log.d("Erro CameraGaleria", "Erro ao fechar o ByteArrayOutputStream.");
        }
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void deletarImagemUri(Uri imageUri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(imageUri, null, null);
        } catch (Exception e) {
            Log.d("Erro CameraGaleria", "Erro ao deletar imagem.");
        }
    }
}
