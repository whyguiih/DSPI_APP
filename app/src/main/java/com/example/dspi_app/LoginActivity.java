package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.splashscreen.SplashScreen;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // INSTALANDO A SPLASH
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Tela cheia (Edge-to-Edge) para o gradiente preencher até a bateria
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        // Previne que o teclado ou as barras cubram a interface
        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        EditText nome = findViewById(R.id.inputEmail);
        EditText senha = findViewById(R.id.inputSenha);


        // Evento de clique do Botão de Login
        Button btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> {
            String Pnome = nome.getText().toString();
            String Psenha = senha.getText().toString();

            // URL do seu script PHP (se for local, use o IP da sua máquina)
            String url = "http://192.168.0.140/api/login.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.contains("success")) {
                            Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Erro de conexão", Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("nome_usuarios", Pnome);
                    params.put("senha", Psenha);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(stringRequest);
        });
    }
}