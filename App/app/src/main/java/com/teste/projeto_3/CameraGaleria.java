package com.teste.projeto_3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraGaleria {
    private final Activity activity;
    private Uri imagemUri;
    private final ActivityResultLauncher<Intent> cameraLauncher;
    private final ActivityResultLauncher<Intent> galleryLauncher;
    private final ActivityResultLauncher<String> requestPermissionGalleryLauncher;
    private final ActivityResultLauncher<String> requestPermissionCameraLauncher;
    private CallbackCameraGaleria callbackCameraGaleria;

    public CameraGaleria(Activity activity, ActivityResultRegistry registry, LifecycleOwner lifecycleOwner) {
        this.activity = activity;

        this.cameraLauncher = registry.register("cameraLauncher", lifecycleOwner,
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri imagemUri = ArmazenamentoUriCallback.getInstance().getUri();
                        if (imagemUri != null) {
                            try {
                                setCallbackCameraGaleria(ArmazenamentoUriCallback.getInstance().getCallbackCameraGaleria());
                                callbackCameraGaleria.onImageSelected(imagemUri);
                            } catch (Exception e) {
                                Log.e("Erro CameraGaleria", "Erro ao processar a imagem da câmera", e);
                                activity.runOnUiThread(() ->
                                        Toast.makeText(activity.getApplicationContext(), "Erro ao processar a imagem da câmera", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                });

        this.galleryLauncher = registry.register("galleryLauncher", lifecycleOwner,
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uriFotoPerfil = result.getData().getData();
                        if (uriFotoPerfil != null) {
                            try {
                                String imagemBase64 = converterUriParaBase64(uriFotoPerfil);
                                if (callbackCameraGaleria != null) {
                                    callbackCameraGaleria.onImageSelected(uriFotoPerfil);
                                }
                            } catch (Exception e) {
                                Log.e("Erro CameraGaleria", "Erro ao carregar imagem da galeria", e);
                                activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                });

        this.requestPermissionGalleryLauncher = registry.register("requestPermissionGalleryLauncher", lifecycleOwner,
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        abrirGaleria();
                    } else {
                        activity.runOnUiThread(() -> popupPermissao("Acesso à galeria negado",
                                "É necessário permissão para acessar a galeria ao executar esta ação. Vá para as configurações e permita manualmente."));
                    }
                });

        this.requestPermissionCameraLauncher = registry.register("requestPermissionCameraLauncher", lifecycleOwner,
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                            abrirCamera(callbackCameraGaleria);
                    } else {
                        activity.runOnUiThread(() -> popupPermissao("Acesso à câmera negado",
                                "É necessário permissão para acessar a câmera ao executar esta ação. Vá para as configurações e permita manualmente."));
                    }
                });
    }

    public interface CallbackCameraGaleria {
        void onImageSelected(Uri uri);
    }

    public CallbackCameraGaleria getCallbackCameraGaleria() {
        return callbackCameraGaleria;
    }

    public void setCallbackCameraGaleria(CallbackCameraGaleria callbackCameraGaleria) {
        this.callbackCameraGaleria = callbackCameraGaleria;
    }

    public void pedirPermissaoCamera(CallbackCameraGaleria callbackCameraGaleria) {
        try {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionCameraLauncher.launch(Manifest.permission.CAMERA);
            } else {
                abrirCamera(callbackCameraGaleria);
            }
        } catch (Exception e) {
            Log.e("Erro CameraGaleria", "Erro ao pedir permissão para câmera", e);
        }
    }

    public void pedirPermissaoGaleria() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria();
                } else {
                    requestPermissionGalleryLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else { // Android 12 ou inferior
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria();
                } else {
                    requestPermissionGalleryLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        } catch (Exception e) {
            Log.e("Erro CameraGaleria", "Erro ao pedir permissão para galeria", e);
        }
    }

    public void abrirCamera(CallbackCameraGaleria ccg) {
        try {
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intentCamera.resolveActivity(activity.getPackageManager()) != null) {
                File imagem = criarImagem();
                Uri imagemUri = FileProvider.getUriForFile(activity, "com.teste.projeto_3", imagem);
                ArmazenamentoUriCallback.getInstance().setUri(imagemUri);
                ArmazenamentoUriCallback.getInstance().setCallbackCameraGaleria(ccg);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imagemUri);
                cameraLauncher.launch(intentCamera);
            }
        } catch (Exception e) {
            Log.e("Erro CameraGaleria", "Erro ao abrir a câmera", e);
        }
    }

    public void abrirGaleria() {
        try {
            Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intentGaleria);
        } catch (Exception e) {
            Log.e("Erro CameraGaleria", "Erro ao abrir a galeria", e);
        }
    }

    private File criarImagem() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nomeArquivoImagem = "JPEG_" + timeStamp + "_";
        File diretorio = activity.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nomeArquivoImagem, ".jpg", diretorio);
    }

    public void setImagemUri(Uri imagemUri) {
        this.imagemUri = imagemUri;
    }

    public Uri getImagemUri() {
        return imagemUri;
    }

    public String converterUriParaBase64(Uri imageUri) {
        InputStream inputStream = null;
        try {
            inputStream = activity.getApplicationContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // Verifica o tamanho da imagem
            int tamanhoImagem = inputStream.available();

            // Carrega a imagem como Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getApplicationContext().getContentResolver(), imageUri);

            // Corrige a orientação da imagem
            bitmap = rotacionarImagemCheck(activity.getApplicationContext(), bitmap, imageUri);

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
            ContentResolver contentResolver = activity.getApplicationContext().getContentResolver();
            contentResolver.delete(imageUri, null, null);
        } catch (Exception e) {
            Log.d("Erro CameraGaleria", "Erro ao deletar imagem.");
        }
    }

    public void popupPermissao(String titulo, String mensagem) {
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("Ir para configurações", (dialogConfirmar, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getApplicationContext().getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialogCancelar, which) -> dialogCancelar.dismiss())
                .create();

        // Altera a cor do botão exibido
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.green2));
            negativeButton.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.modern_red));
        });

        dialog.show();
    }
}
