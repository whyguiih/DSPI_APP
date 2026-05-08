package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    // ESTE NÚMERO MUDA EM CADA TELA: 0=Inicio, 1=Projetos, 2=Nai, 3=Empresas, 4=Conta
    private final int CURRENT_TAB_INDEX = 0;

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

        configurarBolhaAnimada();

        // Cliques para as outras telas
        findViewById(R.id.btnProjetos).setOnClickListener(v -> navegarPara(ProjetosActivity.class, 1));
        findViewById(R.id.btnNai).setOnClickListener(v -> navegarPara(NaiActivity.class, 2));
        findViewById(R.id.btnEmpresas).setOnClickListener(v -> navegarPara(EmpresasActivity.class, 3));
        findViewById(R.id.btnConta).setOnClickListener(v -> navegarPara(ContaActivity.class, 4));
    }

    private void configurarBolhaAnimada() {
        // Descobre de qual aba viemos (se não houver, assume que já estava aqui)
        int oldTabIndex = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

        // Espera a tela calcular os tamanhos reais para fazer a matemática
        bottomNavLayout.post(() -> {
            float tabWidth = bottomNavLayout.getWidth() / 5f;
            activeBubble.getLayoutParams().width = (int) tabWidth;
            activeBubble.requestLayout();

            // Coloca a bolha invisível onde ela estava na tela antiga
            activeBubble.setTranslationX(oldTabIndex * tabWidth);

            // Faz ela deslizar como vidro até o botão atual da tela aberta
            if (oldTabIndex != CURRENT_TAB_INDEX) {
                activeBubble.animate()
                        .translationX(CURRENT_TAB_INDEX * tabWidth)
                        .setDuration(350) // Velocidade do deslize
                        .setInterpolator(new DecelerateInterpolator(1.5f)) // Freia suavemente no fim
                        .start();
            }
        });
    }

    private void navegarPara(Class<?> activityClass, int newTabIndex) {
        if (CURRENT_TAB_INDEX == newTabIndex) return; // Impede abrir a tela atual de novo
        Intent intent = new Intent(this, activityClass);
        // Envia o ID de onde estávamos para a bolha da próxima tela saber de onde sair
        intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        startActivity(intent);
        overridePendingTransition(0, 0); // Corta a animação feia do Android
        finish();
    }
}