package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjetosActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 1;
    private String nivel;
    private String nomeUsuario;
    private String emailLogado;
    private List<Projeto> todosMeusProjetos = new ArrayList<>();
    private List<Projeto> todosOutrosProjetos = new ArrayList<>();

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

        configurarBolhaAnimada();

        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = prefs.getString("nivel_de_acesso", "6");
        nomeUsuario = prefs.getString("nome_usuario", "");
        emailLogado = prefs.getString("email_logado", "");

        if (nomeUsuario == null) nomeUsuario = "";
        if (emailLogado == null) emailLogado = "";

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        Button btnAbrirFormulario = findViewById(R.id.btnAbrirFormulario);
        // Regra: Alunos, DR/DN, Avaliador e Empresa não criam projetos por aqui.
        if ("4".equals(nivel) || "6".equals(nivel) || "1".equals(nivel) || "2".equals(nivel) || "5".equals(nivel)) {
            btnAbrirFormulario.setVisibility(View.GONE);
        }

        btnAbrirFormulario.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("email_usuario", emailLogado);
            startActivity(intent);
        });

        RecyclerView rvMeus = findViewById(R.id.rvMeusProjetos);
        RecyclerView rvOutros = findViewById(R.id.rvOutrosProjetos);
        rvMeus.setLayoutManager(new LinearLayoutManager(this));
        rvOutros.setLayoutManager(new LinearLayoutManager(this));

        EditText etPesquisa = findViewById(R.id.etPesquisa);
        etPesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filtrarProjetos(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        buscarProjetosDaApi();
    }

    private void buscarProjetosDaApi() {
        String url = "https://api-dspi.whyguiih.workers.dev/listar-projetos";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.optBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            todosMeusProjetos.clear();
                            todosOutrosProjetos.clear();

                            String userLower = nomeUsuario.trim().toLowerCase();
                            String emailLower = emailLogado.trim().toLowerCase();

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Projeto p = parseProjeto(obj);
                                
                                String nomeEqp = p.getNomeEquipe() != null ? p.getNomeEquipe().trim().toLowerCase() : "";
                                String integrante = p.getIntegrantes() != null ? p.getIntegrantes().trim().toLowerCase() : "";
                                String orientador = p.getOrientador() != null ? p.getOrientador().trim().toLowerCase() : "";
                                String coorientador = p.getNomeCoorientador() != null ? p.getNomeCoorientador().trim().toLowerCase() : "";
                                String donoEmail = p.getUsuario() != null ? p.getUsuario().trim().toLowerCase() : "";

                                boolean isMeu = false;

                                // 1. Lógica para Integrantes/Donos (Alunos e Professores)
                                if (!userLower.isEmpty()) {
                                    if (nomeEqp.contains(userLower) || integrante.contains(userLower) || 
                                        orientador.contains(userLower) || coorientador.contains(userLower) || 
                                        donoEmail.contains(userLower)) {
                                        isMeu = true;
                                    }
                                }
                                if (!emailLower.isEmpty() && donoEmail.contains(emailLower)) {
                                    isMeu = true;
                                }

                                // 2. Lógica específica para Empresas (Nível 4)
                                if ("4".equals(nivel)) {
                                    String emailVinc = obj.optString("empresa_vinculada_email", "").trim().toLowerCase();
                                    boolean match = !emailVinc.isEmpty() && emailVinc.equals(emailLower);
                                    Log.d("PROJETOS", "Projeto [" + p.getNomeProjeto() + "] - Empresa Vinculada: " + emailVinc + " | Usuário: " + emailLower + " | Match: " + match);
                                    if (match) {
                                        isMeu = true;
                                    }
                                }

                                if (isMeu) {
                                    todosMeusProjetos.add(p);
                                } else {
                                    todosOutrosProjetos.add(p);
                                }
                            }
                            configurarListasDeProjetos(todosMeusProjetos, todosOutrosProjetos);
                        }
                    } catch (Exception e) {
                        Log.e("PROJETOS", "Erro ao processar JSON", e);
                    }
                }, error -> Log.e("API", "Erro de rede ao listar projetos")
        );
        Volley.newRequestQueue(this).add(request);
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

    private void configurarListasDeProjetos(List<Projeto> meus, List<Projeto> outros) {
        RecyclerView rvMeus = findViewById(R.id.rvMeusProjetos);
        RecyclerView rvOutros = findViewById(R.id.rvOutrosProjetos);
        TextView tvMeus = findViewById(R.id.tvSeusProjetos);
        TextView tvOutros = findViewById(R.id.tvOutrosProjetos);

        tvMeus.setVisibility(meus.isEmpty() ? View.GONE : View.VISIBLE);
        rvMeus.setVisibility(meus.isEmpty() ? View.GONE : View.VISIBLE);
        rvMeus.setAdapter(new ProjetoAdapter(meus, this::abrirPaginaDetalhes, this::confirmarExclusaoProjeto, "3".equals(nivel)));

        tvOutros.setVisibility(outros.isEmpty() ? View.GONE : View.VISIBLE);
        rvOutros.setVisibility(outros.isEmpty() ? View.GONE : View.VISIBLE);
        rvOutros.setAdapter(new ProjetoAdapter(outros, this::abrirPaginaDetalhes, null, false));
    }

    private void abrirPaginaDetalhes(Projeto projeto) {
        String userLower = nomeUsuario.trim().toLowerCase();
        String emailLower = emailLogado.trim().toLowerCase();
        String nomeEqp = projeto.getNomeEquipe() != null ? projeto.getNomeEquipe().trim().toLowerCase() : "";
        String integrante = projeto.getIntegrantes() != null ? projeto.getIntegrantes().trim().toLowerCase() : "";
        String orientador = projeto.getOrientador() != null ? projeto.getOrientador().trim().toLowerCase() : "";
        String coorientador = projeto.getNomeCoorientador() != null ? projeto.getNomeCoorientador().trim().toLowerCase() : "";
        String donoEmail = projeto.getUsuario() != null ? projeto.getUsuario().trim().toLowerCase() : "";

        // Verifica se o usuário logado é dono ou integrante do projeto
        boolean isMeu = (!userLower.isEmpty() && (nomeEqp.contains(userLower) || integrante.contains(userLower) || orientador.contains(userLower) || coorientador.contains(userLower) || donoEmail.contains(userLower)));
        if (!emailLower.isEmpty() && donoEmail.contains(emailLower)) isMeu = true;
        
        // Verifica se o usuário logado é a Empresa vinculada ao projeto
        String emailVinc = projeto.getEmpresaVinculadaEmail() != null ? projeto.getEmpresaVinculadaEmail().trim().toLowerCase() : "";
        boolean isVinc = "4".equals(nivel) && !emailVinc.isEmpty() && emailVinc.equals(emailLower);

        Intent intent;
        // Se for ADM (1), DR/DN (2), Integrante (isMeu) ou Empresa Vinculada (isVinc), abre o Formulário Completo
        if ("1".equals(nivel) || "2".equals(nivel) || isMeu || isVinc) {
            intent = new Intent(this, FormularioActivity.class);
            String id = (projeto.getUsuario() != null && !projeto.getUsuario().isEmpty() && !projeto.getUsuario().equals("null")) ? projeto.getUsuario() : projeto.getNomeEquipe();
            intent.putExtra("projeto_usuario", id);
        } else {
            // Caso contrário, abre apenas os Detalhes resumidos
            intent = new Intent(this, ProjetoDetalhesActivity.class);
            intent.putExtra("projeto_selecionado", projeto);
        }
        intent.putExtra("nivel_de_acesso", nivel);
        intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void filtrarProjetos(String query) {
        String t = query.toLowerCase().trim();
        List<Projeto> fMeus = new ArrayList<>();
        List<Projeto> fOutros = new ArrayList<>();
        for (Projeto p : todosMeusProjetos) { if (p.getNomeProjeto().toLowerCase().contains(t) || p.getNomeEquipe().toLowerCase().contains(t)) fMeus.add(p); }
        for (Projeto p : todosOutrosProjetos) { if (p.getNomeProjeto().toLowerCase().contains(t) || p.getNomeEquipe().toLowerCase().contains(t)) fOutros.add(p); }
        configurarListasDeProjetos(fMeus, fOutros);
    }

    public static class ProjetoAdapter extends RecyclerView.Adapter<ProjetoAdapter.ViewHolder> {
        private final List<Projeto> projetos;
        private final OnItemClickListener listener;
        private final OnDeleteClickListener deleteListener;
        private final boolean podeExcluir;
        public interface OnItemClickListener { void onItemClick(Projeto p); }
        public interface OnDeleteClickListener { void onDeleteClick(Projeto p); }

        public ProjetoAdapter(List<Projeto> p, OnItemClickListener l, OnDeleteClickListener d, boolean pe) {
            this.projetos = p; this.listener = l; this.deleteListener = d; this.podeExcluir = pe;
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.activity_projeto, p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Projeto p = projetos.get(pos);
            h.tvNome.setText(p.getNomeProjeto());
            h.tvStatus.setText("Status: " + p.getStatus());
            h.tvEquipe.setText("Equipe: " + p.getNomeEquipe());
            h.tvBadge.setVisibility((p.getVideoUrl() != null && !p.getVideoUrl().isEmpty() && !p.getVideoUrl().equals("null")) ? View.VISIBLE : View.GONE);
            h.btnDel.setVisibility(podeExcluir ? View.VISIBLE : View.GONE);
            h.btnDel.setOnClickListener(v -> { if (deleteListener != null) deleteListener.onDeleteClick(p); });
            h.itemView.setOnClickListener(v -> listener.onItemClick(p));
        }
        @Override public int getItemCount() { return projetos.size(); }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNome, tvStatus, tvEquipe, tvBadge; Button btnDel;
            public ViewHolder(View iv) { super(iv); tvNome = iv.findViewById(R.id.tvItemNomeProjeto); tvStatus = iv.findViewById(R.id.tvItemStatus); tvEquipe = iv.findViewById(R.id.tvItemEquipe); tvBadge = iv.findViewById(R.id.tvBadgePitch); btnDel = iv.findViewById(R.id.btnExcluirProjeto); }
        }
    }

    private void confirmarExclusaoProjeto(Projeto p) {
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Excluir").setMessage("Apagar projeto?").setPositiveButton("Sim", (d, w) -> {
            String url = "https://api-dspi.whyguiih.workers.dev/excluir-projeto";
            JSONObject body = new JSONObject();
            try { body.put("nome_equipe", p.getNomeEquipe()); } catch (Exception e) {}
            Volley.newRequestQueue(this).add(new JsonObjectRequest(Request.Method.POST, url, body, r -> buscarProjetosDaApi(), null));
        }).setNegativeButton("Não", null).show();
    }

    private void configurarBolhaAnimada() {
        int old = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View b = findViewById(R.id.activeBubble);
        LinearLayout n = findViewById(R.id.bottomNavLayout);
        if (n != null) {
            n.post(() -> {
                float tw = n.getWidth() / 5f;
                b.getLayoutParams().width = (int) tw;
                b.requestLayout();
                b.setTranslationX(old * tw);
                if (old != CURRENT_TAB_INDEX) b.animate().translationX(CURRENT_TAB_INDEX * tw).setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
            });
        }
    }
}
