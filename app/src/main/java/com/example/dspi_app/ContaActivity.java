package com.example.dspi_app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ContaActivity extends AppCompatActivity {

    // Índice 3 representa a aba "Conta" no Bottom Navigation (0=Inicio, 1=Projetos, 2=Nai, 3=Conta, 4=Empresas)
    private final int CURRENT_TAB_INDEX = 3;
    private String nivel;
    private String emailLogado;

    private String nomeUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajuste para preencher a tela toda (Edge to Edge)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_conta_usuario);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // Recuperar dados de sessão (nível de acesso, email, nome e foto)
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        nivel = getIntent().getStringExtra("nivel_de_acesso");
        if (nivel == null) {
            nivel = prefs.getString("nivel_de_acesso", "5");
        }
        boolean usuarioEmpresa = prefs.getBoolean("empresa_verificador", false);

        emailLogado = prefs.getString("email_logado", "usuario@email.com");

        // Configurar o menu inferior
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        LinearLayout btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        LinearLayout btnMeusProjetos = findViewById(R.id.btnMeusProjetos);
        LinearLayout btnMeuCurriculo = findViewById(R.id.btnMeuCurriculo);
        LinearLayout btnCadastro = findViewById(R.id.btnCadastrar);
        LinearLayout btnSair = findViewById(R.id.btnSair);
        LinearLayout btnNecessidades = findViewById(R.id.btnNecessidades);
        LinearLayout btnVerificarCurriculo = findViewById(R.id.btnVerificarCurriculo);

        // Esconder botões de Currículo se não for nível 5 (Candidato/Aluno)
        if ("5".equals(nivel)) {
            btnMeuCurriculo.setVisibility(View.VISIBLE);
            if (btnVerificarCurriculo != null) {
                btnVerificarCurriculo.setVisibility(View.VISIBLE);
            }
        } else {
            btnMeuCurriculo.setVisibility(View.GONE);
            if (btnVerificarCurriculo != null) {
                btnVerificarCurriculo.setVisibility(View.GONE);
            }
        }

        if (btnVerificarCurriculo != null) {
            btnVerificarCurriculo.setOnClickListener(v -> gerarCurriculoPDF());
        }

        if (usuarioEmpresa) {
            // Se for empresa, mostra o botão
            btnNecessidades.setVisibility(View.VISIBLE);

            // 3. Já deixamos o clique preparado para a Parte 2.0 (abrir a subpágina)
            btnNecessidades.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContaActivity.this, CadastroNecessidadesActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            btnNecessidades.setVisibility(View.GONE);
        }

        // Esconder o botão "Meus Projetos" para usuários de nível 6
        if ("6".equals(nivel) || "2".equals(nivel) || "1".equals(nivel) ) {
            btnMeusProjetos.setVisibility(View.GONE);
        }

// Esconder o botão "Criar Conta"
        if ("2".equals(nivel) || "3".equals(nivel)) {
            btnCadastro.setVisibility(View.VISIBLE);
        } else {
            btnCadastro.setVisibility(View.GONE);
        }

        // Configuração dos botões e subpáginas

        // 1. Botão Editar Perfil -> Leva ao PerfilActivity para alterar dados pessoais
        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(ContaActivity.this, PerfilActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("email_usuario", emailLogado);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // 2. Botão Meus Projetos -> Leva para a ProjetosActivity
        btnMeusProjetos.setOnClickListener(v -> {
            Intent intent = new Intent(ContaActivity.this, ProjetosActivity.class);
            intent.putExtra("nivel_de_acesso", nivel);
            intent.putExtra("email_usuario", emailLogado);
            intent.putExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish(); // Fecha a tela de conta para não empilhar
        });

        btnMeuCurriculo.setOnClickListener(v -> {
            // Atenção aqui ao nome correto da SUA activity:
            Intent intent = new Intent(ContaActivity.this, CarregamentoCurriculoActivity.class);

            // Você precisa garantir que essas variáveis de fato têm o nome e email do usuário logado
            intent.putExtra("NOME_USUARIO", nomeUsuario);
            intent.putExtra("EMAIL_USUARIO", emailLogado);
            startActivity(intent);
        });

        btnCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(ContaActivity.this, CadastroActivity.class);
            startActivity(intent);
        });


            btnSair.setOnClickListener(v -> {
                // Limpa as SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("SESSAO_USER", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(ContaActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

    private void gerarCurriculoPDF() {
        Toast.makeText(this, "Sincronizando dados profissionais...", Toast.LENGTH_SHORT).show();
        String url = "https://api-dspi.whyguiih.workers.dev/preencher-curriculo";

        // Puxa os dados reais da conta do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        String emailSessao = prefs.getString("email_logado", "");
        String nomeSessao = prefs.getString("nome_usuario", "Usuário");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email_sessao", emailSessao); // Chave mestre para busca
            jsonBody.put("nome_usuario", nomeSessao);
            jsonBody.put("email_usuario", emailSessao);
        } catch (JSONException e) { e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Currículo pronto! Baixando...", Toast.LENGTH_SHORT).show();
                            baixarCurriculoNoAndroid(emailSessao);
                        } else {
                            mostrarErroGrande("Aviso do Servidor", response.optString("message"));
                        }
                    } catch (JSONException e) {
                        mostrarErroGrande("Erro de Processamento", "Houve uma falha ao ler a resposta do servidor.");
                    }
                },
                error -> {
                    String mensagemErro = "Não foi possível conectar ao servidor de nuvem.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            mensagemErro = data.optString("message", mensagemErro);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    mostrarErroGrande("Aviso", mensagemErro);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void baixarCurriculoNoAndroid(String identificador) {
        String codificado = Uri.encode(identificador);
        String urlPython = "https://avell.tailfdec8e.ts.net:8443/download-curriculo/" + codificado;
        android.util.Log.d("DOWNLOAD_DEBUG", "Solicitando PDF em: " + urlPython);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlPython));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Currículo Profissional");
        request.setDescription("Baixando currículo...");
        request.setMimeType("application/pdf");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String nomeFinal = "Curriculo_" + System.currentTimeMillis() + ".pdf";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nomeFinal);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Download iniciado! Verifique sua pasta de Downloads.", Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarErroGrande(String titulo, String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("Entendido", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("SESSAO_USER", MODE_PRIVATE);
        TextView txtNomeUsuario = findViewById(R.id.txtNomeUsuario);
        TextView txtEmailUsuario = findViewById(R.id.txtEmailUsuario);
        android.widget.ImageView imgAvatar = findViewById(R.id.imgAvatar);

        String nome = prefs.getString("nome_usuario", "Nome Completo");
        String email = prefs.getString("email_logado", "usuario@email.com");
        String foto = prefs.getString("foto_usuario", "");

        txtNomeUsuario.setText(nome);
        txtEmailUsuario.setText(email);

        // Conversão dinâmica de DP para Pixels para manter proporção perfeita
        int radiusPx = (int) (16 * getResources().getDisplayMetrics().density);

        if (!foto.isEmpty()) {
            try {
                if (foto.startsWith("http")) {
                    Glide.with(this)
                            .load(foto)
                            .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                            .into(imgAvatar);
                    imgAvatar.setPadding(0, 0, 0, 0);
                } else {
                    byte[] decodedString = android.util.Base64.decode(foto, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    if (decodedByte != null) {
                        Glide.with(this)
                                .load(decodedByte)
                                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                                .into(imgAvatar);
                        imgAvatar.setPadding(0, 0, 0, 0);
                    } else {
                        throw new Exception("Erro ao decodificar Bitmap");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Glide.with(this).load(R.drawable.ic_conta).transform(new CenterCrop(), new RoundedCorners(radiusPx)).into(imgAvatar);
                int innerPadding = (int) (14 * getResources().getDisplayMetrics().density);
                imgAvatar.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
            }
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_conta)
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .into(imgAvatar);

            // Garante que o ícone padrão fique centralizado dentro da moldura de vidro
            int innerPadding = (int) (14 * getResources().getDisplayMetrics().density);
            imgAvatar.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);
        }
    }
}