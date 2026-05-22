package com.example.dspi_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FormularioActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_formulario); // Conecta com o XML criado

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
        // Volta todas as abas para uma cor um pouco mais apagada (opcional para feedback visual)
        TextView[] todasAbas = {tabEquipe, tabConhecimentos, tabRecursos, tabCronograma, tabCanva, tabCurriculo, tabEmpresa, tabPitch, tabIA, tabPlanilha, tabComplementares, tabCompletude};

        for (TextView tab : todasAbas) {
            tab.setAlpha(0.5f); // Deixa meio transparente as inativas
        }

        // Deixa 100% opaca a aba que está ativa
        tabAtiva.setAlpha(1.0f);
    }
}