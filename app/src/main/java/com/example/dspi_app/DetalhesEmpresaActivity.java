package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetalhesEmpresaActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 3;
    private String nivel;
    private String nomeUsuarioLogado;
    private String emailUsuarioLogado;

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

        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = prefs.getString("nivel_de_acesso", getIntent().getStringExtra("nivel_de_acesso"));
        nomeUsuarioLogado = prefs.getString("nome_usuario", "");
        emailUsuarioLogado = prefs.getString("email_logado", "");

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String nomeEmpresaPerfil = intent.hasExtra("nome") ? intent.getStringExtra("nome") : intent.getStringExtra("nome_empresa");
        String emailEmpresaPerfil = intent.hasExtra("email") ? intent.getStringExtra("email") : intent.getStringExtra("email_contato");
        String cnpj = intent.getStringExtra("cnpj");
        String telefone = intent.hasExtra("telefone") ? intent.getStringExtra("telefone") : intent.getStringExtra("telefone_contato");
        String endereco = intent.getStringExtra("endereco");
        String fotoPerfil = intent.getStringExtra("foto_perfil");
        String descricao = intent.hasExtra("sobre") ? intent.getStringExtra("sobre") : intent.getStringExtra("descricao");
        String setor = intent.getStringExtra("setor");

        TextView tvNomeEmpresa = findViewById(R.id.tvNomeEmpresa);
        TextView txtSetorEmpresa = findViewById(R.id.txtSetorEmpresa);
        TextView txtCnpjEmpresa = findViewById(R.id.txtCnpjEmpresa);
        TextView txtTelefone = findViewById(R.id.txtTelefone);
        TextView txtEmail = findViewById(R.id.txtEmail);
        TextView txtEndereco = findViewById(R.id.txtEndereco);
        TextView txtSobreEmpresa = findViewById(R.id.txtSobreEmpresa);
        ImageView imgEmpresaLogo = findViewById(R.id.imgEmpresaLogo);
        RecyclerView recyclerProjetosAfiliados = findViewById(R.id.recycler_projetos_afiliados);
        LinearLayout btnVerNecessidades = findViewById(R.id.btnVerNecessidades);

        if (btnVerNecessidades != null) {
            btnVerNecessidades.setOnClickListener(v -> {
                Intent intentList = new Intent(this, ListagemNecessidadesActivity.class);
                intentList.putExtra("email_empresa_alvo", emailEmpresaPerfil);
                startActivity(intentList);
            });
        }

        if (tvNomeEmpresa != null) tvNomeEmpresa.setText(nomeEmpresaPerfil != null ? nomeEmpresaPerfil : "Empresa");
        if (txtSetorEmpresa != null) txtSetorEmpresa.setText(setor != null && !setor.isEmpty() ? "Setor: " + setor : "Setor: Não informado");
        if (txtSobreEmpresa != null) txtSobreEmpresa.setText(descricao != null && !descricao.isEmpty() ? descricao : "Nenhuma descrição disponível.");
        if (txtEmail != null) txtEmail.setText(emailEmpresaPerfil != null ? emailEmpresaPerfil : "Sem e-mail");
        if (txtEndereco != null) txtEndereco.setText(endereco != null ? endereco : "Endereço não informado");

        if (imgEmpresaLogo != null) {
            int radiusPx = (int) (16 * getResources().getDisplayMetrics().density);
            if (fotoPerfil != null && !fotoPerfil.isEmpty() && !fotoPerfil.equals("null")) {
                if (fotoPerfil.startsWith("http")) {
                    Glide.with(this).load(fotoPerfil).transform(new CenterCrop(), new RoundedCorners(radiusPx)).into(imgEmpresaLogo);
                } else {
                    Glide.with(this).load(R.drawable.ic_empresas).transform(new CenterCrop(), new RoundedCorners(radiusPx)).into(imgEmpresaLogo);
                }
            } else {
                Glide.with(this).load(R.drawable.ic_empresas).transform(new CenterCrop(), new RoundedCorners(radiusPx)).into(imgEmpresaLogo);
            }
        }

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();

        if (recyclerProjetosAfiliados != null) {
            recyclerProjetosAfiliados.setLayoutManager(new LinearLayoutManager(this));
            buscarProjetosDaEmpresa(nomeEmpresaPerfil, emailEmpresaPerfil, recyclerProjetosAfiliados);
        }
    }

    private void buscarProjetosDaEmpresa(String nomeEmpresa, String emailEmpresa, RecyclerView recyclerView) {
        String url = "https://api-dspi.whyguiih.workers.dev/listar-projetos";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            List<Projeto> projetosAfiliados = new ArrayList<>();
                            String targetNome = (nomeEmpresa != null) ? nomeEmpresa.trim().toLowerCase() : "";
                            String targetEmail = (emailEmpresa != null) ? emailEmpresa.trim().toLowerCase() : "";

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String empresaVinc = obj.optString("empresa_vinculada", "").trim().toLowerCase();
                                
                                if (!empresaVinc.isEmpty()) {
                                    if ((!targetNome.isEmpty() && empresaVinc.equals(targetNome)) || 
                                        (!targetEmail.isEmpty() && empresaVinc.equals(targetEmail))) {
                                        
                                        Projeto p = new Projeto(
                                                obj.optString("nome_projeto", "Sem Nome"),
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
                                                obj.optString("empresa_vinculada", ""),
                                                obj.optString("video_url", ""),
                                                obj.optString("nome_coorientador", ""),
                                                obj.optString("usuario", "")
                                        );
                                        p.setComentarioEmpresa(obj.optString("comentario_empresa", ""));
                                        projetosAfiliados.add(p);
                                    }
                                }
                            }

                            if (!projetosAfiliados.isEmpty()) {
                                recyclerView.setAdapter(new ProjetosActivity.ProjetoAdapter(projetosAfiliados, projeto -> {
                                    boolean visualizandoPropriaEmpresa = 
                                            targetNome.equalsIgnoreCase(nomeUsuarioLogado) || 
                                            targetEmail.equalsIgnoreCase(emailUsuarioLogado);

                                    if ("4".equals(nivel) && !visualizandoPropriaEmpresa) {
                                        Toast.makeText(this, "Acesso Restrito", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Intent intent = new Intent(this, ProjetoDetalhesActivity.class);
                                    intent.putExtra("projeto_selecionado", projeto);
                                    intent.putExtra("nivel_de_acesso", nivel);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                }, null, false));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Erro ao carregar afiliados", e);
                    }
                },
                error -> Log.e("API_ERROR", "Falha na conexão", error)
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
                    activeBubble.animate().translationX(CURRENT_TAB_INDEX * tabWidth).setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
                }
            });
        }
    }
}
