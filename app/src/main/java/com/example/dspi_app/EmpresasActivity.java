package com.example.dspi_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmpresasActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 3; // 3 = Empresas
    private final String BASE_URL = "https://api-dspi.whyguiih.workers.dev"; // A mesma API do seu repositório

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_empresas);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        configurarBolhaAnimada();

        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        // Dispara a busca das empresas lá na API
        carregarListaDeEmpresas();
    }

    private void carregarListaDeEmpresas() {
        LinearLayout listaEmpresasLayout = findViewById(R.id.listaEmpresasLayout);
        listaEmpresasLayout.removeAllViews(); // Limpa antes de preencher

        // Você precisará dessa rota na sua API lá no Cloudflare Worker
        String url = BASE_URL + "/listar-empresas";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.optBoolean("success")) {
                            JSONArray data = response.optJSONArray("data");
                            if (data != null) {
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject empresa = data.getJSONObject(i);

                                    // Pega exatamente as colunas da tb_empresas
                                    String nome = empresa.optString("nome_empresa", "Empresa Desconhecida");
                                    String endereco = empresa.optString("endereco", "Endereço não informado");
                                    String fotoPerfil = empresa.optString("foto_perfil", "");

                                    adicionarEmpresaNaTela(listaEmpresasLayout, nome, endereco, fotoPerfil);
                                }
                            }
                        } else {
                            Toast.makeText(this, "Nenhuma empresa encontrada.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao ler as empresas.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Falha na conexão com o servidor.", Toast.LENGTH_SHORT).show();
                }
        );

        // Adiciona a requisição na fila do Volley
        Volley.newRequestQueue(this).add(request);
    }

    private void adicionarEmpresaNaTela(LinearLayout container, String nome, String endereco, String fotoPerfil) {
        View itemEmpresa = getLayoutInflater().inflate(R.layout.item_empresa, container, false);

        TextView txtNome = itemEmpresa.findViewById(R.id.txtNomeEmpresa);
        TextView txtEndereco = itemEmpresa.findViewById(R.id.txtEnderecoEmpresa);
        ImageView imgEmpresa = itemEmpresa.findViewById(R.id.imgEmpresa);

        txtNome.setText(nome);
        txtEndereco.setText(endereco);

        // Tratamento da imagem conforme os dados cadastrados no banco (ex: "/drawable/threeeo.png")
        if (fotoPerfil != null && !fotoPerfil.isEmpty() && !fotoPerfil.equals("null")) {
            String nomeImagem = fotoPerfil.replace("/drawable/", "").replace(".png", "").replace(".jpg", "");
            int resourceId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
            if (resourceId != 0) {
                imgEmpresa.setImageResource(resourceId);
            }
        }

        container.addView(itemEmpresa);
    }

    private void configurarBolhaAnimada() {
        int oldTabIndex = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

        bottomNavLayout.post(() -> {
            float tabWidth = bottomNavLayout.getWidth() / 5f;
            activeBubble.getLayoutParams().width = (int) tabWidth;
            activeBubble.requestLayout();
            activeBubble.setTranslationX(oldTabIndex * tabWidth);
            if (oldTabIndex != CURRENT_TAB_INDEX) {
                activeBubble.animate().translationX(CURRENT_TAB_INDEX * tabWidth).setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
            }
        });
    }
}