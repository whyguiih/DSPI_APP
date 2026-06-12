package com.example.dspi_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetalhesEmpresaActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 4; // Mantém aceso o ícone de Empresas
    private String nivel;
    private String emailUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_detalhes_empresa);

        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // Recupera as credenciais vindas da tela anterior
        nivel = getIntent().getStringExtra("nivel_de_acesso");
        emailUsuario = getIntent().getStringExtra("email_usuario");

        // Inicializa o menu inferior padrão
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        Button btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> {
                finish();
                overridePendingTransition(0, 0);
            });
        }

        // REGRA DE NEGÓCIO EXCLUSIVA: Configuração para o Usuário Empresa (Nível 4)
        if ("4".equals(nivel)) {
            configurarPainelExclusivoEmpresa();
        }
    }

    private void configurarPainelExclusivoEmpresa() {
        // Exemplo: Altera títulos ou ativa botões que apenas a própria empresa pode interagir
        TextView tvNomeEmpresa = findViewById(R.id.tvNomeEmpresa); // Use os IDs certos do seu activity_detalhes_empresa.xml
        if (tvNomeEmpresa != null) {
            tvNomeEmpresa.setText("Área de Gestão: " + emailUsuario.toUpperCase());
        }

        Toast.makeText(this, "Bem-vindo ao seu painel corporativo corporativo", Toast.LENGTH_SHORT).show();
    }
}