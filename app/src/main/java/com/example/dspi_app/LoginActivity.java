package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.splashscreen.SplashScreen;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        EditText nome = findViewById(R.id.inputEmail);
        EditText senha = findViewById(R.id.inputSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> {
            String Pnome = nome.getText().toString().trim();
            String Psenha = senha.getText().toString().trim();

            if (Pnome.isEmpty() || Psenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ATENÇÃO: COLOQUE A SUA URL AQUI
            String url = "https://api-dspi.whyguiih.workers.dev/";

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("nome_usuarios", Pnome);
                jsonBody.put("senha", Psenha);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                String nivel = response.getString("nivel");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("nivel_de_acesso", nivel);
                                startActivity(intent);
                                finish();
                            } else {
                                // Agora vai ler a mensagem do Worker e mostrar no ecrã!
                                String mensagem = response.optString("message", "Login falhou");
                                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Erro no formato da resposta", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
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