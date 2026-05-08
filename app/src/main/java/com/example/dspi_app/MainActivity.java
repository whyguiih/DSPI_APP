package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private View activeBubble;
    private boolean isNavigating = false; // Evita bugar se a pessoa clicar em 2 botões rápido

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        activeBubble = findViewById(R.id.activeBubble);
        LinearLayout btnInicio = findViewById(R.id.btnInicio);

        // Posiciona a bolha INICIALMENTE na aba Início
        btnInicio.post(() -> {
            activeBubble.getLayoutParams().width = btnInicio.getWidth();
            activeBubble.setX(btnInicio.getX());
            activeBubble.requestLayout();
        });

        // Configura as navegações das outras abas
        findViewById(R.id.btnProjetos).setOnClickListener(v -> animateAndNavigate(v, ProjetosActivity.class));
        findViewById(R.id.btnNai).setOnClickListener(v -> animateAndNavigate(v, NaiActivity.class));
        findViewById(R.id.btnEmpresas).setOnClickListener(v -> animateAndNavigate(v, EmpresasActivity.class));
        findViewById(R.id.btnConta).setOnClickListener(v -> animateAndNavigate(v, ContaActivity.class));
    }

    private void animateAndNavigate(View targetTab, Class<?> activityClass) {
        if (isNavigating) return;
        isNavigating = true;

        // Desliza a bolha para o X do botão clicado
        activeBubble.animate()
                .x(targetTab.getX())
                .setDuration(250) // Duração do deslize em ms
                .withEndAction(() -> {
                    startActivity(new Intent(this, activityClass));
                    overridePendingTransition(0, 0);
                    finish();
                })
                .start();
    }
}