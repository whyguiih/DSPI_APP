package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class                       PerfilActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 3; // Mantém a aba "Conta" ativa
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
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        
                        Glide.with(this)
                                .load(bitmap)
                                .apply(RequestOptions.circleCropTransform())
                                .into(imgPerfil);

                        imgPerfil.setPadding(0, 0, 0, 0);
                        fotoBase64 = bitmapToBase64(bitmap);
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

        // Carregar dados atuais
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        emailAntigo = prefs.getString("email_logado", "email@exemplo.com");
        inputNome.setText(prefs.getString("nome_usuario", "Nome do Usuário"));
        inputEmail.setText(emailAntigo);
        fotoBase64 = prefs.getString("foto_usuario", "");

        if (!fotoBase64.isEmpty()) {
            if (fotoBase64.startsWith("http")) {
                Glide.with(this)
                        .load(fotoBase64)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imgPerfil);
                imgPerfil.setPadding(0, 0, 0, 0);
            } else {
                    byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    
                    Glide.with(this)
                            .load(decodedByte)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imgPerfil);
                            
                    imgPerfil.setPadding(0, 0, 0, 0);
            }
        }

        btnBack.setOnClickListener(v -> finish());

        btnAlterarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        btnSalvar.setOnClickListener(v -> {
            String novoNome = inputNome.getText().toString().trim();
            String novoEmail = inputEmail.getText().toString().trim();

            if (novoNome.isEmpty() || novoEmail.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            enviarParaAPI(novoNome, novoEmail, fotoBase64);
        });
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
                            SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("nome_usuario", nome);
                            editor.putString("email_logado", email);
                            editor.putString("foto_usuario", foto);
                            editor.apply();

                            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
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