package com.example.dspi_app;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.EditText;
import android.widget.Toast;

public class ProjetoDetalhesActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 1;
    private LinearLayout layoutDetalhes;
    private String nivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_projeto_detalhes);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        configurarBolhaFixa();

        nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        Button btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0); // Sai sem animação para transição perfeita
        });

        layoutDetalhes = findViewById(R.id.layoutDetalhesDinamicos);

        Projeto projeto = (Projeto) getIntent().getSerializableExtra("projeto_selecionado");
        if (projeto != null) {
            preencherDadosDoProjeto(projeto);
        }
    }

    private void preencherDadosDoProjeto(Projeto p) {
        adicionarSecaoTitulo(p.getNomeProjeto(), "Equipe " + p.getNomeEquipe() + " | Status: " + p.getStatus());

        adicionarCabecalho("Informações da Equipe");
        adicionarCampo("Integrantes:", p.getIntegrantes());
        adicionarCampo("Orientador:", p.getOrientador());

        adicionarCabecalho("Canvas do Projeto (tb_canva)");
        // AJUSTE NESTAS DUAS LINHAS PARA CONECTAR AOS NOVOS MÉTODOS:
        adicionarCampo("Proposta Chave:", p.getPropostaChave());
        adicionarCampo("Segmentos de Clientes:", p.getSegmentosClientes());
        adicionarCampo("Atividades Chaves:", p.getAtividadesChaves());
        adicionarCampo("Recursos Chaves:", p.getRecursosChaves());
        adicionarCampo("Relacionamentos:", p.getRelacionamentosClientes());
        adicionarCampo("Canais:", p.getCanais());
        adicionarCampo("Estrutura de Custos:", p.getEstruturaCustos());
        adicionarCampo("Fluxo de Receita:", p.getFluxoReceita());
        adicionarCampo("Parceiros Chaves:", p.getParceirosChaves());

        adicionarCabecalho("Acompanhamento (tb_acompanhamento)");
        adicionarCampo("Tarefas Atuais:", p.getTarefas());
        adicionarCampo("Dificuldades Enxergadas:", p.getDificuldadesEnxergadas());
    }

    private void configurarBolhaFixa() {
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

        bottomNavLayout.post(() -> {
            float tabWidth = bottomNavLayout.getWidth() / 5f;
            activeBubble.getLayoutParams().width = (int) tabWidth;
            activeBubble.requestLayout();
            activeBubble.setTranslationX(CURRENT_TAB_INDEX * tabWidth);
        });
    }

    private void adicionarSecaoTitulo(String titulo, String subtitulo) {
        TextView tvTitulo = new TextView(this);
        tvTitulo.setText(titulo);
        tvTitulo.setTextColor(0xFFFFFFFF);
        tvTitulo.setTextSize(24);
        tvTitulo.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        tvTitulo.setPadding(0, 8, 0, 4);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitulo);
        tvSub.setTextColor(0xFFFFD700);
        tvSub.setTextSize(16);
        tvSub.setTypeface(getResources().getFont(R.font.neo_sans));
        tvSub.setPadding(0, 0, 0, 16);

        layoutDetalhes.addView(tvTitulo);
        layoutDetalhes.addView(tvSub);
    }

    private void adicionarCabecalho(String texto) {
        View linha = new View(this);
        linha.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        linha.setBackgroundColor(0x4DFFFFFF);

        TextView tv = new TextView(this);
        tv.setText(texto.toUpperCase());
        tv.setTextColor(0xFFB3E5FC);
        tv.setTextSize(14);
        tv.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        tv.setPadding(0, 24, 0, 12);

        layoutDetalhes.addView(linha);
        layoutDetalhes.addView(tv);
    }

    private void adicionarCampo(String rotulo, String valor) {
        TextView tvRotulo = new TextView(this);
        tvRotulo.setText(rotulo);
        tvRotulo.setTextColor(0xFFFFFFFF);
        tvRotulo.setTextSize(16);
        tvRotulo.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        tvRotulo.setPadding(0, 8, 0, 2);

        TextView tvValor = new TextView(this);
        tvValor.setText(valor != null && !valor.isEmpty() ? valor : "Dado não preenchido.");
        tvValor.setTextColor(0xE6FFFFFF);
        tvValor.setTextSize(15);
        tvValor.setTypeface(getResources().getFont(R.font.neo_sans));
        tvValor.setPadding(0, 0, 0, 12);

        layoutDetalhes.addView(tvRotulo);
        layoutDetalhes.addView(tvValor);

        // Se o usuário for Nível 4 (Empresa), insere uma área de feedback para esse campo específico
        if ("4".equals(nivel)) {
            LinearLayout layoutComentario = new LinearLayout(this);
            layoutComentario.setOrientation(LinearLayout.HORIZONTAL);
            layoutComentario.setPadding(0, 4, 0, 16);

            EditText etComentario = new EditText(this);
            LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            etComentario.setLayoutParams(lpEdit);
            etComentario.setHint("Comentário da empresa sobre " + rotulo.replace(":", "") + "...");
            etComentario.setHintTextColor(0x80FFFFFF);
            etComentario.setTextColor(0xFFFFFFFF);
            etComentario.setTextSize(14);
            etComentario.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));

            Button btnSalvarComentario = new Button(this);
            btnSalvarComentario.setText("Salvar");
            btnSalvarComentario.setTextSize(12);
            btnSalvarComentario.setTextColor(0xFFFFFFFF);
            btnSalvarComentario.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF0077CC));

            btnSalvarComentario.setOnClickListener(v -> {
                String feedback = etComentario.getText().toString().trim();
                if (!feedback.isEmpty()) {
                    // Pronto para realizar a requisição HTTP (Volley) salvando o feedback para esta seção
                    Toast.makeText(this, "Comentário salvo para " + rotulo, Toast.LENGTH_SHORT).show();
                    etComentario.setText(""); // Limpa o campo após salvar
                } else {
                    Toast.makeText(this, "Digite algo antes de salvar.", Toast.LENGTH_SHORT).show();
                }
            });

            layoutComentario.addView(etComentario);
            layoutComentario.addView(btnSalvarComentario);
            layoutDetalhes.addView(layoutComentario);
        }
    }
}