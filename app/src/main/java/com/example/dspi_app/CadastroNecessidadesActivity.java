package com.example.dspi_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CadastroNecessidadesActivity extends AppCompatActivity {

    private EditText inputNomeNecessidade, inputDescricaoNecessidade;
    private Button btnSalvarNecessidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_necessidades_cadastro); // Nome do seu XML

        inputNomeNecessidade = findViewById(R.id.inputNomeNecessidade);
        inputDescricaoNecessidade = findViewById(R.id.inputDescricaoNecessidade);
        btnSalvarNecessidade = findViewById(R.id.btnSalvarNecessidade);

        btnSalvarNecessidade.setOnClickListener(v -> enviarFormulario());
    }

    private void enviarFormulario() {
        String nome = inputNomeNecessidade.getText().toString().trim();
        String descricao = inputDescricaoNecessidade.getText().toString().trim();

        if (nome.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Pegamos o EMAIL salvo no login em vez do ID (já que o ID não vem da API)
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        String usuarioEmail = prefs.getString("email_logado", "");

        if (usuarioEmail.isEmpty()) {
            Toast.makeText(this, "Erro: Usuário não identificado. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        // Desativa o botão para evitar cliques duplos
        btnSalvarNecessidade.setEnabled(false);
        btnSalvarNecessidade.setText("Salvando...");

        String url = "https://api-dspi.whyguiih.workers.dev/necessidades-cadastro";

        JSONObject jsonBody = new JSONObject();
        try {
            // 2. Enviamos como "usuario" para bater exatamente com o que a API espera
            jsonBody.put("usuario", usuarioEmail);
            jsonBody.put("nome", nome);
            jsonBody.put("descricao", descricao);
        } catch (JSONException e) {
            Log.e("CadastroNec", "Erro ao montar JSON", e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, response -> {
            try {
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Necessidade cadastrada com sucesso!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String mensagem = response.optString("message", "Erro ao cadastrar.");
                    Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                    btnSalvarNecessidade.setEnabled(true);
                    btnSalvarNecessidade.setText("Salvar Necessidade");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                btnSalvarNecessidade.setEnabled(true);
                btnSalvarNecessidade.setText("Salvar Necessidade");
            }
        }, error -> {
            // Log mais detalhado para descobrirmos se ainda der erro
            String erroMsg = "Erro de conexão.";
            if (error.networkResponse != null) {
                erroMsg += " Status: " + error.networkResponse.statusCode;
                if (error.networkResponse.data != null) {
                    erroMsg += " Body: " + new String(error.networkResponse.data);
                }
            }
            Toast.makeText(this, "Falha no servidor: " + erroMsg, Toast.LENGTH_LONG).show();
            Log.e("CadastroNec", "Erro Volley: " + erroMsg, error);

            btnSalvarNecessidade.setEnabled(true);
            btnSalvarNecessidade.setText("Salvar Necessidade");
        }) {
            // ISSO AQUI FAZ TODA A DIFERENÇA PARA A API ENTENDER O JSON!
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Timeout estendido
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Adiciona a requisição na fila do Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}