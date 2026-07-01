package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        credentialManager = CredentialManager.create(this);

        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        EditText nome = findViewById(R.id.inputEmail);
        EditText senha = findViewById(R.id.inputSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnGoogle = findViewById(R.id.Google);

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> loginComGoogle());
        }

        if (btnEntrar != null) {
            btnEntrar.setOnClickListener(v -> {
                String Pnome = nome.getText().toString().trim();
                String Psenha = senha.getText().toString().trim();

                if (Pnome.isEmpty() || Psenha.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://api-dspi.whyguiih.workers.dev/login";
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("nome_usuarios", Pnome);
                    jsonBody.put("senha", Psenha);
                } catch (JSONException e) {
                    Log.e("LoginActivity", "Erro ao montar JSON de login", e);
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String nivel = response.getString("nivel");
                            String email = response.optString("email", response.optString("email_usuario", ""));
                            String foto = response.optString("foto_perfil", response.optString("foto_usuario", ""));
                            String nomeExibicao = response.optString("nome_usuarios", response.optString("nome_usuario", Pnome));

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("nivel_de_acesso", nivel);
                            intent.putExtra("email_usuario", email);

                            getSharedPreferences("SESSAO_USER", MODE_PRIVATE).edit()
                                    .putString("email_logado", email)
                                    .putString("nome_usuario", nomeExibicao)
                                    .putString("foto_usuario", foto)
                                    .putString("nivel_de_acesso", nivel)
                                    .apply();

                            startActivity(intent);
                            finish();
                        } else {
                            String mensagem = response.optString("message", "Login falhou");
                            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Erro no formato da resposta", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    Log.e("VOLLEY_ERROR", "Erro de Servidor: " + error.toString());
                    Toast.makeText(this, "Erro interno do servidor. Tente mais tarde.", Toast.LENGTH_LONG).show();
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                Volley.newRequestQueue(this).add(jsonObjectRequest);
            });
        }
    }

    private void loginComGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("74823722629-sbp45df3jbncfrkrmj5vpab9qvdfj8m2.apps.googleusercontent.com")
                .setAutoSelectEnabled(true)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(this, request, null, ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        try {
                            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
                            String idToken = googleIdTokenCredential.getIdToken();

                            Log.d("GOOGLE_LOGIN", "Token recebido: " + idToken);
                            enviarTokenParaAPI(idToken);

                        } catch (Exception e) {
                            Log.e("GOOGLE_LOGIN", "Erro ao converter token: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e("GOOGLE_LOGIN", "Erro no fluxo de login", e);
                        Toast.makeText(LoginActivity.this, "O login com Google foi cancelado ou falhou.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void enviarTokenParaAPI(String idToken) {
        String url = "https://api-dspi.whyguiih.workers.dev/login-google";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idToken", idToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {

                            // AGORA PUXA O NÍVEL QUE VEIO DA API (antes estava chumbado "6")
                            String nivel = response.optString("nivel", "6");

                            String email = response.optString("email", response.optString("email_usuario", ""));
                            String nome = response.optString("nome_usuarios", response.optString("nome_usuario", email));
                            String foto = response.optString("foto_perfil", response.optString("foto_usuario", ""));

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("nivel_de_acesso", nivel); // Usa o nível dinâmico
                            intent.putExtra("email_usuario", email);

                            getSharedPreferences("SESSAO_USER", MODE_PRIVATE).edit()
                                    .putString("email_logado", email)
                                    .putString("nome_usuario", nome)
                                    .putString("foto_usuario", foto)
                                    .putString("nivel_de_acesso", nivel) // Usa o nível dinâmico
                                    .apply();

                            startActivity(intent);
                            finish();
                        } else {
                            String mensagem = response.optString("message", "Falha na autenticação com o Google");
                            Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, "Erro na resposta do servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VOLLEY_ERROR", "Erro de Servidor: " + error.toString());
                    Toast.makeText(LoginActivity.this, "Erro ao validar conta Google no servidor.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}