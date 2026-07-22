package com.example.dspi_app;

import android.content.Context;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CadastroRepository {

    private static final String URL_API = "https://api-dspi.whyguiih.workers.dev/cadastro";

    public interface CadastroListener {
        void onSucesso(String mensagem);
        void onErro(String erro);
    }

    private final Context context;

    public CadastroRepository(Context context) {
        this.context = context;
    }

    public void cadastrar(String nome,
                          String email,
                          String senha,
                          int nivelAcesso,
                          CadastroListener listener) {

        new Thread(() -> {

            try {

                URL url = new URL(URL_API);

                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("POST");
                conexao.setRequestProperty("Content-Type", "application/json");
                conexao.setDoOutput(true);

                JSONObject json = new JSONObject();

                json.put("nome", nome);
                json.put("email", email);
                json.put("senha", senha);
                json.put("nivel_de_acesso", nivelAcesso);

                OutputStream os = conexao.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int codigo = conexao.getResponseCode();

                if (codigo == 200) {

                    ((CadastroActivity) context).runOnUiThread(() ->
                            listener.onSucesso("Cadastro realizado com sucesso!")
                    );

                } else {

                    ((CadastroActivity) context).runOnUiThread(() ->
                            listener.onErro("Código HTTP: " + codigo)
                    );

                }

                conexao.disconnect();

            } catch (Exception e) {

                ((CadastroActivity) context).runOnUiThread(() ->
                        listener.onErro(e.getMessage())
                );

            }

        }).start();

    }

}