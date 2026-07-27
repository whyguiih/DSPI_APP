package com.example.dspi_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CadastroActivity extends AppCompatActivity {

    private EditText inputNome;
    private EditText inputEmailCadastro;
    private EditText inputSenhaCadastro;
    private EditText inputConfirmaSenha;

    private AppCompatButton btnCadastrar;
    private TextView txtFazerLogin;
    private AutoCompleteTextView inputNivelAcesso;

    private CadastroRepository cadastroRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Campos
        inputNome = findViewById(R.id.inputNome);
        inputEmailCadastro = findViewById(R.id.inputEmailCadastro);
        inputSenhaCadastro = findViewById(R.id.inputSenhaCadastro);
        inputConfirmaSenha = findViewById(R.id.inputConfirmaSenha);
        ImageView btnVoltar = findViewById(R.id.btnVoltar);

        // Botões
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnCadastrar.setOnClickListener(v -> cadastrar());

        btnVoltar.setOnClickListener(v -> {
            finish();
        });

        inputNivelAcesso = findViewById(R.id.inputNivelAcesso);

        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        String nivelLogado = prefs.getString("nivel_de_acesso", "");

        String[] niveis;
        if ("3".equals(nivelLogado)) {
            niveis = new String[]{"Aluno"};
            inputNivelAcesso.setText("Aluno", false);
        } else {
            niveis = new String[]{
                    "Avaliador",
                    "DR/DN",
                    "Professor",
                    "Empresa",
                    "Aluno",
                    "Público Externo"
            };
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                niveis
        );

        inputNivelAcesso.setAdapter(adapter);

        inputNivelAcesso.setOnClickListener(v -> {
            if (!"3".equals(nivelLogado)) {
                inputNivelAcesso.showDropDown();
            }
        });

        cadastroRepository = new CadastroRepository(this);
    }

    private void cadastrar() {

        String nome_usuarios = inputNome.getText().toString().trim();
        String email = inputEmailCadastro.getText().toString().trim();
        String senha = inputSenhaCadastro.getText().toString().trim();
        String confirmarSenha = inputConfirmaSenha.getText().toString().trim();

        String nivelTexto = inputNivelAcesso.getText().toString().trim();

        int nivel_de_acesso = 0;

        switch (nivelTexto) {
            case "Avaliador":
                nivel_de_acesso = 1;
                break;

            case "DR/DN":
                nivel_de_acesso = 2;
                break;

            case "Professor":
                nivel_de_acesso = 3;
                break;

            case "Empresa":
                nivel_de_acesso = 4;
                break;

            case "Aluno":
                nivel_de_acesso = 5;
                break;

            case "Público Externo":
                nivel_de_acesso = 6;
                break;
        }

        if (TextUtils.isEmpty(nome_usuarios)) {
            mostrarMensagemGrande("Campo Obrigatório", "Por favor, digite seu nome completo.", false);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            mostrarMensagemGrande("Campo Obrigatório", "Por favor, digite seu e-mail.", false);
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            mostrarMensagemGrande("Campo Obrigatório", "Por favor, digite uma senha.", false);
            return;
        }

        if (TextUtils.isEmpty(confirmarSenha)) {
            mostrarMensagemGrande("Campo Obrigatório", "Por favor, confirme sua senha.", false);
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarMensagemGrande("Erro", "As senhas digitadas não coincidem.", false);
            return;
        }

        if (nivel_de_acesso == 0) {
            mostrarMensagemGrande("Campo Obrigatório", "Por favor, selecione um nível de acesso.", false);
            return;
        }

        // Apenas para teste
        cadastroRepository.cadastrar(
                nome_usuarios,
                email,
                senha,
                nivel_de_acesso,
                new CadastroRepository.CadastroListener() {

                    @Override
                    public void onSucesso(String mensagem) {
                        mostrarMensagemGrande("Sucesso", mensagem, true);
                    }

                    @Override
                    public void onErro(String erro) {
                        mostrarMensagemGrande("Erro no Cadastro", erro, false);
                    }

                });

        // Na próxima etapa vamos enviar esses dados ao banco.
    }

    private void mostrarMensagemGrande(String titulo, String mensagem, boolean finalizar) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (finalizar) finish();
                })
                .setIcon(finalizar ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
                .show();
    }
}