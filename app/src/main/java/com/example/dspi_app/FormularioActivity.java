package com.example.dspi_app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class FormularioActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 1;

    private LinearLayout formEquipe, formConhecimentos, formRecursos, formCronograma,
            formCanva, formEmpresa, formPitch,
            formIA, formPlanilha, formComplementares, formCompletude, formRelatorio;

    private TextView tabEquipe, tabConhecimentos, tabRecursos, tabCronograma,
            tabCanva, tabEmpresa, tabPitch,
            tabIA, tabPlanilha, tabComplementares, tabCompletude, tabRelatorio;

    private Button btnGerarRelatorio, btnVisualizarRelatorio;

    // Inputs da Equipe
    private EditText etNomeEquipe, etNomeProjeto, etEmail, etAreaCurso, etAreaProjeto,
            etNomeOrientador, etNomeCoorientador, etIntegrante1, etIntegrante2,
            etIntegrante3, etIntegrante4, etIntegrante5;

    // Inputs de Conhecimentos
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

    // inputs canva
    private EditText etCanvaAtividadesChaves, etCanvaPropostaChave, etCanvaRelacionamentos,
            etCanvaSegmentos, etCanvaRecursosChaves, etCanvaCanais,
            etCanvaEstruturaCustos, etCanvaFluxoReceita, etCanvaParceirosChaves;


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
    private ProgressBar pbVideoBuffer;
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
    //completude
    private EditText etCompleQuantidade, etCompleIdentificacao, etCompleResponsavel;
    private Spinner spCompleStatusEquipe, spCompleStatusConhecimento, spCompleStatusRecursos,
            spCompleStatusCanvas, spCompleStatusPitchEscrito, spCompleStatusPitchVideo,
            spCompleStatusCronograma, spCompleStatusFotoEquipe, spCompleStatusFotosEtapa;

    // Relatório
    private EditText etRelNomeEmpresa, etRelEmailEmpresa, etRelSetorEmpresa, etRelDescricao, etRelRoteiroPitch, etRelIntegrante1, etRelIntegrante2, etRelIntegrante3, etRelIntegrante4, etRelIntegrante5,
            etRelOrientador, etRelCoorientador, etRelNomeProjeto, etRelNomeEquipe, etRelAreaAtuProjeto, etRelAreaAtuCurso, etRelUnidadeSenai, etRelGestor, etRelFerramentaIA, etRelLinkAcesso,
            etRelLicenca, etRelEtapaUso, etRelPrompt, etRelMotivoUso, etRelFerramentasProj, etRelEquipamentosProj, etRelQuantCompra, etRelQuantUtilizada, etRelPreco, etRelFornecedor,
            etRelModoObtencao, etRelProcessamento, etRelAlternativaUso, etRelQuantUtilizada2, etRelFormaPagamento, etRelPrecoTotal;

    private Button btnEditarDados;
    private boolean modoEdicao = false;
    private String emailUsuario;
    private String targetEmail;
    private String urlRelatorioPdf = "";

    EditText etComplDescricao;

    private Spinner spPlanilhaStatus;

    private int salvamentosPendentes = 0;
    private boolean houveErroAoSalvar = false;

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

        spPlanilhaStatus = findViewById(R.id.spPlanilhaStatus);

        String[] status = {
                "Não iniciado",
                "Concluído",
                "Em atraso",
                "Necessitamos de auxílio"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                status
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPlanilhaStatus.setAdapter(adapter);

        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);
        configurarBolhaAnimada();

        emailUsuario = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("email_logado", "");
        targetEmail = getIntent().getStringExtra("projeto_usuario");
        if (targetEmail == null || targetEmail.isEmpty()) {
            targetEmail = emailUsuario;
        }

        vincularComponentes();

        // Lógica de visibilidade e nomes baseada no nível de acesso
        if ("1".equals(nivel)) {
            tabRelatorio.setText("Arquivos");
            // tabCanva e formCanva permanecem visíveis para nível 1 editar (visualizar)

            // Ocultar campos de texto do relatório para o nível 1
            for (int i = 0; i < formRelatorio.getChildCount(); i++) {
                View child = formRelatorio.getChildAt(i);
                if (child instanceof EditText) {
                    child.setVisibility(View.GONE);
                }
            }
            
            // Mostrar botões de geração na aba de Arquivos (antigo Relatório)
            Button btnAcaoCanvaRelatorio = findViewById(R.id.btnAcaoCanvaRelatorio);
            if (btnAcaoCanvaRelatorio != null) {
                btnAcaoCanvaRelatorio.setVisibility(View.VISIBLE);
                btnAcaoCanvaRelatorio.setOnClickListener(v -> Toast.makeText(this, "Em breve: Gerar Canva", Toast.LENGTH_SHORT).show());
            }
            
            // O botão de gerar relatório também não deve ter função no momento para o nível 1
            btnGerarRelatorio.setOnClickListener(v -> Toast.makeText(this, "Em breve: Gerar Relatório", Toast.LENGTH_SHORT).show());

        } else {
            // Se não for nível 1, esconde relatório de todos os usuários
            tabRelatorio.setVisibility(View.GONE);
            formRelatorio.setVisibility(View.GONE);
        }
        buscarFormularioNoBanco("equipe");
        buscarFormularioNoBanco("conhecimentos");
        buscarFormularioNoBanco("recursos");
        buscarFormularioNoBanco("cronograma");
        buscarFormularioNoBanco("canva");
        buscarFormularioNoBanco("empresa");
        buscarFormularioNoBanco("pitch");
        buscarFormularioNoBanco("ia");
        buscarFormularioNoBanco("planilha");
        buscarFormularioNoBanco("complementares");
        buscarFormularioNoBanco("completude");
        buscarFormularioNoBanco("relatorio");

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

        // ===== BOTÃO GERAR RELATÓRIO =====
        btnGerarRelatorio.setOnClickListener(v -> {
            if ("1".equals(nivel)) {
                Toast.makeText(this, "Em breve: Gerar Relatório", Toast.LENGTH_SHORT).show();
            } else {
                String usuarioAtual = (targetEmail != null && !targetEmail.isEmpty()) ? targetEmail : emailUsuario;
                fazerRequisicaoNode(usuarioAtual);
            }
        });


        btnEditarDados = findViewById(R.id.btnEditarDados);

        String nivelValidacao = getIntent().getStringExtra("nivel_de_acesso");
        if (nivelValidacao != null && (nivelValidacao.trim().equals("5") || nivelValidacao.trim().equals("2") || nivelValidacao.trim().equals("1"))) {
            btnEditarDados.setVisibility(View.GONE);
            btnUploadVideo.setVisibility(View.GONE);
        }

        atualizarTodosFormularios(false);

        btnEditarDados.setOnClickListener(v -> {
            modoEdicao = !modoEdicao;
            atualizarTodosFormularios(modoEdicao);



            if (modoEdicao) {
                btnEditarDados.setText("Salvar Alterações");
            } else {
                btnEditarDados.setText("Editar Dados");

                salvamentosPendentes = 0;
                houveErroAoSalvar = false;

                salvarFormularioNoBanco("equipe");
                salvarFormularioNoBanco("conhecimentos");
                salvarFormularioNoBanco("recursos");
                salvarFormularioNoBanco("cronograma");
                salvarFormularioNoBanco("canva");
                salvarFormularioNoBanco("empresa");
                salvarFormularioNoBanco("pitch");
                salvarFormularioNoBanco("ia");
                salvarFormularioNoBanco("planilha");
                salvarFormularioNoBanco("complementares");
                salvarFormularioNoBanco("completude");
                salvarFormularioNoBanco("relatorio");
            }
        });

        configurarCliques();
        destacarAba(tabEquipe);
    } // FIM DO ONCREATE

    // =========================================================================
    // MÉTODOS DE REQUISIÇÃO E DOWNLOAD (RELATÓRIO E CANVA)
    // =========================================================================

    private void fazerRequisicaoNode(String usuarioAtual) {
        Toast.makeText(this, "Processando dados na nuvem...", Toast.LENGTH_SHORT).show();

        String urlNode = "https://api-dspi.whyguiih.workers.dev/gerar-relatorio?usuario=" + usuarioAtual;
        JSONObject jsonBody = new JSONObject();

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.POST,
                urlNode,
                jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Dados prontos! Iniciando download...", Toast.LENGTH_SHORT).show();
                            String nomeEquipe = etNomeEquipe.getText().toString().trim();
                            if (nomeEquipe.isEmpty()) {
                                nomeEquipe = usuarioAtual;
                            }
                            baixarPdfNoAndroid(nomeEquipe);
                        } else {
                            Toast.makeText(this, "Aviso: " + response.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Erro ao processar resposta do servidor.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String erroMsg = "Erro de conexão. Status: ";
                    if (error.networkResponse != null) {
                        erroMsg += error.networkResponse.statusCode;
                    } else {
                        erroMsg += "Desconhecido";
                    }
                    Toast.makeText(this, erroMsg, Toast.LENGTH_LONG).show();
                }
        );

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void baixarPdfNoAndroid(String nomeEquipeOuUsuario) {
        String nomeCodificado = Uri.encode(nomeEquipeOuUsuario);
        String urlPython = "http://10.0.0.192:5000/download-relatorio/" + nomeCodificado;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlPython));
        request.setTitle("Relatório " + nomeEquipeOuUsuario);
        request.setDescription("Baixando seu relatório PDF...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String nomeArquivoSeguro = nomeEquipeOuUsuario.replaceAll("[^a-zA-Z0-9]", "_");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Relatorio_" + nomeArquivoSeguro + ".pdf");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Download do relatório iniciado...", Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================================
    // DEMAIS MÉTODOS DO APLICATIVO
    // =========================================================================

    private void vincularComponentes() {
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

        etPlanoCurso = findViewById(R.id.inputPlanoCurso);
        etConhecimentosAplicados = findViewById(R.id.inputConhecimentos);
        etCapacidadesAplicadas = findViewById(R.id.inputCapacidades);

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

        etCronogramaProcesso = findViewById(R.id.etCronoProcesso);
        etCronogramaEtapas = findViewById(R.id.etCronoEtapas);
        etCronogramaResponsavel = findViewById(R.id.etCronoResponsavel);
        etCronogramaDataInicio = findViewById(R.id.etCronoDataInicio);
        etCronogramaDataFinal = findViewById(R.id.etCronoDataFinal);
        etCronogramaObservacoes = findViewById(R.id.etCronoObservacoes);

        etCanvaAtividadesChaves = findViewById(R.id.etCanvaAtividadesChaves);
        etCanvaPropostaChave = findViewById(R.id.etCanvaPropostaChave);
        etCanvaRelacionamentos = findViewById(R.id.etCanvaRelacionamentoClientes);
        etCanvaSegmentos = findViewById(R.id.etCanvaSegmentosClientes);
        etCanvaRecursosChaves = findViewById(R.id.etCanvaRecursosChaves);
        etCanvaCanais = findViewById(R.id.etCanvaCanais);
        etCanvaEstruturaCustos = findViewById(R.id.etCanvaEstruturaCustos);
        etCanvaFluxoReceita = findViewById(R.id.etCanvaFluxoReceita);
        etCanvaParceirosChaves = findViewById(R.id.etCanvaParceirosChaves);

        etEmpresaNome = findViewById(R.id.etEmpresaNome);
        etEmpresaCnpj = findViewById(R.id.etEmpresaCnpj);
        etEmpresaRegiao = findViewById(R.id.etEmpresaRegiao);
        etEmpresaTelefone = findViewById(R.id.etEmpresaTelefone);
        etEmpresaEmail = findViewById(R.id.etEmpresaEmail);
        etEmpresaObjetivos = findViewById(R.id.etEmpresaObjetivos);
        etEmpresaProblema = findViewById(R.id.etEmpresaProblema);

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
        pbVideoBuffer = findViewById(R.id.pbVideoBuffer);

        etIaNomeFerramenta = findViewById(R.id.etIaNomeFerramenta);
        etIaLinkAcesso = findViewById(R.id.etIaLinkAcesso);
        etIaTipoLicenca = findViewById(R.id.etIaTipoLicenca);
        etIaEtapaUso = findViewById(R.id.etIaEtapaUso);
        etIaCriacaoPrompt = findViewById(R.id.etIaCriacaoPrompt);
        etIaDescricaoUso = findViewById(R.id.etIaDescricaoUso);

        etPlanilhaTarefas = findViewById(R.id.etPlanilhaTarefas);
        etPlanilhaAlunoResponsavel = findViewById(R.id.etPlanilhaAlunoResponsavel);
        etPlanilhaProfessorArea = findViewById(R.id.etPlanilhaProfessorArea);
        etPlanilhaInicioPrevisto = findViewById(R.id.etPlanilhaInicioPrevisto);
        etPlanilhaFimPrevisto = findViewById(R.id.etPlanilhaFimPrevisto);
        etPlanilhaInicioRealizado = findViewById(R.id.etPlanilhaInicioRealizado);
        etPlanilhaFimRealizado = findViewById(R.id.etPlanilhaFimRealizado);
        etPlanilhaDuracaoDias = findViewById(R.id.etPlanilhaDuracaoDias);
        etPlanilhaDescricao = findViewById(R.id.etPlanilhaDescricao);
        etPlanilhaDificuldades = findViewById(R.id.etPlanilhaDificuldades);
        etPlanilhaImpacto = findViewById(R.id.etPlanilhaImpacto);

        etCompleQuantidade = findViewById(R.id.etCompleQuantidade);
        etCompleIdentificacao = findViewById(R.id.etCompleIdentificacao);
        etCompleResponsavel = findViewById(R.id.etCompleResponsavel);
        etComplUnidade = findViewById(R.id.etComplUnidade);
        etComplCoordenador = findViewById(R.id.etComplCoordenador);
        etComplGestor = findViewById(R.id.etComplGestor);
        etComplEmpresa = findViewById(R.id.etComplEmpresa);
        etComplProjeto = findViewById(R.id.etComplProjeto);
        etComplDescricao = findViewById(R.id.etComplDescricao);

        spCompleStatusEquipe = findViewById(R.id.spCompleStatusEquipe);
        spCompleStatusConhecimento = findViewById(R.id.spCompleStatusConhecimento);
        spCompleStatusRecursos = findViewById(R.id.spCompleStatusRecursos);
        spCompleStatusCanvas = findViewById(R.id.spCompleStatusCanvas);
        spCompleStatusPitchEscrito = findViewById(R.id.spCompleStatusPitchEscrito);
        spCompleStatusPitchVideo = findViewById(R.id.spCompleStatusPitchVideo);
        spCompleStatusCronograma = findViewById(R.id.spCompleStatusCronograma);
        spCompleStatusFotoEquipe = findViewById(R.id.spCompleStatusFotoEquipe);
        spCompleStatusFotosEtapa = findViewById(R.id.spCompleStatusFotosEtapa);

        configurarSpinnerCompletude(spCompleStatusEquipe);
        configurarSpinnerCompletude(spCompleStatusConhecimento);
        configurarSpinnerCompletude(spCompleStatusRecursos);
        configurarSpinnerCompletude(spCompleStatusCanvas);
        configurarSpinnerCompletude(spCompleStatusPitchEscrito);
        configurarSpinnerCompletude(spCompleStatusPitchVideo);
        configurarSpinnerCompletude(spCompleStatusCronograma);
        configurarSpinnerCompletude(spCompleStatusFotoEquipe);
        configurarSpinnerCompletude(spCompleStatusFotosEtapa);

        etRelNomeEmpresa = findViewById(R.id.etRelNomeEmpresa);
        etRelEmailEmpresa = findViewById(R.id.etRelEmailEmpresa);
        etRelSetorEmpresa = findViewById(R.id.etRelSetorEmpresa);
        etRelDescricao = findViewById(R.id.etRelDescricao);
        etRelRoteiroPitch = findViewById(R.id.etRelRoteiroPitch);
        etRelIntegrante1 = findViewById(R.id.etRelIntegrante1);
        etRelIntegrante2 = findViewById(R.id.etRelIntegrante2);
        etRelIntegrante3 = findViewById(R.id.etRelIntegrante3);
        etRelIntegrante4 = findViewById(R.id.etRelIntegrante4);
        etRelIntegrante5 = findViewById(R.id.etRelIntegrante5);
        etRelOrientador = findViewById(R.id.etRelOrientador);
        etRelCoorientador = findViewById(R.id.etRelCoorientador);
        etRelNomeProjeto = findViewById(R.id.etRelNomeProjeto);
        etRelNomeEquipe = findViewById(R.id.etRelNomeEquipe);
        etRelAreaAtuProjeto = findViewById(R.id.etRelAreaAtuProjeto);
        etRelAreaAtuCurso = findViewById(R.id.etRelAreaAtuCurso);
        etRelUnidadeSenai = findViewById(R.id.etRelUnidadeSenai);
        etRelGestor = findViewById(R.id.etRelGestor);
        etRelFerramentaIA = findViewById(R.id.etRelFerramentaIA);
        etRelLinkAcesso = findViewById(R.id.etRelLinkAcesso);
        etRelLicenca = findViewById(R.id.etRelLicenca);
        etRelEtapaUso = findViewById(R.id.etRelEtapaUso);
        etRelPrompt = findViewById(R.id.etRelPrompt);
        etRelMotivoUso = findViewById(R.id.etRelMotivoUso);
        etRelFerramentasProj = findViewById(R.id.etRelFerramentasProj);
        etRelEquipamentosProj = findViewById(R.id.etRelEquipamentosProj);
        etRelQuantCompra = findViewById(R.id.etRelQuantCompra);
        etRelQuantUtilizada = findViewById(R.id.etRelQuantUtilizada);
        etRelPreco = findViewById(R.id.etRelPreco);
        etRelFornecedor = findViewById(R.id.etRelFornecedor);
        etRelModoObtencao = findViewById(R.id.etRelModoObtencao);
        etRelProcessamento = findViewById(R.id.etRelProcessamento);
        etRelAlternativaUso = findViewById(R.id.etRelAlternativaUso);
        etRelQuantUtilizada2 = findViewById(R.id.etRelQuantUtilizada2);
        etRelFormaPagamento = findViewById(R.id.etRelFormaPagamento);
        etRelPrecoTotal = findViewById(R.id.etRelPrecoTotal);

        formEquipe = findViewById(R.id.formEquipe);
        formConhecimentos = findViewById(R.id.formConhecimentos);
        formRecursos = findViewById(R.id.formRecursos);
        formCronograma = findViewById(R.id.formCronograma);
        formCanva = findViewById(R.id.formCanva);
        formEmpresa = findViewById(R.id.formEmpresa);
        formPitch = findViewById(R.id.formPitch);
        formIA = findViewById(R.id.formIA);
        formPlanilha = findViewById(R.id.formPlanilha);
        formComplementares = findViewById(R.id.formComplementares);
        formCompletude = findViewById(R.id.formCompletude);
        formRelatorio = findViewById(R.id.formRelatorio);

        tabEquipe = findViewById(R.id.tabEquipe);
        tabConhecimentos = findViewById(R.id.tabConhecimentos);
        tabRecursos = findViewById(R.id.tabRecursos);
        tabCronograma = findViewById(R.id.tabCronograma);
        tabCanva = findViewById(R.id.tabCanva);
        tabEmpresa = findViewById(R.id.tabEmpresa);
        tabPitch = findViewById(R.id.tabPitch);
        tabIA = findViewById(R.id.tabIA);
        tabPlanilha = findViewById(R.id.tabPlanilha);
        tabComplementares = findViewById(R.id.tabComplementares);
        tabCompletude = findViewById(R.id.tabCompletude);
        tabRelatorio = findViewById(R.id.tabRelatorio);

        btnGerarRelatorio = findViewById(R.id.btnGerarRelatorio);
        btnVisualizarRelatorio = findViewById(R.id.btnVisualizarRelatorio);
    }

    private void configurarSpinnerCompletude(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Não iniciada", "Parcial", "Concluido"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void selecionarValorSpinner(Spinner spinner, String valor) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;

        if (valor == null || valor.isEmpty()) {
            spinner.setSelection(0); // padrão: "Não iniciada"
            return;
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    //================================================
    //===================SALVAR NO BANCO==============
    //==================================================
    private void salvarFormularioNoBanco(String tipo) {

        salvamentosPendentes++;

        FormularioRepository repository = new FormularioRepository(this);
        JSONObject campos = new JSONObject();

        try {

            if (tipo.equals("equipe")) {

                campos.put("nome_equipe", etNomeEquipe.getText().toString().trim());
                campos.put("nome_projeto", etNomeProjeto.getText().toString().trim());
                campos.put("email", etEmail.getText().toString().trim());
                campos.put("area_atuacao_curso", etAreaCurso.getText().toString().trim());
                campos.put("area_atuacao_projeto", etAreaProjeto.getText().toString().trim());
                campos.put("nome_orientador", etNomeOrientador.getText().toString().trim());
                campos.put("nome_coorientador", etNomeCoorientador.getText().toString().trim());
                campos.put("nome_integrante", etIntegrante1.getText().toString().trim());
                campos.put("nome_integrante2", etIntegrante2.getText().toString().trim());
                campos.put("nome_integrante3", etIntegrante3.getText().toString().trim());
                campos.put("nome_integrante4", etIntegrante4.getText().toString().trim());
                campos.put("nome_integrante5", etIntegrante5.getText().toString().trim());

            } else if (tipo.equals("conhecimentos")) {

                campos.put("plano_curso",
                        etPlanoCurso.getText().toString().trim());

                campos.put("conhecimentos_aplicados",
                        etConhecimentosAplicados.getText().toString().trim());

                campos.put("capacidades_aplicadas",
                        etCapacidadesAplicadas.getText().toString().trim());

            } else if (tipo.equals("recursos")) {

                campos.put("ferramentas",
                        etRecursosFerramentas.getText().toString().trim());

                campos.put("equipamentos",
                        etRecursosEquipamentos.getText().toString().trim());

                campos.put("descricao_produto",
                        etRecursosDescricao.getText().toString().trim());

                campos.put("quant_comprada",
                        etRecursosQtdComprada.getText().toString().trim());

                campos.put("quant_utilizada",
                        etRecursosQtdUtilizada.getText().toString().trim());

                campos.put("preco_estimado",
                        etRecursosPrecoEstimado.getText().toString().trim());

                campos.put("uni_medida",
                        etRecursosUnidadeMedida.getText().toString().trim());

                campos.put("fornecedor_principal",
                        etRecursosFornecedor.getText().toString().trim());

                campos.put("modo_obtencao",
                        etRecursosModoObtencao.getText().toString().trim());

                campos.put("disponibilidade",
                        etRecursosDisponibilidade.getText().toString().trim());

                campos.put("pagamento",
                        etRecursosPagamento.getText().toString().trim());

                campos.put("alternativas_consideradas",
                        etRecursosAlternativas.getText().toString().trim());

                campos.put("preco_total",
                        etRecursosPrecoTotal.getText().toString().trim());

            } else if (tipo.equals("cronograma")) {

                campos.put("processo",
                        etCronogramaProcesso.getText().toString().trim());

                campos.put("etapas",
                        etCronogramaEtapas.getText().toString().trim());

                campos.put("responsavel",
                        etCronogramaResponsavel.getText().toString().trim());

                campos.put("data_inicio",
                        etCronogramaDataInicio.getText().toString().trim());

                campos.put("data_final",
                        etCronogramaDataFinal.getText().toString().trim());

                campos.put("observacoes",
                        etCronogramaObservacoes.getText().toString().trim());

            } else if (tipo.equals("canva")) {

                campos.put("atividades_chaves",
                        etCanvaAtividadesChaves.getText().toString().trim());

                campos.put("proposta_chave",
                        etCanvaPropostaChave.getText().toString().trim());

                campos.put("relacionamentos_clientes",
                        etCanvaRelacionamentos.getText().toString().trim());

                campos.put("segmentos_clientes",
                        etCanvaSegmentos.getText().toString().trim());

                campos.put("recursos_chaves",
                        etCanvaRecursosChaves.getText().toString().trim());

                campos.put("canais",
                        etCanvaCanais.getText().toString().trim());

                campos.put("estrutura_custos",
                        etCanvaEstruturaCustos.getText().toString().trim());

                campos.put("fluxo_receita",
                        etCanvaFluxoReceita.getText().toString().trim());

                campos.put("parceiros_chaves",
                        etCanvaParceirosChaves.getText().toString().trim());

            }  else if (tipo.equals("empresa")) {

                campos.put("nome_empresa",
                        etEmpresaNome.getText().toString().trim());

                campos.put("cnpj",
                        etEmpresaCnpj.getText().toString().trim());

                campos.put("regiao",
                        etEmpresaRegiao.getText().toString().trim());

                campos.put("telefone_contato",
                        etEmpresaTelefone.getText().toString().trim());

                campos.put("email_contato",
                        etEmpresaEmail.getText().toString().trim());

                campos.put("objetivos",
                        etEmpresaObjetivos.getText().toString().trim());

                campos.put("problema_projeto",
                        etEmpresaProblema.getText().toString().trim());

            } else if (tipo.equals("pitch")) {

                campos.put("roteiro",
                        etPitchRoteiro.getText().toString().trim());

            } else if (tipo.equals("ia")) {

                campos.put("nome_ferramenta",
                        etIaNomeFerramenta.getText().toString().trim());

                campos.put("link_acesso",
                        etIaLinkAcesso.getText().toString().trim());

                campos.put("tipo_licenca",
                        etIaTipoLicenca.getText().toString().trim());

                campos.put("etapa_uso",
                        etIaEtapaUso.getText().toString().trim());

                campos.put("criacao_prompt",
                        etIaCriacaoPrompt.getText().toString().trim());

                campos.put("descricao_uso",
                        etIaDescricaoUso.getText().toString().trim());

            } else if (tipo.equals("planilha")) {

                campos.put("tarefas",
                        etPlanilhaTarefas.getText().toString().trim());

                campos.put("aluno_responsavel",
                        etPlanilhaAlunoResponsavel.getText().toString().trim());

                campos.put("professor_da_area",
                        etPlanilhaProfessorArea.getText().toString().trim());

                campos.put("inicio_previsto",
                        etPlanilhaInicioPrevisto.getText().toString().trim());

                campos.put("fim_previsto",
                        etPlanilhaFimPrevisto.getText().toString().trim());

                campos.put("inicio_realizado",
                        etPlanilhaInicioRealizado.getText().toString().trim());

                campos.put("fim_realizado",
                        etPlanilhaFimRealizado.getText().toString().trim());

                campos.put("duracao",
                        etPlanilhaDuracaoDias.getText().toString().trim());

                campos.put("status",
                        spPlanilhaStatus.getSelectedItem().toString());

                campos.put("descricao_da_tarefa",
                        etPlanilhaDescricao.getText().toString().trim());

                campos.put("dificuldades_enxergadas",
                        etPlanilhaDificuldades.getText().toString().trim());

                campos.put("impacto_nas_outras",
                        etPlanilhaImpacto.getText().toString().trim());
            } else if (tipo.equals("complementares")) {
                campos.put("unidade_nome_comercial", etComplUnidade.getText().toString().trim());
                campos.put("coordenador_pedagogico", etComplCoordenador.getText().toString().trim());
                campos.put("gestor", etComplGestor.getText().toString().trim());
                campos.put("empresa", etComplEmpresa.getText().toString().trim());
                campos.put("projeto", etComplProjeto.getText().toString().trim());
                campos.put("descricao", etComplDescricao.getText().toString().trim()); // ✅ corrigido
            }  else if (tipo.equals("completude")) {

                campos.put("qtd",
                        etCompleQuantidade.getText().toString().trim());

                campos.put("equipe_unidade_empresa",
                        etCompleIdentificacao.getText().toString().trim());

                campos.put("responsavel_preenchimento",
                        etCompleResponsavel.getText().toString().trim());

                campos.put("dados_equipe", spCompleStatusEquipe.getSelectedItem().toString());
                campos.put("conhecimentos", spCompleStatusConhecimento.getSelectedItem().toString());
                campos.put("recursos_aplicados", spCompleStatusRecursos.getSelectedItem().toString());
                campos.put("canvas_preencher", spCompleStatusCanvas.getSelectedItem().toString());
                campos.put("pitch_escrito", spCompleStatusPitchEscrito.getSelectedItem().toString());
                campos.put("pitch_video", spCompleStatusPitchVideo.getSelectedItem().toString());
                campos.put("cronograma", spCompleStatusCronograma.getSelectedItem().toString());
                campos.put("foto_equipe", spCompleStatusFotoEquipe.getSelectedItem().toString());
                campos.put("fotos_etapa_projeto", spCompleStatusFotosEtapa.getSelectedItem().toString());
            }
            else if (tipo.equals("relatorio")) {

                campos.put("nome_empresa",
                        etRelNomeEmpresa.getText().toString().trim());

                campos.put("e_mail_empresa",
                        etRelEmailEmpresa.getText().toString().trim());

                campos.put("setor_empresa",
                        etRelSetorEmpresa.getText().toString().trim());

                campos.put("descricao",
                        etRelDescricao.getText().toString().trim());

                campos.put("roteiro_pitch",
                        etRelRoteiroPitch.getText().toString().trim());

                campos.put("integrante1",
                        etRelIntegrante1.getText().toString().trim());

                campos.put("integrante2",
                        etRelIntegrante2.getText().toString().trim());

                campos.put("integrante3",
                        etRelIntegrante3.getText().toString().trim());

                campos.put("integrante4",
                        etRelIntegrante4.getText().toString().trim());

                campos.put("integrante5",
                        etRelIntegrante5.getText().toString().trim());

                campos.put("orientador",
                        etRelOrientador.getText().toString().trim());

                campos.put("coorientador",
                        etRelCoorientador.getText().toString().trim());

                campos.put("nome_projeto",
                        etRelNomeProjeto.getText().toString().trim());

                campos.put("nome_equipe",
                        etRelNomeEquipe.getText().toString().trim());

                campos.put("area_atuacao_projeto",
                        etRelAreaAtuProjeto.getText().toString().trim());

                campos.put("area_atuacao_curso",
                        etRelAreaAtuCurso.getText().toString().trim());

                campos.put("unidade_senai",
                        etRelUnidadeSenai.getText().toString().trim());

                campos.put("gestor",
                        etRelGestor.getText().toString().trim());

                campos.put("ferramenta_ia",
                        etRelFerramentaIA.getText().toString().trim());

                campos.put("link_acesso",
                        etRelLinkAcesso.getText().toString().trim());

                campos.put("licenca",
                        etRelLicenca.getText().toString().trim());

                campos.put("etapa_de_usu",
                        etRelEtapaUso.getText().toString().trim());

                campos.put("prompt",
                        etRelPrompt.getText().toString().trim());

                campos.put("motivo_usu",
                        etRelMotivoUso.getText().toString().trim());

                campos.put("ferramentas_projeto",
                        etRelFerramentasProj.getText().toString().trim());

                campos.put("equipamentos_projeto",
                        etRelEquipamentosProj.getText().toString().trim());

                campos.put("quant_compra",
                        etRelQuantCompra.getText().toString().trim());

                campos.put("quant_utilizada",
                        etRelQuantUtilizada.getText().toString().trim());

                campos.put("preco",
                        etRelPreco.getText().toString().trim());

                campos.put("fornecedor",
                        etRelFornecedor.getText().toString().trim());

                campos.put("modo_obtencao",
                        etRelModoObtencao.getText().toString().trim());

                campos.put("processamento",
                        etRelProcessamento.getText().toString().trim());

                campos.put("alternativa_de_uso",
                        etRelAlternativaUso.getText().toString().trim());

                campos.put("quant_utilizada_2",
                        etRelQuantUtilizada2.getText().toString().trim());

                campos.put("forma_pagamento",
                        etRelFormaPagamento.getText().toString().trim());

                campos.put("preco_total",
                        etRelPrecoTotal.getText().toString().trim());

            }


            repository.salvarFormulario(tipo, campos, new FormularioRepository.OnSalvoListener() {

                @Override
                public void onSucesso() {

                    salvamentosPendentes--;

                    if (salvamentosPendentes == 0 && !houveErroAoSalvar) {

                        Toast.makeText(FormularioActivity.this,
                                "Todos os dados foram salvos com sucesso!",
                                Toast.LENGTH_LONG).show();

                        houveErroAoSalvar = false;
                    }
                }

                @Override
                public void onErro(String erro) {

                    salvamentosPendentes--;

                    houveErroAoSalvar = true;

                    Toast.makeText(FormularioActivity.this,
                            "Erro ao salvar " + tipo + ":\n\n" + erro,
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {

            Toast.makeText(this,
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    //================================================
    //===================BUSCAR NO BANCO==============
    //==================================================
    private void buscarFormularioNoBanco(String tipo) {

        FormularioRepository repository = new FormularioRepository(this);

        repository.buscarFormulario(tipo, new FormularioRepository.OnBuscaListener() {

            @Override
            public void onSucesso(JSONObject dados) {

                if (tipo.equals("equipe")) {

                    etNomeEquipe.setText(dados.optString("nome_equipe"));
                    etNomeProjeto.setText(dados.optString("nome_projeto"));
                    etEmail.setText(dados.optString("email"));
                    etAreaCurso.setText(dados.optString("area_atuacao_curso"));
                    etAreaProjeto.setText(dados.optString("area_atuacao_projeto"));
                    etNomeOrientador.setText(dados.optString("nome_orientador"));
                    etNomeCoorientador.setText(dados.optString("nome_coorientador"));
                    etIntegrante1.setText(dados.optString("nome_integrante"));
                    etIntegrante2.setText(dados.optString("nome_integrante2"));
                    etIntegrante3.setText(dados.optString("nome_integrante3"));
                    etIntegrante4.setText(dados.optString("nome_integrante4"));
                    etIntegrante5.setText(dados.optString("nome_integrante5"));

                } else if (tipo.equals("conhecimentos")) {

                    etPlanoCurso.setText(
                            dados.optString("plano_curso"));

                    etConhecimentosAplicados.setText(
                            dados.optString("conhecimentos_aplicados"));

                    etCapacidadesAplicadas.setText(
                            dados.optString("capacidades_aplicadas"));

                } else if (tipo.equals("recursos")) {

                    etRecursosFerramentas.setText(
                            dados.optString("ferramentas"));

                    etRecursosEquipamentos.setText(
                            dados.optString("equipamentos"));

                    etRecursosDescricao.setText(
                            dados.optString("descricao_produto"));

                    etRecursosQtdComprada.setText(
                            dados.optString("quant_comprada"));

                    etRecursosQtdUtilizada.setText(
                            dados.optString("quant_utilizada"));

                    etRecursosPrecoEstimado.setText(
                            dados.optString("preco_estimado"));

                    etRecursosUnidadeMedida.setText(
                            dados.optString("uni_medida"));

                    etRecursosFornecedor.setText(
                            dados.optString("fornecedor_principal"));

                    etRecursosModoObtencao.setText(
                            dados.optString("modo_obtencao"));

                    etRecursosDisponibilidade.setText(
                            dados.optString("disponibilidade"));

                    etRecursosPagamento.setText(
                            dados.optString("pagamento"));

                    etRecursosAlternativas.setText(
                            dados.optString("alternativas_consideradas"));

                    etRecursosPrecoTotal.setText(
                            dados.optString("preco_total"));

                } else if (tipo.equals("cronograma")) {

                    etCronogramaProcesso.setText(
                            dados.optString("processo"));

                    etCronogramaEtapas.setText(
                            dados.optString("etapas"));

                    etCronogramaResponsavel.setText(
                            dados.optString("responsavel"));

                    etCronogramaDataInicio.setText(
                            dados.optString("data_inicio"));

                    etCronogramaDataFinal.setText(
                            dados.optString("data_final"));

                    etCronogramaObservacoes.setText(
                            dados.optString("observacoes"));

                } else if (tipo.equals("canva")) {

                    etCanvaAtividadesChaves.setText(
                            dados.optString("atividades_chaves"));

                    etCanvaPropostaChave.setText(
                            dados.optString("proposta_chave"));

                    etCanvaRelacionamentos.setText(
                            dados.optString("relacionamentos_clientes"));

                    etCanvaSegmentos.setText(
                            dados.optString("segmentos_clientes"));

                    etCanvaRecursosChaves.setText(
                            dados.optString("recursos_chaves"));

                    etCanvaCanais.setText(
                            dados.optString("canais"));

                    etCanvaEstruturaCustos.setText(
                            dados.optString("estrutura_custos"));

                    etCanvaFluxoReceita.setText(
                            dados.optString("fluxo_receita"));

                    etCanvaParceirosChaves.setText(
                            dados.optString("parceiros_chaves"));

                } else if (tipo.equals("empresa")) {

                    etEmpresaNome.setText(
                            dados.optString("nome_empresa"));

                    etEmpresaCnpj.setText(
                            dados.optString("cnpj"));

                    etEmpresaRegiao.setText(
                            dados.optString("regiao"));

                    etEmpresaTelefone.setText(
                            dados.optString("telefone_contato"));

                    etEmpresaEmail.setText(
                            dados.optString("email_contato"));

                    etEmpresaObjetivos.setText(
                            dados.optString("objetivos"));

                    etEmpresaProblema.setText(
                            dados.optString("problema_projeto"));

                } else if (tipo.equals("pitch")) {

                    etPitchRoteiro.setText(
                            dados.optString("roteiro"));

                } else if (tipo.equals("ia")) {

                    etIaNomeFerramenta.setText(
                            dados.optString("nome_ferramenta"));

                    etIaLinkAcesso.setText(
                            dados.optString("link_acesso"));

                    etIaTipoLicenca.setText(
                            dados.optString("tipo_licenca"));

                    etIaEtapaUso.setText(
                            dados.optString("etapa_uso"));

                    etIaCriacaoPrompt.setText(
                            dados.optString("criacao_prompt"));

                    etIaDescricaoUso.setText(
                            dados.optString("descricao_uso"));

                } else if (tipo.equals("planilha")) {

                    etPlanilhaTarefas.setText(
                            dados.optString("tarefas"));

                    etPlanilhaAlunoResponsavel.setText(
                            dados.optString("aluno_responsavel"));

                    etPlanilhaProfessorArea.setText(
                            dados.optString("professor_da_area"));

                    etPlanilhaInicioPrevisto.setText(
                            dados.optString("inicio_previsto"));

                    etPlanilhaFimPrevisto.setText(
                            dados.optString("fim_previsto"));

                    etPlanilhaInicioRealizado.setText(
                            dados.optString("inicio_realizado"));

                    etPlanilhaFimRealizado.setText(
                            dados.optString("fim_realizado"));

                    etPlanilhaDuracaoDias.setText(
                            dados.optString("duracao"));

                    String status = dados.optString("status");

                    ArrayAdapter adapter = (ArrayAdapter) spPlanilhaStatus.getAdapter();

                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).toString().equals(status)) {
                            spPlanilhaStatus.setSelection(i);
                            break;
                        }
                    }

                    etPlanilhaDescricao.setText(
                            dados.optString("descricao_da_tarefa"));

                    etPlanilhaDificuldades.setText(
                            dados.optString("dificuldades_enxergadas"));

                    etPlanilhaImpacto.setText(
                            dados.optString("impacto_nas_outras"));
                }else if (tipo.equals("complementares")) {
                    etComplUnidade.setText(dados.optString("unidade_nome_comercial"));
                    etComplCoordenador.setText(dados.optString("coordenador_pedagogico"));
                    etComplGestor.setText(dados.optString("gestor"));
                    etComplEmpresa.setText(dados.optString("empresa"));
                    etComplProjeto.setText(dados.optString("projeto"));
                    etComplDescricao.setText(dados.optString("descricao")); // ✅ corrigido
                }
                else if (tipo.equals("completude")) {

                    etCompleQuantidade.setText(dados.optString("qtd"));
                    etCompleIdentificacao.setText(dados.optString("equipe_unidade_empresa"));
                    etCompleResponsavel.setText(dados.optString("responsavel_preenchimento"));

                    selecionarValorSpinner(spCompleStatusEquipe, dados.optString("dados_equipe"));
                    selecionarValorSpinner(spCompleStatusConhecimento, dados.optString("conhecimentos"));
                    selecionarValorSpinner(spCompleStatusRecursos, dados.optString("recursos_aplicados"));
                    selecionarValorSpinner(spCompleStatusCanvas, dados.optString("canvas_preencher"));
                    selecionarValorSpinner(spCompleStatusPitchEscrito, dados.optString("pitch_escrito"));
                    selecionarValorSpinner(spCompleStatusPitchVideo, dados.optString("pitch_video"));
                    selecionarValorSpinner(spCompleStatusCronograma, dados.optString("cronograma"));
                    selecionarValorSpinner(spCompleStatusFotoEquipe, dados.optString("foto_equipe"));
                    selecionarValorSpinner(spCompleStatusFotosEtapa, dados.optString("fotos_etapa_projeto"));
                }
                else if (tipo.equals("relatorio")) {

                    etRelNomeEmpresa.setText(
                            dados.optString("nome_empresa"));

                    etRelEmailEmpresa.setText(
                            dados.optString("e_mail_empresa"));

                    etRelSetorEmpresa.setText(
                            dados.optString("setor_empresa"));

                    etRelDescricao.setText(
                            dados.optString("descricao"));

                    etRelRoteiroPitch.setText(
                            dados.optString("roteiro_pitch"));

                    etRelIntegrante1.setText(
                            dados.optString("integrante1"));

                    etRelIntegrante2.setText(
                            dados.optString("integrante2"));

                    etRelIntegrante3.setText(
                            dados.optString("integrante3"));

                    etRelIntegrante4.setText(
                            dados.optString("integrante4"));

                    etRelIntegrante5.setText(
                            dados.optString("integrante5"));

                    etRelOrientador.setText(
                            dados.optString("orientador"));

                    etRelCoorientador.setText(
                            dados.optString("coorientador"));

                    etRelNomeProjeto.setText(
                            dados.optString("nome_projeto"));

                    etRelNomeEquipe.setText(
                            dados.optString("nome_equipe"));

                    etRelAreaAtuProjeto.setText(
                            dados.optString("area_atuacao_projeto"));

                    etRelAreaAtuCurso.setText(
                            dados.optString("area_atuacao_curso"));

                    etRelUnidadeSenai.setText(
                            dados.optString("unidade_senai"));

                    etRelGestor.setText(
                            dados.optString("gestor"));

                    etRelFerramentaIA.setText(
                            dados.optString("ferramenta_ia"));

                    etRelLinkAcesso.setText(
                            dados.optString("link_acesso"));

                    etRelLicenca.setText(
                            dados.optString("licenca"));

                    etRelEtapaUso.setText(
                            dados.optString("etapa_de_usu"));

                    etRelPrompt.setText(
                            dados.optString("prompt"));

                    etRelMotivoUso.setText(
                            dados.optString("motivo_usu"));

                    etRelFerramentasProj.setText(
                            dados.optString("ferramentas_projeto"));

                    etRelEquipamentosProj.setText(
                            dados.optString("equipamentos_projeto"));

                    etRelQuantCompra.setText(
                            dados.optString("quant_compra"));

                    etRelQuantUtilizada.setText(
                            dados.optString("quant_utilizada"));

                    etRelPreco.setText(
                            dados.optString("preco"));

                    etRelFornecedor.setText(
                            dados.optString("fornecedor"));

                    etRelModoObtencao.setText(
                            dados.optString("modo_obtencao"));

                    etRelProcessamento.setText(
                            dados.optString("processamento"));

                    etRelAlternativaUso.setText(
                            dados.optString("alternativa_de_uso"));

                    etRelQuantUtilizada2.setText(
                            dados.optString("quant_utilizada_2"));

                    etRelFormaPagamento.setText(
                            dados.optString("forma_pagamento"));

                    etRelPrecoTotal.setText(
                            dados.optString("preco_total"));

                }


            }

            @Override
            public void onErro(String erro) {

            }
        });
    }

    private void configurarCliques() {
        tabEquipe.setOnClickListener(v -> alternarFormulario(formEquipe, tabEquipe));
        tabConhecimentos.setOnClickListener(v -> alternarFormulario(formConhecimentos, tabConhecimentos));
        tabRecursos.setOnClickListener(v -> alternarFormulario(formRecursos, tabRecursos));
        tabCronograma.setOnClickListener(v -> alternarFormulario(formCronograma, tabCronograma));
        tabCanva.setOnClickListener(v -> alternarFormulario(formCanva, tabCanva));
        tabEmpresa.setOnClickListener(v -> alternarFormulario(formEmpresa, tabEmpresa));
        tabPitch.setOnClickListener(v -> alternarFormulario(formPitch, tabPitch));
        tabIA.setOnClickListener(v -> alternarFormulario(formIA, tabIA));
        tabPlanilha.setOnClickListener(v -> alternarFormulario(formPlanilha, tabPlanilha));
        tabComplementares.setOnClickListener(v -> alternarFormulario(formComplementares, tabComplementares));
        tabCompletude.setOnClickListener(v -> alternarFormulario(formCompletude, tabCompletude));
        tabRelatorio.setOnClickListener(v -> alternarFormulario(formRelatorio, tabRelatorio));
    }

    private void alternarFormulario(LinearLayout formAtivo, TextView tabAtiva) {
        formEquipe.setVisibility(View.GONE);
        formConhecimentos.setVisibility(View.GONE);
        formRecursos.setVisibility(View.GONE);
        formCronograma.setVisibility(View.GONE);
        formCanva.setVisibility(View.GONE);
        formEmpresa.setVisibility(View.GONE);
        formPitch.setVisibility(View.GONE);
        formIA.setVisibility(View.GONE);
        formPlanilha.setVisibility(View.GONE);
        formComplementares.setVisibility(View.GONE);
        formCompletude.setVisibility(View.GONE);
        formRelatorio.setVisibility(View.GONE);

        formAtivo.setVisibility(View.VISIBLE);
        destacarAba(tabAtiva);
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
        pbVideoBuffer.setVisibility(View.VISIBLE);
        btnPlayPause.setVisibility(View.GONE);
        tvVideoStatus.setText("Pitch em vídeo enviado!");

        Uri videoUri = Uri.parse(url);
        vvPitch.setVideoURI(videoUri);

        vvPitch.setOnPreparedListener(mp -> {
            pbVideoBuffer.setVisibility(View.GONE);
            btnPlayPause.setVisibility(View.VISIBLE);
            videoSeekBar.setMax(vvPitch.getDuration());
            atualizarProgressoSeekBar();
        });

        vvPitch.setOnInfoListener((mp, what, extra) -> {
            if (what == 701) { // MEDIA_INFO_BUFFERING_START
                pbVideoBuffer.setVisibility(View.VISIBLE);
            } else if (what == 702) { // MEDIA_INFO_BUFFERING_END
                pbVideoBuffer.setVisibility(View.GONE);
            }
            return false;
        });
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
            } else if (view instanceof android.widget.Spinner) {
                view.setEnabled(habilitado);
                view.setAlpha(habilitado ? 1.0f : 0.6f);
            } else if (view instanceof android.widget.Button && view.getId() != R.id.btnEditarDados) {
                if (view.getId() == R.id.btnGerarRelatorio || view.getId() == R.id.btnVisualizarRelatorio || view.getId() == R.id.btnAcaoCanvaRelatorio) {
                    view.setEnabled(true);
                    view.setAlpha(1.0f);
                } else {
                    view.setEnabled(habilitado);
                    view.setAlpha(habilitado ? 1.0f : 0.5f);
                }
            } else if (view instanceof android.widget.FrameLayout || view instanceof android.widget.LinearLayout) {
                // Se houver containers internos (ex: controles de vídeo), pode ser necessário tratar recursivamente ou desabilitar o container
                view.setEnabled(habilitado);
            }
        }
    }

    private void atualizarTodosFormularios(boolean habilitado) {
        definirCamposEditaveis(formEquipe, habilitado);
        definirCamposEditaveis(formConhecimentos, habilitado);
        definirCamposEditaveis(formRecursos, habilitado);
        definirCamposEditaveis(formCronograma, habilitado);
        definirCamposEditaveis(formCanva, habilitado);
        definirCamposEditaveis(formEmpresa, habilitado);
        definirCamposEditaveis(formPitch, habilitado);
        definirCamposEditaveis(formIA, habilitado);
        definirCamposEditaveis(formPlanilha, habilitado);
        definirCamposEditaveis(formComplementares, habilitado);
        definirCamposEditaveis(formCompletude, habilitado);
        definirCamposEditaveis(formRelatorio, habilitado);
    }

    private void destacarAba(TextView tabAtiva) {
        TextView[] todasAbas = {tabEquipe, tabConhecimentos, tabRecursos, tabCronograma, tabCanva, tabEmpresa, tabPitch, tabIA, tabPlanilha, tabComplementares, tabCompletude, tabRelatorio};
        for (TextView tab : todasAbas) {
            tab.setAlpha(0.5f);
        }
        tabAtiva.setAlpha(1.0f);
    }

    private void abrirPdf(String url) {
        if (url == null || url.isEmpty()) return;
        urlRelatorioPdf = url;
        btnVisualizarRelatorio.setVisibility(View.VISIBLE);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nenhum visualizador de PDF encontrado", Toast.LENGTH_SHORT).show();
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}