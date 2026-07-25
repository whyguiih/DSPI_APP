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

    public void cadastrar(String nome_usuarios,
                          String email,
                          String senha,
                          int nivel_de_acesso,
                          CadastroListener listener) {

        new Thread(() -> {

            try {

                URL url = new URL(URL_API);

                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("POST");
                conexao.setRequestProperty("Content-Type", "application/json");
                conexao.setDoOutput(true);

                JSONObject json = new JSONObject();

                json.put("nome_usuarios", nome_usuarios);
                json.put("email", email);
                json.put("senha", senha);
                json.put("nivel_de_acesso", nivel_de_acesso);

                OutputStream os = conexao.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int codigo = conexao.getResponseCode();

                if (codigo == 200 || codigo == 201) {

                    ((CadastroActivity) context).runOnUiThread(() ->
                            listener.onSucesso("Cadastro realizado com sucesso!")
                    );

                } else {
                    // Tentar ler a mensagem de erro do servidor
                    java.io.InputStream is = conexao.getErrorStream();
                    String msgErro = "Código HTTP: " + codigo;
                    if (is != null) {
                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                        if (s.hasNext()) {
                            msgErro += " - " + s.next();
                        }
                    }
                    final String erroFinal = msgErro;
                    ((CadastroActivity) context).runOnUiThread(() ->
                            listener.onErro(erroFinal)
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