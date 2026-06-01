package com.example.dspi_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

public class FormularioActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 1;

    private LinearLayout formEquipe, formConhecimentos, formRecursos, formCronograma,
            formCronogramaEspecifico, formCanva, formCurriculo, formEmpresa, formPitch,
            formIA, formPlanilha, formComplementares, formCompletude;

    private TextView tabEquipe, tabConhecimentos, tabRecursos, tabCronograma,
            tabCronogramaEspecifico, tabCanva, tabCurriculo, tabEmpresa, tabPitch,
            tabIA, tabPlanilha, tabComplementares, tabCompletude;

    private EditText etNomeEquipe, etNomeProjeto, etEmail, etAreaCurso, etAreaProjeto,
            etNomeOrientador, etNomeCoorientador, etIntegrante1, etIntegrante2,
            etIntegrante3, etIntegrante4, etIntegrante5;

    private Button btnEditarDados;
    private boolean modoEdicao = false;

    private String emailUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_formulario);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();

        emailUsuario = getSharedPreferences("SESSAO_USER", MODE_PRIVATE)
                .getString("email_logado", "");

        etNomeEquipe = findViewById(R.id.etNomeEquipe);
        etNomeProjeto = findViewById(R.id.etNomeProjeto);
        etEmail = findViewById(R.id.etEmail);
        etAreaCurso = findViewById(R.id.etAreaCurso);
        etAreaProjeto = findViewById(R.id.etAreaProjeto);
        etNomeOrientador = findViewById(R.id.etNomeOrientador);
        etNomeCoorientador = findViewById(R.id.etNomeCoorientador);
        etIntegrante1 = findViewById(R.id.etIntegrante1);
        etIntegrante2 = findViewById(R.id.etIntegrante2);
        etIntegrante3 = findViewById(R.id.etIntegrante3);
        etIntegrante4 = findViewById(R.id.etIntegrante4);
        etIntegrante5 = findViewById(R.id.etIntegrante5);

        formEquipe = findViewById(R.id.formEquipe);
        formConhecimentos = findViewById(R.id.formConhecimentos);
        formRecursos = findViewById(R.id.formRecursos);
        formCronograma = findViewById(R.id.formCronograma);
        formCronogramaEspecifico = findViewById(R.id.formCronogramaEspecifico);
        formCanva = findViewById(R.id.formCanva);
        formCurriculo = findViewById(R.id.formCurriculo);
        formEmpresa = findViewById(R.id.formEmpresa);
        formPitch = findViewById(R.id.formPitch);
        formIA = findViewById(R.id.formIA);
        formPlanilha = findViewById(R.id.formPlanilha);
        formComplementares = findViewById(R.id.formComplementares);
        formCompletude = findViewById(R.id.formCompletude);

        tabEquipe = findViewById(R.id.tabEquipe);
        tabConhecimentos = findViewById(R.id.tabConhecimentos);
        tabRecursos = findViewById(R.id.tabRecursos);
        tabCronograma = findViewById(R.id.tabCronograma);
        tabCronogramaEspecifico = findViewById(R.id.tabCronogramaEspecifico);
        tabCanva = findViewById(R.id.tabCanva);
        tabCurriculo = findViewById(R.id.tabCurriculo);
        tabEmpresa = findViewById(R.id.tabEmpresa);
        tabPitch = findViewById(R.id.tabPitch);
        tabIA = findViewById(R.id.tabIA);
        tabPlanilha = findViewById(R.id.tabPlanilha);
        tabComplementares = findViewById(R.id.tabComplementares);
        tabCompletude = findViewById(R.id.tabCompletude);

        btnEditarDados = findViewById(R.id.btnEditarDados);

        atualizarTodosFormularios(false);

        btnEditarDados.setOnClickListener(v -> {
            modoEdicao = !modoEdicao;
            atualizarTodosFormularios(modoEdicao);

            if (modoEdicao) {
                btnEditarDados.setText("Salvar Alterações");
            } else {
                btnEditarDados.setText("Editar Dados");
                salvarEquipeNoCloudflare();
            }
        });

        configurarCliques();

        destacarAba(tabEquipe);

        carregarDadosExistentesDoBanco();
    }

    private void configurarCliques() {
        tabEquipe.setOnClickListener(v -> alternarFormulario(formEquipe, tabEquipe));
        tabConhecimentos.setOnClickListener(v -> alternarFormulario(formConhecimentos, tabConhecimentos));
        tabRecursos.setOnClickListener(v -> alternarFormulario(formRecursos, tabRecursos));
        tabCronograma.setOnClickListener(v -> alternarFormulario(formCronograma, tabCronograma));
        tabCronogramaEspecifico.setOnClickListener(v -> alternarFormulario(formCronogramaEspecifico, tabCronogramaEspecifico));
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
        formEquipe.setVisibility(View.GONE);
        formConhecimentos.setVisibility(View.GONE);
        formRecursos.setVisibility(View.GONE);
        formCronograma.setVisibility(View.GONE);
        formCronogramaEspecifico.setVisibility(View.GONE);
        formCanva.setVisibility(View.GONE);
        formCurriculo.setVisibility(View.GONE);
        formEmpresa.setVisibility(View.GONE);
        formPitch.setVisibility(View.GONE);
        formIA.setVisibility(View.GONE);
        formPlanilha.setVisibility(View.GONE);
        formComplementares.setVisibility(View.GONE);
        formCompletude.setVisibility(View.GONE);

        formAtivo.setVisibility(View.VISIBLE);
        destacarAba(tabAtiva);
    }

    private void carregarDadosExistentesDoBanco() {
        FormularioRepository repository = new FormularioRepository(this);

        repository.carregarEquipe(new FormularioRepository.OnDadosCarregadosListener() {
            @Override
            public void onSucesso(JSONObject dados) {
                etNomeEquipe.setText(dados.optString("nome_equipe", ""));
                etNomeProjeto.setText(dados.optString("nome_projeto", ""));
                etEmail.setText(dados.optString("email", ""));
                etAreaCurso.setText(dados.optString("area_atuacao_curso", ""));
                etAreaProjeto.setText(dados.optString("area_atuacao_projeto", ""));
                etNomeOrientador.setText(dados.optString("nome_orientador", ""));
                etNomeCoorientador.setText(dados.optString("nome_coorientador", ""));
                etIntegrante1.setText(dados.optString("nome_integrante", ""));
                etIntegrante2.setText(dados.optString("nome_integrante2", ""));
                etIntegrante3.setText(dados.optString("nome_integrante3", ""));
                etIntegrante4.setText(dados.optString("nome_integrante4", ""));
                etIntegrante5.setText(dados.optString("nome_integrante5", ""));
            }

            @Override
            public void onNaoEncontrado() {
                Toast.makeText(FormularioActivity.this, "Preencha sua equipe pela primeira vez.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onErro(String erro) {
                android.util.Log.e("ERRO_CARREGAR", erro);
            }
        });
    }

    private void salvarEquipeNoCloudflare() {
        FormularioRepository repository = new FormularioRepository(this);

        repository.salvarEquipe(
                etNomeEquipe.getText().toString().trim(),
                etNomeProjeto.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etAreaCurso.getText().toString().trim(),
                etAreaProjeto.getText().toString().trim(),
                etNomeOrientador.getText().toString().trim(),
                etNomeCoorientador.getText().toString().trim(),
                etIntegrante1.getText().toString().trim(),
                etIntegrante2.getText().toString().trim(),
                etIntegrante3.getText().toString().trim(),
                etIntegrante4.getText().toString().trim(),
                etIntegrante5.getText().toString().trim()
        );
    }

    private void definirCamposEditaveis(LinearLayout formulario, boolean habilitado) {
        for (int i = 0; i < formulario.getChildCount(); i++) {
            View view = formulario.getChildAt(i);
            if (view instanceof android.widget.EditText) {
                android.widget.EditText editText = (android.widget.EditText) view;
                editText.setEnabled(habilitado);
                editText.setFocusable(habilitado);
                editText.setFocusableInTouchMode(habilitado);
                editText.setClickable(habilitado);

                editText.setTextColor(android.graphics.Color.WHITE);
                editText.setHintTextColor(android.graphics.Color.parseColor("#80FFFFFF"));

                if (!habilitado) {
                    editText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#66FFFFFF")));
                } else {
                    editText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF")));
                }
            }
        }
    }

    private void atualizarTodosFormularios(boolean habilitado) {
        definirCamposEditaveis(formEquipe, habilitado);
        definirCamposEditaveis(formConhecimentos, habilitado);
        definirCamposEditaveis(formRecursos, habilitado);
        definirCamposEditaveis(formCronograma, habilitado);
        definirCamposEditaveis(formCronogramaEspecifico, habilitado);
        definirCamposEditaveis(formCanva, habilitado);
        definirCamposEditaveis(formCurriculo, habilitado);
        definirCamposEditaveis(formEmpresa, habilitado);
        definirCamposEditaveis(formPitch, habilitado);
        definirCamposEditaveis(formIA, habilitado);
        definirCamposEditaveis(formPlanilha, habilitado);
        definirCamposEditaveis(formComplementares, habilitado);
        definirCamposEditaveis(formCompletude, habilitado);
    }

    private void destacarAba(TextView tabAtiva) {
        TextView[] todasAbas = {tabEquipe, tabConhecimentos, tabRecursos, tabCronograma, tabCronogramaEspecifico, tabCanva, tabCurriculo, tabEmpresa, tabPitch, tabIA, tabPlanilha, tabComplementares, tabCompletude};
        for (TextView tab : todasAbas) {
            tab.setAlpha(0.5f);
        }
        tabAtiva.setAlpha(1.0f);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}