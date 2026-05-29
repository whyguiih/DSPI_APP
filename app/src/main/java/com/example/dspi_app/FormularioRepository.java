package com.example.dspi_app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class FormularioRepository {

    private final Context context;
    private final String BASE_URL = "https://api-dspi.whyguiih.workers.dev";

    // Garanta que está exatamente como "public static interface"
    public static interface OnDadosCarregadosListener {
        void onSucesso(JSONObject dados);
        void onNaoEncontrado();
        void onErro(String erro);
    }

    public FormularioRepository(Context context) {
        this.context = context;
    }

    // Método interno para pegar o e-mail logado da SharedPreferences
    private String getEmailUsuario() {
        return context.getSharedPreferences("SESSAO_USER", Context.MODE_PRIVATE)
                .getString("email_logado", "");
    }

    // =========================================================================
    // 👥 PARTE 1: MÉTODOS DA TABELA EQUIPE
    // =========================================================================

    public void carregarEquipe(OnDadosCarregadosListener listener) {
        String emailUsuario = getEmailUsuario();
        if (emailUsuario.isEmpty()) {
            listener.onErro("Usuário não autenticado.");
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("usuario", emailUsuario);
        } catch (JSONException e) {
            listener.onErro("Erro ao montar requisição.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/buscar-equipe",
                jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            if (response.getBoolean("existe")) {
                                listener.onSucesso(response.getJSONObject("data"));
                            } else {
                                listener.onNaoEncontrado();
                            }
                        } else {
                            listener.onErro(response.optString("error", "Erro na API"));
                        }
                    } catch (JSONException e) {
                        listener.onErro("Erro ao processar resposta do banco.");
                    }
                },
                error -> listener.onErro("Falha na conexão com o servidor.")
        );

        Volley.newRequestQueue(context).add(request);
    }

    public void salvarEquipe(
            String nomeEquipe, String nomeProjeto, String email,
            String areaCurso, String areaProjeto, String orientador, String coorientador,
            String int1, String int2, String int3, String int4, String int5) {

        String emailUsuario = getEmailUsuario();
        if (emailUsuario.isEmpty()) {
            Toast.makeText(context, "Erro: Usuário não autenticado.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("usuario", emailUsuario);
            jsonBody.put("nome_equipe", nomeEquipe);
            jsonBody.put("nome_projeto", nomeProjeto);
            jsonBody.put("email", email);
            jsonBody.put("area_atuacao_curso", areaCurso);
            jsonBody.put("area_atuacao_projeto", areaProjeto);
            jsonBody.put("nome_orientador", orientador);
            jsonBody.put("nome_coorientador", coorientador);
            jsonBody.put("nome_integrante", int1);
            jsonBody.put("nome_integrante2", int2);
            jsonBody.put("nome_integrante3", int3);
            jsonBody.put("nome_integrante4", int4);
            jsonBody.put("nome_integrante5", int5);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BASE_URL + "/salvar-equipe",
                jsonBody,
                response -> Toast.makeText(context, "Equipe salva com sucesso!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(context, "Erro ao salvar dados da equipe.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(context).add(request);
    }


    // =========================================================================
    // 🧠 PARTE 2: MÉTODOS DA TABELA CONHECIMENTOS (Próxima que vamos fazer)
    // =========================================================================

    /*
    public void salvarConhecimentos(...) {
        // Quando criarmos, o código vai entrar aqui dentro desse mesmo arquivo!
    }

    public void carregarConhecimentos(OnDadosCarregadosListener listener) {
        // O carregador de conhecimentos entra aqui usando a mesma Interface genérica
    }
    */

}