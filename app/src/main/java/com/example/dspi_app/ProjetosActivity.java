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
    private List<Projeto> todosMeusProjetos = new ArrayList<>();
    private List<Projeto> todosOutrosProjetos = new ArrayList<>();
    private EditText etPesquisa;

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
        nivel = prefs.getString("nivel_de_acesso", getIntent().getStringExtra("nivel_de_acesso"));
        
        // CORREÇÃO: Prioriza o nome de exibição em vez do e-mail
        nomeUsuario = prefs.getString("nome_usuario", "");
        if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
            nomeUsuario = prefs.getString("email_logado", "");
        }

        if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
            nomeUsuario = getIntent().getStringExtra("email_usuario");
        }
        if (nomeUsuario == null) nomeUsuario = "";

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        Button btnAbrirFormulario = findViewById(R.id.btnAbrirFormulario);

        // =========================================================================
        // TRAVA PARA ALUNO, EMPRESA E ORIENTADOR (5): Não veem o botão do formulário
        // =========================================================================
        if ("4".equals(nivel) || "6".equals(nivel) || "1".equals(nivel) || "2".equals(nivel) || "5".equals(nivel)) {
            btnAbrirFormulario.setVisibility(View.GONE);
        }

        btnAbrirFormulario.setOnClickListener(v -> {
            Intent intent = new Intent(ProjetosActivity.this, FormularioActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
            intent.putExtra("email_usuario", nomeUsuario);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        RecyclerView rvMeusProjetos = findViewById(R.id.rvMeusProjetos);
        RecyclerView rvOutrosProjetos = findViewById(R.id.rvOutrosProjetos);
        rvMeusProjetos.setLayoutManager(new LinearLayoutManager(this));
        rvOutrosProjetos.setLayoutManager(new LinearLayoutManager(this));

        etPesquisa = findViewById(R.id.etPesquisa);
        etPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarProjetos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buscarProjetosDaApi();
    }

    private void buscarProjetosDaApi() {
        String url = "https://api-dspi.whyguiih.workers.dev/listar-projetos";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray data = response.getJSONArray("data");
                            todosMeusProjetos.clear();
                            todosOutrosProjetos.clear();

                            String userLogado = nomeUsuario.trim();

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
                                        obj.optString("empresa_vinculada", ""),
                                        obj.optString("video_url", "")
                                );

                                p.setComentarioEmpresa(obj.optString("comentario_empresa", ""));

                                String empresaVinc = p.getEmpresaVinculada() != null ? p.getEmpresaVinculada().trim() : "";
                                String nomeEqp = p.getNomeEquipe() != null ? p.getNomeEquipe().trim() : "";

                                if ("4".equals(nivel)) {
                                    // CORREÇÃO: Empresa compara com seu NOME (nomeUsuario agora tem o nome real)
                                    if (!userLogado.isEmpty() && empresaVinc.equalsIgnoreCase(userLogado)) {
                                        todosMeusProjetos.add(p);
                                    } else if (empresaVinc.isEmpty() || empresaVinc.equalsIgnoreCase("null") || empresaVinc.equalsIgnoreCase("Nenhuma")) {
                                        todosOutrosProjetos.add(p);
                                    }
                                } else {
                                    if (!userLogado.isEmpty() && nomeEqp.equalsIgnoreCase(userLogado)) {
                                        todosMeusProjetos.add(p);
                                    } else {
                                        todosOutrosProjetos.add(p);
                                    }
                                }
                            }
                            configurarListasDeProjetos(todosMeusProjetos, todosOutrosProjetos);
                        } else {
                            Toast.makeText(this, "Erro da API: " + response.optString("error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar dados", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Falha de Conexão com API", Toast.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void filtrarProjetos(String query) {
        String termo = query.toLowerCase().trim();
        List<Projeto> filtradosMeus = new ArrayList<>();
        List<Projeto> filtradosOutros = new ArrayList<>();

        for (Projeto p : todosMeusProjetos) {
            if (p.getNomeProjeto().toLowerCase().contains(termo) || p.getNomeEquipe().toLowerCase().contains(termo)) {
                filtradosMeus.add(p);
            }
        }

        for (Projeto p : todosOutrosProjetos) {
            if (p.getNomeProjeto().toLowerCase().contains(termo) || p.getNomeEquipe().toLowerCase().contains(termo)) {
                filtradosOutros.add(p);
            }
        }

        configurarListasDeProjetos(filtradosMeus, filtradosOutros);
    }

    private void configurarListasDeProjetos(List<Projeto> meusProjetos, List<Projeto> outrosProjetos) {
        RecyclerView rvMeusProjetos = findViewById(R.id.rvMeusProjetos);
        RecyclerView rvOutrosProjetos = findViewById(R.id.rvOutrosProjetos);
        TextView tvSeusProjetos = findViewById(R.id.tvSeusProjetos);
        TextView tvOutrosProjetos = findViewById(R.id.tvOutrosProjetos);

        tvSeusProjetos.setText("Meus Projetos");

        if (meusProjetos.isEmpty()) {
            tvSeusProjetos.setVisibility(View.GONE);
            rvMeusProjetos.setVisibility(View.GONE);
        } else {
            tvSeusProjetos.setVisibility(View.VISIBLE);
            rvMeusProjetos.setVisibility(View.VISIBLE);
            rvMeusProjetos.setAdapter(new ProjetoAdapter(meusProjetos, this::abrirPaginaDetalhes));
        }

        if (outrosProjetos.isEmpty()) {
            tvOutrosProjetos.setVisibility(View.GONE);
            rvOutrosProjetos.setVisibility(View.GONE);
        } else {
            tvOutrosProjetos.setVisibility(View.VISIBLE);
            rvOutrosProjetos.setVisibility(View.VISIBLE);
            rvOutrosProjetos.setAdapter(new ProjetoAdapter(outrosProjetos, this::abrirPaginaDetalhes));
        }
    }

    private void configurarBolhaAnimada() {
        int oldTabIndex = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

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

    private void abrirPaginaDetalhes(Projeto projeto) {
        Intent intent;
        String userLogado = nomeUsuario.trim();
        String nomeEqp = projeto.getNomeEquipe() != null ? projeto.getNomeEquipe().trim() : "";
        boolean isMeuProjeto = !userLogado.isEmpty() && nomeEqp.equalsIgnoreCase(userLogado);

        if ("2".equals(nivel) || isMeuProjeto) {
            // Nível 2 ou o próprio dono do projeto vão para o Formulário
            intent = new Intent(ProjetosActivity.this, FormularioActivity.class);
            intent.putExtra("projeto_usuario", projeto.getNomeEquipe());
        } else {
            // Restante (Nível 5, 4, 6 vendo projetos alheios) vão para Detalhes resumido
            intent = new Intent(ProjetosActivity.this, ProjetoDetalhesActivity.class);
            intent.putExtra("projeto_selecionado", projeto);
        }

        intent.putExtra("nivel_de_acesso", nivel);
        intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public static class ProjetoAdapter extends RecyclerView.Adapter<ProjetoAdapter.ViewHolder> {
        private final List<Projeto> projetos;
        private final OnItemClickListener listener;

        public interface OnItemClickListener { void onItemClick(Projeto projeto); }

        public ProjetoAdapter(List<Projeto> projetos, OnItemClickListener listener) {
            this.projetos = projetos;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_projeto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Projeto projeto = projetos.get(position);
            holder.tvNome.setText(projeto.getNomeProjeto());
            String st = projeto.getStatus();
            holder.tvStatus.setText("Status: " + (st == null || st.trim().isEmpty() || st.equals("null") ? "Não Iniciado" : st));
            holder.tvEquipe.setText("Equipe: " + projeto.getNomeEquipe());

            // Exibe badge se houver vídeo
            if (projeto.getVideoUrl() != null && !projeto.getVideoUrl().isEmpty() && !projeto.getVideoUrl().equals("null")) {
                holder.tvBadgePitch.setVisibility(View.VISIBLE);
            } else {
                holder.tvBadgePitch.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(projeto));
        }

        @Override
        public int getItemCount() { return projetos.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNome, tvStatus, tvEquipe, tvBadgePitch;
            public ViewHolder(View itemView) {
                super(itemView);
                tvNome = itemView.findViewById(R.id.tvItemNomeProjeto);
                tvStatus = itemView.findViewById(R.id.tvItemStatus);
                tvEquipe = itemView.findViewById(R.id.tvItemEquipe);
                tvBadgePitch = itemView.findViewById(R.id.tvBadgePitch);
            }
        }
    }
}