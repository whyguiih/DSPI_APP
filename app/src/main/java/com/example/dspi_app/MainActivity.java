package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Diz ao Android para deixar o aplicativo ocupar a tela toda (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        // 2. Protege o layout contra a barra de status e barra de botões inferiores
        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            // Pega o tamanho real dinâmico das barras do sistema
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Aplica esse tamanho como espaçamento (padding) dentro do layout
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });

        // 3. Configura a navegação modular
        ImageButton btnInicio = findViewById(R.id.btnInicio);
        ImageButton btnProjetos = findViewById(R.id.btnProjetos);

        btnInicio.setOnClickListener(v -> {
            // Lógica para a aba de Início
        });

        btnProjetos.setOnClickListener(v -> {
            // Estrutura separada para não misturar telas no mesmo arquivo
            // Intent intent = new Intent(MainActivity.this, ProjetosActivity.class);
            // startActivity(intent);
        });
    }
}