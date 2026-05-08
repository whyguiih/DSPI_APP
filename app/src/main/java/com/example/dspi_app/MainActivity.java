package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    // 0 indica que esta é a tela de Início
    private final int CURRENT_TAB_INDEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Previne que o layout fique embaixo da barra de bateria e navegação
        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Configura a bolha de vidro da barra inferior
        configurarBolhaAnimada();

        // ---------------------------------------------------------
        // NAVEGAÇÃO INFERIOR (Troca de Telas Principais)
        // ---------------------------------------------------------
        findViewById(R.id.btnProjetos).setOnClickListener(v -> navegarPara(ProjetosActivity.class, 1));
        findViewById(R.id.btnNai).setOnClickListener(v -> navegarPara(NaiActivity.class, 2));
        findViewById(R.id.btnEmpresas).setOnClickListener(v -> navegarPara(EmpresasActivity.class, 3));
        findViewById(R.id.btnConta).setOnClickListener(v -> navegarPara(ContaActivity.class, 4));

        // ---------------------------------------------------------
        // LÓGICA DAS SUBPÁGINAS (Dentro da tela Início)
        // ---------------------------------------------------------
        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);
        TextView txtSubpageTitle = findViewById(R.id.txtSubpageTitle);
        ImageButton btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // Botão de voltar (setinha) retorna para a grade de botões (Child 0)
        btnBackToMenu.setOnClickListener(v -> viewAnimator.setDisplayedChild(0));

        // Ação padronizada para abrir as subpáginas (Child 1)
        View.OnClickListener openSubpage = v -> {
            String title = "";
            int id = v.getId();

            if (id == R.id.btnSenai) title = "O que é o Senai?";
            else if (id == R.id.btnDspi) title = "O que é o DSPI?";
            else if (id == R.id.btnIntegraInstitucional) title = "O que é a Integra?";
            else if (id == R.id.btnIntegraApp) title = "O que é o Integra?";
            else if (id == R.id.btnCursos) title = "Cursos Disponíveis";
            else if (id == R.id.btnProjetosAlunos) title = "Projetos de Alunos";
            else if (id == R.id.btnVagas) title = "Vagas de Estágio";
            else if (id == R.id.btnSuporte) title = "Suporte e Contato";

            txtSubpageTitle.setText(title);
            viewAnimator.setDisplayedChild(1); // Exibe a subpágina
        };

        // Atribuindo o clique a todos os 8 botões
        findViewById(R.id.btnSenai).setOnClickListener(openSubpage);
        findViewById(R.id.btnDspi).setOnClickListener(openSubpage);
        findViewById(R.id.btnIntegraInstitucional).setOnClickListener(openSubpage);
        findViewById(R.id.btnIntegraApp).setOnClickListener(openSubpage);
        findViewById(R.id.btnCursos).setOnClickListener(openSubpage);
        findViewById(R.id.btnProjetosAlunos).setOnClickListener(openSubpage);
        findViewById(R.id.btnVagas).setOnClickListener(openSubpage);
        findViewById(R.id.btnSuporte).setOnClickListener(openSubpage);
    }

    private void configurarBolhaAnimada() {
        int oldTabIndex = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

        bottomNavLayout.post(() -> {
            float tabWidth = bottomNavLayout.getWidth() / 5f;
            activeBubble.getLayoutParams().width = (int) tabWidth;
            activeBubble.requestLayout();
            activeBubble.setTranslationX(oldTabIndex * tabWidth);
            if (oldTabIndex != CURRENT_TAB_INDEX) {
                activeBubble.animate()
                        .translationX(CURRENT_TAB_INDEX * tabWidth)
                        .setDuration(350)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start();
            }
        });
    }

    private void navegarPara(Class<?> activityClass, int newTabIndex) {
        if (CURRENT_TAB_INDEX == newTabIndex) return;
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}