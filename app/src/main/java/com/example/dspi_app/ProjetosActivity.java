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

public class ProjetosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        // Ajuste responsivo para barras do sistema
        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Setup dos Cliques
        setupNavigation();
    }

    private void setupNavigation() {
        LinearLayout btnInicio = findViewById(R.id.btnInicio);
        LinearLayout btnProjetos = findViewById(R.id.btnProjetos);
        LinearLayout btnNai = findViewById(R.id.btnNai);
        LinearLayout btnEmpresas = findViewById(R.id.btnEmpresas);
        LinearLayout btnConta = findViewById(R.id.btnConta);

        // Exemplo para abrir a página de Projetos
        btnProjetos.setOnClickListener(v -> {
            startActivity(new Intent(this, ProjetosActivity.class));
            overridePendingTransition(0, 0); // Remove animação para parecer troca de aba
            finish();
        });

        // Adicione os outros botões seguindo a mesma lógica...
    }
}