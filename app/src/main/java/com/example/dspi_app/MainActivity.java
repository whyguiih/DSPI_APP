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
        configurarMenuLateral();

        // Navegação da barra inferior
        findViewById(R.id.btnProjetos).setOnClickListener(v -> navegarPara(ProjetosActivity.class, 1));
        findViewById(R.id.btnNai).setOnClickListener(v -> navegarPara(NaiActivity.class, 2));
        findViewById(R.id.btnEmpresas).setOnClickListener(v -> navegarPara(EmpresasActivity.class, 3));
        findViewById(R.id.btnConta).setOnClickListener(v -> navegarPara(ContaActivity.class, 4));
    }

    private void configurarMenuLateral() {
        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);
        TextView txtSubpageTitle = findViewById(R.id.txtSubpageTitle);
        ImageButton btnBack = findViewById(R.id.btnBackToMenu);

        btnBack.setOnClickListener(v -> viewAnimator.setDisplayedChild(0));

        View.OnClickListener listener = v -> {
            String titulo = "";
            int id = v.getId();

            // Títulos atualizados para refletirem o novo design
            if (id == R.id.btnSenai) titulo = "O que é o Senai?";
            else if (id == R.id.btnDspi) titulo = "O que é o DSPI?";
            else if (id == R.id.btnInstituicao) titulo = "A instituição";
            else if (id == R.id.btnAplicativo) titulo = "O aplicativo";
            else if (id == R.id.btnCursos) titulo = "Cursos disponíveis";

            txtSubpageTitle.setText(titulo);
            viewAnimator.setDisplayedChild(1);
        };

        // IDs atualizados
        findViewById(R.id.btnSenai).setOnClickListener(listener);
        findViewById(R.id.btnDspi).setOnClickListener(listener);
        findViewById(R.id.btnInstituicao).setOnClickListener(listener);
        findViewById(R.id.btnAplicativo).setOnClickListener(listener);
        findViewById(R.id.btnCursos).setOnClickListener(listener);
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
                activeBubble.animate().translationX(CURRENT_TAB_INDEX * tabWidth).setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
            }
        });
    }

    private void navegarPara(Class<?> activityClass, int newTabIndex) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}