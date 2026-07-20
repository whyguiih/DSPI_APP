package com.example.dspi_app;

import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.os.Handler;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.json.JSONException;
import org.json.JSONObject;

public class FormularioActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 1;

    private LinearLayout formEquipe, formConhecimentos, formRecursos, formCronograma,
            formCronogramaEspecifico, formCanva, formCurriculo, formEmpresa, formPitch,
            formIA, formPlanilha, formComplementares, formCompletude;

    private TextView tabEquipe, tabConhecimentos, tabRecursos, tabCronograma,
            tabCronogramaEspecifico, tabCanva, tabCurriculo, tabEmpresa, tabPitch,
            tabIA, tabPlanilha, tabComplementares, tabCompletude;

    // Inputs da Equipe
    private EditText etNomeEquipe, etNomeProjeto, etEmail, etAreaCurso, etAreaProjeto,
            etNomeOrientador, etNomeCoorientador, etIntegrante1, etIntegrante2,
            etIntegrante3, etIntegrante4, etIntegrante5;

    // Inputs de Conhecimentos (Adicionados!)
    private EditText etPlanoCurso, etConhecimentosAplicados, etCapacidadesAplicadas;

    // inputs recursos
    private EditText etRecursosFerramentas, etRecursosEquipamentos, etRecursosDescricao,
            etRecursosQtdComprada, etRecursosQtdUtilizada, etRecursosPrecoEstimado,
            etRecursosUnidadeMedida, etRecursosFornecedor, etRecursosModoObtencao,
            etRecursosDisponibilidade, etRecursosPagamento, etRecursosAlternativas,
            etRecursosPrecoTotal;

    // inputs cronograma
    private EditText etCronogramaProcesso, etCronogramaEtapas, etCronogramaResponsavel,
            etCronogramaDataInicio, etCronogramaDataFinal, etCronogramaObservacoes;

    // inputs cronograma especifico
    private EditText etCronoEspProcessos, etCronoEspEtapas, etCronoEspResponsavel,
            etCronoEspDataInicio, etCronoEspDataFinal, etCronoEspObservacoes;

    // inputs canva
    private EditText etCanvaAtividadesChaves, etCanvaPropostaChave, etCanvaRelacionamentos,
            etCanvaSegmentos, etCanvaRecursosChaves, etCanvaCanais,
            etCanvaEstruturaCustos, etCanvaFluxoReceita, etCanvaParceirosChaves;

    // Inputs Currículo
    private EditText etCurrNome, etCurrDataNacimento, etCurrCpf, etCurrEmpresaVinculado,
            etCurrProjeto, etCurrTelefone, etCurrEmail, etCurrNomeReponsavel,
            etCurrNumeroResponsavel, etCurrEmailRepsonsavel, etCurrHabilidades,
            etCurrFezProjeto, etCurrCidade, etCurrMotivoProjeto, etCurrAprendoMais,
            etCurrPrefiroTrabalhar;

    // Inputs da Empresa
    private EditText etEmpresaNome, etEmpresaCnpj, etEmpresaRegiao, etEmpresaTelefone,
            etEmpresaEmail, etEmpresaObjetivos, etEmpresaProblema;

    //Pitch
    private EditText etPitchRoteiro;
    private Button btnUploadVideo;
    private ProgressBar pbVideoUpload;
    private TextView tvVideoStatus;
    private VideoView vvPitch;
    private View videoContainer;
    private ImageButton btnPlayPause, btnRewind, btnForward;
    private SeekBar videoSeekBar;
    private Handler videoHandler = new Handler();
    private Runnable updateSeekBar;
    private ActivityResultLauncher<String> videoPickerLauncher;

    //Uso de ia
    private EditText etIaNomeFerramenta,etIaLinkAcesso,etIaTipoLicenca,etIaEtapaUso,etIaCriacaoPrompt,etIaDescricaoUso;

    //Planilha
    private EditText etPlanilhaTarefas, etPlanilhaAlunoResponsavel, etPlanilhaProfessorArea,
            etPlanilhaInicioPrevisto, etPlanilhaFimPrevisto, etPlanilhaInicioRealizado,
            etPlanilhaFimRealizado, etPlanilhaDuracaoDias, etPlanilhaStatus,
            etPlanilhaDescricao, etPlanilhaDificuldades, etPlanilhaImpacto;

    //complementares
    private EditText etComplUnidade, etComplCoordenador, etComplGestor, etComplEmpresa, etComplProjeto, etComplDescricaoProjeto, etComplQtdProjetos, etComplI1Nome, etComplI1Email,
            etComplI1Camiseta, etComplI1Rg, etComplI1Cpf, etComplI1DataNasc, etComplI1Idade, etComplI1Telefone, etComplI2Nome, etComplI2Email, etComplI2Camiseta, etComplI2Rg, etComplI2Cpf,
            etComplI2DataNasc, etComplI2Idade, etComplI2Telefone, etComplI3Nome, etComplI3Email, etComplI3Camiseta, etComplI3Rg, etComplI3Cpf, etComplI3DataNasc, etComplI3Idade, etComplI3Telefone,
            etComplI4Nome, etComplI4Email, etComplI4Camiseta, etComplI4Rg, etComplI4Cpf, etComplI4DataNasc, etComplI4Idade, etComplI4Telefone, etComplI5Nome, etComplI5Email, etComplI5Camiseta,
            etComplI5Rg, etComplI5Cpf, etComplI5DataNasc, etComplI5Idade, etComplI5Telefone, etComplI6Nome, etComplI6Email, etComplI6Camiseta, etComplI6Rg, etComplI6Cpf, etComplI6DataNasc,
            etComplI6Idade, etComplI6Telefone, etComplI7Nome, etComplI7Email, etComplI7Camiseta, etComplI7Rg, etComplI7Cpf, etComplI7DataNasc, etComplI7Idade, etComplI7Telefone;

    //completude
    private EditText etCompleQuantidade, etCompleIdentificacao, etCompleResponsavel, etCompleStatusEquipe, etCompleStatusConhecimento,
            etCompleStatusRecursos, etCompleStatusCanvas, etCompleStatusPitchEscrito, etCompleStatusPitchVideo, etCompleStatusCronograma, etCompleStatusFotoEquipe, etCompleStatusFotosEtapa;

    private Button btnEditarDados;
    private boolean modoEdicao = false;
    private String emailUsuario;
    private String targetEmail; // E-mail do dono do projeto que está sendo visualizado

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

        emailUsuario = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("email_logado", "");
        targetEmail = getIntent().getStringExtra("projeto_usuario");
        if (targetEmail == null || targetEmail.isEmpty()) {
            targetEmail = emailUsuario;
        }

        // Vincular componentes - Equipe
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

        // Vincular componentes - Conhecimentos (Adicionados!)
        etPlanoCurso = findViewById(R.id.inputPlanoCurso);
        etConhecimentosAplicados = findViewById(R.id.inputConhecimentos);
        etCapacidadesAplicadas = findViewById(R.id.inputCapacidades);

        // Vincular Recursos
        etRecursosFerramentas = findViewById(R.id.etRecursosFerramentas);
        etRecursosEquipamentos = findViewById(R.id.etRecursosEquipamentos);
        etRecursosDescricao = findViewById(R.id.etRecursosDescricao);
        etRecursosQtdComprada = findViewById(R.id.etRecursosQtdComprada);
        etRecursosQtdUtilizada = findViewById(R.id.etRecursosQtdUtilizada);
        etRecursosPrecoEstimado = findViewById(R.id.etRecursosPrecoEstimado);
        etRecursosUnidadeMedida = findViewById(R.id.etRecursosUnidadeMedida);
        etRecursosFornecedor = findViewById(R.id.etRecursosFornecedor);
        etRecursosModoObtencao = findViewById(R.id.etRecursosModoObtencao);
        etRecursosDisponibilidade = findViewById(R.id.etRecursosDisponibilidade);
        etRecursosPagamento = findViewById(R.id.etRecursosPagamento);
        etRecursosAlternativas = findViewById(R.id.etRecursosAlternativas);
        etRecursosPrecoTotal = findViewById(R.id.etRecursosPrecoTotal);

        // Vincular Cronograma (insira seus IDs corretos do arquivo XML layout aqui)
        etCronogramaProcesso = findViewById(R.id.etCronoProcesso);
        etCronogramaEtapas = findViewById(R.id.etCronoEtapas);
        etCronogramaResponsavel = findViewById(R.id.etCronoResponsavel);
        etCronogramaDataInicio = findViewById(R.id.etCronoDataInicio);
        etCronogramaDataFinal = findViewById(R.id.etCronoDataFinal);
        etCronogramaObservacoes = findViewById(R.id.etCronoObservacoes);

        // Vincular Cronograma Específico
        etCronoEspProcessos = findViewById(R.id.etCronoEspecProcessos); // Ajuste os IDs conforme o seu XML
        etCronoEspEtapas = findViewById(R.id.etCronoEspecEtapas);
        etCronoEspResponsavel = findViewById(R.id.etCronoEspecResponsavel);
        etCronoEspDataInicio = findViewById(R.id.etCronoEspecDataInicio);
        etCronoEspDataFinal = findViewById(R.id.etCronoEspecDataFinal);
        etCronoEspObservacoes = findViewById(R.id.etCronoEspecObservacoes);

        // Vincular Canva
        etCanvaAtividadesChaves = findViewById(R.id.etCanvaAtividadesChaves); // Ajuste os IDs conforme o seu XML
        etCanvaPropostaChave = findViewById(R.id.etCanvaPropostaChave);
        etCanvaRelacionamentos = findViewById(R.id.etCanvaRelacionamentoClientes);
        etCanvaSegmentos = findViewById(R.id.etCanvaSegmentosClientes);
        etCanvaRecursosChaves = findViewById(R.id.etCanvaRecursosChaves);
        etCanvaCanais = findViewById(R.id.etCanvaCanais);
        etCanvaEstruturaCustos = findViewById(R.id.etCanvaEstruturaCustos);
        etCanvaFluxoReceita = findViewById(R.id.etCanvaFluxoReceita);
        etCanvaParceirosChaves = findViewById(R.id.etCanvaParceirosChaves);

        // Vincular Currículo
        etCurrNome = findViewById(R.id.etCurrNome); // Ajuste os IDs conforme o seu XML
        etCurrDataNacimento = findViewById(R.id.etCurrDataNascimento);
        etCurrCpf = findViewById(R.id.etCurrCpf);
        etCurrEmpresaVinculado = findViewById(R.id.etCurrEmpresaVinculada);
        etCurrProjeto = findViewById(R.id.etCurrProjeto);
        etCurrTelefone = findViewById(R.id.etCurrTelefone);
        etCurrEmail = findViewById(R.id.etCurrEmail);
        etCurrNomeReponsavel = findViewById(R.id.etCurrNomeResponsavel);
        etCurrNumeroResponsavel = findViewById(R.id.etCurrNumeroResponsavel);
        etCurrEmailRepsonsavel = findViewById(R.id.etCurrEmailResponsavel);
        etCurrHabilidades = findViewById(R.id.etCurrHabilidades);
        etCurrFezProjeto = findViewById(R.id.etCurrFezProjeto);
        etCurrCidade = findViewById(R.id.etCurrCidade);
        etCurrMotivoProjeto = findViewById(R.id.etCurrMotivoProjeto);
        etCurrAprendoMais = findViewById(R.id.etCurrAprendoMais);
        etCurrPrefiroTrabalhar = findViewById(R.id.etCurrPrefiroTrabalhar);

        // Vincular Empresa
        etEmpresaNome = findViewById(R.id.etEmpresaNome);
        etEmpresaCnpj = findViewById(R.id.etEmpresaCnpj);
        etEmpresaRegiao = findViewById(R.id.etEmpresaRegiao);
        etEmpresaTelefone = findViewById(R.id.etEmpresaTelefone);
        etEmpresaEmail = findViewById(R.id.etEmpresaEmail);
        etEmpresaObjetivos = findViewById(R.id.etEmpresaObjetivos);
        etEmpresaProblema = findViewById(R.id.etEmpresaProblema);

        //Vincular pitch
        etPitchRoteiro = findViewById(R.id.etPitchRoteiro);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);
        pbVideoUpload = findViewById(R.id.pbVideoUpload);
        tvVideoStatus = findViewById(R.id.tvVideoStatus);
        vvPitch = findViewById(R.id.vvPitch);
        videoContainer = findViewById(R.id.videoContainer);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);
        videoSeekBar = findViewById(R.id.videoSeekBar);

        configurarControlesVideo();

        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        realizarUploadVideo(uri);
                    }
                }
        );

        btnUploadVideo.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));

        //Vincular formulario
        etIaNomeFerramenta = findViewById(R.id.etIaNomeFerramenta);
        etIaLinkAcesso = findViewById(R.id.etIaLinkAcesso);
        etIaTipoLicenca = findViewById(R.id.etIaTipoLicenca);
        etIaEtapaUso = findViewById(R.id.etIaEtapaUso);
        etIaCriacaoPrompt = findViewById(R.id.etIaCriacaoPrompt);
        etIaDescricaoUso = findViewById(R.id.etIaDescricaoUso);

        //Planilhas
        etPlanilhaTarefas = findViewById(R.id.etPlanilhaTarefas);
        etPlanilhaAlunoResponsavel = findViewById(R.id.etPlanilhaAlunoResponsavel);
        etPlanilhaProfessorArea = findViewById(R.id.etPlanilhaProfessorArea);
        etPlanilhaInicioPrevisto = findViewById(R.id.etPlanilhaInicioPrevisto);
        etPlanilhaFimPrevisto = findViewById(R.id.etPlanilhaFimPrevisto);
        etPlanilhaInicioRealizado = findViewById(R.id.etPlanilhaInicioRealizado);
        etPlanilhaFimRealizado = findViewById(R.id.etPlanilhaFimRealizado);
        etPlanilhaDuracaoDias = findViewById(R.id.etPlanilhaDuracaoDias);
        etPlanilhaStatus = findViewById(R.id.etPlanilhaStatus);
        etPlanilhaDescricao = findViewById(R.id.etPlanilhaDescricao);
        etPlanilhaDificuldades = findViewById(R.id.etPlanilhaDificuldades);
        etPlanilhaImpacto = findViewById(R.id.etPlanilhaImpacto);

        //Complementares

        // Inicialização - Dados Gerais
        etComplUnidade = findViewById(R.id.etComplUnidade);
        etComplCoordenador = findViewById(R.id.etComplCoordenador);
        etComplGestor = findViewById(R.id.etComplGestor);
        etComplEmpresa = findViewById(R.id.etComplEmpresa);
        etComplProjeto = findViewById(R.id.etComplProjeto);
        etComplDescricaoProjeto = findViewById(R.id.etComplDescricaoProjeto);
        etComplQtdProjetos = findViewById(R.id.etComplQtdProjetos);

// Inicialização - Integrante 1
        etComplI1Nome = findViewById(R.id.etComplI1Nome);
        etComplI1Email = findViewById(R.id.etComplI1Email);
        etComplI1Camiseta = findViewById(R.id.etComplI1Camiseta);
        etComplI1Rg = findViewById(R.id.etComplI1Rg);
        etComplI1Cpf = findViewById(R.id.etComplI1Cpf);
        etComplI1DataNasc = findViewById(R.id.etComplI1DataNasc);
        etComplI1Idade = findViewById(R.id.etComplI1Idade);
        etComplI1Telefone = findViewById(R.id.etComplI1Telefone);

// Inicialização - Integrante 2
        etComplI2Nome = findViewById(R.id.etComplI2Nome);
        etComplI2Email = findViewById(R.id.etComplI2Email);
        etComplI2Camiseta = findViewById(R.id.etComplI2Camiseta);
        etComplI2Rg = findViewById(R.id.etComplI2Rg);
        etComplI2Cpf = findViewById(R.id.etComplI2Cpf);
        etComplI2DataNasc = findViewById(R.id.etComplI2DataNasc);
        etComplI2Idade = findViewById(R.id.etComplI2Idade);
        etComplI2Telefone = findViewById(R.id.etComplI2Telefone);

// Inicialização - Integrante 3
        etComplI3Nome = findViewById(R.id.etComplI3Nome);
        etComplI3Email = findViewById(R.id.etComplI3Email);
        etComplI3Camiseta = findViewById(R.id.etComplI3Camiseta);
        etComplI3Rg = findViewById(R.id.etComplI3Rg);
        etComplI3Cpf = findViewById(R.id.etComplI3Cpf);
        etComplI3DataNasc = findViewById(R.id.etComplI3DataNasc);
        etComplI3Idade = findViewById(R.id.etComplI3Idade);
        etComplI3Telefone = findViewById(R.id.etComplI3Telefone);

// Inicialização - Integrante 4
        etComplI4Nome = findViewById(R.id.etComplI4Nome);
        etComplI4Email = findViewById(R.id.etComplI4Email);
        etComplI4Camiseta = findViewById(R.id.etComplI4Camiseta);
        etComplI4Rg = findViewById(R.id.etComplI4Rg);
        etComplI4Cpf = findViewById(R.id.etComplI4Cpf);
        etComplI4DataNasc = findViewById(R.id.etComplI4DataNasc);
        etComplI4Idade = findViewById(R.id.etComplI4Idade);
        etComplI4Telefone = findViewById(R.id.etComplI4Telefone);

// Inicialização - Integrante 5
        etComplI5Nome = findViewById(R.id.etComplI5Nome);
        etComplI5Email = findViewById(R.id.etComplI5Email);
        etComplI5Camiseta = findViewById(R.id.etComplI5Camiseta);
        etComplI5Rg = findViewById(R.id.etComplI5Rg);
        etComplI5Cpf = findViewById(R.id.etComplI5Cpf);
        etComplI5DataNasc = findViewById(R.id.etComplI5DataNasc);
        etComplI5Idade = findViewById(R.id.etComplI5Idade);
        etComplI5Telefone = findViewById(R.id.etComplI5Telefone);

// Inicialização - Integrante 6
        etComplI6Nome = findViewById(R.id.etComplI6Nome);
        etComplI6Email = findViewById(R.id.etComplI6Email);
        etComplI6Camiseta = findViewById(R.id.etComplI6Camiseta);
        etComplI6Rg = findViewById(R.id.etComplI6Rg);
        etComplI6Cpf = findViewById(R.id.etComplI6Cpf);
        etComplI6DataNasc = findViewById(R.id.etComplI6DataNasc);
        etComplI6Idade = findViewById(R.id.etComplI6Idade);
        etComplI6Telefone = findViewById(R.id.etComplI6Telefone);

// Inicialização - Integrante 7
        etComplI7Nome = findViewById(R.id.etComplI7Nome);
        etComplI7Email = findViewById(R.id.etComplI7Email);
        etComplI7Camiseta = findViewById(R.id.etComplI7Camiseta);
        etComplI7Rg = findViewById(R.id.etComplI7Rg);
        etComplI7Cpf = findViewById(R.id.etComplI7Cpf);
        etComplI7DataNasc = findViewById(R.id.etComplI7DataNasc);
        etComplI7Idade = findViewById(R.id.etComplI7Idade);
        etComplI7Telefone = findViewById(R.id.etComplI7Telefone);

        //completude
        etCompleQuantidade = findViewById(R.id.etCompleQuantidade);
        etCompleIdentificacao = findViewById(R.id.etCompleIdentificacao);
        etCompleResponsavel = findViewById(R.id.etCompleResponsavel);
        etCompleStatusEquipe = findViewById(R.id.etCompleStatusEquipe);
        etCompleStatusConhecimento = findViewById(R.id.etCompleStatusConhecimentos);
        etCompleStatusRecursos = findViewById(R.id.etCompleStatusRecursos);
        etCompleStatusCanvas = findViewById(R.id.etCompleStatusCanvas);
        etCompleStatusPitchEscrito = findViewById(R.id.etCompleStatusPitchEscrito);
        etCompleStatusPitchVideo = findViewById(R.id.etCompleStatusPitchVideo);
        etCompleStatusCronograma = findViewById(R.id.etCompleStatusCronograma);
        etCompleStatusFotoEquipe = findViewById(R.id.etCompleStatusFotoEquipe);
        etCompleStatusFotosEtapa = findViewById(R.id.etCompleStatusFotosEtapa);

        // Inicializar Layouts e Abas
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

        String nivelValidacao = getIntent().getStringExtra("nivel_de_acesso");
        if (nivelValidacao != null && (nivelValidacao.trim().equals("5") || nivelValidacao.trim().equals("2"))) {
            btnEditarDados.setVisibility(View.GONE);
        }

        atualizarTodosFormularios(false);

        btnEditarDados.setOnClickListener(v -> {
            modoEdicao = !modoEdicao;
            atualizarTodosFormularios(modoEdicao);

            if (modoEdicao) {
                btnEditarDados.setText("Salvar Alterações");
            } else {
                btnEditarDados.setText("Editar Dados");
                salvarTodosFormularios(); // <- substitui as 13 chamadas
            }
        });

        configurarCliques();
        destacarAba(tabEquipe);

        // Chamadas unificadas para trazer as tabelas existentes ao iniciar a tela
        carregarDadosDoBanco("equipe");
        carregarDadosDoBanco("conhecimentos");
        carregarDadosDoBanco("recursos");
        carregarDadosDoBanco("cronograma");
        carregarDadosDoBanco("cronograma_especifico");
        carregarDadosDoBanco("canva");
        carregarDadosDoBanco("curriculo");
        carregarDadosDoBanco("empresas");
        carregarDadosDoBanco("pitch");
        carregarDadosDoBanco("uso_ia");
        carregarDadosDoBanco("planilha");
        carregarDadosDoBanco("informacoes_complementares");
        carregarDadosDoBanco("informacoes_completude");
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

    private void salvarTodosFormularios() {
        String[] tipos = {
                "equipe", "conhecimentos", "recursos", "cronograma",
                "cronograma_especifico", "canva", "curriculo", "empresas",
                "pitch", "uso_ia", "planilha", "informacoes_complementares",
                "informacoes_completude"
        };

        final int total = tipos.length;
        final int[] concluidos = {0};
        final boolean[] temErro = {false};

        FormularioRepository.OnSalvoListener listener = new FormularioRepository.OnSalvoListener() {
            @Override
            public void onSucesso() {
                concluidos[0]++;
                if (concluidos[0] == total) {
                    String msg = temErro[0]
                            ? "Erro ao salvar alguns dados. Verifique sua conexão."
                            : "Todos os dados salvos com sucesso!";
                    Toast.makeText(FormularioActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErro(String erro) {
                temErro[0] = true;
                concluidos[0]++;
                if (concluidos[0] == total) {
                    Toast.makeText(FormularioActivity.this,
                            "Erro ao salvar alguns dados. Verifique sua conexão.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        for (String tipo : tipos) {
            salvarFormularioNoBanco(tipo, listener);
        }
    }

    // MÉTODO UNIFICADO: Carrega qualquer formulário dinamicamente pelo tipo
    private void carregarDadosDoBanco(String tipo) {
        FormularioRepository repository = new FormularioRepository(this);

        repository.carregarDados(tipo, targetEmail, new FormularioRepository.OnDadosCarregadosListener() {
            @Override
            public void onSucesso(JSONObject dados) {
                if (tipo.equals("equipe")) {
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
                else if (tipo.equals("conhecimentos")) {
                    etPlanoCurso.setText(dados.optString("plano_curso", ""));
                    etConhecimentosAplicados.setText(dados.optString("conhecimentos_aplicados", ""));
                    etCapacidadesAplicadas.setText(dados.optString("capacidades_aplicadas", ""));
                }

                else if (tipo.equals("recursos")) {
                    // LOG DE DEBUG: Vamos ver exatamente o que a API está devolvendo para o app
                    android.util.Log.d("DEBUG_RECURSOS", "JSON recebido: " + dados.toString());

                    JSONObject dadosRecursos = dados;

                    // Se o JSON recebido contiver uma chave interna chamada "data", nós entramos nela
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosRecursos = dados.optJSONObject("data");
                    }

                    if (dadosRecursos != null) {
                        etRecursosFerramentas.setText(dadosRecursos.optString("ferramentas", ""));
                        etRecursosEquipamentos.setText(dadosRecursos.optString("equipamentos", ""));
                        etRecursosDescricao.setText(dadosRecursos.optString("descricao_produto", ""));
                        etRecursosQtdComprada.setText(dadosRecursos.optString("quant_comprada", ""));
                        etRecursosQtdUtilizada.setText(dadosRecursos.optString("quant_utilizada", ""));

                        // Tratamento para os valores REAL (Double)
                        double precoEst = dadosRecursos.optDouble("preco_estimado", 0.0);
                        etRecursosPrecoEstimado.setText(precoEst > 0 ? String.valueOf(precoEst) : "");

                        etRecursosUnidadeMedida.setText(dadosRecursos.optString("uni_medida", ""));
                        etRecursosFornecedor.setText(dadosRecursos.optString("fornecedor_principal", ""));
                        etRecursosModoObtencao.setText(dadosRecursos.optString("modo_obtencao", ""));
                        etRecursosDisponibilidade.setText(dadosRecursos.optString("disponibilidade", ""));
                        etRecursosPagamento.setText(dadosRecursos.optString("pagamento", ""));

                        // Nome exato da coluna com erro de digitação do seu banco (alternativas_consideredas)
                        etRecursosAlternativas.setText(dadosRecursos.optString("alternativas_consideredas", ""));

                        double precoTot = dadosRecursos.optDouble("preco_total", 0.0);
                        etRecursosPrecoTotal.setText(precoTot > 0 ? String.valueOf(precoTot) : "");
                    } else {
                        android.util.Log.e("DEBUG_RECURSOS", "O objeto de dados de recursos veio nulo.");
                    }
                }

                else if (tipo.equals("cronograma")) {
                    JSONObject dadosCronograma = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosCronograma = dados.optJSONObject("data");
                    }

                    if (dadosCronograma != null) {
                        etCronogramaProcesso.setText(dadosCronograma.optString("processo", ""));
                        etCronogramaEtapas.setText(dadosCronograma.optString("etapas", ""));
                        etCronogramaResponsavel.setText(dadosCronograma.optString("responsavel", ""));
                        etCronogramaDataInicio.setText(dadosCronograma.optString("data_inicio", ""));
                        etCronogramaDataFinal.setText(dadosCronograma.optString("data_final", ""));
                        etCronogramaObservacoes.setText(dadosCronograma.optString("observacoes", ""));
                    }
                }

                else if (tipo.equals("cronograma_especifico")) {
                    JSONObject dadosCronoEsp = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosCronoEsp = dados.optJSONObject("data");
                    }

                    if (dadosCronoEsp != null) {
                        etCronoEspProcessos.setText(dadosCronoEsp.optString("processos", ""));
                        etCronoEspEtapas.setText(dadosCronoEsp.optString("etapas", ""));
                        etCronoEspResponsavel.setText(dadosCronoEsp.optString("responsavel", ""));
                        etCronoEspDataInicio.setText(dadosCronoEsp.optString("data_inicio", ""));
                        etCronoEspDataFinal.setText(dadosCronoEsp.optString("data_final", ""));
                        etCronoEspObservacoes.setText(dadosCronoEsp.optString("observacoes", ""));
                    }
                }

                else if (tipo.equals("canva")) {
                    JSONObject dadosCanva = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosCanva = dados.optJSONObject("data");
                    }

                    if (dadosCanva != null) {
                        etCanvaAtividadesChaves.setText(dadosCanva.optString("atividades_chaves", ""));
                        etCanvaPropostaChave.setText(dadosCanva.optString("proposta_chave", ""));
                        etCanvaRelacionamentos.setText(dadosCanva.optString("relacionamentos_clientes", ""));
                        etCanvaSegmentos.setText(dadosCanva.optString("segmentos_clientes", ""));
                        etCanvaRecursosChaves.setText(dadosCanva.optString("recursos_chaves", ""));
                        etCanvaCanais.setText(dadosCanva.optString("canais", ""));
                        etCanvaEstruturaCustos.setText(dadosCanva.optString("estrutura_custos", ""));
                        etCanvaFluxoReceita.setText(dadosCanva.optString("fluxo_receita", ""));
                        etCanvaParceirosChaves.setText(dadosCanva.optString("parceiros_chaves", ""));
                    }
                }

                else if (tipo.equals("curriculo")) {
                    JSONObject dadosCurr = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosCurr = dados.optJSONObject("data");
                    }

                    if (dadosCurr != null) {
                        etCurrNome.setText(dadosCurr.optString("nome", ""));
                        etCurrDataNacimento.setText(dadosCurr.optString("data_nacimento", ""));
                        etCurrCpf.setText(dadosCurr.optString("cpf", ""));
                        etCurrEmpresaVinculado.setText(dadosCurr.optString("empresa_vinculado", ""));
                        etCurrProjeto.setText(dadosCurr.optString("projeto", ""));
                        etCurrTelefone.setText(dadosCurr.optString("telefone", ""));
                        etCurrEmail.setText(dadosCurr.optString("email", ""));
                        etCurrNomeReponsavel.setText(dadosCurr.optString("nome_reponsavel", ""));
                        etCurrNumeroResponsavel.setText(dadosCurr.optString("numero_responsavel", ""));
                        etCurrEmailRepsonsavel.setText(dadosCurr.optString("email_repsonsavel", ""));
                        etCurrHabilidades.setText(dadosCurr.optString("habilidades", ""));
                        etCurrFezProjeto.setText(dadosCurr.optString("fez_projeto", ""));
                        etCurrCidade.setText(dadosCurr.optString("cidade", ""));
                        etCurrMotivoProjeto.setText(dadosCurr.optString("motivo_projeto", ""));
                        etCurrAprendoMais.setText(dadosCurr.optString("aprendo_mais", ""));
                        etCurrPrefiroTrabalhar.setText(dadosCurr.optString("prefiro_trabalhar", ""));
                    }
                }

                else if (tipo.equals("empresas")) {
                    JSONObject dadosEmp = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosEmp = dados.optJSONObject("data");
                    }

                    if (dadosEmp != null) {
                        etEmpresaNome.setText(dadosEmp.optString("nome_empresa", ""));
                        etEmpresaCnpj.setText(dadosEmp.optString("cnpj", ""));
                        etEmpresaRegiao.setText(dadosEmp.optString("regiao", ""));
                        etEmpresaTelefone.setText(dadosEmp.optString("telefone_contato", ""));
                        etEmpresaEmail.setText(dadosEmp.optString("email_contato", ""));
                        etEmpresaObjetivos.setText(dadosEmp.optString("objetivos", ""));
                        etEmpresaProblema.setText(dadosEmp.optString("problema_projeto", ""));
                    }
                }

                else if (tipo.equals("pitch")) {
                    JSONObject dadosPitch= dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosPitch = dados.optJSONObject("data");
                    }

                    if (dadosPitch != null) {
                        etPitchRoteiro.setText(dadosPitch.optString("roteiro", ""));
                        String videoUrl = dadosPitch.optString("video_url", "");
                        if (!videoUrl.isEmpty()) {
                            configurarPlayerVideo(videoUrl);
                        }
                    }
                }

                else if (tipo.equals("uso_ia")) {
                    JSONObject dadosUsoIa = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosUsoIa = dados.optJSONObject("data");
                    }

                    if (dadosUsoIa != null) {
                        etIaNomeFerramenta.setText(dadosUsoIa.optString("nome_ferramenta", ""));
                        etIaLinkAcesso.setText(dadosUsoIa.optString("link_acesso", ""));
                        etIaTipoLicenca.setText(dadosUsoIa.optString("tipo_licenca", ""));
                        etIaEtapaUso.setText(dadosUsoIa.optString("etapa_uso", ""));
                        etIaCriacaoPrompt.setText(dadosUsoIa.optString("criacao_prompt", ""));
                        etIaDescricaoUso.setText(dadosUsoIa.optString("descricao_uso", ""));
                    }
                }

                else if (tipo.equals("planilha")) {
                    JSONObject dadosPlanilha = dados;
                    if (dados.has("data") && !dados.isNull("data")) {
                        dadosPlanilha = dados.optJSONObject("data");
                    }

                    if (dadosPlanilha != null) {
                        etPlanilhaTarefas.setText(dadosPlanilha.optString("tarefas", ""));
                        etPlanilhaAlunoResponsavel.setText(dadosPlanilha.optString("aluno_responsavel", ""));
                        etPlanilhaProfessorArea.setText(dadosPlanilha.optString("professor_area", ""));
                        etPlanilhaInicioPrevisto.setText(dadosPlanilha.optString("inicio_previsto", ""));
                        etPlanilhaFimPrevisto.setText(dadosPlanilha.optString("fim_previsto", ""));
                        etPlanilhaInicioRealizado.setText(dadosPlanilha.optString("inicio_realizado", ""));
                        etPlanilhaFimRealizado.setText(dadosPlanilha.optString("fim_realizado", ""));

                        // Tratamento para duracao_dias que está definido como INTEGER no banco
                        int duracaoDias = dadosPlanilha.optInt("duracao_dias", 0);
                        etPlanilhaDuracaoDias.setText(duracaoDias > 0 ? String.valueOf(duracaoDias) : "");

                        etPlanilhaStatus.setText(dadosPlanilha.optString("status", ""));

                        // Colunas com nomes específicos conforme a imagem do seu banco:
                        etPlanilhaDescricao.setText(dadosPlanilha.optString("descricao_tarefa", ""));
                        etPlanilhaDificuldades.setText(dadosPlanilha.optString("dificuldades_enxergadas", ""));
                        etPlanilhaImpacto.setText(dadosPlanilha.optString("impacto_outras_tarefas", ""));
                    }
                }

                else if (tipo.equals("informacoes_complementares")) {
                    // 1. Proteção contra dados totalmente nulos
                    if (dados != null) {
                        JSONObject dadosCompl = dados;

                        if (dados.has("data") && !dados.isNull("data")) {
                            dadosCompl = dados.optJSONObject("data");
                        }

                        if (dadosCompl != null) {
                            // Dados Gerais do Projeto
                            etComplUnidade.setText(dadosCompl.optString("unidade_nome_comercial", ""));
                            etComplCoordenador.setText(dadosCompl.optString("coordenador_pedagogico", ""));
                            etComplGestor.setText(dadosCompl.optString("gestor", ""));
                            etComplEmpresa.setText(dadosCompl.optString("empresa", ""));
                            etComplProjeto.setText(dadosCompl.optString("projeto", ""));
                            etComplDescricaoProjeto.setText(dadosCompl.optString("descricao", ""));

                            // 2. Forma mais simples e direta (preserva o '0' se ele existir no JSON)
                            etComplQtdProjetos.setText(dadosCompl.optString("qtd_projetos", ""));

                            // Integrante 1
                            etComplI1Nome.setText(dadosCompl.optString("enai_cax1", ""));
                            etComplI1Email.setText(dadosCompl.optString("email1", ""));
                            etComplI1Camiseta.setText(dadosCompl.optString("camiseta1", ""));
                            etComplI1Rg.setText(dadosCompl.optString("rg1", ""));
                            etComplI1Cpf.setText(dadosCompl.optString("cpf1", ""));
                            etComplI1DataNasc.setText(dadosCompl.optString("data_nascimento1", ""));
                            etComplI1Idade.setText(dadosCompl.optString("idade1", ""));
                            etComplI1Telefone.setText(dadosCompl.optString("telefone1", ""));

                            // Integrante 2
                            etComplI2Nome.setText(dadosCompl.optString("enai_cax2", ""));
                            etComplI2Email.setText(dadosCompl.optString("email2", ""));
                            etComplI2Camiseta.setText(dadosCompl.optString("camiseta2", ""));
                            etComplI2Rg.setText(dadosCompl.optString("rg2", ""));
                            etComplI2Cpf.setText(dadosCompl.optString("cpf2", ""));
                            etComplI2DataNasc.setText(dadosCompl.optString("data_nascimento2", ""));
                            etComplI2Idade.setText(dadosCompl.optString("idade2", ""));
                            etComplI2Telefone.setText(dadosCompl.optString("telefone2", ""));

                            // Integrante 3
                            etComplI3Nome.setText(dadosCompl.optString("enai_cax3", ""));
                            etComplI3Email.setText(dadosCompl.optString("email3", ""));
                            etComplI3Camiseta.setText(dadosCompl.optString("camiseta3", ""));
                            etComplI3Rg.setText(dadosCompl.optString("rg3", ""));
                            etComplI3Cpf.setText(dadosCompl.optString("cpf3", ""));
                            etComplI3DataNasc.setText(dadosCompl.optString("data_nascimento3", ""));
                            etComplI3Idade.setText(dadosCompl.optString("idade3", ""));
                            etComplI3Telefone.setText(dadosCompl.optString("telefone3", ""));

                            // Integrante 4
                            etComplI4Nome.setText(dadosCompl.optString("enai_cax4", ""));
                            etComplI4Email.setText(dadosCompl.optString("email4", ""));
                            etComplI4Camiseta.setText(dadosCompl.optString("camiseta4", ""));
                            etComplI4Rg.setText(dadosCompl.optString("rg4", ""));
                            etComplI4Cpf.setText(dadosCompl.optString("cpf4", ""));
                            etComplI4DataNasc.setText(dadosCompl.optString("data_nascimento4", ""));
                            etComplI4Idade.setText(dadosCompl.optString("idade4", ""));
                            etComplI4Telefone.setText(dadosCompl.optString("telefone4", ""));

                            // Integrante 5
                            etComplI5Nome.setText(dadosCompl.optString("enai_cax5", ""));
                            etComplI5Email.setText(dadosCompl.optString("email5", ""));
                            etComplI5Camiseta.setText(dadosCompl.optString("camiseta5", ""));
                            etComplI5Rg.setText(dadosCompl.optString("rg5", ""));
                            etComplI5Cpf.setText(dadosCompl.optString("cpf5", ""));
                            etComplI5DataNasc.setText(dadosCompl.optString("data_nascimento5", ""));
                            etComplI5Idade.setText(dadosCompl.optString("idade5", ""));
                            etComplI5Telefone.setText(dadosCompl.optString("telefone5", ""));

                            // Integrante 6
                            etComplI6Nome.setText(dadosCompl.optString("enai_cax6", ""));
                            etComplI6Email.setText(dadosCompl.optString("email6", ""));
                            etComplI6Camiseta.setText(dadosCompl.optString("camiseta6", ""));
                            etComplI6Rg.setText(dadosCompl.optString("rg6", ""));
                            etComplI6Cpf.setText(dadosCompl.optString("cpf6", ""));
                            etComplI6DataNasc.setText(dadosCompl.optString("data_nascimento6", ""));
                            etComplI6Idade.setText(dadosCompl.optString("idade6", ""));
                            etComplI6Telefone.setText(dadosCompl.optString("telefone6", ""));

                            // Integrante 7
                            etComplI7Nome.setText(dadosCompl.optString("enai_cax7", ""));
                            etComplI7Email.setText(dadosCompl.optString("email7", ""));
                            etComplI7Camiseta.setText(dadosCompl.optString("camiseta7", ""));
                            etComplI7Rg.setText(dadosCompl.optString("rg7", ""));
                            etComplI7Cpf.setText(dadosCompl.optString("cpf7", ""));
                            etComplI7DataNasc.setText(dadosCompl.optString("data_nascimento7", ""));
                            etComplI7Idade.setText(dadosCompl.optString("idade7", ""));
                            etComplI7Telefone.setText(dadosCompl.optString("telefone7", ""));
                        }
                    }
                }

                else if (tipo.equals("informacoes_completude")) {
                    if (dados != null) {
                        JSONObject dadosCpt = dados;

                        if (dados.has("data") && !dados.isNull("data")) {
                            dadosCpt = dados.optJSONObject("data");
                        }

                        if (dadosCpt != null) {
                            // Preenche a tela usando as suas variáveis correspondentes de forma segura
                            etCompleQuantidade.setText(dadosCpt.optString("qtd", ""));
                            etCompleIdentificacao.setText(dadosCpt.optString("equipe_unidade_empresa", ""));
                            etCompleResponsavel.setText(dadosCpt.optString("responsavel_preenchimento", ""));
                            etCompleStatusEquipe.setText(dadosCpt.optString("dados_equipe", ""));
                            etCompleStatusConhecimento.setText(dadosCpt.optString("conhecimentos", ""));
                            etCompleStatusRecursos.setText(dadosCpt.optString("recursos_aplicados", ""));
                            etCompleStatusCanvas.setText(dadosCpt.optString("canvas_preencher", ""));
                            etCompleStatusPitchEscrito.setText(dadosCpt.optString("pitch_escrito", ""));
                            etCompleStatusPitchVideo.setText(dadosCpt.optString("pitch_video", ""));
                            etCompleStatusCronograma.setText(dadosCpt.optString("cronograma", ""));
                            etCompleStatusFotoEquipe.setText(dadosCpt.optString("foto_equipe", ""));
                            etCompleStatusFotosEtapa.setText(dadosCpt.optString("fotos_etapa_projeto", ""));
                        }
                    }
                }

            }

            @Override
            public void onNaoEncontrado() {
                if (tipo.equals("equipe")) {
                    Toast.makeText(FormularioActivity.this, "Preencha seus dados pela primeira vez.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErro(String erro) {
                android.util.Log.e("ERRO_CARREGAR_" + tipo.toUpperCase(), erro);
            }
        });
    }

    // 🎯 MÉTODO UNIFICADO: Prepara o pacote JSON específico de cada tipo e envia
    private void salvarFormularioNoBanco(String tipo, FormularioRepository.OnSalvoListener listener) {
        FormularioRepository repository = new FormularioRepository(this);
        JSONObject jsonCampos = new JSONObject();
        try {
            if (tipo.equals("equipe")) {
                jsonCampos.put("nome_equipe", etNomeEquipe.getText().toString().trim());
                jsonCampos.put("nome_projeto", etNomeProjeto.getText().toString().trim());
                jsonCampos.put("email", etEmail.getText().toString().trim());
                jsonCampos.put("area_atuacao_curso", etAreaCurso.getText().toString().trim());
                jsonCampos.put("area_atuacao_projeto", etAreaProjeto.getText().toString().trim());
                jsonCampos.put("nome_orientador", etNomeOrientador.getText().toString().trim());
                jsonCampos.put("nome_coorientador", etNomeCoorientador.getText().toString().trim());
                jsonCampos.put("nome_integrante", etIntegrante1.getText().toString().trim());
                jsonCampos.put("nome_integrante2", etIntegrante2.getText().toString().trim());
                jsonCampos.put("nome_integrante3", etIntegrante3.getText().toString().trim());
                jsonCampos.put("nome_integrante4", etIntegrante4.getText().toString().trim());
                jsonCampos.put("nome_integrante5", etIntegrante5.getText().toString().trim());
            }
            else if (tipo.equals("conhecimentos")) {
                jsonCampos.put("plano_curso", etPlanoCurso.getText().toString().trim());
                jsonCampos.put("conhecimentos_aplicados", etConhecimentosAplicados.getText().toString().trim());
                jsonCampos.put("capacidades_aplicadas", etCapacidadesAplicadas.getText().toString().trim());
            }

            else if (tipo.equals("recursos")) {
                jsonCampos.put("ferramentas", etRecursosFerramentas.getText().toString().trim());
                jsonCampos.put("equipamentos", etRecursosEquipamentos.getText().toString().trim());
                jsonCampos.put("descricao_produto", etRecursosDescricao.getText().toString().trim());
                jsonCampos.put("quant_comprada", etRecursosQtdComprada.getText().toString().trim());
                jsonCampos.put("quant_utilizada", etRecursosQtdUtilizada.getText().toString().trim());

                // Tratamento seguro para os números decimais (REAL)
                String pEst = etRecursosPrecoEstimado.getText().toString().trim().replace(",", ".");
                jsonCampos.put("preco_estimado", pEst.isEmpty() ? 0.0 : Double.parseDouble(pEst));

                jsonCampos.put("uni_medida", etRecursosUnidadeMedida.getText().toString().trim());
                jsonCampos.put("fornecedor_principal", etRecursosFornecedor.getText().toString().trim());
                jsonCampos.put("modo_obtencao", etRecursosModoObtencao.getText().toString().trim());
                jsonCampos.put("disponibilidade", etRecursosDisponibilidade.getText().toString().trim());
                jsonCampos.put("pagamento", etRecursosPagamento.getText().toString().trim());
                jsonCampos.put("alternativas_consideredas", etRecursosAlternativas.getText().toString().trim());

                String pTot = etRecursosPrecoTotal.getText().toString().trim().replace(",", ".");
                jsonCampos.put("preco_total", pTot.isEmpty() ? 0.0 : Double.parseDouble(pTot));
            }

            else if (tipo.equals("cronograma")) {
                jsonCampos.put("processo", etCronogramaProcesso.getText().toString().trim());
                jsonCampos.put("etapas", etCronogramaEtapas.getText().toString().trim());
                jsonCampos.put("responsavel", etCronogramaResponsavel.getText().toString().trim());
                jsonCampos.put("data_inicio", etCronogramaDataInicio.getText().toString().trim());
                jsonCampos.put("data_final", etCronogramaDataFinal.getText().toString().trim());
                jsonCampos.put("observacoes", etCronogramaObservacoes.getText().toString().trim());
            }

            else if (tipo.equals("cronograma_especifico")) {
                jsonCampos.put("processos", etCronoEspProcessos.getText().toString().trim());
                jsonCampos.put("etapas", etCronoEspEtapas.getText().toString().trim());
                jsonCampos.put("responsavel", etCronoEspResponsavel.getText().toString().trim());
                jsonCampos.put("data_inicio", etCronoEspDataInicio.getText().toString().trim());
                jsonCampos.put("data_final", etCronoEspDataFinal.getText().toString().trim());
                jsonCampos.put("observacoes", etCronoEspObservacoes.getText().toString().trim());
            }

            else if (tipo.equals("canva")) {
                jsonCampos.put("atividades_chaves", etCanvaAtividadesChaves.getText().toString().trim());
                jsonCampos.put("proposta_chave", etCanvaPropostaChave.getText().toString().trim());
                jsonCampos.put("relacionamentos_clientes", etCanvaRelacionamentos.getText().toString().trim());
                jsonCampos.put("segmentos_clientes", etCanvaSegmentos.getText().toString().trim());
                jsonCampos.put("recursos_chaves", etCanvaRecursosChaves.getText().toString().trim());
                jsonCampos.put("canais", etCanvaCanais.getText().toString().trim());
                jsonCampos.put("estrutura_custos", etCanvaEstruturaCustos.getText().toString().trim());
                jsonCampos.put("fluxo_receita", etCanvaFluxoReceita.getText().toString().trim());
                jsonCampos.put("parceiros_chaves", etCanvaParceirosChaves.getText().toString().trim());
            }

            else if (tipo.equals("curriculo")) {
                jsonCampos.put("nome", etCurrNome.getText().toString().trim());
                jsonCampos.put("data_nacimento", etCurrDataNacimento.getText().toString().trim());
                jsonCampos.put("cpf", etCurrCpf.getText().toString().trim());
                jsonCampos.put("empresa_vinculado", etCurrEmpresaVinculado.getText().toString().trim());
                jsonCampos.put("projeto", etCurrProjeto.getText().toString().trim());
                jsonCampos.put("telefone", etCurrTelefone.getText().toString().trim());
                jsonCampos.put("email", etCurrEmail.getText().toString().trim());
                jsonCampos.put("nome_reponsavel", etCurrNomeReponsavel.getText().toString().trim());
                jsonCampos.put("numero_responsavel", etCurrNumeroResponsavel.getText().toString().trim());
                jsonCampos.put("email_repsonsavel", etCurrEmailRepsonsavel.getText().toString().trim());
                jsonCampos.put("habilidades", etCurrHabilidades.getText().toString().trim());
                jsonCampos.put("fez_projeto", etCurrFezProjeto.getText().toString().trim());
                jsonCampos.put("cidade", etCurrCidade.getText().toString().trim());
                jsonCampos.put("motivo_projeto", etCurrMotivoProjeto.getText().toString().trim());
                jsonCampos.put("aprendo_mais", etCurrAprendoMais.getText().toString().trim());
                jsonCampos.put("prefiro_trabalhar", etCurrPrefiroTrabalhar.getText().toString().trim());
            }

            else if (tipo.equals("empresas")) {
                jsonCampos.put("nome_empresa", etEmpresaNome.getText().toString().trim());
                jsonCampos.put("cnpj", etEmpresaCnpj.getText().toString().trim());
                jsonCampos.put("regiao", etEmpresaRegiao.getText().toString().trim());
                jsonCampos.put("telefone_contato", etEmpresaTelefone.getText().toString().trim());
                jsonCampos.put("email_contato", etEmpresaEmail.getText().toString().trim());
                jsonCampos.put("objetivos", etEmpresaObjetivos.getText().toString().trim());
                jsonCampos.put("problema_projeto", etEmpresaProblema.getText().toString().trim());
            }

            else if (tipo.equals("pitch")) {
                jsonCampos.put("roteiro", etPitchRoteiro.getText().toString().trim());
                if (videoContainer.getVisibility() == View.VISIBLE && vvPitch.getTag() != null) {
                    jsonCampos.put("video_url", vvPitch.getTag().toString());
                }
            }

            else if (tipo.equals("uso_ia")) {
                jsonCampos.put("nome_ferramenta", etIaNomeFerramenta.getText().toString().trim());
                jsonCampos.put("link_acesso", etIaLinkAcesso.getText().toString().trim());
                jsonCampos.put("tipo_licenca", etIaTipoLicenca.getText().toString().trim());
                jsonCampos.put("etapa_uso", etIaEtapaUso.getText().toString().trim());
                jsonCampos.put("criacao_prompt", etIaCriacaoPrompt.getText().toString().trim());
                jsonCampos.put("descricao_uso", etIaDescricaoUso.getText().toString().trim());
            }

            else if (tipo.equals("planilha")) {
                jsonCampos.put("tarefas", etPlanilhaTarefas.getText().toString().trim());
                jsonCampos.put("aluno_responsavel", etPlanilhaAlunoResponsavel.getText().toString().trim());
                jsonCampos.put("professor_area", etPlanilhaProfessorArea.getText().toString().trim());
                jsonCampos.put("inicio_previsto", etPlanilhaInicioPrevisto.getText().toString().trim());
                jsonCampos.put("fim_previsto", etPlanilhaFimPrevisto.getText().toString().trim());
                jsonCampos.put("inicio_realizado", etPlanilhaInicioRealizado.getText().toString().trim());
                jsonCampos.put("fim_realizado", etPlanilhaFimRealizado.getText().toString().trim());

                // Convertendo para número (Integer) para casar com o tipo do banco de dados
                String duracaoStr = etPlanilhaDuracaoDias.getText().toString().trim();
                int duracao = duracaoStr.isEmpty() ? 0 : Integer.parseInt(duracaoStr);
                jsonCampos.put("duracao_dias", duracao);

                jsonCampos.put("status", etPlanilhaStatus.getText().toString().trim());

                // Chaves ajustadas conforme os nomes exatos das colunas do seu banco:
                jsonCampos.put("descricao_tarefa", etPlanilhaDescricao.getText().toString().trim());
                jsonCampos.put("dificuldades_enxergadas", etPlanilhaDificuldades.getText().toString().trim());
                jsonCampos.put("impacto_outras_tarefas", etPlanilhaImpacto.getText().toString().trim());
            }

            else if (tipo.equals("informacoes_complementares")) {
                // Dados Gerais do Projeto
                jsonCampos.put("unidade_nome_comercial", etComplUnidade.getText().toString().trim());
                jsonCampos.put("coordenador_pedagogico", etComplCoordenador.getText().toString().trim());
                jsonCampos.put("gestor", etComplGestor.getText().toString().trim());
                jsonCampos.put("empresa", etComplEmpresa.getText().toString().trim());
                jsonCampos.put("projeto", etComplProjeto.getText().toString().trim());
                jsonCampos.put("descricao", etComplDescricaoProjeto.getText().toString().trim());

                // Convertendo a quantidade de projetos para número inteiro (INTEGER no banco)
                String qtdProjetosStr = etComplQtdProjetos.getText().toString().trim();
                int qtdProjetos = qtdProjetosStr.isEmpty() ? 0 : Integer.parseInt(qtdProjetosStr);
                jsonCampos.put("qtd_projetos", qtdProjetos);

                // Integrante 1
                jsonCampos.put("enai_cax1", etComplI1Nome.getText().toString().trim());
                jsonCampos.put("email1", etComplI1Email.getText().toString().trim());
                jsonCampos.put("camiseta1", etComplI1Camiseta.getText().toString().trim());
                jsonCampos.put("rg1", etComplI1Rg.getText().toString().trim());
                jsonCampos.put("cpf1", etComplI1Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento1", etComplI1DataNasc.getText().toString().trim());
                jsonCampos.put("idade1", etComplI1Idade.getText().toString().trim());
                jsonCampos.put("telefone1", etComplI1Telefone.getText().toString().trim());

                // Integrante 2
                jsonCampos.put("enai_cax2", etComplI2Nome.getText().toString().trim());
                jsonCampos.put("email2", etComplI2Email.getText().toString().trim());
                jsonCampos.put("camiseta2", etComplI2Camiseta.getText().toString().trim());
                jsonCampos.put("rg2", etComplI2Rg.getText().toString().trim());
                jsonCampos.put("cpf2", etComplI2Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento2", etComplI2DataNasc.getText().toString().trim());
                jsonCampos.put("idade2", etComplI2Idade.getText().toString().trim());
                jsonCampos.put("telefone2", etComplI2Telefone.getText().toString().trim());

                // Integrante 3
                jsonCampos.put("enai_cax3", etComplI3Nome.getText().toString().trim());
                jsonCampos.put("email3", etComplI3Email.getText().toString().trim());
                jsonCampos.put("camiseta3", etComplI3Camiseta.getText().toString().trim());
                jsonCampos.put("rg3", etComplI3Rg.getText().toString().trim());
                jsonCampos.put("cpf3", etComplI3Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento3", etComplI3DataNasc.getText().toString().trim());
                jsonCampos.put("idade3", etComplI3Idade.getText().toString().trim());
                jsonCampos.put("telefone3", etComplI3Telefone.getText().toString().trim());

                // Integrante 4
                jsonCampos.put("enai_cax4", etComplI4Nome.getText().toString().trim());
                jsonCampos.put("email4", etComplI4Email.getText().toString().trim());
                jsonCampos.put("camiseta4", etComplI4Camiseta.getText().toString().trim());
                jsonCampos.put("rg4", etComplI4Rg.getText().toString().trim());
                jsonCampos.put("cpf4", etComplI4Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento4", etComplI4DataNasc.getText().toString().trim());
                jsonCampos.put("idade4", etComplI4Idade.getText().toString().trim());
                jsonCampos.put("telefone4", etComplI4Telefone.getText().toString().trim());

                // Integrante 5
                jsonCampos.put("enai_cax5", etComplI5Nome.getText().toString().trim());
                jsonCampos.put("email5", etComplI5Email.getText().toString().trim());
                jsonCampos.put("camiseta5", etComplI5Camiseta.getText().toString().trim());
                jsonCampos.put("rg5", etComplI5Rg.getText().toString().trim());
                jsonCampos.put("cpf5", etComplI5Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento5", etComplI5DataNasc.getText().toString().trim());
                jsonCampos.put("idade5", etComplI5Idade.getText().toString().trim());
                jsonCampos.put("telefone5", etComplI5Telefone.getText().toString().trim());

                // Integrante 6
                jsonCampos.put("enai_cax6", etComplI6Nome.getText().toString().trim());
                jsonCampos.put("email6", etComplI6Email.getText().toString().trim());
                jsonCampos.put("camiseta6", etComplI6Camiseta.getText().toString().trim());
                jsonCampos.put("rg6", etComplI6Rg.getText().toString().trim());
                jsonCampos.put("cpf6", etComplI6Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento6", etComplI6DataNasc.getText().toString().trim());
                jsonCampos.put("idade6", etComplI6Idade.getText().toString().trim());
                jsonCampos.put("telefone6", etComplI6Telefone.getText().toString().trim());

                // Integrante 7
                jsonCampos.put("enai_cax7", etComplI7Nome.getText().toString().trim());
                jsonCampos.put("email7", etComplI7Email.getText().toString().trim());
                jsonCampos.put("camiseta7", etComplI7Camiseta.getText().toString().trim());
                jsonCampos.put("rg7", etComplI7Rg.getText().toString().trim());
                jsonCampos.put("cpf7", etComplI7Cpf.getText().toString().trim());
                jsonCampos.put("data_nascimento7", etComplI7DataNasc.getText().toString().trim());
                jsonCampos.put("idade7", etComplI7Idade.getText().toString().trim());
                jsonCampos.put("telefone7", etComplI7Telefone.getText().toString().trim());
            }

            else if (tipo.equals("informacoes_completude")) {
                // Tratamento seguro para a quantidade (INTEGER no banco)
                String qtdStr = etCompleQuantidade.getText().toString().trim();
                int qtd = qtdStr.isEmpty() ? 0 : Integer.parseInt(qtdStr);
                jsonCampos.put("qtd", qtd);

                // Mapeamento das colunas TEXT com as suas variáveis exatas
                jsonCampos.put("equipe_unidade_empresa", etCompleIdentificacao.getText().toString().trim());
                jsonCampos.put("responsavel_preenchimento", etCompleResponsavel.getText().toString().trim());
                jsonCampos.put("dados_equipe", etCompleStatusEquipe.getText().toString().trim());
                jsonCampos.put("conhecimentos", etCompleStatusConhecimento.getText().toString().trim());
                jsonCampos.put("recursos_aplicados", etCompleStatusRecursos.getText().toString().trim());
                jsonCampos.put("canvas_preencher", etCompleStatusCanvas.getText().toString().trim());
                jsonCampos.put("pitch_escrito", etCompleStatusPitchEscrito.getText().toString().trim());
                jsonCampos.put("pitch_video", etCompleStatusPitchVideo.getText().toString().trim());
                jsonCampos.put("cronograma", etCompleStatusCronograma.getText().toString().trim());
                jsonCampos.put("foto_equipe", etCompleStatusFotoEquipe.getText().toString().trim());
                jsonCampos.put("fotos_etapa_projeto", etCompleStatusFotosEtapa.getText().toString().trim());

                // Se precisar salvar o usuário, descomente a linha abaixo e passe a variável dele:
                // jsonCampos.put("usuario", usuarioLogado);
            }

            repository.salvarDados(tipo, jsonCampos, listener);  // <- passa o listener
        } catch (JSONException e) {
            listener.onErro("Erro JSON em: " + tipo);  // erro também contabilizado
        }
    }

    private void realizarUploadVideo(Uri uri) {
        tvVideoStatus.setText("Preparando envio...");
        pbVideoUpload.setVisibility(View.VISIBLE);
        pbVideoUpload.setProgress(0);
        btnUploadVideo.setEnabled(false);

        FormularioRepository repository = new FormularioRepository(this);
        repository.uploadVideo(uri, new FormularioRepository.OnUploadProgressListener() {
            @Override
            public void onProgress(int progress) {
                pbVideoUpload.setProgress(progress);
                tvVideoStatus.setText("Enviando vídeo: " + progress + "%");
            }

            @Override
            public void onSucesso(String videoUrl) {
                tvVideoStatus.setText("Vídeo enviado com sucesso!");
                pbVideoUpload.setVisibility(View.GONE);
                btnUploadVideo.setEnabled(true);
                if (!videoUrl.isEmpty()) {
                    configurarPlayerVideo(videoUrl);
                }
                Toast.makeText(FormularioActivity.this, "Vídeo do pitch enviado!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onErro(String erro) {
                tvVideoStatus.setText("Erro ao enviar vídeo.");
                pbVideoUpload.setVisibility(View.GONE);
                btnUploadVideo.setEnabled(true);
                Toast.makeText(FormularioActivity.this, erro, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarControlesVideo() {
        btnPlayPause.setOnClickListener(v -> {
            if (vvPitch.isPlaying()) {
                vvPitch.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                vvPitch.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                atualizarProgressoSeekBar();
            }
        });

        btnRewind.setOnClickListener(v -> {
            int current = vvPitch.getCurrentPosition();
            vvPitch.seekTo(Math.max(current - 10000, 0));
        });

        btnForward.setOnClickListener(v -> {
            int current = vvPitch.getCurrentPosition();
            vvPitch.seekTo(Math.min(current + 10000, vvPitch.getDuration()));
        });

        vvPitch.setOnPreparedListener(mp -> {
            videoSeekBar.setMax(vvPitch.getDuration());
            atualizarProgressoSeekBar();
        });

        vvPitch.setOnCompletionListener(mp -> {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            videoSeekBar.setProgress(0);
        });

        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    vvPitch.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (vvPitch != null) {
                    videoSeekBar.setProgress(vvPitch.getCurrentPosition());
                    videoHandler.postDelayed(this, 500);
                }
            }
        };
    }

    private void atualizarProgressoSeekBar() {
        videoHandler.removeCallbacks(updateSeekBar);
        videoHandler.post(updateSeekBar);
    }

    private void configurarPlayerVideo(String url) {
        if (url == null || url.isEmpty()) return;

        vvPitch.setTag(url);
        videoContainer.setVisibility(View.VISIBLE);
        tvVideoStatus.setText("Pitch em vídeo enviado!");

        Uri videoUri = Uri.parse(url);
        vvPitch.setVideoURI(videoUri);
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
            } else if (view instanceof android.widget.Button && view.getId() != R.id.btnEditarDados) {
                view.setEnabled(habilitado);
                view.setAlpha(habilitado ? 1.0f : 0.5f);
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