package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;

public class PerfilActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 3;
    private String nivel;
    private EditText inputNome, inputEmail;
    private ImageView imgPerfil;
    private String fotoBase64 = "";
    private String emailAntigo = "";

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        // 1. Carrega o Bitmap original
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap srcBitmap = BitmapFactory.decodeStream(inputStream);
                        if (inputStream != null) inputStream.close();

                        // 2. CORREÇÃO DEFINITIVA DE ORIENTAÇÃO
                        int degrees = getOrientationDegrees(imageUri);
                        Bitmap finalBitmap = rotateBitmap(srcBitmap, degrees);

                        // 3. Define o raio de curvatura dos cantos convertido para Pixels (ex: 16dp)
                        int radiusPx = (int) (16 * getResources().getDisplayMetrics().density);

                        // 4. Exibe no Glide perfeitamente quadrado com cantos arredondados
                        Glide.with(this)
                                .load(finalBitmap)
                                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                                .into(imgPerfil);

                        imgPerfil.setPadding(0, 0, 0, 0);

                        // Salva o Base64 já com a rotação corrigida em pé
                        fotoBase64 = bitmapToBase64(finalBitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao selecionar imagem", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_perfil);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        nivel = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("nivel_de_acesso", "5");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        inputNome = findViewById(R.id.inputNome);
        inputEmail = findViewById(R.id.inputEmail);
        imgPerfil = findViewById(R.id.imgPerfil);
        ImageButton btnBack = findViewById(R.id.btnBack);
        View btnSalvar = findViewById(R.id.btnSalvar);
        View btnAlterarFoto = findViewById(R.id.btnAlterarFoto);
        TextView btnRemoverFoto = findViewById(R.id.btnRemoverFoto);

        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        emailAntigo = prefs.getString("email_logado", "email@exemplo.com");
        inputNome.setText(prefs.getString("nome_usuario", "Nome do Usuário"));
        inputEmail.setText(emailAntigo);

        fotoBase64 = prefs.getString("foto_usuario", "");

        int radiusPx = (int) (16 * getResources().getDisplayMetrics().density);

        if (!fotoBase64.isEmpty()) {
            if (fotoBase64.startsWith("http")) {
                Glide.with(this)
                        .load(fotoBase64)
                        .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                        .into(imgPerfil);
                imgPerfil.setPadding(0, 0, 0, 0);
            } else {
                byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                Glide.with(this)
                        .load(decodedByte)
                        .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                        .into(imgPerfil);
                imgPerfil.setPadding(0, 0, 0, 0);
            }
        }

        btnBack.setOnClickListener(v -> finish());

        btnAlterarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Dentro do onCreate, na ação do btnRemoverFoto:
        btnRemoverFoto.setOnClickListener(v -> {
            fotoBase64 = "";

            Glide.with(this)
                    .load(R.drawable.ic_conta)
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .into(imgPerfil);

            // Adiciona um pequeno recuo apenas para o ícone padrão ficar centralizado e bonito
            int innerPadding = (int) (16 * getResources().getDisplayMetrics().density);
            imgPerfil.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
        });

        btnSalvar.setOnClickListener(v -> {
            String novoNome = inputNome.getText().toString().trim();
            String novoEmail = inputEmail.getText().toString().trim();

            if (novoNome.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Alterações")
                    .setMessage("Deseja realmente salvar as alterações no seu perfil?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        enviarParaAPI(novoNome, novoEmail, fotoBase64);
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
    }

    // MÉTODO AUXILIAR: Detecta rotação por múltiplos caminhos (Provedor de Mídia ou Metadados EXIF)
    private int getOrientationDegrees(Uri uri) {
        // Caminho 1: Tenta ler a coluna de orientação diretamente do banco de dados do Android
        String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int colIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
                if (colIndex != -1) {
                    return cursor.getInt(colIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Caminho 2: Fallback para leitura física de cabeçalho EXIF se o caminho 1 falhar
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // MÉTODO AUXILIAR: Gira mecanicamente o bitmap com base nos graus encontrados
    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void enviarParaAPI(String nome, String email, String foto) {
        String url = "https://api-dspi.whyguiih.workers.dev/atualizar-perfil";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email_atual", emailAntigo);
            jsonBody.put("novo_nome", nome);
            jsonBody.put("novo_email", email);
            jsonBody.put("foto_perfil", foto);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String urlFotoR2 = response.has("foto_url") ? response.getString("foto_url") : foto;

                            SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("nome_usuario", nome);
                            editor.putString("email_logado", email);
                            editor.putString("foto_usuario", urlFotoR2);
                            editor.apply();

                            Toast.makeText(this, "Perfil updated com sucesso!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "Erro ao atualizar no servidor", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}