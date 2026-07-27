package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        nivel = prefs.getString("nivel_de_acesso", "6");
        nomeUsuarioLogado = prefs.getString("nome_usuario", "");
        emailUsuarioLogado = prefs.getString("email_logado", "");

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        // Dados da empresa que está sendo exibida no perfil
        String nomeEmpPerfil = intent.hasExtra("nome") ? intent.getStringExtra("nome") : intent.getStringExtra("nome_empresa");
        String emailEmpPerfil = intent.hasExtra("email") ? intent.getStringExtra("email") : intent.getStringExtra("email_contato");
        String fotoPerfil = intent.getStringExtra("foto_perfil");
        String desc = intent.hasExtra("sobre") ? intent.getStringExtra("sobre") : intent.getStringExtra("descricao");
        String setor = intent.getStringExtra("setor");

        TextView tvNome = findViewById(R.id.tvNomeEmpresa);
        TextView tvSetor = findViewById(R.id.txtSetorEmpresa);
        TextView tvEmail = findViewById(R.id.txtEmail);
        TextView tvEnd = findViewById(R.id.txtEndereco);
        TextView tvSobre = findViewById(R.id.txtSobreEmpresa);
        ImageView imgLogo = findViewById(R.id.imgEmpresaLogo);
        RecyclerView rvAfiliados = findViewById(R.id.recycler_projetos_afiliados);
        LinearLayout btnNecessidades = findViewById(R.id.btnVerNecessidades);

        if (btnNecessidades != null) {
            btnNecessidades.setOnClickListener(v -> {
                Intent i = new Intent(this, ListagemNecessidadesActivity.class);
                i.putExtra("email_empresa_alvo", emailEmpPerfil);
                startActivity(i);
            });
        }

        if (tvNome != null) tvNome.setText(nomeEmpPerfil);
        if (tvSetor != null) tvSetor.setText(setor != null ? "Setor: " + setor : "Setor: Não informado");
        if (tvSobre != null) tvSobre.setText(desc);
        if (tvEmail != null) tvEmail.setText(emailEmpPerfil);
        if (tvEnd != null) tvEnd.setText(intent.getStringExtra("endereco"));

        if (imgLogo != null) {
            int radius = (int) (16 * getResources().getDisplayMetrics().density);
            if (fotoPerfil != null && fotoPerfil.startsWith("http")) {
                Glide.with(this).load(fotoPerfil).transform(new CenterCrop(), new RoundedCorners(radius)).into(imgLogo);
            } else {
                Glide.with(this).load(R.drawable.ic_empresas).transform(new CenterCrop(), new RoundedCorners(radius)).into(imgLogo);
            }
        }

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();

        if (rvAfiliados != null) {
            rvAfiliados.setLayoutManager(new LinearLayoutManager(this));
            buscarProjetosAfiliados(nomeEmpPerfil, emailEmpPerfil, rvAfiliados);
        }
    }

    private void buscarProjetosAfiliados(String nEmp, String eEmp, RecyclerView rv) {
        String url = "https://api-dspi.whyguiih.workers.dev/listar-projetos";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        if (res.getBoolean("success")) {
                            JSONArray data = res.getJSONArray("data");
                            List<Projeto> list = new ArrayList<>();
                            String targetN = (nEmp != null) ? nEmp.trim().toLowerCase() : "";
                            String targetE = (eEmp != null) ? eEmp.trim().toLowerCase() : "";

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String vincEmail = obj.optString("empresa_vinculada_email", "").trim().toLowerCase();
                                // Filtra projetos usando o e-mail da empresa vinculada
                                if (!vincEmail.isEmpty() && vincEmail.equals(targetE)) {
                                    list.add(parseProjeto(obj));
                                }
                            }

                            if (!list.isEmpty()) {
                                rv.setAdapter(new ProjetosActivity.ProjetoAdapter(list, p -> {
                                    // Verifica se a empresa logada é a dona do perfil que está visualizando
                                    boolean visualizandoProprioPerfil = targetN.equalsIgnoreCase(nomeUsuarioLogado.trim()) || 
                                                                       targetE.equalsIgnoreCase(emailUsuarioLogado.trim());
                                    
                                    Intent intent;
                                    // Se for Nível 4 (Empresa) e for o seu próprio perfil, abre as Tabelas Completas
                                    if ("4".equals(nivel) && visualizandoProprioPerfil) {
                                        intent = new Intent(this, FormularioActivity.class);
                                        String id = (p.getUsuario() != null && !p.getUsuario().isEmpty() && !p.getUsuario().equals("null")) ? p.getUsuario() : p.getNomeEquipe();
                                        intent.putExtra("projeto_usuario", id);
                                    } else {
                                        // Caso contrário, abre apenas Detalhes resumidos
                                        intent = new Intent(this, ProjetoDetalhesActivity.class);
                                        intent.putExtra("projeto_selecionado", p);
                                    }
                                    intent.putExtra("nivel_de_acesso", nivel);
                                    intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
                                    startActivity(intent);
                                    overridePendingTransition(0, 0);
                                }, null, false));
                            } else {
                                rv.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) { Log.e("API", "Erro ao filtrar afiliados", e); }
                }, e -> Log.e("API", "Erro de rede")
        );
        Volley.newRequestQueue(this).add(req);
    }

    private Projeto parseProjeto(JSONObject obj) {
        Projeto p = new Projeto(
                obj.optString("nome_projeto", ""), obj.optString("nome_equipe", ""),
                obj.optString("status", ""), obj.optString("nome_integrante", ""),
                obj.optString("nome_orientador", ""), obj.optString("proposta_chave", ""),
                obj.optString("segmentos_clientes", ""), obj.optString("atividades_chaves", ""),
                obj.optString("recursos_chaves", ""), obj.optString("relacionamentos_clientes", ""),
                obj.optString("canais", ""), obj.optString("estrutura_custos", ""),
                obj.optString("fluxo_receita", ""), obj.optString("parceiros_chaves", ""),
                obj.optString("tarefas", ""), obj.optString("dificuldades_enxergadas", ""),
                obj.optString("empresa_vinculada", ""), obj.optString("video_url", ""),
                obj.optString("nome_coorientador", ""), obj.optString("usuario", ""),
                obj.optString("empresa_vinculada_email", "")
        );
        p.setComentarioEmpresa(obj.optString("comentario_empresa", ""));
        return p;
    }

    private void configurarBolhaAnimada() {
        int oldIdx = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View bubble = findViewById(R.id.activeBubble);
        LinearLayout nav = findViewById(R.id.bottomNavLayout);
        if (bubble != null && nav != null) {
            nav.post(() -> {
                float tw = nav.getWidth() / 5f;
                bubble.getLayoutParams().width = (int) tw;
                bubble.requestLayout();
                bubble.setTranslationX(oldIdx * tw);
                if (oldIdx != CURRENT_TAB_INDEX) bubble.animate().translationX(CURRENT_TAB_INDEX * tw).setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
            });
        }
    }
}
