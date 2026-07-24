package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class ContaActivity extends AppCompatActivity {

    // Índice 3 representa a aba "Conta" no Bottom Navigation (0=Inicio, 1=Projetos, 2=Nai, 3=Conta, 4=Empresas)
    private final int CURRENT_TAB_INDEX = 3;
    private String nivel;
    private String emailLogado;

    private String nomeUsuario;

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

        // Recuperar dados de sessão (nível de acesso, email, nome e foto)
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = getIntent().getStringExtra("nivel_de_acesso");
        if (nivel == null) {
            nivel = prefs.getString("nivel_de_acesso", "5");
        }

        emailLogado = prefs.getString("email_logado", "usuario@email.com");

        // Configurar o menu inferior
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        // Vincular componentes da tela
        LinearLayout btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        LinearLayout btnMeusProjetos = findViewById(R.id.btnMeusProjetos);
        LinearLayout btnMeuCurriculo = findViewById(R.id.btnMeuCurriculo); // Confira se o ID está certo!

        LinearLayout btnSair = findViewById(R.id.btnSair);

        // Esconder o botão "Meus Projetos" para usuários de nível 6
        if ("6".equals(nivel) || "2".equals(nivel) || "1".equals(nivel) ) {
            btnMeusProjetos.setVisibility(View.GONE);
        }

        // Esconder o botão "Meu Currículo" se não for nível 5
        if (!"5".equals(nivel)) {
            btnMeuCurriculo.setVisibility(View.GONE);
        }

        // Configuração dos botões e subpáginas

        // 1. Botão Editar Perfil -> Leva ao PerfilActivity para alterar dados pessoais
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(ContaActivity.this, PerfilActivity.class);
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

        btnMeuCurriculo.setOnClickListener(v -> {
            // Atenção aqui ao nome correto da SUA activity:
            Intent intent = new Intent(ContaActivity.this, CarregamentoCurriculoActivity.class);

            // Você precisa garantir que essas variáveis de fato têm o nome e email do usuário logado
            intent.putExtra("NOME_USUARIO", nomeUsuario);
            intent.putExtra("EMAIL_USUARIO", emailLogado);
            startActivity(intent);
        });


        btnSair.setOnClickListener(v -> {
            // Limpa as SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ContaActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        TextView txtNomeUsuario = findViewById(R.id.txtNomeUsuario);
        TextView txtEmailUsuario = findViewById(R.id.txtEmailUsuario);
        android.widget.ImageView imgAvatar = findViewById(R.id.imgAvatar);

        String nome = prefs.getString("nome_usuario", "Nome Completo");
        String email = prefs.getString("email_logado", "usuario@email.com");
        String foto = prefs.getString("foto_usuario", "");

        txtNomeUsuario.setText(nome);
        txtEmailUsuario.setText(email);

        // Conversão dinâmica de DP para Pixels para manter proporção perfeita
        int radiusPx = (int) (16 * getResources().getDisplayMetrics().density);

        if (!foto.isEmpty()) {
            if (foto.startsWith("http")) {
                Glide.with(this)
                        .load(foto)
                        .transform(new CenterCrop(), new RoundedCorners(radiusPx)) // Modificado aqui
                        .into(imgAvatar);
                imgAvatar.setPadding(0, 0, 0, 0);
            } else {
                byte[] decodedString = android.util.Base64.decode(foto, android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                Glide.with(this)
                        .load(decodedByte)
                        .transform(new CenterCrop(), new RoundedCorners(radiusPx)) // Modificado aqui
                        .into(imgAvatar);

                imgAvatar.setPadding(0, 0, 0, 0);
            }
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_conta)
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .into(imgAvatar);

            // Garante que o ícone padrão fique centralizado dentro da moldura de vidro
            int innerPadding = (int) (14 * getResources().getDisplayMetrics().density);
            imgAvatar.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
        }
    }
}