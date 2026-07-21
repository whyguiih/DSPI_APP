package com.example.dspi_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarregamentoCurriculoActivity extends AppCompatActivity {

    private String nomeUsuario;
    private String emailUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carregamento_curriculo); // Layout com a sua animação do drawable

        nomeUsuario = getIntent().getStringExtra("NOME_USUARIO");
        emailUsuario = getIntent().getStringExtra("EMAIL_USUARIO");

        // Animação giratória no ícone de loading
        ImageView imgLoading = findViewById(R.id.imgLoading);
        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
        imgLoading.startAnimation(rotate);

        // Chama a API
        preencherCurriculoNoBanco();
    }

    private void preencherCurriculoNoBanco() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Rota atualizada para apenas preencher o banco
                URL url = new URL("https://seu-worker.dspi.workers.dev/preencher-curriculo");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("nome_usuario", nomeUsuario);
                jsonParam.put("email_usuario", emailUsuario);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    String response = scanner.useDelimiter("\\A").next();
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getBoolean("success")) {
                        handler.post(() -> {
                            Toast.makeText(this, "Base de dados preparada para a IA!", Toast.LENGTH_SHORT).show();
                            finish(); // Fecha o Loading e volta para a tela de Conta
                        });
                        return;
                    }
                }

                handler.post(() -> mostrarErro("Falha ao organizar dados no banco."));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> mostrarErro("Erro de conexão."));
            }
        });
    }

    private void mostrarErro(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish(); // Fecha a tela e volta para a Conta mesmo com erro
    }
}