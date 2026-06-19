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

        // Recupera sessão do usuário logado (Usado APENAS para validar a permissão de visualização do balão)
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = prefs.getString("nivel_de_acesso", getIntent().getStringExtra("nivel_de_acesso"));
        nomeUsuarioLogado = prefs.getString("email_logado", "");
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

        // =================================================================================
        // LÓGICA DE PERMISSÃO PARA MOSTRAR O BALÃO DE FEEDBACK
        // =================================================================================
        String empresaVinculada = p.getEmpresaVinculada() != null ? p.getEmpresaVinculada().trim() : "";
        boolean isMinhaEmpresa = !nomeUsuarioLogado.isEmpty() && empresaVinculada.equalsIgnoreCase(nomeUsuarioLogado.trim());

        if ("4".equals(nivel) && isMinhaEmpresa) {
            adicionarSecaoComentarioGeral(p);
        }
    }

    private void adicionarSecaoComentarioGeral(Projeto p) {
        View linha = new View(this);
        linha.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        linha.setBackgroundColor(0x4DFFFFFF);
        LinearLayout.LayoutParams lpLinha = (LinearLayout.LayoutParams) linha.getLayoutParams();
        lpLinha.setMargins(0, 48, 0, 24);
        linha.setLayoutParams(lpLinha);
        layoutDetalhes.addView(linha);

        TextView tvTituloFeedback = new TextView(this);
        tvTituloFeedback.setText("FEEDBACK GERAL DA EMPRESA");
        tvTituloFeedback.setTextColor(0xFFB3E5FC);
        tvTituloFeedback.setTextSize(14);
        tvTituloFeedback.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        tvTituloFeedback.setPadding(0, 0, 0, 16);
        layoutDetalhes.addView(tvTituloFeedback);

        EditText etComentario = new EditText(this);
        LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lpEdit.setMargins(0, 0, 0, 16);
        etComentario.setLayoutParams(lpEdit);
        etComentario.setHint("Escreva suas orientações, elogios e feedbacks gerais para a equipe aqui...");
        etComentario.setHintTextColor(0x80FFFFFF);
        etComentario.setTextColor(0xFFFFFFFF);
        etComentario.setTextSize(15);
        etComentario.setTypeface(getResources().getFont(R.font.neo_sans));
        etComentario.setMinLines(5);
        etComentario.setGravity(Gravity.TOP | Gravity.START);
        etComentario.setPadding(40, 40, 40, 40);
        etComentario.setBackground(getResources().getDrawable(R.drawable.bg_input_glass, getTheme()));

        Button btnSalvarComentario = new Button(this);
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
        );
        lpBtn.setMargins(0, 8, 0, 48);
        btnSalvarComentario.setLayoutParams(lpBtn);
        btnSalvarComentario.setText("ENVIAR FEEDBACK");
        btnSalvarComentario.setTextSize(14);
        btnSalvarComentario.setTypeface(getResources().getFont(R.font.neo_sans_bold_italic));
        btnSalvarComentario.setTextColor(0xFFFFFFFF);
        btnSalvarComentario.setBackground(getResources().getDrawable(R.drawable.bg_button_login, getTheme()));

        // =====================================================================
        // DISPARO DA REQUISIÇÃO (Pegando a equipe do PROJETO ABERTO)
        // =====================================================================
        btnSalvarComentario.setOnClickListener(v -> {
            String feedback = etComentario.getText().toString().trim();
            if (!feedback.isEmpty()) {

                // Pega a equipe EXATA que está vinculada a este card aberto
                String nomeEquipeDoProjetoAberto = p.getNomeEquipe();
                if(nomeEquipeDoProjetoAberto == null) nomeEquipeDoProjetoAberto = "";

                // Chama a API enviando o nome da equipe alvo e o texto
                salvarComentarioNoBanco(nomeEquipeDoProjetoAberto, feedback, etComentario);
            } else {
                Toast.makeText(this, "O balão de feedback está vazio.", Toast.LENGTH_SHORT).show();
            }
        });

        layoutDetalhes.addView(etComentario);
        layoutDetalhes.addView(btnSalvarComentario);
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

    private void salvarComentarioNoBanco(String equipeAlvo, String comentario, EditText caixaTexto) {
        String url = "https://api-dspi.whyguiih.workers.dev/salvar-comentario";

        JSONObject jsonBody = new JSONObject();
        try {
            // As chaves do JSON agora estão alinhadas com o Worker atualizado: "nome_equipe" e "comentario"
            jsonBody.put("nome_equipe", equipeAlvo);
            jsonBody.put("comentario", comentario);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Feedback enviado com sucesso!", Toast.LENGTH_SHORT).show();
                            caixaTexto.setText(""); // Limpa o campo para ficar bonito
                        } else {
                            // Se a API retornar sucesso=false, mostra a mensagem real do Worker
                            Toast.makeText(this, "Aviso: " + response.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Erro no processamento da resposta.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String erroDetalhe = "Falha de conexão";
                    if(error.networkResponse != null && error.networkResponse.data != null) {
                        erroDetalhe = new String(error.networkResponse.data);
                    }
                    Log.e("API_COMENTARIO", "Erro Volley: " + erroDetalhe);
                    Toast.makeText(this, "Erro da API: " + erroDetalhe, Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                10000, 0, com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}