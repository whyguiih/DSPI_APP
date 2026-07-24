package com.example.dspi_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CurriculoRepository {

    private static final String URL_API = "https://api-dspi.whyguiih.workers.dev/salvar-curriculo";
    private final Context context;

    public interface CurriculoListener {
        void onSucesso(String mensagem);
        void onErro(String erro);
    }

    public interface OnDadosCarregadosListener {
        void onSucesso(JSONObject dados);
        void onNaoEncontrado();
        void onErro(String erro);
    }

    public CurriculoRepository(Context context) {
        this.context = context;
    }

    public void salvar(String nome, String email, String dataNascimento, String telefone, String cidade,
                       String habilidade, String oQueFez, String projeto, String empresa,
                       String motivo, String aprendo, String prefiroTrabalhar,
                       CurriculoListener listener) {

        JSONObject json = new JSONObject();
        try {
            json.put("nome", nome);
            json.put("email", email);
            json.put("data_nascimento", dataNascimento);
            json.put("telefone", telefone);
            json.put("cidade", cidade);
            json.put("habilidades", habilidade);
            json.put("fez_projeto", oQueFez); // Sincronizado com body.fez_projeto na API
            json.put("projeto", projeto);
            json.put("empresa_vinculado", empresa); // Sincronizado com body.empresa_vinculado na API
            json.put("motivo_projeto", motivo); // Sincronizado com body.motivo_projeto na API
            json.put("aprendo_mais", aprendo); // Sincronizado com body.aprendo_mais na API
            json.put("prefiro_trabalhar", prefiroTrabalhar); // Sincronizado com body.prefiro_trabalhar na API
        } catch (JSONException e) {
            listener.onErro("Erro ao preparar dados: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_API, json,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            listener.onSucesso(response.optString("message", "Currículo salvo com sucesso!"));
                        } else {
                            listener.onErro(response.optString("message", "Erro ao salvar currículo."));
                        }
                    } catch (JSONException e) {
                        listener.onErro("Erro na resposta do servidor.");
                    }
                },
                error -> {
                    String detalhesErro = "Erro de rede";
                    if (error.networkResponse != null) {
                        detalhesErro = "Código HTTP: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(responseBody);
                            detalhesErro += "\nServidor: " + data.optString("message", "Sem mensagem");
                        } catch (Exception e) {
                            detalhesErro += "\nResposta bruta: " + new String(error.networkResponse.data);
                        }
                    } else if (error.getMessage() != null) {
                        detalhesErro = error.getMessage();
                    }
                    listener.onErro(detalhesErro);
                }
        );

        Volley.newRequestQueue(context).add(request);
    }

    public void carregarDados(String email, OnDadosCarregadosListener listener) {
        String url = "https://api-dspi.whyguiih.workers.dev/buscar-dados";
        JSONObject json = new JSONObject();
        try {
            json.put("usuario", email);
            json.put("tipo", "curriculo");
        } catch (JSONException e) {
            listener.onErro("Erro ao preparar busca: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            if (response.getBoolean("existe")) {
                                listener.onSucesso(response.getJSONObject("data"));
                            } else {
                                listener.onNaoEncontrado();
                            }
                        } else {
                            listener.onErro(response.optString("error", "Erro ao carregar dados."));
                        }
                    } catch (JSONException e) {
                        listener.onErro("Erro ao ler resposta: " + e.getMessage());
                    }
                },
                error -> listener.onErro("Falha de conexão: " + error.getMessage())
        );

        Volley.newRequestQueue(context).add(request);
    }
}
