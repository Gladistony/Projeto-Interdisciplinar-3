package com.teste.projeto_3;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.teste.projeto_3.http.EnviarRequisicao;
import com.teste.projeto_3.model.User;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TelaPrincipal extends AppCompatActivity {
    EnviarRequisicao er;

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        // Cnfiguração para barras de status (ajuste de insets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialize o launcher da câmera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                            /*
                            // Salvar a imagem original na galeria
                            salvarImagemNaGaleria(imageBitmap);

                            // Converte o Bitmap para um array de bytes
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            // Codifica o array de bytes em Base64
                            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                            enviarImagemPerfil(encodedImage);*/
                            // Aplica o recorte circular
                            Bitmap circularBitmap = cropToCircle(imageBitmap);

                            ImageView imageView = findViewById(R.id.imageView);
                            if (imageView != null) {
                                imageView.setAlpha(0f);
                                imageView.setImageBitmap(circularBitmap);
                                imageView.animate().setDuration(500).alpha(1f).start();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Captura de imagem cancelada", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Configurar botão para abrir a câmera
        Button cameraButton = findViewById(R.id.ligarCamera);
        cameraButton.setOnClickListener(v -> abrirCamera());

        // Configurar informações do usuário logado
        er = new EnviarRequisicao(getApplicationContext());
        onLoggedIn();
    }

    public void enviarImagemPerfil(String encoded) {
        if (er.possuiInternet(getApplicationContext())) {
            try {
                User user = new User();
                user.setId(er.obterMemoriaInterna("idConexao"));
                user.setDestino("perfil");
                user.setFile(encoded);

                // Converter o objeto User para JSON
                Gson gson = new Gson();
                String userJson = gson.toJson(user);

                er.post("upload-image", userJson, response -> {
                    if (response.startsWith("Erro")) {
                        runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                    } else {
                        // Processar resposta da requisição
                        String a = response;
                        String b = "kjsdbdfjks";
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
            }
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    // Método para recortar a imagem em formato circular
    private Bitmap cropToCircle(Bitmap bitmap) {
        int width = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Desenha um círculo
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, paint);

        // Define o modo de recorte
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Desenha a imagem
        canvas.drawBitmap(bitmap, (width - bitmap.getWidth()) / 2f, (width - bitmap.getHeight()) / 2f, paint);

        return output;
    }

    private void abrirCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    // O usuário já negou antes, então mostramos um alerta explicando
                    mostrarDialogoPermissao();
                } else {
                    // Se for a primeira vez ou se o usuário não marcou "Não perguntar novamente"
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
                }
                return;
            }
        }

        // Se a permissão já foi concedida, abre a câmera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Câmera indisponível no dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoPermissao() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permissão necessária")
                .setMessage("O aplicativo precisa da permissão para acessar a câmera. Por favor, ative-a nas configurações.")
                .setPositiveButton("Ir para Configurações", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão para a câmera concedida", Toast.LENGTH_SHORT).show();
            } else {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    // Se o usuário marcou "Não perguntar novamente", mostramos um alerta
                    mostrarDialogoPermissao();
                } else {
                    Toast.makeText(this, "Permissão para a câmera negada", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void onLoggedIn() {
        TextView nome = findViewById(R.id.textNomeUsuario);
        TextView email = findViewById(R.id.textEmailUsuario);
        ImageView imageView = findViewById(R.id.imageView);
        Intent intentInfoLogin = getIntent();
        nome.setText(intentInfoLogin.getStringExtra("nome_completo"));
        email.setText(intentInfoLogin.getStringExtra("email"));
        String imagemUrl = intentInfoLogin.getStringExtra("url_foto");
        if (!imagemUrl.isEmpty()) {
            try {
                Picasso.get().load(intentInfoLogin.getStringExtra("url_foto")).into(imageView);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao definir a imagem de perfil", Toast.LENGTH_SHORT).show());
            }
        }
    }

    public void deslogar(View v) {
        if (er.possuiInternet(getApplicationContext())) {
            User userLogin = new User();
            userLogin.setId(er.obterMemoriaInterna("idConexao"));

            // Converter o objeto User para JSON
            Gson gson = new Gson();
            String userJson = gson.toJson(userLogin);

            er.post("logout", userJson, response -> {
                if (response.startsWith("Erro")) {
                    runOnUiThread(() -> Toast.makeText(this, response, Toast.LENGTH_LONG).show());
                } else {
                    try {
                        // Processar resposta da requisição
                        User responseLogin = gson.fromJson(response, User.class);
                        if (responseLogin.getMessage().equals("Usuario deslogado")) {
                            Intent intentLoginCadastro = new Intent(this, LoginCadastro.class);
                            startActivity(intentLoginCadastro);
                            finish();
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, "Erro ao deslogar", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao processar a resposta. Tente novamente.", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            runOnUiThread(() -> Toast.makeText(this, "Verifique sua conexão com a internet.", Toast.LENGTH_SHORT).show());
        }
    }

    public void abrirTelaProduto(View v){
        startActivity(new Intent(TelaPrincipal.this, TelaProdutos.class));
    }

    private void salvarImagemNaGaleria(Bitmap bitmap) {
        String nomeArquivo = "IMG_" + System.currentTimeMillis() + ".jpg";
        OutputStream fos;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                String imagensDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File image = new File(imagensDir, nomeArquivo);
                fos = new FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            //runOnUiThread(() -> Toast.makeText(this, "Imagem salva na galeria", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            //runOnUiThread(() -> Toast.makeText(this, "Erro ao salvar a imagem", Toast.LENGTH_SHORT).show());
        }
    }


}