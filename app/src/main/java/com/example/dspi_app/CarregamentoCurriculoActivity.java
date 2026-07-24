package com.example.dspi_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CarregamentoCurriculoActivity extends AppCompatActivity {

    private final int CURRENT_TAB_INDEX = 3; // Referência à aba de perfil
    private EditText inputNome;
    private EditText inputEmailCadastro;
    private EditText inputDataNascimento;
    private EditText inputTelefone;
    private EditText inputCidade;
    private EditText inputHabilidade;
    private EditText inputOQueFez;
    private EditText inputProjeto;
    private EditText inputEmpresa;
    private EditText inputMotivo;
    private EditText inputAprendo;
    private EditText inputPrefiroTrabalhar;

    private AppCompatButton btnCadastrar;

    private CurriculoRepository curriculoRepository;
    private String emailUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_curriculo);

        // Recupera o nível de acesso
        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        if (nivel == null || nivel.isEmpty()) {
            nivel = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("nivel_de_acesso", "");
        }

        // Bloqueio de segurança: APENAS nível 5 (Alunos) pode entrar.
        if (!"5".equals(nivel)) {
            finish();
            return;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Inicialização dos Campos com tratamento de erro
        try {
            inputNome = findViewById(R.id.inputNome);
            inputEmailCadastro = findViewById(R.id.inputEmailCadastro);
            inputDataNascimento = findViewById(R.id.inputDataNascimento);
            inputTelefone = findViewById(R.id.inputTelefone);
            inputCidade = findViewById(R.id.inputCidade);
            inputHabilidade = findViewById(R.id.inputHabilidade);
            inputOQueFez = findViewById(R.id.inputOQueFez);
            inputProjeto = findViewById(R.id.inputProjeto);
            inputEmpresa = findViewById(R.id.inputEmpresa);
            inputMotivo = findViewById(R.id.inputMotivo);
            inputAprendo = findViewById(R.id.inputAprendo);
            inputPrefiroTrabalhar = findViewById(R.id.inputPrefiroTrabalhar);

            btnCadastrar = findViewById(R.id.btnCadastrar);

            if (btnCadastrar != null) {
                btnCadastrar.setOnClickListener(v -> salvarCurriculo());
            }
        } catch (Exception e) {
            android.util.Log.e("ERROR_CURRICULO", "Erro ao vincular componentes", e);
            Toast.makeText(this, "Erro técnico na interface", Toast.LENGTH_SHORT).show();
        }

        curriculoRepository = new CurriculoRepository(this);

        emailUsuarioLogado = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).getString("email_logado", "");
        if (!emailUsuarioLogado.isEmpty()) {
            carregarDadosExistentes();
        }

        if (btnCadastrar != null) {
            btnCadastrar.setOnClickListener(v -> salvarCurriculo());
        }
    }

    private void carregarDadosExistentes() {
        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("CARREGANDO...");

        curriculoRepository.carregarDados(emailUsuarioLogado, new CurriculoRepository.OnDadosCarregadosListener() {
            @Override
            public void onSucesso(org.json.JSONObject dados) {
                btnCadastrar.setEnabled(true);
                btnCadastrar.setText("ATUALIZAR");

                inputNome.setText(dados.optString("nome", ""));
                inputEmailCadastro.setText(dados.optString("email", ""));
                inputDataNascimento.setText(dados.optString("data_nascimento", ""));
                inputTelefone.setText(dados.optString("telefone", ""));
                inputCidade.setText(dados.optString("cidade", ""));
                inputHabilidade.setText(dados.optString("habilidades", ""));
                inputOQueFez.setText(dados.optString("fez_projeto", ""));
                inputProjeto.setText(dados.optString("projeto", ""));
                inputEmpresa.setText(dados.optString("empresa_vinculado", ""));
                inputMotivo.setText(dados.optString("motivo_projeto", ""));
                inputAprendo.setText(dados.optString("aprendo_mais", ""));
                inputPrefiroTrabalhar.setText(dados.optString("prefiro_trabalhar", ""));
            }

            @Override
            public void onNaoEncontrado() {
                btnCadastrar.setEnabled(true);
                btnCadastrar.setText("CADASTRAR");
                // Preenche e-mail e nome básicos da sessão se for o primeiro acesso
                SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
                inputNome.setText(prefs.getString("nome_usuario", ""));
                inputEmailCadastro.setText(emailUsuarioLogado);
            }

            @Override
            public void onErro(String erro) {
                btnCadastrar.setEnabled(true);
                btnCadastrar.setText("CADASTRAR");
                android.util.Log.e("CURRICULO_AUTOCOMPLETE", erro);
            }
        });
    }

    private void salvarCurriculo() {
        String nome = inputNome.getText().toString().trim();
        String email = inputEmailCadastro.getText().toString().trim();
        String dataNasc = inputDataNascimento.getText().toString().trim();
        String telefone = inputTelefone.getText().toString().trim();
        String cidade = inputCidade.getText().toString().trim();
        String habilidade = inputHabilidade.getText().toString().trim();
        String oQueFez = inputOQueFez.getText().toString().trim();
        String projeto = inputProjeto.getText().toString().trim();
        String empresa = inputEmpresa.getText().toString().trim();
        String motivo = inputMotivo.getText().toString().trim();
        String aprendo = inputAprendo.getText().toString().trim();
        String prefiroTrabalhar = inputPrefiroTrabalhar.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            inputNome.setError("Digite seu nome.");
            inputNome.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            inputEmailCadastro.setError("Digite seu e-mail.");
            inputEmailCadastro.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(telefone)) {
            inputTelefone.setError("Digite seu Telefone.");
            inputTelefone.requestFocus();
            return;
        }

        // ... Outras validações se necessário ...

        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("SALVANDO...");

        curriculoRepository.salvar(
                nome, email, dataNasc, telefone, cidade, habilidade, oQueFez,
                projeto, empresa, motivo, aprendo, prefiroTrabalhar,
                new CurriculoRepository.CurriculoListener() {
                    @Override
                    public void onSucesso(String mensagem) {
                        Toast.makeText(CarregamentoCurriculoActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onErro(String erro) {
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText("CADASTRAR");

                        // Exibe um diálogo com o erro detalhado em vez de um Toast simples
                        new androidx.appcompat.app.AlertDialog.Builder(CarregamentoCurriculoActivity.this)
                                .setTitle("Erro ao Salvar")
                                .setMessage(erro != null ? erro : "Ocorreu um erro desconhecido no servidor.")
                                .setPositiveButton("OK", null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
        );
    }
}
