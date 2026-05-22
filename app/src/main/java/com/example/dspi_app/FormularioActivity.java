package com.example.dspi_app;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class FormularioActivity extends AppCompatActivity {

    // Índice da aba para manter a navegação apontando para "Projetos"
    private final int CURRENT_TAB_INDEX = 1;

    // Declaração dos Layouts de Formulário (Subtópicos)
    private LinearLayout formEquipe, formConhecimentos, formRecursos, formCronograma,
            formCanva, formCurriculo, formEmpresa, formPitch,
            formIA, formPlanilha, formComplementares, formCompletude;

    // Declaração dos botões de Aba (Tópicos)
    private TextView tabEquipe, tabConhecimentos, tabRecursos, tabCronograma,
            tabCanva, tabCurriculo, tabEmpresa, tabPitch,
            tabIA, tabPlanilha, tabComplementares, tabCompletude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. CONFIGURAÇÃO PARA NÃO INVADIR AS BARRAS DE STATUS/NAVEGAÇÃO ---
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_formulario);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // --- 2. CONFIGURAÇÃO DO MENU INFERIOR (FINGINDO ESTAR EM PROJETOS) ---
        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();

        // Inicializar Forms
        formEquipe = findViewById(R.id.formEquipe);
        formConhecimentos = findViewById(R.id.formConhecimentos);
        formRecursos = findViewById(R.id.formRecursos);
        formCronograma = findViewById(R.id.formCronograma);
        formCanva = findViewById(R.id.formCanva);
        formCurriculo = findViewById(R.id.formCurriculo);
        formEmpresa = findViewById(R.id.formEmpresa);
        formPitch = findViewById(R.id.formPitch);
        formIA = findViewById(R.id.formIA);
        formPlanilha = findViewById(R.id.formPlanilha);
        formComplementares = findViewById(R.id.formComplementares);
        formCompletude = findViewById(R.id.formCompletude);

        // Inicializar Tabs
        tabEquipe = findViewById(R.id.tabEquipe);
        tabConhecimentos = findViewById(R.id.tabConhecimentos);
        tabRecursos = findViewById(R.id.tabRecursos);
        tabCronograma = findViewById(R.id.tabCronograma);
        tabCanva = findViewById(R.id.tabCanva);
        tabCurriculo = findViewById(R.id.tabCurriculo);
        tabEmpresa = findViewById(R.id.tabEmpresa);
        tabPitch = findViewById(R.id.tabPitch);
        tabIA = findViewById(R.id.tabIA);
        tabPlanilha = findViewById(R.id.tabPlanilha);
        tabComplementares = findViewById(R.id.tabComplementares);
        tabCompletude = findViewById(R.id.tabCompletude);

        // Configurar Eventos de Clique
        configurarCliques();

        // Destacar a primeira aba por padrão ao abrir a tela
        destacarAba(tabEquipe);
    }

    private void configurarCliques() {
        tabEquipe.setOnClickListener(v -> alternarFormulario(formEquipe, tabEquipe));
        tabConhecimentos.setOnClickListener(v -> alternarFormulario(formConhecimentos, tabConhecimentos));
        tabRecursos.setOnClickListener(v -> alternarFormulario(formRecursos, tabRecursos));
        tabCronograma.setOnClickListener(v -> alternarFormulario(formCronograma, tabCronograma));
        tabCanva.setOnClickListener(v -> alternarFormulario(formCanva, tabCanva));
        tabCurriculo.setOnClickListener(v -> alternarFormulario(formCurriculo, tabCurriculo));
        tabEmpresa.setOnClickListener(v -> alternarFormulario(formEmpresa, tabEmpresa));
        tabPitch.setOnClickListener(v -> alternarFormulario(formPitch, tabPitch));
        tabIA.setOnClickListener(v -> alternarFormulario(formIA, tabIA));
        tabPlanilha.setOnClickListener(v -> alternarFormulario(formPlanilha, tabPlanilha));
        tabComplementares.setOnClickListener(v -> alternarFormulario(formComplementares, tabComplementares));
        tabCompletude.setOnClickListener(v -> alternarFormulario(formCompletude, tabCompletude));
    }

    private void alternarFormulario(LinearLayout formAtivo, TextView tabAtiva) {
        // Esconder todos os formulários
        formEquipe.setVisibility(View.GONE);
        formConhecimentos.setVisibility(View.GONE);
        formRecursos.setVisibility(View.GONE);
        formCronograma.setVisibility(View.GONE);
        formCanva.setVisibility(View.GONE);
        formCurriculo.setVisibility(View.GONE);
        formEmpresa.setVisibility(View.GONE);
        formPitch.setVisibility(View.GONE);
        formIA.setVisibility(View.GONE);
        formPlanilha.setVisibility(View.GONE);
        formComplementares.setVisibility(View.GONE);
        formCompletude.setVisibility(View.GONE);

        // Mostrar apenas o formulário clicado
        formAtivo.setVisibility(View.VISIBLE);

        // Destacar a aba clicada
        destacarAba(tabAtiva);
    }

    private void destacarAba(TextView tabAtiva) {
        TextView[] todasAbas = {tabEquipe, tabConhecimentos, tabRecursos, tabCronograma, tabCanva, tabCurriculo, tabEmpresa, tabPitch, tabIA, tabPlanilha, tabComplementares, tabCompletude};

        for (TextView tab : todasAbas) {
            tab.setAlpha(0.5f); // Deixa meio transparente as inativas
        }

        tabAtiva.setAlpha(1.0f); // Deixa 100% opaca a aba que está ativa
    }

    // --- MÉTODOS PARA MANTER A ILUSÃO VISUAL DE UMA ÚNICA TELA ---

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

    @Override
    public void finish() {
        super.finish();
        // Remove a animação de deslize se o usuário apertar o botão "Voltar" do celular
        overridePendingTransition(0, 0);
    }
}