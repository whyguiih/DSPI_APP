package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContaActivity extends AppCompatActivity {

    // Índice 3 representa a aba "Conta" no Bottom Navigation (0=Inicio, 1=Projetos, 2=Nai, 3=Conta, 4=Empresas)
    private final int CURRENT_TAB_INDEX = 3;
    private String nivel;
    private String emailLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajuste para preencher a tela toda (Edge to Edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_conta_usuario);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Recuperar dados de sessão (nível de acesso e email)
        nivel = getIntent().getStringExtra("nivel_de_acesso");
        if (nivel == null) {
            nivel = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("nivel_de_acesso", "5");
        }

        emailLogado = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("email_logado", "sandramara");

        // Configurar o menu inferior
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        // Vincular componentes da tela
        TextView txtNomeUsuario = findViewById(R.id.txtNomeUsuario);
        TextView txtEmailUsuario = findViewById(R.id.txtEmailUsuario);

        LinearLayout btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        LinearLayout btnMeusProjetos = findViewById(R.id.btnMeusProjetos);
        LinearLayout btnConfiguracoes = findViewById(R.id.btnConfiguracoes);
        LinearLayout btnSair = findViewById(R.id.btnSair);

        // Exemplo: Populando os dados baseados na linha da Sandramara (tb_curriculo_alunos)
        txtNomeUsuario.setText("Nome Completo");
        txtEmailUsuario.setText("email@mail.co");

        // Configuração dos botões e subpáginas

        // 1. Botão Editar Perfil -> Leva ao FormularioActivity para preencher o currículo/dados
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(ContaActivity.this, FormularioActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("email_usuario", emailLogado);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // 2. Botão Meus Projetos -> Leva para a ProjetosActivity
        btnMeusProjetos.setOnClickListener(v -> {
            Intent intent = new Intent(ContaActivity.this, ProjetosActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("email_usuario", emailLogado);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish(); // Fecha a tela de conta para não empilhar
        });

        // 3. Botão Configurações -> Placeholder para uma futura tela
        btnConfiguracoes.setOnClickListener(v -> {
            Toast.makeText(ContaActivity.this, "Configurações em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        // 4. Botão Sair da Conta -> Limpa a sessão e volta para o Login
        btnSair.setOnClickListener(v -> {
            // Limpa as SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            // Redireciona para o login
            Intent intent = new Intent(ContaActivity.this, LoginActivity.class);
            // Limpa o histórico de telas para que o botão voltar do Android não retorne para a conta
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}