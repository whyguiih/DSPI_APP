package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        setContentView(R.layout.activity_projetos);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        findViewById(R.id.btnInicio).setOnClickListener(v -> navegarPara(MainActivity.class));
        findViewById(R.id.btnNai).setOnClickListener(v -> navegarPara(NaiActivity.class));
        findViewById(R.id.btnEmpresas).setOnClickListener(v -> navegarPara(EmpresasActivity.class));
        findViewById(R.id.btnConta).setOnClickListener(v -> navegarPara(ContaActivity.class));
    }

    private void navegarPara(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        overridePendingTransition(0, 0);
        finish();
    }
}