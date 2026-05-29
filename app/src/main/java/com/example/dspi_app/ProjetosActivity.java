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

        // APLICA O RECUO EXATAMENTE NO PAI PARA NÃO INVADIR NENHUMA BARRA!
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
        btnAbrirFormulario.setOnClickListener(v -> {
            Intent intent = new Intent(ProjetosActivity.this, FormularioActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);

            intent.putExtra("email_usuario", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        RecyclerView rvProjetos = findViewById(R.id.rvProjetos);
        rvProjetos.setLayoutManager(new LinearLayoutManager(this));

        // Mocks dos dados baseados no seu SQL Cloudflare D1
        List<Projeto> listaProjetos = carregarDadosMock();
        rvProjetos.setAdapter(new ProjetoAdapter(listaProjetos, this::abrirPaginaDetalhes));
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

    private List<Projeto> carregarDadosMock() {
        List<Projeto> lista = new ArrayList<>();
        lista.add(new Projeto(
                "Drones Logísticos Autônomos", "Equipe Gazo", "Em Andamento",
                "João, Maria, Pedro", "Prof. Silva",
                "Revolucionar a logística de última milha reduzindo o tempo de entrega em 80%.",
                "Hospitais regionais, indústrias metalmecânicas, e-commerces.",
                "Navegação autônoma LiDAR, gestão de tráfego aéreo, manutenção preventiva.",
                "Frota de drones, software de IA, equipe de engenheiros aeroespaciais.",
                "Rastreamento em tempo real, suporte técnico dedicado.",
                "API para e-commerces, aplicativo mobile, parcerias.",
                "Certificação aeronáutica, baterias, salários, seguros.",
                "Taxa por quilômetro voado, modelo DaaS.",
                "Fabricantes de baterias, ANAC, provedores de telecomunicações.",
                "Finalizar testes LiDAR, homologação ANAC.", "Forte ventania durante os voos de teste."
        ));
        lista.add(new Projeto(
                "Monitoramento IoT Agrícola", "Equipe B", "Concluído",
                "Lucas, Ana", "Prof. Marcos",
                "Maximizar a produtividade agrícola através de dados precisos.",
                "Pequenos e médios agricultores da região de Garibaldi.",
                "Monitoramento em tempo real de sensores de solo.",
                "Sensores IoT, plataforma cloud, especialistas em agronomia.",
                "Consultoria personalizada pós-venda, suporte via WhatsApp 24/7.",
                "Aplicação móvel offline, portal de administração web.",
                "Infraestrutura de nuvem, aquisição de hardware.",
                "Modelo de assinatura mensal por hectare.",
                "Fabricantes de microcontroladores (ESP32), sindicatos rurais.",
                "Instalar 50 sensores nas fazendas parceiras.", "Dificuldade de sinal 4G no campo."
        ));
        return lista;
    }

    // Adaptador interno do RecyclerView
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