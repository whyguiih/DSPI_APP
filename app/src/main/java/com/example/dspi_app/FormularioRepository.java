package com.example.dspi_app;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class FormularioRepository {

    private final Context context;
    private final String BASE_URL = "https://api-dspi.whyguiih.workers.dev";

    public interface OnDadosCarregadosListener {
        void onSucesso(JSONObject dados);
        void onNaoEncontrado();
        void onErro(String erro);
    }

    // Nova interface para salvar
    public interface OnSalvoListener {
        void onSucesso();
        void onErro(String erro);
    }

    public FormularioRepository(Context context) {
        this.context = context;
    }

    private String getEmailUsuario() {
        return context.getSharedPreferences("SESSAO_USER", Context.MODE_PRIVATE)
                .getString("email_logado", "");
    }

    // =========================================================
    // CARREGAR
    // =========================================================
    public void carregarDados(String tipo, OnDadosCarregadosListener listener) {
        String emailUsuario = getEmailUsuario();
        if (emailUsuario.isEmpty()) {
            listener.onErro("Usuário não autenticado.");
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("usuario", emailUsuario);
            jsonBody.put("tipo", tipo);
        } catch (JSONException e) {
            listener.onErro("Erro ao montar requisição.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/buscar-dados",
                jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            if (response.getBoolean("existe")) {
                                JSONObject data = response.optJSONObject("data");
                                listener.onSucesso(data != null ? data : response);
                            } else {
                                listener.onNaoEncontrado();
                            }
                        } else {
                            listener.onErro(response.optString("error", "Erro na API"));
                        }
                    } catch (JSONException e) {
                        listener.onErro("Erro ao processar resposta.");
                    }
                },
                error -> listener.onErro("Falha na conexão com o servidor.")
        );

        Volley.newRequestQueue(context).add(request);
    }

    // =========================================================
    // SALVAR — agora avisa via callback, sem Toast próprio
    // =========================================================
    public void salvarDados(String tipo, JSONObject jsonCampos, OnSalvoListener listener) {
        String emailUsuario = getEmailUsuario();
        if (emailUsuario.isEmpty()) {
            if (listener != null) listener.onErro("Usuário não autenticado.");
            return;
        }

        try {
            jsonCampos.put("usuario", emailUsuario);
            jsonCampos.put("tipo", tipo);
        } catch (JSONException e) {
            if (listener != null) listener.onErro("Erro ao montar requisição.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/salvar-dados",
                jsonCampos,
                response -> {
                    if (listener != null) listener.onSucesso();
                },
                error -> {
                    if (listener != null) listener.onErro("Falha ao salvar: " + tipo);
                }
        );

        Volley.newRequestQueue(context).add(request);
    }
}