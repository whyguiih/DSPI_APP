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

    private final int CURRENT_TAB_INDEX = 3;

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

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        String nome = getIntent().getStringExtra("nome_empresa");
        String cnpj = getIntent().getStringExtra("cnpj");
        String telefone = getIntent().getStringExtra("telefone_contato");
        String email = getIntent().getStringExtra("email_contato");
        String endereco = getIntent().getStringExtra("endereco");
        String fotoPerfil = getIntent().getStringExtra("foto_perfil");
        String descricao = getIntent().getStringExtra("descricao");
        String setor = getIntent().getStringExtra("setor");

        // Vincula as Views
        TextView tvNomeEmpresa = findViewById(R.id.tvNomeEmpresa);
        TextView txtSetorEmpresa = findViewById(R.id.txtSetorEmpresa);
        TextView txtCnpjEmpresa = findViewById(R.id.txtCnpjEmpresa);
        TextView txtTelefone = findViewById(R.id.txtTelefone);
        TextView txtEmail = findViewById(R.id.txtEmail);
        TextView txtEndereco = findViewById(R.id.txtEndereco);
        TextView txtSobreEmpresa = findViewById(R.id.txtSobreEmpresa);
        ImageView imgEmpresaLogo = findViewById(R.id.imgEmpresaLogo);

        // Preenchimentos básicos
        tvNomeEmpresa.setText(nome != null ? nome : "Empresa");
        txtSetorEmpresa.setText(setor != null && !setor.isEmpty() ? "Setor: " + setor : "Setor: Não informado");
        txtSobreEmpresa.setText(descricao != null && !descricao.isEmpty() ? descricao : "Nenhuma descrição disponível ainda.");

        String cnpjFormatado = cnpj != null ? cnpj : "";
        String apenasNumerosCnpj = cnpjFormatado.replaceAll("\\D", ""); // Garante que só tem números
        if (apenasNumerosCnpj.length() == 14) {
            cnpjFormatado = String.format("%s.%s.%s/%s-%s",
                    apenasNumerosCnpj.substring(0, 2),
                    apenasNumerosCnpj.substring(2, 5),
                    apenasNumerosCnpj.substring(5, 8),
                    apenasNumerosCnpj.substring(8, 12),
                    apenasNumerosCnpj.substring(12, 14));
        }
        txtCnpjEmpresa.setText(!cnpjFormatado.isEmpty() ? "CNPJ: " + cnpjFormatado : "CNPJ: Não informado");

        String telefoneFormatado = telefone != null ? telefone : "";
        String apenasNumeros = telefoneFormatado.replaceAll("\\D", "");
        if (apenasNumeros.length() == 11) {
            telefoneFormatado = String.format("(%s) %s %s-%s",
                    apenasNumeros.substring(0, 2), apenasNumeros.substring(2, 3),
                    apenasNumeros.substring(3, 7), apenasNumeros.substring(7, 11));
        } else if (apenasNumeros.length() == 10) {
            telefoneFormatado = String.format("(%s) %s-%s",
                    apenasNumeros.substring(0, 2), apenasNumeros.substring(2, 6), apenasNumeros.substring(6, 10));
        }
        txtTelefone.setText(!telefoneFormatado.isEmpty() ? telefoneFormatado : "Sem telefone");

        String emailFormatado = email != null && !email.isEmpty() ? email : "Sem e-mail";
        emailFormatado = emailFormatado.replace("@", "\u2060@\u2060").replace(".", "\u2060.\u2060");
        txtEmail.setText(emailFormatado);

        String enderecoFormatado = endereco != null ? endereco.trim() : "";

        if (!enderecoFormatado.isEmpty()) {
            String[] parts = enderecoFormatado.split(",");

            String logradouro = parts[0].trim();
            String logradouroLower = logradouro.toLowerCase();

            if (logradouroLower.startsWith("avenida ")) {
                logradouro = "Av. " + logradouro.substring(8).trim();
            } else if (logradouroLower.startsWith("rua ")) {
                logradouro = "R. " + logradouro.substring(4).trim();
            } else if (!logradouroLower.startsWith("r. ") && !logradouroLower.startsWith("a. ")) {
                logradouro = "R. " + logradouro;
            }
            enderecoFormatado = logradouro;

            if (parts.length > 1) {
                String bairro = parts[1].trim();
                bairro = bairro.replaceAll("(?i)\\bbairro\\b", "").trim();
                enderecoFormatado += ", B. " + bairro;
            }

            if (parts.length > 2) {
                StringBuilder resto = new StringBuilder();
                for (int i = 2; i < parts.length; i++) {
                    resto.append(", ").append(parts[i].trim());
                }
                String restoStr = resto.toString();
                restoStr = restoStr.replaceAll("(?i)\\bapartamento\\b", "Ap.");
                enderecoFormatado += restoStr;
            }

        } else {
            enderecoFormatado = "Endereço não informado";
        }

        txtEndereco.setText(enderecoFormatado);

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
                            .setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
                }
            });
        }
    }
}