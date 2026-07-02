package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetalhesEmpresaActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 3;
    private String nivel;
    private String nomeUsuarioLogado;

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

        // ===================================================================
        // RECUPERAR SESSÃO DO USUÁRIO LOGADO (Quem está fuçando o app?)
        // ===================================================================
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = prefs.getString("nivel_de_acesso", getIntent().getStringExtra("nivel_de_acesso"));

        nomeUsuarioLogado = prefs.getString("email_logado", "");
        if (nomeUsuarioLogado == null || nomeUsuarioLogado.trim().isEmpty()) {
            nomeUsuarioLogado = getIntent().getStringExtra("email_usuario");
        }
        if (nomeUsuarioLogado == null) {
            nomeUsuarioLogado = "";
        }

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        String nome = getIntent().getStringExtra("nome_empresa"); // Empresa que está sendo visualizada
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
        RecyclerView recyclerProjetosAfiliados = findViewById(R.id.recycler_projetos_afiliados);

        // Preenchimentos básicos
        tvNomeEmpresa.setText(nome != null ? nome : "Empresa");
        txtSetorEmpresa.setText(setor != null && !setor.isEmpty() ? "Setor: " + setor : "Setor: Não informado");
        txtSobreEmpresa.setText(descricao != null && !descricao.isEmpty() ? descricao : "Nenhuma descrição disponível ainda.");

        String cnpjFormatado = cnpj != null ? cnpj : "";
        String apenasNumerosCnpj = cnpjFormatado.replaceAll("\\D", "");
        if (apenasNumerosCnpj.length() == 14) {
            cnpjFormatado = String.format("%s.%s.%s/%s-%s",
                    apenasNumerosCnpj.substring(0, 2), apenasNumerosCnpj.substring(2, 5),
                    apenasNumerosCnpj.substring(5, 8), apenasNumerosCnpj.substring(8, 12), apenasNumerosCnpj.substring(12, 14));
        }
        txtCnpjEmpresa.setText(!cnpjFormatado.isEmpty() ? "CNPJ: " + cnpjFormatado : "CNPJ: Não informado");

        String telefoneFormatado = telefone != null ? telefone : "";
        String apenasNumeros = telefoneFormatado.replaceAll("\\D", "");
        if (apenasNumeros.length() == 11) {
            telefoneFormatado = String.format("(%s) %s %s-%s", apenasNumeros.substring(0, 2), apenasNumeros.substring(2, 3), apenasNumeros.substring(3, 7), apenasNumeros.substring(7, 11));
        } else if (apenasNumeros.length() == 10) {
            telefoneFormatado = String.format("(%s) %s-%s", apenasNumeros.substring(0, 2), apenasNumeros.substring(2, 6), apenasNumeros.substring(6, 10));
        }
        txtTelefone.setText(!telefoneFormatado.isEmpty() ? telefoneFormatado : "Sem telefone");

        String emailFormatado = email != null && !email.isEmpty() ? email : "Sem e-mail";
        emailFormatado = emailFormatado.replace("@", "\u2060@\u2060").replace(".", "\u2060.\u2060");
        txtEmail.setText(emailFormatado);

        String enderecoFormatado = endereco != null ? endereco.trim() : "Endereço não informado";
        txtEndereco.setText(enderecoFormatado);

        // LÓGICA DE FOTO ATUALIZADA AQUI:
        if (fotoPerfil != null && !fotoPerfil.isEmpty() && !fotoPerfil.equals("null")) {
            imgEmpresaLogo.setImageTintList(null);
            imgEmpresaLogo.setPadding(0, 0, 0, 0);
            imgEmpresaLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (fotoPerfil.startsWith("http")) {
                // Imagem gerada no R2
                Glide.with(this)
                        .load(fotoPerfil)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imgEmpresaLogo);
            } else if (fotoPerfil.length() > 100) {
                // Imagem salva em Base64 localmente
                try {
                    byte[] decodedString = Base64.decode(fotoPerfil, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(this)
                            .load(decodedByte)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imgEmpresaLogo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Nome de imagem na pasta drawable
                String nomeImagem = fotoPerfil.replace("/drawable/", "").replace(".png", "").replace(".jpg", "");
                int resourceId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
                if (resourceId != 0) {
                    imgEmpresaLogo.setImageResource(resourceId);
                }
            }
        } else {
            imgEmpresaLogo.setImageResource(R.drawable.ic_empresas);
        }

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();

        recyclerProjetosAfiliados.setLayoutManager(new LinearLayoutManager(this));

        if (nome != null && !nome.trim().isEmpty()) {
            buscarProjetosDaEmpresa(nome, recyclerProjetosAfiliados);
        }
    }

    private void buscarProjetosDaEmpresa(String nomeEmpresaQueEstaSendoVisualizada, RecyclerView recyclerView) {
        String url = "https://api-dspi.whyguiih.workers.dev/listar-projetos";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            List<Projeto> projetosAfiliados = new ArrayList<>();

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);

                                Projeto p = new Projeto(
                                        obj.optString("nome_projeto", "Projeto Sem Nome"),
                                        obj.optString("nome_equipe", "Sem Equipe"),
                                        obj.optString("status", "Não iniciado"),
                                        obj.optString("nome_integrante", ""),
                                        obj.optString("nome_orientador", ""),
                                        obj.optString("proposta_chave", ""),
                                        obj.optString("segmentos_clientes", ""),
                                        obj.optString("atividades_chaves", ""),
                                        obj.optString("recursos_chaves", ""),
                                        obj.optString("relacionamentos_clientes", ""),
                                        obj.optString("canais", ""),
                                        obj.optString("estrutura_custos", ""),
                                        obj.optString("fluxo_receita", ""),
                                        obj.optString("parceiros_chaves", ""),
                                        obj.optString("tarefas", ""),
                                        obj.optString("dificuldades_enxergadas", ""),
                                        obj.optString("empresa_vinculada", "")
                                );

                                String empresaVinc = p.getEmpresaVinculada() != null ? p.getEmpresaVinculada() : "";

                                if (!empresaVinc.trim().isEmpty() && empresaVinc.trim().equalsIgnoreCase(nomeEmpresaQueEstaSendoVisualizada.trim())) {
                                    projetosAfiliados.add(p);
                                }
                            }

                            if (!projetosAfiliados.isEmpty()) {
                                recyclerView.setAdapter(new ProjetosActivity.ProjetoAdapter(projetosAfiliados, projeto -> {

                                    if ("4".equals(nivel) && !nomeEmpresaQueEstaSendoVisualizada.trim().equalsIgnoreCase(nomeUsuarioLogado.trim())) {
                                        Toast.makeText(DetalhesEmpresaActivity.this, "Acesso Negado: Você só pode acessar os detalhes dos seus próprios projetos afiliados.", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Intent intent = new Intent(DetalhesEmpresaActivity.this, ProjetoDetalhesActivity.class);
                                    intent.putExtra("projeto_selecionado", projeto);
                                    intent.putExtra("nivel_de_acesso", nivel);
                                    intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                }));
                            } else {
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao carregar projetos afiliados", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Falha na conexão com a API", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
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