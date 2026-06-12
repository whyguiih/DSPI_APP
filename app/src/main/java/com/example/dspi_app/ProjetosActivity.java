package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProjetosActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 1; // 1 = Projetos
    private String nivel;
    private String email;

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

        nivel = getIntent().getStringExtra("nivel_de_acesso");
        email = getIntent().getStringExtra("email_usuario");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        Button btnAbrirFormulario = findViewById(R.id.btnAbrirFormulario);
        if ("4".equals(nivel)) {
            btnAbrirFormulario.setVisibility(View.GONE); // Empresas não criam/editam formulários diretamente
        }
        btnAbrirFormulario.setOnClickListener(v -> {
            Intent intent = new Intent(ProjetosActivity.this, FormularioActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);

            intent.putExtra("email_usuario", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        RecyclerView rvMeusProjetos = findViewById(R.id.rvMeusProjetos);
        RecyclerView rvOutrosProjetos = findViewById(R.id.rvOutrosProjetos);

        rvMeusProjetos.setLayoutManager(new LinearLayoutManager(this));
        rvOutrosProjetos.setLayoutManager(new LinearLayoutManager(this));

        List<Projeto> todosProjetos = carregarDadosMock();
        List<Projeto> meusProjetos = new ArrayList<>();
        List<Projeto> outrosProjetos = new ArrayList<>();

        for (Projeto p : todosProjetos) {
            if ("4".equals(nivel)) {
                // Filtra os projetos vinculados à empresa do usuário logado
                // (Exemplo usando o mock: se o nome da equipe/empresa bater com o vínculo do usuário)
                if (p.getNomeEquipe().contains("Gazo")) {
                    meusProjetos.add(p);
                } else {
                    outrosProjetos.add(p);
                }
            } else {
                // Lógica original para estudantes e admin
                if (p.getNomeProjeto().contains("Drones")) {
                    meusProjetos.add(p);
                } else {
                    outrosProjetos.add(p);
                }
            }
        }

        TextView tvSeusProjetos = findViewById(R.id.tvSeusProjetos);
        if (meusProjetos.isEmpty()) {
            tvSeusProjetos.setVisibility(View.GONE);
            rvMeusProjetos.setVisibility(View.GONE);
        } else {
            rvMeusProjetos.setAdapter(new ProjetoAdapter(meusProjetos, this::abrirPaginaDetalhes));
        }

        TextView tvOutrosProjetos = findViewById(R.id.tvOutrosProjetos);
        if (outrosProjetos.isEmpty()) {
            tvOutrosProjetos.setVisibility(View.GONE);
            rvOutrosProjetos.setVisibility(View.GONE);
        } else {
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
        Intent intent = new Intent(ProjetosActivity.this, ProjetoDetalhesActivity.class);
        intent.putExtra("projeto_selecionado", projeto);
        intent.putExtra("nivel_de_acesso", nivel);
        intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // Substitua o método carregarDadosMock() antigo por este:
    private List<Projeto> carregarDadosMock() {
        List<Projeto> lista = new ArrayList<>();

        // Dados reais extraídos de db_dspi.sql -> tb_canva (id: 41 - Equipe Gazo)
        lista.add(new Projeto(
                "Drones Logísticos Autônomos", "gazo", "Em Andamento",
                "João, Maria, Pedro", "Prof. Silva",
                "Revolucionar a logística de última milha (last-mile) reduzindo o tempo de entrega de suprimentos médicos e peças industriais críticas em até 80%.",
                "Hospitais regionais que necessitam de transporte urgente, indústrias metalmecânicas com unidades fabris distantes.",
                "Desenvolvimento de algoritmos de navegação autônoma com desvio de obstáculos baseado em LiDAR, gestão de tráfego aéreo privado.",
                "Frota de drones com tecnologia de propulsão redundante, software de inteligência artificial para controle de enxame.",
                "Interface de rastreamento em tempo real com precisão centimétrica, suporte técnico dedicado.",
                "Plataforma de gestão logística integrada via API para grandes e-commerces, aplicativo mobile.",
                "Elevados investimentos em certificação aeronáutica e conformidade legal, manutenção de baterias de alta densidade.",
                "Taxa por quilômetro voado ou por entrega realizada, mensalidade por drone alocado (DaaS).",
                "Fabricantes de células de bateria de alto desempenho, agências reguladoras (ANAC).",
                "Finalizar testes LiDAR, homologação ANAC.", "Forte ventania durante os voos de teste."
        ));

        // Dados reais extraídos de db_dspi.sql -> tb_canva (id: 17 - Equipe b)
        lista.add(new Projeto(
                "Monitoramento IoT Agrícola", "b", "Concluído",
                "Lucas, Ana", "Prof. Marcos",
                "Maximizar a produtividade agrícola através de dados precisos, reduzindo o desperdício de recursos naturais e defensivos químicos.",
                "Pequenos e médios agricultores da região de Garibaldi e Carlos Barbosa, cooperativas vinícolas.",
                "Monitoramento em tempo real de sensores de solo, processamento de dados climáticos via satélite.",
                "Sensores IoT de alta precisão, plataforma de processamento em nuvem escalável.",
                "Consultoria personalizada pós-venda, treinamentos presenciais nas cooperativas locais, suporte via WhatsApp.",
                "Aplicação móvel offline (para áreas sem sinal), portal de administração web.",
                "Custos fixos de infraestrutura de nuvem, aquisição e calibração de hardware IoT.",
                "Modelo de assinatura mensal por hectare monitorado, venda de kits de sensores.",
                "Fabricantes de microcontroladores (ESP32), sindicatos rurais de Carlos Barbosa.",
                "Instalar 50 sensores nas fazendas.", "Dificuldade de sinal 4G em algumas propriedades rurais."
        ));

        // Dados reais extraídos de db_dspi.sql -> tb_canva (id: 44 - Equipe dipp)
        lista.add(new Projeto(
                "App de Móveis Virtuais 3D", "dipp", "Não iniciado",
                "Carlos, Beatriz", "Prof. Almeida",
                "Permitir que usuários visualizem móveis e cores em suas casas usando apenas a câmera do celular com realismo impressionante.",
                "Pessoas em processo de reforma ou mudança, arquitetos autônomos e lojas de móveis.",
                "Desenvolvimento de algoritmos de renderização, curadoria de catálogo de móveis digitais.",
                "Plataforma mobile, banco de dados de modelos 3D, equipe de desenvolvedores.",
                "Chatbot de auxílio criativo, comunidade para compartilhamento de projetos.",
                "App Store, Google Play, anúncios em redes sociais visuais (Instagram/Pinterest).",
                "Manutenção do aplicativo, hospedagem em nuvem, marketing digital e salários.",
                "Assinatura mensal para profissionais, comissão sobre móveis vendidos pelo app.",
                "Fabricantes de móveis, lojas de tintas, desenvolvedores de motores gráficos.",
                "Renderização do primeiro lote de poltronas.", "Problemas de incompatibilidade com câmeras Android antigas."
        ));

        return lista;
    }

    // Estrutura pronta para buscar da sua Cloudflare Worker no futuro (substituindo o Mock)
    private void buscarProjetosDaApi() {
        /*
        String url = "https://api-dspi.whyguiih.workers.dev/listar-projetos";

        // A API deverá fazer um SELECT mesclando as tabelas através do nome da equipe:
        // SELECT * FROM tb_equipe INNER JOIN tb_canva ON tb_equipe.nome_equipe = tb_canva.usuario INNER JOIN tb_acompanhamento_projeto ...

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
            response -> {
                List<Projeto> projetosAPI = new ArrayList<>();
                // Popular a lista com response.getJSONObject(i).getString("proposta_chave"), etc...
                // rvMeusProjetos.setAdapter(new ProjetoAdapter(projetosAPI, this::abrirPaginaDetalhes));
            },
            error -> Toast.makeText(this, "Erro ao carregar do Banco de Dados real.", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
        */
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
            holder.tvStatus.setText("Status: " + projeto.getStatus());
            holder.tvEquipe.setText("Equipe: " + projeto.getNomeEquipe());
            holder.itemView.setOnClickListener(v -> listener.onItemClick(projeto));
        }

        @Override
        public int getItemCount() { return projetos.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNome, tvStatus, tvEquipe;
            public ViewHolder(View itemView) {
                super(itemView);
                tvNome = itemView.findViewById(R.id.tvItemNomeProjeto);
                tvStatus = itemView.findViewById(R.id.tvItemStatus);
                tvEquipe = itemView.findViewById(R.id.tvItemEquipe);
            }
        }
    }
}