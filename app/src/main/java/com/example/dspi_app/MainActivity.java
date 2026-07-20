package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        configurarBolhaAnimada();
        configurarMenuLateral();
        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        String email = getIntent().getStringExtra("email_usuario");

        if (email == null || email.isEmpty()) {
            email = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("email_logado", "");
        }

        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        if (email != null && !email.isEmpty()) {
            verificarCronogramaEAvisar(email);
        }
    }

    private void verificarCronogramaEAvisar(String emailOuNome) {
        String url = "https://api-dspi.whyguiih.workers.dev/buscar-dados";
        JSONObject jsonBody = new JSONObject();
        try {
            // Enviamos o email/nome logado para a API buscar onde ele é 'responsavel'
            jsonBody.put("usuario", emailOuNome);
            jsonBody.put("tipo", "cronograma");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success") && response.getBoolean("existe")) {
                            // A API agora pode retornar um array de tarefas ou um objeto único
                            // Vamos tratar ambos para garantir compatibilidade
                            Object data = response.get("data");
                            
                            if (data instanceof JSONObject) {
                                processarTarefa((JSONObject) data);
                            } else if (data instanceof org.json.JSONArray) {
                                org.json.JSONArray tarefas = (org.json.JSONArray) data;
                                for (int i = 0; i < tarefas.length(); i++) {
                                    processarTarefa(tarefas.getJSONObject(i));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> android.util.Log.e("API_ERROR", "Erro ao buscar alertas"));

        Volley.newRequestQueue(this).add(request);
    }

    private void processarTarefa(JSONObject dados) {
        String dataInicio = dados.optString("data_inicio", "");
        String dataFinal = dados.optString("data_final", "");
        if (dataFinal.isEmpty()) dataFinal = dados.optString("dados_final", "");
        
        String etapa = dados.optString("etapas", "da tarefa atual");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String hoje = sdf.format(new Date());

        boolean comecaHoje = isMesmoDia(dataInicio, hoje);
        boolean terminaHoje = isMesmoDia(dataFinal, hoje);

        if (comecaHoje && terminaHoje) {
            mostrarAlertaWeb("⚠Atenção! Hoje começa e termina: " + etapa + "!", "#F44336");
        } else if (terminaHoje) {
            mostrarAlertaWeb("Prazo final: Hoje deve ser entregue: " + etapa + "!", "#FF9800");
        } else if (comecaHoje) {
            mostrarAlertaWeb("Hoje começa: " + etapa + "!", "#2196F3");
        }
    }

    private boolean isMesmoDia(String dataBanco, String hoje) {
        if (dataBanco == null || dataBanco.isEmpty()) return false;
        
        dataBanco = dataBanco.trim();
        // Caso 1: Formatos idênticos (ex: YYYY-MM-DD)
        if (dataBanco.equals(hoje)) return true;

        // Caso 2: Banco está em DD/MM/YYYY (comum em preenchimento manual)
        if (dataBanco.contains("/")) {
            try {
                String[] partes = dataBanco.split("/");
                if (partes.length == 3) {
                    // Converte DD/MM/YYYY para YYYY-MM-DD para comparar com 'hoje'
                    String dataInvertida = partes[2] + "-" + partes[1] + "-" + partes[0];
                    return dataInvertida.equals(hoje);
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }



    private void mostrarAlertaWeb(String mensagem, String corHexa) {
        try {
            View rootView = findViewById(android.R.id.content);
            // Aumentado o tempo para 8 segundos (8000ms)
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(rootView, mensagem, 8000);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(android.graphics.Color.parseColor(corHexa));

            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = android.view.Gravity.TOP;
            params.topMargin = 120;
            snackbarView.setLayoutParams(params);

            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(android.graphics.Color.BLACK);
            textView.setTextSize(16);
            textView.setMaxLines(3);
            snackbar.show();
        } catch (Exception e) {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show(); // Fallback se a tela bugar
        }
    }
    


    private void configurarMenuLateral() {
        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);
        TextView txtSubpageTitle = findViewById(R.id.txtSubpageTitle);
        TextView txtSubpageContent = findViewById(R.id.txtSubpageContent);
        ImageButton btnBack = findViewById(R.id.btnBackToMenu);

        btnBack.setOnClickListener(v -> viewAnimator.setDisplayedChild(0));

        View.OnClickListener listener = v -> {
            int id = v.getId();
            txtSubpageTitle.setText(getTituloPorId(id));
            txtSubpageContent.setText(getConteudoPorId(id));
            viewAnimator.setDisplayedChild(1);
        };

        findViewById(R.id.btnPitch).setOnClickListener(listener);
        findViewById(R.id.btnCanvas).setOnClickListener(listener);
        findViewById(R.id.btnTabelas).setOnClickListener(listener);
        findViewById(R.id.btnAppInfo).setOnClickListener(listener);
        findViewById(R.id.btnRegulamento).setOnClickListener(listener);
    }

    private String getTituloPorId(int id) {
        if (id == R.id.btnPitch) return "Estrutura do Pitch";
        if (id == R.id.btnCanvas) return "Estrutura do Canvas";
        if (id == R.id.btnTabelas) return "Preenchimento das Tabelas";
        if (id == R.id.btnAppInfo) return "Informações do App";
        if (id == R.id.btnRegulamento) return "Regulamento";
        return "";
    }

    private String getConteudoPorId(int id) {
        if (id == R.id.btnPitch) {
            return "PÁGINA 1: Segmento de Clientes (Para quem?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "• O que preencher na prática:\n\n" +
                    "• Para quem estamos criando valor?\n" +
                    "• Quem são nosso público mais importantes?\n\n" +
                    "Exemplos:\n" +
                    "• Mercado de massa\n" +
                    "• Nicho de mercado\n" +
                    "• Segmentado\n" +
                    "• Diversificado\n" +
                    "• Plataformas Multifacetadas\n" +
                    "\n" +
                    "PÁGINA 2: Relacionamento com o Cliente (Para quem?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Que tipo de relacionamento temos com nosso público?\n" +
                    "• O que os segmentos esperam que possamos estabelecer e manter com eles?\n" +
                    "• Quão caros eles são?\n\n" +
                    "Exemplos:\n" +
                    "• Assistência pessoal\n" +
                    "• Assistência Pessoal Dedicada\n" +
                    "• Self-Service\n" +
                    "• Serviços Automatizados\n" +
                    "• Comunidades\n" +
                    "• Co-criação\n" +
                    "\n" +
                    "PÁGINA 3: Canais (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Através de quais canais atingimos nossos Segmentos de Público?\n" +
                    "• Gostaríamos de atingir? Como podemos alcançá-los agora?\n" +
                    "• Como estão integrados os nossos canais?\n" +
                    "• Quais funcionam melhor? Quais tem melhor custo-benefício?\n" +
                    "• Como estamos integrando-os às rotinas do público?\n" +
                    "• Fases do Canal:\n" +
                    "• Conscientização (Como podemos aumentar a conscientização sobre os nossos produtos e serviços?)\n" +
                    " • Avaliação (Como podemos ajudar o público a avaliar a Proposta de Valor da nossa organização?) \n" +
                    " • Compra (Como podemos permitir que o público compre produtos e serviços específicos?) \n" +
                    " • Entrega (Como entregaremos uma proposta de valor para nosso público?)\n" +
                    "• Pós-venda (Como podemos oferecer suporte pós-venda ao nosso público?)\n" +
                    "\n" +
                    "PÁGINA 4: Proposta de Valor (O quê?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Que problemas do nosso público estamos ajudando a resolver? \n" +
                    "• Que pacotes de produtos e serviços oferecemos a cada Segmento de Público? \n" +
                    "• Quais necessidades do público estamos satisfazendo?\n\n" +
                    "Categorias:\n" +
                    "• Novidade\n" +
                    "• Execução\n" +
                    "• Personalização\n" +
                    "• Projeto\n" +
                    "• Preço\n" +
                    "• Redução de Custos\n" +
                    "• Redução de Risco\n" +
                    "\n" +
                    "PÁGINA 5: Atividades-Chave (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Que atividades-chave a nossa Proposta de Valor exige?\n" +
                    "• Nossos Canais de Distribuição?\n" +
                    "• Relacionamento com o público?\n" +
                    "• Fontes de receita?\n\n" +
                    "Categorias:\n" +
                    "• Produção\n" +
                    "• Resolução de problemas\n" +
                    "• Plataforma / Rede\n" +
                    "\n" +
                    "PÁGINA 6: Parceiros-Chave (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "• Quem são os nossos principais parceiros?\n" +
                    "• Quem são os nossos principais fornecedores?\n" +
                    "• Recursos-chave que estamos adquirindo de parceiros?\n" +
                    "• Principais Atividades que os parceiros realizam?\n" +
                    "• Motivações para parcerias:\n" +
                    "• Otimização e economia\n" +
                    "• Redução de risco e incerteza\n" +
                    "• Aquisição de recursos especiais e atividades\n" +
                    "\n" +
                    "PÁGINA 7: Estrutura de Custos (Quanto?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Quais são os custos mais importantes inerentes ao nosso modelo de negócio?\n" +
                    "• Quais os recursos-chave mais caros?\n" +
                    "• Quais as atividades-chave mais caras?\n" +
                    "• Seu negócio é mais:\n" +
                    "• Guiado por custos (structure de custo mais enxuta, proposta de valor de baixo preço, máxima automação, terceirização extensiva)\n" +
                    "• Guiado por valor (focada na criação de valor, proposta de valor premium)\n\n" +
                    "Exemplos de Características:\n" +
                    "• Custos  Fixos (salários, aluguéis, serviços públicos)\n" +
                    "• Custos Variáveis\n" +
                    "• Economias de escala\n" +
                    "• Economias de escopo\n" +
                    "\n" +
                    "PÁGINA 8: Fluxo de Receitas (Quanto?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Por quais valores nosso público estão realmente interessados em pagar? \n" +
                    "• Por quais eles pagam atualmente? Como eles estão pagando atualmente?\n" +
                    "• Como eles gostariam de pagar? Como cada fluxo de receita contribui para a receita global?\n\n" +
                    "Tipos:\n" +
                    "• Venda de ativos\n" +
                    "• Taxa de utilização\n" +
                    "• Taxas de inscrição\n" +
                    "• Empréstimos / Aluguel / Arrendamento\n" +
                    "• Licenciamento\n" +
                    "• Taxas de corretagem\n" +
                    "• Publicidade\n" +
                    "• Preços Fixos:\n" +
                    "• Preço de tabela\n" +
                    "• Produtos dependentes\n" +
                    "• Segmentos de Público dependentes\n" +
                    "• Volumes dependentes\n" +
                    "• Precificação dinâmica:\n" +
                    "• Negociação (barganha)\n" +
                    "• Gestão de Rendimentos\n" +
                    "• Mercado em Tempo Real\n" +
                    "\n" +
                    "Página 9: Atividades chaves (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Quais recursos-chave nossa Proposta de Valor requer?\n" +
                    "• Nossos canais de distribuição?\n" +
                    "• Relacionamento com o público?\n" +
                    "• Fontes de receita?\n\n" +
                    "Tipos de recursos:\n" +
                    "• Físico\n" +
                    "• Intelectual (Patentes de marcas, direitos autorais, dados privilegiados)\n" +
                    "• Humano\n" +
                    "• Financeiro\n" +
                    "\n";

        }
        if (id == R.id.btnCanvas) {
            return "PÁGINA 1: Segmento de Clientes (Para quem?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Para quem estamos criando valor?\n" +
                    "• Quem são nosso público mais importantes?\n\n" +
                    "Exemplos:\n" +
                    "• Mercado de massa\n" +
                    "• Nicho de mercado\n" +
                    "• Segmentado\n" +
                    "• Diversificado\n" +
                    "• Plataformas Multifacetadas\n" +
                    "\n" +
                    "PÁGINA 2: Relacionamento com o Cliente (Para quem?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Que tipo de relacionamento temos com nosso público?\n" +
                    "• O que os segmentos esperam que possamos estabelecer e manter com eles?\n" +
                    "• Quão caros eles são?\n\n" +
                    "Exemplos:\n" +
                    "• Assistência pessoal\n" +
                    "• Assistência Pessoal Dedicada\n" +
                    "• Self-Service\n" +
                    "• Serviços Automatizados\n" +
                    "• Comunidades\n" +
                    "• Co-criação\n" +
                    "\n" +
                    "PÁGINA 3: Canais (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Através de quais canais atingimos nossos Segmentos de Público?\n" +
                    "• Gostaríamos de atingir? Como podemos alcançá-los agora?\n" +
                    "• Como estão integrados os nossos canais?\n" +
                    "• Quais funcionam melhor? Quais tem melhor custo-benefício?\n" +
                    "• Como estamos integrando-os às rotinas do público?\n\n" +
                    "Fases do Canal:\n" +
                    "• Conscientização (Como podemos aumentar a conscientização sobre os nossos produtos e serviços?)\n" +
                    "•  Avaliação (Como podemos ajudar o público a avaliar a Proposta de Valor da nossa organização?) \n" +
                    "• Compra (Como podemos permitir que o público compre produtos e serviços específicos?) \n" +
                    "• Entrega (Como entregaremos uma proposta de valor para nosso público?)\n" +
                    "• Pós-venda (Como podemos oferecer suporte pós-venda ao nosso público?)\n" +
                    "\n" +
                    "PÁGINA 4: Proposta de Valor (O quê?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Que problemas do nosso público estamos ajudando a resolver? \n" +
                    "• Que pacotes de produtos e serviços oferecemos a cada Segmento de Público? \n" +
                    "• Quais necessidades do público estamos satisfazendo?\n\n" +
                    "Categorias:\n" +
                    "• Novidade\n" +
                    "• Execução\n" +
                    "• Personalização\n" +
                    "• Projeto\n" +
                    "• Preço\n" +
                    "• Redução de Custos\n" +
                    "• Redução de Risco\n" +
                    "\n" +
                    "PÁGINA 5: Atividades-Chave (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Que atividades-chave a nossa Proposta de Valor exige?\n" +
                    "• Nossos Canais de Distribuição?\n" +
                    "• Relacionamento com o público?\n" +
                    "• Fontes de receita?\n\n" +
                    "Categorias:\n" +
                    "• Produção\n" +
                    "• Resolução de problemas\n" +
                    "• Plataforma / Rede\n" +
                    "\n" +
                    "PÁGINA 6: Parceiros-Chave (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "• Quem são os nossos principais parceiros?\n" +
                    "• Quem são os nossos principais fornecedores?\n" +
                    "• Recursos-chave que estamos adquirindo de parceiros?\n" +
                    "• Principais Atividades que os parceiros realizam?\n\n" +
                    "Motivações para parcerias:\n" +
                    "• Otimização e economia\n" +
                    "• Redução de risco e incerteza\n" +
                    "• Aquisição de recursos especiais e atividades\n" +
                    "\n" +
                    "PÁGINA 7: Estrutura de Custos (Quanto?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Quais são os custos mais importantes inerentes ao nosso modelo de negócio?\n" +
                    "• Quais os recursos-chave mais caros?\n" +
                    "• Quais as atividades-chave mais caras?\n\n" +
                    "Seu negócio é mais:\n" +
                    "• Guiado por custos (estrutura de custo mais enxuta, proposta de valor de baixo preço, máxima automação, terceirização extensiva)\n" +
                    "• Guiado por valor (focada na criação de valor, proposta de valor premium)\n" +
                    "Exemplos de Características:\n\n" +
                    "• Custos  Fixos (salários, aluguéis, serviços públicos)\n" +
                    "• Custos Variáveis\n" +
                    "• Economias de escala\n" +
                    "• Economias de escopo\n" +
                    "\n" +
                    "PÁGINA 8: Fluxo de Receitas (Quanto?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Por quais valores nosso público estão realmente interessados em pagar? \n" +
                    "• Por quais eles pagam atualmente? Como eles estão pagando atualmente?\n" +
                    "• Como eles gostariam de pagar? Como cada fluxo de receita contribui para a receita global?\n\n" +
                    "Tipos:\n" +
                    "• Venda de ativos\n" +
                    "• Taxa de utilização\n" +
                    "• Taxas de inscrição\n" +
                    "• Empréstimos / Aluguel / Arrendamento\n" +
                    "• Licenciamento\n" +
                    "• Taxas de corretagem\n" +
                    "• Publicidade\n" +
                    "• Preços Fixos:\n" +
                    "• Preço de tabela\n" +
                    "• Produtos dependentes\n" +
                    "• Segmentos de Público dependentes\n" +
                    "• Volumes dependentes\n" +
                    "• Precificação dinâmica:\n" +
                    "• Negociação (barganha)\n" +
                    "• Gestão de Rendimentos\n" +
                    "• Mercado em Tempo Real\n" +
                    "\n" +
                    "Página 9: Atividades chaves (Como?)\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "O que preencher na prática:\n\n" +
                    "• Quais recursos-chave nossa Proposta de Valor requer?\n" +
                    "• Nossos canais de distribuição?\n" +
                    "• Relacionamento com o público?\n" +
                    "• Fontes de receita?\n\n" +
                    "Tipos de recursos:\n" +
                    "• Físico\n" +
                    "• Intelectual (Patentes de marcas, direitos autorais, dados privilegiados)\n" +
                    "• Humano\n" +
                    "• Financeiro\n" +
                    "\n";
        }
        if (id == R.id.btnTabelas) {
            return "\n" +
                    "1. Tabela: Dados da Equipe e Currículo\n" +
                    "• O que é: O registro de quem são os autores e qual o perfil profissional de cada um.\n\n" +
                    "Como explicar:\n" +
                    "• Unidade: Nome da escola ou centro de tecnologias.\n" +
                    "• Área de atuação: O nome do curso técnico (ex: Eletromecânica, TI).\n" +
                    "• Função no projeto: O que o aluno faz na prática (ex: Programador, Gerente de Compras, Técnico de Montagem).\n" +
                    "• Currículo: Os campos \"Por que escolheu fazer o projeto?\", \"Aprendo mais?\" e \"Prefiro trabalhar?\" servem para o perfil socioemocional. O aluno deve ser sincero sobre suas afinidades (ex: \"Aprendo mais na prática\", \"Prefiro trabalhar em grupo\").\n" +
                    "• Melhoria: Peça para que as \"Habilidades\" sejam descritas com termos técnicos do curso (ex: em vez de \"mexer em computadores\", usar \"manutenção de hardware e redes\").\n\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• O que coloco em 'Empresa que é vinculado'?\" Se o aluno não trabalha, deve colocar \"Estudante [Nome da Instituição]\".\n" +
                    "\"• Quem é o responsável no currículo?\" Geralmente é o pai, mãe ou tutor legal (necessário para menores de idade).\n" +
                    "\n\n" +
                    "2. Tabela: Conhecimentos\n" +
                    "• O que é: A fundamentação teórica e técnica que sustenta o projeto.\n" +
                    "Como explicar:\n\n" +
                    "• Plano de curso: Consultar o documento oficial do curso e identificar quais Unidades Curriculares (matérias) se aplicam ao projeto.\n" +
                    "• Conhecimentos aplicados: A teoria (ex: Leis de Ohm, Lógica de Programação).\n" +
                    "• Capacidades aplicadas: A prática (ex: Montar circuitos, codificar em Java).\n" +
                    "• Melhoria: O aluno não deve apenas listar matérias, mas explicar como aquele conhecimento ajuda no projeto.\n\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• Preciso colocar todas as matérias do curso?\" Não, apenas as que têm relação direta com o problema que o projeto resolve.\n" +
                    "\n\n" +
                    "3. Tabela: Recursos Aplicados (Custos e Logística)\n" +
                    "• O que é: O inventário detalhado de tudo o que é necessário para o projeto existir.\n\n" +
                    "Como explicar:\n" +
                    "• Descrição do produto: Nome técnico e especificações (ex: \"Cabo flexível 2,5mm²\").\n" +
                    "• Quantidade comprada vs. utilizada: Importante para medir desperdício.\n" +
                    "• Modo de obtenção e Disponibilidade: É pronta entrega? Precisa encomendar? Foi doação?\n" +
                    "• Alternativas consideradas: Mostrar que pesquisou outros materiais ou marcas antes de decidir.\n" +
                    "• Melhoria: Reforce a importância do \"Preço Total\" para entender a viabilidade econômica do projeto.\n\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• O que coloco se o recurso for um software gratuito?\" No preço, coloque R$ 0,00, mas descreva o fornecedor e o modo de obtenção (download).\n" +
                    "\n\n" +
                    "4. Tabela: Cronograma\n" +
                    "• O que é: A linha do tempo do projeto.\n\n" +
                    "Como explicar:\n" +
                    "• Processo e Etapas: Dividir o trabalho em grandes blocos (Pesquisa, Aquisição de Materiais, Montagem, Testes, Finalização).\n" +
                    "• Responsável: Indicar qual integrante da equipe lidera aquela etapa.\n" +
                    "• Melhoria: O campo \"Observações\" deve ser usado para registrar imprevistos (ex: \"Atraso na entrega do fornecedor\").\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• As datas podem mudar?\" Sim, o cronograma é vivo, mas alterações devem ser justificadas.\n" +
                    "\n\n" +
                    "5. Tabela: Modelo de Negócio (Canva)\n" +
                    "• O que é: A estrutura estratégica do projeto como um negócio.\n\n" +
                    "Como explicar:\n" +
                    "• Proposta Chave (Valor): Qual o benefício real que você entrega? (ex: economia de energia, agilidade no processo).\n" +
                    "• Segmentos de Clientes: Quem vai usar/comprar?\n" +
                    "• Canais: Como o produto chega ao cliente?\n" +
                    "• Fluxo de Receita: Como o projeto se paga? (Venda direta, mensalidade, economia gerada).\n" +
                    "• Melhoria: Foque na \"Estrutura de Custos\" cruzando os dados com a tabela de Recursos Aplicados.\n\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• O que são Atividades Chave?\" São as ações indispensáveis para o negócio funcionar (ex: produção, manutenção, suporte).\n" +
                    "\n\n" +
                    "6. Tabela: Empresa\n" +
                    "• O que é: A identidade jurídica ou simulada que ampara o projeto.\n\n" +
                    "Como explicar:\n" +
                    "• Missão: A razão de existir. Visão: Onde quer estar em 5 anos. Objetivos: Metas claras.\n" +
                    "• Problema do projeto: O \"dor\" ou necessidade que motivou a criação do projeto.\n" +
                    "• Melhoria: A missão deve ser inspiradora, mas realista.\n\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• E se a empresa for fictícia?\" Invente um nome e CNPJ (formato padrão) condizentes com a região.\n" +
                    "\n\n" +
                    "7. Pitch (Roteiro)\n" +
                    "• O que é: A apresentação de impacto para \"vender\" a ideia em poucos minutos.\n\n" +
                    "Como explicar:\n" +
                    "• Roteiro: Deve conter: Introdução (Problema) -> A Solução (O Projeto) -> Diferenciais (Por que nós?) -> Fechamento.\n" +
                    "• Melhoria: O roteiro deve ser escrito para ser falado, com frases curtas e impacto.\n\n" +
                    "Dúvidas frequentes:\n" +
                    "\"• Preciso ler o roteiro?\" Não, o roteiro serve para treinar a fala e garantir que nenhum ponto importante (como o custo ou a solução do problema) seja esquecido.\n";
        }
        if (id == R.id.btnAppInfo) {
            return "DSPI App - Versão 1.0.0\n\n" +
                    "Este aplicativo foi desenvolvido para facilitar o acompanhamento dos projetos DSPI, " +
                    "integrando alunos e mentores de forma eficiente." +
                    "1- Nessa página é possível fazer alterações nas informações do projeto. Mantivemos o design com base em planilhas para gerar familiaridade e simplificar o preenchimento para o usuário. Além disso, é possível fazer o upload de arquivos tais quais o vídeo do pitch, a apresentação de slides e o plano de curso, permitindo também a análise dos documentos pela NAI. Acompanhe mais sobre as tabelas na aba “Tabela.”\n" +
                    "2- Nesta página, encontra-se todos os projetos que já foram cadastrados no aplicativo, separando-os entre os projetos do ano atual e os anteriores, facilitando o acesso dos usuários. \n" +
                    "As informações dos projetos estão completas, mostrando tudo aquilo que foi pedido para os docentes preencherem durante a execução do mesmo.\n" +
                    "\n" +
                    "3- Nessa página é possível acessar as informações do projeto de forma restrita, para usar de inspiração (ou apenas para conhecimento) e manter a privacidade dos integrantes,. Também estão misturados projetos do ano atual, com projetos mais antigos, entretanto há uma ordem de postagem do mais recente para o mais antigo.\n" +
                    "4- Nessa página vai ser possível para os alunos verem as informações completas de seus projetos, no entanto, sem possibilidade de alteração.\n" +
                    "5- Nessa páginas, as empresa tem o acesso aos canvas e pitch’s dos projetos que abrangem seus pedidos, sendo possível fazer comentários sobre os projetos.\n";
        }
        if (id == R.id.btnRegulamento) {
            return "1. Introdução\n\n" +
                    " \tO Desafio SENAI de Projetos Integradores (DSPI) é uma iniciativa do Departamento Nacional (DN) do SENAI, que tem como público-alvo todos os Centros de Formação Profissional (CFPs) do SENAI Brasil, visando fortalecer o aprendizado e criar uma rede entre as escolas, permitindo com isso a intensificação da aplicação da Metodologia SENAI de Educação Profissional (MSEP). Ao Departamento Regional (DR) do SENAI-RS compete o gerenciamento e o acompanhamento do Desafio no estado, além da relação deste com o Inova Estadual. \n" +
                    "\n" +
                    "2. Comissão de Avaliação \n\n" +
                    "Constituída por especialistas técnicos, instrutores do SENAI, pesquisadores e empresários, especialmente convidados pela Coordenação Geral do DR. \n" +
                    "\n" +
                    "3. Participantes \n\n" +
                    "Alunos dos cursos cujas categorias sejam elegíveis para inscrição desde que estejam devidamente matriculados no ano corrente ao desafio, sendo orientados pelo CFP. \n" +
                    "\n" +
                    "• Fornecer todas as informações solicitadas pela Coordenação nos prazos predeterminados. • Responsabilizar-se pelas informações fornecidas durante todo o processo.\n" +
                    " • Desenvolver o projeto de trabalho nos termos deste Regulamento.\n" +
                    " • Garantir a presença de pelo menos um aluno durante todas as atividades relacionadas ao projeto, inclusive na premiação. \n" +
                    "\n" +
                    "4. Premissa \n\n" +
                    "Os projetos precisam ser submetidos por equipes constituídas por, no mínimo, 02 (dois) e, no máximo, 05 (cinco) integrantes, os quais devem participar integralmente de todas as etapas do DSPI, abrangendo desde a concepção até a implementação.\n" +
                    "\n" +
                    " • É mandatório o envolvimento de, pelo menos, dois cursos de áreas distintas.\n" +
                    " • Cada equipe poderá dispor do suporte técnico de 01 (um) instrutor para cada curso participante, sendo um designado como orientador principal e o outro como coorientador. \n" +
                    "• Cada instrutor fica limitado à orientação de, no máximo, dois projetos simultaneamente. \n" +
                    "\n" +
                    "Categorias elegíveis para inscrição: \n" +
                    "• Qualificação Profissional, incluindo a Aprendizagem Industrial Básica.\n" +
                    "• Técnico de Nível Médio, abrangendo a Aprendizagem Técnica.\n" +
                    "• Graduação Tecnológica, Bacharelado ou Pós-Graduação.\n" +
                    "\n" +
                    "EM CASO DE EQUIPES COMPOSTAS POR CATEGORIAS MISTAS, AS MESMAS ESTARÃO IMPEDIDAS DE DISPUTAR O INTEGRA SENAI NACIONAL, VISTO QUE O REGULAMENTO DO DEPARTAMENTO NACIONAL VETA A PARTICIPAÇÃO DE ALUNOS DE DIFERENTES CATEGORIAS EM UM MESMO GRUPO.\n" +
                    "\n" +
                    "5 .Premiação\n\n" +
                    " Mostra Inova Estadual – 1º, 2º e 3º lugar \n\n" +
                    "\n" +
                    "• Avaliação de forma presencial durante a Mostra. \n" +
                    "• As classificações serão definidas pela maior pontuação elencada pela banca avaliadora.\n" +
                    " • O resultado será anunciado na Mostra, bem como a realização da premiação \n" +
                    "\n" +
                    "\n" +
                    "6. Fomento e Compras Para os Projetos\n\n" +
                    " A sugestão é que possamos utilizar os recursos do SENAI Lab e/ou recursos de desenho em softwares para solução. O DR disponibilizará para cada projeto aprovado o valor de R$ 2.000,00 (dois mil reais). No âmbito deste regulamento, é indispensável enfatizar que o CFP deve estar plenamente consciente e cumprir os prazos de compras definidos pela política de Compras GESUP. Além disso, é imprescindível que o planejamento dos insumos seja realizado com antecedência. O CFP é responsável pelas compras de insumos e/ou serviços, bem como pelo pagamento dessas. As Notas Fiscais referentes a estas aquisições deverão ser encaminhadas por e-mail ao Departamento Regional. \n" +
                    "\n" +
                    "7. Nível de Entrega do Protótipo \n\n" +
                    "O protótipo deverá apresentar o desenvolvimento de um Mínimo Produto Viável (MVP) que permita demonstrar a funcionalidade/solução proposta para o desafio apresentado pela indústria. Entende-se por MVP básico a montagem do protótipo utilizando recursos como: estrutura em MDF, robótica Lego, placa com Arduíno, softwares, simuladores, impressoras 3D ou insumos disponíveis na UO. Fica vetado o recebimento de doações de materiais externos. Um exemplo disso é a doação da indústria demandante do desafio. Com o objetivo de garantir a qualidade da apresentação do protótipo, NÃO É PERMITIDA a utilização dos seguintes materiais: poliestireno expandido (Isopor), papelão, cartolina e EVA. \n" +
                    "\n" +
                    "Protótipo nível 1: baixa fidelidade\n" +
                    "Protótipo nível 2: média fidelidade.\n" +
                    "Protótipo 3: alta fidelidade.\n" +
                    "\n" +
                    "Dimensões do Protótipo O protótipo não deverá ultrapassar as seguintes dimensões:\n" +
                    "• Opção A: C 50cm x L 50cm x H 60cm \n" +
                    "• Opção B: C 50cm x L 60cm x H 50cm\n" +
                    "• Opção C: C 60cm x L 50cm x H 60cm\n" +
                    " • Peso máximo de 30kg \n" +
                    "\n" +
                    "8. SENAI Lab\n\n" +
                    " \tO SENAI Lab é um pilar para a SAGA SENAI de Inovação em que se desenvolve a cultura Maker. Dessa forma, o SENAI-RS incentiva a utilização deste espaço como um ambiente propício para a idealização e fabricação de protótipos para o Grand Prix, DSPI e Inova, independentemente do seu nível de maturidade. Além disso, o SENAI Lab é um espaço de conexão com o mundo industrial, podendo provocar a indústria e os demais interessados a trazerem seus próprios problemas para serem resolvidos por meio de abordagens inovadoras. \n" +
                    "\n" +
                    "9. Usuários Desistentes \n\n" +
                    "Diretrizes caso haja desistências de integrantes das equipes de projetos:\n" +
                    "• A equipe se manterá ativa caso permaneça com no mínimo 02 integrantes. \n" +
                    "• Poderá haver uma substituição de integrantes da equipe (alunos e orientadores) no prazo máximo de 60 dias do início do projeto, desde que devidamente comunicada e formalizado para a Coordenação Geral. \n" +
                    "\n" +
                    "\n" +
                    "10. Critérios de Avaliação\n\n" +
                    " \tAs avaliações dos projetos serão compostas pelos critérios abaixo descritos:\n" +
                    " a) Justificativa e Objetivos:\n" +
                    " • A solução proposta está adequada à demanda da indústria?\n" +
                    " • O projeto apresentou de modo claro o objetivo da proposta?\n" +
                    " • Há contribuição do projeto para a indústria e para a sociedade? \n" +
                    "\n" +
                    "b) Metodologia e Desenvolvimento: \n" +
                    "• O passo a passo da construção do projeto está claro e sucinto?\n" +
                    " • Estão descritas as características técnicas da solução proposta? \n" +
                    "\n" +
                    "c) Viabilidade: \n" +
                    "• A equipe apresentou os diferenciais de sua solução perante os concorrentes?\n" +
                    " • Há viabilidade técnica e financeira do projeto? \n" +
                    "• Existe tecnologia e recursos disponíveis para fabricar a solução em escala industrial? \n" +
                    "• Os números apresentados nos estudos são conclusivos? \n" +
                    "• A solução é factível de ser implementada onde se propõe? \n" +
                    "\n" +
                    "d) Qualidade do Pitch\n" +
                    " • Os alunos são protagonistas na apresentação?\n" +
                    " • A informação foi apresentada de forma clara? \n" +
                    "• A equipe apresentou os diferenciais da solução perante os concorrentes?\n" +
                    " • A equipe apresentou a viabilidade e o que necessita para implementar a solução?\n" +
                    " • O protótipo e seu funcionamento foram apresentados de forma clara? \n" +
                    "\n" +
                    "Observações: \n" +
                    "• A apresentação do pitch deverá ser realizada pelo(s) aluno(s) e não pelo orientador. \n" +
                    "\n" +
                    "e) Qualidade do Canvas: \n" +
                    "• Houve o preenchimento do canvas de forma clara? \n" +
                    "• O canvas permite a implementação da solução ou a construção de um plano de negócios coerente? \n" +
                    "• Há coerência entre canvas com a solução apresentada?\n" +
                    "\n" +
                    " f) Qualidade do Protótipo:\n" +
                    " • O protótipo demonstra funcionalidade? \n" +
                    "• O protótipo facilita a compreensão clara da solução proposta? \n" +
                    "\n" +
                    "11. Propriedade Intelectual \n" +
                    "A proteção dos trabalhos apresentados, no todo ou em parte, se dará conforme a legislação brasileira de propriedade intelectual, notadamente a Lei n.º 9.279, de 14 de maio de 1996, e com a política de propriedade intelectual do SENAI-RS. Em caso de Proteção da Propriedade Industrial, os pedidos serão encaminhados pelo SENAI-RS ao Instituto Nacional da Propriedade Industrial — INPI e terão como titular o Serviço Nacional de Aprendizagem Industrial — Departamento Regional do Rio Grande do Sul — SENAI-RS e como inventores/autores os participantes nominados no Termo de Declaração de Invenção. A titularidade poderá ser negociada e/ou compartilhada com a indústria demandante do desafio caso seja de interesse das partes. Licenciamentos de tecnologias a terceiros, alunos ou indústrias serão discutidos e formalizados individualmente. \n" +
                    "\n" +
                    "12. Disposições Gerais e Transitórias\n" +
                    "A leitura deste Regulamento é obrigatória a TODOS os envolvidos no processo do DSPI.\n" +
                    "A Coordenação Geral poderá rejeitar a inscrição e/ou desclassificar os trabalhos que não preencham os requisitos deste Regulamento.\n" +
                    "No intuito de assegurar o bom desempenho do DSPI, a Coordenação Geral se reserva à prerrogativa de introduzir alterações em partes deste regulamento no decorrer do processo, desde que tais alterações sejam comunicadas para todos os envolvidos por meio de nota de esclarecimentos.\n" +
                    "A Coordenação Geral do DSPI poderá alterar e atualizar esse Regulamento a qualquer momento, sem aviso, sendo sempre responsabilidade dos participantes atentar a quaisquer modificações divulgadas.\n" +
                    "Os participantes são responsáveis por todas as informações fornecidas em todas as etapas do processo e pela veracidade das mesmas durante as apresentações e o preenchimento dos documentos solicitados pela Coordenação Geral.\n" +
                    "Os projetos poderão ser convidados e recomendados para eventos congêneres promovidos pelo DN ou por outras entidades. Para tanto, os trabalhos serão analisados pelo DR, que verificará se estes estão enquadrados nas áreas tecnológicas e conforme os regulamentos dos referidos concursos/eventos.\n" +
                    "Qualquer dúvida dos participantes deverá ser encaminhada à Coordenação Geral do DSPI para o endereço eletrônico divulgado junto à divulgação dos desafios.\n" +
                    "Os casos não previstos neste Regulamento serão analisados e julgados pelo DR. O presente Regulamento entrará em vigor a partir desta data. Os casos omissos serão decididos pela Coordenação Geral do DR\n" +
                    "\n";
        }
        return "";
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
}