package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =========================================================================
        // CHECA A SESSÃO: Se já estiver logado e no prazo de 10 min, pula o login!
        // =========================================================================
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        long tempoExpiracao = prefs.getLong("tempo_expiracao", 0);
        long tempoAtual = System.currentTimeMillis();

        if (tempoExpiracao > tempoAtual) {
            // Renova a sessão (ganha mais 10 min)
            prefs.edit().putLong("tempo_expiracao", tempoAtual + (10 * 60 * 1000)).apply();

            String nivel = prefs.getString("nivel_de_acesso", "");
            String email = prefs.getString("email_logado", "");

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("email_usuario", email);
            startActivity(intent);
            finish();
            return; // Impede a tela de login de carregar
        } else {
            // Se expirou, limpa a sessão antiga
            prefs.edit().clear().apply();
        }

        setContentView(R.layout.activity_login);

        // =========================================================================
        // MAPEAMENTO CORRETO DOS SEUS ELEMENTOS REAIS (Sem inventar botões)
        // =========================================================================
        editEmail = findViewById(R.id.inputEmail);
        editSenha = findViewById(R.id.inputSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> realizarLogin());
    }

    private void realizarLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api-dspi.whyguiih.workers.dev/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("senha", senha);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnEntrar.setEnabled(false);
        btnEntrar.setText("Aguarde...");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("ENTRAR");
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject userData = response.getJSONObject("data");
                            String nivelAcesso = userData.getString("nivel_de_acesso");

                            // Tenta pegar o "nome", se não vier, usa o email
                            String nomeLogado = userData.optString("nome", email);

                            // ==============================================================
                            // SALVA A SESSÃO APÓS O LOGIN COM SUCESSO (+ 10 MINUTOS)
                            // ==============================================================
                            SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email_logado", email);
                            editor.putString("nivel_de_acesso", nivelAcesso);
                            editor.putString("nome_usuario", nomeLogado);
                            editor.putLong("tempo_expiracao", System.currentTimeMillis() + (10 * 60 * 1000));
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("nivel_de_acesso", nivelAcesso);
                            intent.putExtra("email_usuario", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Erro: " + response.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar os dados.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("ENTRAR");
                    Toast.makeText(this, "Erro de conexão com o servidor.", Toast.LENGTH_LONG).show();
                });

        Volley.newRequestQueue(this).add(request);
    }
}