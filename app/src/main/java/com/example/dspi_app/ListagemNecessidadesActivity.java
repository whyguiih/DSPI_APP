package com.example.dspi_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListagemNecessidadesActivity extends AppCompatActivity {

    private RecyclerView recyclerNecessidades;
    private TextView txtListaVazia;
    private List<Necessidade> listaNecessidades;
    private NecessidadeAdapter adapter;
    private String emailEmpresaAlvo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Design Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_listagem_necessidades);

        View mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // 1. Resgatar o email da empresa que foi clicada na tela anterior
        emailEmpresaAlvo = getIntent().getStringExtra("email_empresa_alvo");

        // 2. Vincular elementos do XML
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        recyclerNecessidades = findViewById(R.id.recyclerNecessidades);
        txtListaVazia = findViewById(R.id.txtListaVazia);

        // Ação do Botão Voltar
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        // 3. Configurar o RecyclerView
        listaNecessidades = new ArrayList<>();
        recyclerNecessidades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NecessidadeAdapter(listaNecessidades);
        recyclerNecessidades.setAdapter(adapter);

        // 4. Buscar os dados no banco
        if (emailEmpresaAlvo != null && !emailEmpresaAlvo.isEmpty()) {
            buscarNecessidades();
        } else {
            Toast.makeText(this, "Erro: E-mail da empresa não identificado.", Toast.LENGTH_SHORT).show();
            mostrarListaVazia();
        }
    }

    private void buscarNecessidades() {
        String url = "https://api-dspi.whyguiih.workers.dev/buscar-dados";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("tipo", "necessidades");
            jsonBody.put("usuario", emailEmpresaAlvo); // Enviamos o e-mail da empresa!
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray dadosArray = response.getJSONArray("dados");
                            listaNecessidades.clear();

                            // Percorre todos os itens que vieram do banco
                            for (int i = 0; i < dadosArray.length(); i++) {
                                JSONObject obj = dadosArray.getJSONObject(i);
                                String nome = obj.getString("nome");
                                String descricao = obj.getString("descricao");

                                listaNecessidades.add(new Necessidade(nome, descricao));
                            }

                            // Avisa a lista que os dados chegaram ou exibe mensagem de vazio
                            if (listaNecessidades.isEmpty()) {
                                mostrarListaVazia();
                            } else {
                                txtListaVazia.setVisibility(View.GONE);
                                recyclerNecessidades.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            mostrarListaVazia();
                        }
                    } catch (JSONException e) {
                        Log.e("ListagemNec", "Erro ao ler JSON", e);
                        mostrarListaVazia();
                    }
                },
                error -> {
                    Log.e("ListagemNec", "Erro de conexão", error);
                    Toast.makeText(this, "Erro ao conectar com o servidor.", Toast.LENGTH_SHORT).show();
                    mostrarListaVazia();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); // Essencial para a API não crashar
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void mostrarListaVazia() {
        recyclerNecessidades.setVisibility(View.GONE);
        txtListaVazia.setVisibility(View.VISIBLE);
    }

    // ===================================================================
    // ADAPTER DO RECYCLERVIEW (Clonador de Layouts)
    // ===================================================================
    private static class NecessidadeAdapter extends RecyclerView.Adapter<NecessidadeAdapter.ViewHolder> {
        private final List<Necessidade> necessidades;

        public NecessidadeAdapter(List<Necessidade> necessidades) {
            this.necessidades = necessidades;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // "Infla" (Cria) a visualização usando o seu item_necessidade.xml
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_necessidade, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Coloca o texto correto no molde visual
            Necessidade nec = necessidades.get(position);
            holder.tvNome.setText(nec.getNome());
            holder.tvDescricao.setText(nec.getDescricao());
        }

        @Override
        public int getItemCount() {
            return necessidades.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNome, tvDescricao;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Vincula os IDs que você colocou no item_necessidade.xml
                tvNome = itemView.findViewById(R.id.tvItemNomeNecessidade);
                tvDescricao = itemView.findViewById(R.id.tvItemDescricaoNecessidade);
            }
        }
    }
}