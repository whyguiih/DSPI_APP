package com.example.dspi_app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Deixa a tela inteira (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        // Previne sobreposição da barra de notificações e botões de navegação
        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Configurando os cliques das 5 abas (Agora são LinearLayouts)
        LinearLayout btnInicio = findViewById(R.id.btnInicio);
        LinearLayout btnProjetos = findViewById(R.id.btnProjetos);
        LinearLayout btnNai = findViewById(R.id.btnNai);
        LinearLayout btnEmpresas = findViewById(R.id.btnEmpresas);
        LinearLayout btnConta = findViewById(R.id.btnConta);

        btnInicio.setOnClickListener(v -> {
            // Ação da aba Início
        });

        btnProjetos.setOnClickListener(v -> {
            // Ação da aba Projetos
        });

        btnNai.setOnClickListener(v -> {
            // Ação do botão central Nai
        });

        btnEmpresas.setOnClickListener(v -> {
            // Ação da aba Empresas
        });

        btnConta.setOnClickListener(v -> {
            // Ação da aba Conta
        });
    }
}