package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

        // Botões
        btnCadastrar = findViewById(R.id.btnCadastrar);
        txtFazerLogin = findViewById(R.id.txtFazerLogin);

        btnCadastrar.setOnClickListener(v -> cadastrar());

        txtFazerLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        inputNivelAcesso = findViewById(R.id.inputNivelAcesso);

        String[] niveis = {
                "Adm",
                "DH",
                "Professor",
                "Empresa",
                "Aluno",
                "Público Geral"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                niveis
        );

        inputNivelAcesso.setAdapter(adapter);

        inputNivelAcesso.setOnClickListener(v ->
                inputNivelAcesso.showDropDown());

        cadastroRepository = new CadastroRepository(this);
    }

    private void cadastrar() {

        String nome = inputNome.getText().toString().trim();
        String email = inputEmailCadastro.getText().toString().trim();
        String senha = inputSenhaCadastro.getText().toString().trim();
        String confirmarSenha = inputConfirmaSenha.getText().toString().trim();

        String nivelTexto = inputNivelAcesso.getText().toString().trim();

        int nivel = 0;

        switch (nivelTexto) {
            case "Adm":
                nivel = 1;
                break;

            case "DH":
                nivel = 2;
                break;

            case "Professor":
                nivel = 3;
                break;

            case "Empresa":
                nivel = 4;
                break;

            case "Aluno":
                nivel = 5;
                break;

            case "Público Geral":
                nivel = 6;
                break;
        }

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

        if (TextUtils.isEmpty(senha)) {
            inputSenhaCadastro.setError("Digite sua senha.");
            inputSenhaCadastro.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmarSenha)) {
            inputConfirmaSenha.setError("Confirme sua senha.");
            inputConfirmaSenha.requestFocus();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            inputConfirmaSenha.setError("As senhas não coincidem.");
            inputConfirmaSenha.requestFocus();
            return;
        }

        if (nivel == 0) {
            inputNivelAcesso.setError("Selecione um nível de acesso.");
            inputNivelAcesso.requestFocus();
            return;
        }

        // Apenas para teste
        cadastroRepository.cadastrar(
                nome,
                email,
                senha,
                nivel,
                new CadastroRepository.CadastroListener() {

                    @Override
                    public void onSucesso(String mensagem) {

                        Toast.makeText(
                                CadastroActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();

                        finish();

                    }

                    @Override
                    public void onErro(String erro) {

                        Toast.makeText(
                                CadastroActivity.this,
                                erro,
                                Toast.LENGTH_LONG
                        ).show();

                    }

                });

        // Na próxima etapa vamos enviar esses dados ao banco.
    }
}