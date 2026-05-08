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

public class EmpresasActivity extends AppCompatActivity {
    private View activeBubble;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_empresas);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        activeBubble = findViewById(R.id.activeBubble);
        LinearLayout btnEmpresas = findViewById(R.id.btnEmpresas);

        // Posiciona a bolha INICIALMENTE na aba Empresas
        btnEmpresas.post(() -> {
            activeBubble.getLayoutParams().width = btnEmpresas.getWidth();
            activeBubble.setX(btnEmpresas.getX());
            activeBubble.requestLayout();
        });

        findViewById(R.id.btnInicio).setOnClickListener(v -> animateAndNavigate(v, MainActivity.class));
        findViewById(R.id.btnProjetos).setOnClickListener(v -> animateAndNavigate(v, ProjetosActivity.class));
        findViewById(R.id.btnNai).setOnClickListener(v -> animateAndNavigate(v, NaiActivity.class));
        findViewById(R.id.btnConta).setOnClickListener(v -> animateAndNavigate(v, ContaActivity.class));
    }

    private void animateAndNavigate(View targetTab, Class<?> activityClass) {
        if (isNavigating) return;
        isNavigating = true;
        activeBubble.animate().x(targetTab.getX()).setDuration(250).withEndAction(() -> {
            startActivity(new Intent(this, activityClass));
            overridePendingTransition(0, 0);
            finish();
        }).start();
    }
}