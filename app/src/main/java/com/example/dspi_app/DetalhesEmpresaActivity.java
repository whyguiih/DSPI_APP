package com.example.dspi_app;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetalhesEmpresaActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 3; // Mantemos o 3 porque ainda estamos na área de Empresas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_detalhes_empresa);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Configura o botão voltar
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        // Recebe os dados da lista
        String nome = getIntent().getStringExtra("nome_empresa");
        String cnpj = getIntent().getStringExtra("cnpj");
        String telefone = getIntent().getStringExtra("telefone_contato");
        String email = getIntent().getStringExtra("email_contato");
        String endereco = getIntent().getStringExtra("endereco");
        String fotoPerfil = getIntent().getStringExtra("foto_perfil");
        String descricao = getIntent().getStringExtra("descricao");

        // Vincula as Views atualizadas do novo XML
        TextView tvNomeEmpresa = findViewById(R.id.tvNomeEmpresa);
        TextView txtCnpjEmpresa = findViewById(R.id.txtCnpjEmpresa);
        TextView txtTelefone = findViewById(R.id.txtTelefone);
        TextView txtEmail = findViewById(R.id.txtEmail);
        TextView txtEndereco = findViewById(R.id.txtEndereco);
        TextView txtSobreEmpresa = findViewById(R.id.txtSobreEmpresa);
        ImageView imgEmpresaLogo = findViewById(R.id.imgEmpresaLogo);

        // Preenche com os dados ou fallbacks
        tvNomeEmpresa.setText(nome != null ? nome : "Empresa");
        txtCnpjEmpresa.setText(cnpj != null && !cnpj.isEmpty() ? "CNPJ: " + cnpj : "CNPJ: Não informado");
        txtTelefone.setText(telefone != null && !telefone.isEmpty() ? telefone : "Sem telefone");
        txtEmail.setText(email != null && !email.isEmpty() ? email : "Sem e-mail");
        txtEndereco.setText(endereco != null && !endereco.isEmpty() ? endereco : "Endereço não informado");
        txtSobreEmpresa.setText(descricao != null && !descricao.isEmpty() ? descricao : "Nenhuma descrição disponível ainda.");

        // Tratamento da imagem
        if (fotoPerfil != null && !fotoPerfil.isEmpty() && !fotoPerfil.equals("null")) {
            String nomeImagem = fotoPerfil.replace("/drawable/", "").replace(".png", "").replace(".jpg", "");
            int resourceId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());

            if (resourceId != 0) {
                imgEmpresaLogo.setImageResource(resourceId);
                imgEmpresaLogo.setImageTintList(null);
                imgEmpresaLogo.setPadding(0, 0, 0, 0);
                imgEmpresaLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }

        // Ativação do Menu Inferior e animação da Bolha
        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();
    }

    private void configurarBolhaAnimada() {
        int oldTabIndex = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

        if (activeBubble != null && bottomNavLayout != null) {
            bottomNavLayout.post(() -> {
                float tabWidth = bottomNavLayout.getWidth() / 5f;
                activeBubble.getLayoutParams().width = (int) tabWidth;
                activeBubble.requestLayout();
                activeBubble.setTranslationX(oldTabIndex * tabWidth);
                if (oldTabIndex != CURRENT_TAB_INDEX) {
                    activeBubble.animate().translationX(CURRENT_TAB_INDEX * tabWidth)
                            .setDuration(350)
                            .setInterpolator(new DecelerateInterpolator(1.5f))
                            .start();
                }
            });
        }
    }
}