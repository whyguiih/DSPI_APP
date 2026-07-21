package com.example.dspi_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProjetoDetalhesActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 1;
    private LinearLayout layoutDetalhes;
    private String nivel;
    private String nomeUsuarioLogado;

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

        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = prefs.getString("nivel_de_acesso", getIntent().getStringExtra("nivel_de_acesso"));
        
        // CORREÇÃO: Prioriza o nome de exibição para lógica de comentários e permissões
        nomeUsuarioLogado = prefs.getString("nome_usuario", "");
        if (nomeUsuarioLogado == null || nomeUsuarioLogado.trim().isEmpty()) {
            nomeUsuarioLogado = prefs.getString("email_logado", "");
        }

        if (nomeUsuarioLogado == null || nomeUsuarioLogado.trim().isEmpty()) {
            nomeUsuarioLogado = getIntent().getStringExtra("email_usuario");
        }
        if (nomeUsuarioLogado == null) nomeUsuarioLogado = "";

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        Button btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
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

        // LÓGICA DE COMENTÁRIOS: Empresa Edita, Aluno (Equipe) apenas LÊ (sem botão)
        String comentario = p.getComentarioEmpresa() != null ? p.getComentarioEmpresa().trim() : "";
        boolean temComentario = !comentario.isEmpty() && !comentario.equalsIgnoreCase("null");

        String nomeEquipeDoProjeto = p.getNomeEquipe() != null ? p.getNomeEquipe().trim() : "";
        String empresaVinculada = p.getEmpresaVinculada() != null ? p.getEmpresaVinculada().trim() : "";

        boolean isMinhaEquipe = !nomeUsuarioLogado.isEmpty() && nomeEquipeDoProjeto.equalsIgnoreCase(nomeUsuarioLogado.trim());
        boolean isMinhaEmpresa = "4".equals(nivel) && !nomeUsuarioLogado.isEmpty() && empresaVinculada.equalsIgnoreCase(nomeUsuarioLogado.trim());

        if (isMinhaEmpresa) {
            // A empresa dona vê a caixa de texto (pré-preenchida com o comentário dela para editar)
            adicionarSecaoCriarComentario(p, comentario);
        } else if (isMinhaEquipe && temComentario) {
            // A equipe (aluno) vê APENAS o balão de vidro com a mensagem da empresa
            adicionarSecaoComentarioExistente(comentario);
        }
    }

    private void adicionarSecaoComentarioExistente(String comentarioTexto) {
        adicionarDivisoriaETitulo("FEEDBACK GERAL DA EMPRESA");

        TextView tvComentario = new TextView(this);
        LinearLayout.LayoutParams lpTv = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTv.setMargins(0, 0, 0, 48);
        tvComentario.setLayoutParams(lpTv);
        tvComentario.setText(comentarioTexto);
        tvComentario.setTextColor(0xFFFFFFFF);
        tvComentario.setTextSize(15);
        tvComentario.setTypeface(getResources().getFont(R.font.neo_sans));
        tvComentario.setPadding(40, 40, 40, 40);
        tvComentario.setBackground(getResources().getDrawable(R.drawable.bg_input_glass, getTheme()));

        layoutDetalhes.addView(tvComentario);
    }

    private void adicionarSecaoCriarComentario(Projeto p, String comentarioAtual) {
        adicionarDivisoriaETitulo("FEEDBACK GERAL DA EMPRESA");

        EditText etComentario = new EditText(this);
        LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpEdit.setMargins(0, 0, 0, 16);
        etComentario.setLayoutParams(lpEdit);
        etComentario.setHint("Escreva ou edite suas orientações para a equipe aqui...");
        etComentario.setHintTextColor(0x80FFFFFF);
        etComentario.setTextColor(0xFFFFFFFF);
        etComentario.setTextSize(15);
        etComentario.setTypeface(getResources().getFont(R.font.neo_sans));
        etComentario.setMinLines(5);
        etComentario.setGravity(Gravity.TOP | Gravity.START);
        etComentario.setPadding(40, 40, 40, 40);
        etComentario.setBackground(getResources().getDrawable(R.drawable.bg_input_glass, getTheme()));

        if (!comentarioAtual.isEmpty() && !comentarioAtual.equalsIgnoreCase("null")) {
            etComentario.setText(comentarioAtual);
        }

        Button btnSalvarComentario = new Button(this);
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 140);
        lpBtn.setMargins(0, 8, 0, 48);
        btnSalvarComentario.setLayoutParams(lpBtn);

        btnSalvarComentario.setText(comentarioAtual.isEmpty() ? "ENVIAR FEEDBACK" : "ATUALIZAR FEEDBACK");
        btnSalvarComentario.setTextSize(14);
        btnSalvarComentario.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        btnSalvarComentario.setTextColor(0xFFFFFFFF);
        btnSalvarComentario.setBackground(getResources().getDrawable(R.drawable.bg_button_login, getTheme()));

        btnSalvarComentario.setOnClickListener(v -> {
            String feedback = etComentario.getText().toString().trim();
            if (!feedback.isEmpty()) {
                salvarComentarioNoBanco(p, feedback);
            } else {
                Toast.makeText(this, "O balão de feedback está vazio.", Toast.LENGTH_SHORT).show();
            }
        });

        layoutDetalhes.addView(etComentario);
        layoutDetalhes.addView(btnSalvarComentario);
    }

    private void adicionarDivisoriaETitulo(String titulo) {
        View linha = new View(this);
        linha.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        linha.setBackgroundColor(0x4DFFFFFF);
        LinearLayout.LayoutParams lpLinha = (LinearLayout.LayoutParams) linha.getLayoutParams();
        lpLinha.setMargins(0, 48, 0, 24);
        linha.setLayoutParams(lpLinha);
        layoutDetalhes.addView(linha);

        TextView tvTituloFeedback = new TextView(this);
        tvTituloFeedback.setText(titulo);
        tvTituloFeedback.setTextColor(0xFFB3E5FC);
        tvTituloFeedback.setTextSize(14);
        tvTituloFeedback.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        tvTituloFeedback.setPadding(0, 0, 0, 16);
        layoutDetalhes.addView(tvTituloFeedback);
    }

    private void salvarComentarioNoBanco(Projeto p, String comentarioEnviado) {
        String url = "https://api-dspi.whyguiih.workers.dev/salvar-comentario";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nome_equipe", p.getNomeEquipe());
            jsonBody.put("comentario", comentarioEnviado);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Feedback atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                            p.setComentarioEmpresa(comentarioEnviado);
                            layoutDetalhes.removeAllViews();
                            preencherDadosDoProjeto(p);
                        } else {
                            Toast.makeText(this, "Aviso: " + response.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {}
                },
                error -> Toast.makeText(this, "Erro de Conexão", Toast.LENGTH_LONG).show()
        ) {
            @Override public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>(); h.put("Content-Type", "application/json; charset=utf-8"); return h;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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
}