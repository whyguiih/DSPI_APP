package com.example.dspi_app;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Aplica o efeito Blur (Apenas API 31 ou superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            View glassContainer = findViewById(R.id.glassContainer);

            RenderEffect blurEffect = RenderEffect.createBlurEffect(
                    20f,
                    20f,
                    Shader.TileMode.CLAMP
            );

            // Ative a linha abaixo se quiser desfocar o que tem dentro do container
            // glassContainer.setRenderEffect(blurEffect);
        }

        // 2. Configura a navegação modular (Activities separadas)
        ImageButton btnInicio = findViewById(R.id.btnInicio);
        ImageButton btnProjetos = findViewById(R.id.btnProjetos);

        // O botão início não precisa fazer nada se já estamos na MainActivity
        btnInicio.setOnClickListener(v -> {
            // Já estamos aqui, opcionalmente você pode dar um refresh na tela
        });

        // O botão projetos abre a Activity separada
        btnProjetos.setOnClickListener(v -> {
            // Nota: Você precisará criar uma ProjetosActivity.java depois!
            // Intent intent = new Intent(MainActivity.this, ProjetosActivity.class);
            // startActivity(intent);
        });
    }
}