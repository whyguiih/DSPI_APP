package com.example.dspi_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class EmpresasActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 3; // 3 = Empresas
    private final String BASE_URL = "https://api-dspi.whyguiih.workers.dev"; // Sua API

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

        carregarListaDeEmpresas();
    }

    private void carregarListaDeEmpresas() {
        LinearLayout listaEmpresasLayout = findViewById(R.id.listaEmpresasLayout);
        listaEmpresasLayout.removeAllViews();

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

                                    String nome = empresa.optString("nome_empresa", "Empresa Desconhecida");
                                    String cnpj = empresa.optString("cnpj", "");
                                    String telefone = empresa.optString("telefone_contato", "");
                                    String email = empresa.optString("email_contato", "");
                                    String endereco = empresa.optString("endereco", "");
                                    String fotoPerfil = empresa.optString("foto_perfil", "");
                                    String descricao = empresa.optString("descricao", "Nenhuma descrição disponível ainda.");
                                    String setor = empresa.optString("setor", "Não informado");

                                    adicionarEmpresaNaTela(listaEmpresasLayout, nome, cnpj, telefone, email, endereco, fotoPerfil, descricao, setor);
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
                error -> Toast.makeText(this, "Falha na conexão com o servidor.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void adicionarEmpresaNaTela(LinearLayout container, String nome, String cnpj, String telefone,
                                        String email, String endereco, String fotoPerfil, String descricao, String setor) {

        View itemEmpresa = getLayoutInflater().inflate(R.layout.item_empresa, container, false);

        TextView txtNome = itemEmpresa.findViewById(R.id.txtNomeEmpresa);
        TextView txtEndereco = itemEmpresa.findViewById(R.id.txtEnderecoEmpresa);
        ImageView imgEmpresa = itemEmpresa.findViewById(R.id.imgEmpresa);

        txtNome.setText(nome);
        txtEndereco.setText(endereco);

        // Define o raio do arredondamento (ex: 8dp convertidos para pixels)
        int radiusPx = (int) (8 * getResources().getDisplayMetrics().density);

        if (fotoPerfil != null && !fotoPerfil.isEmpty() && !fotoPerfil.equals("null")) {
            if (fotoPerfil.startsWith("http")) {
                // É um Link gerado pelo Cloudflare R2
                Glide.with(this)
                        .load(fotoPerfil)
                        .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                        .into(imgEmpresa);
            } else if (fotoPerfil.length() > 100) {
                // É um texto gigante de Base64 das contas antigas
                try {
                    byte[] decodedString = Base64.decode(fotoPerfil, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(this)
                            .load(decodedByte)
                            .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                            .into(imgEmpresa);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // É um nome de imagem dentro da pasta drawable local do app
                String nomeImagem = fotoPerfil.replace("/drawable/", "").replace(".png", "").replace(".jpg", "");
                int resourceId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
                if (resourceId != 0) {
                    Glide.with(this)
                            .load(resourceId)
                            .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                            .into(imgEmpresa);
                } else {
                    Glide.with(this)
                            .load(R.drawable.ic_empresas)
                            .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                            .into(imgEmpresa);
                }
            }
        } else {
            // Imagem padrão caso não tenha foto
            Glide.with(this)
                    .load(R.drawable.ic_empresas)
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .into(imgEmpresa);
        }

        itemEmpresa.setOnClickListener(v -> {
            Intent intent = new Intent(EmpresasActivity.this, DetalhesEmpresaActivity.class);
            intent.putExtra("nome_empresa", nome);
            intent.putExtra("cnpj", cnpj);
            intent.putExtra("telefone_contato", telefone);
            intent.putExtra("email_contato", email);
            intent.putExtra("endereco", endereco);
            intent.putExtra("foto_perfil", fotoPerfil);
            intent.putExtra("descricao", descricao);
            intent.putExtra("setor", setor);

            intent.putExtra("nivel_de_acesso", getIntent().getStringExtra("nivel_de_acesso"));
            startActivity(intent);
        });

        container.addView(itemEmpresa);
    }

    private void configurarBolhaAnimada() {
        int oldTabIndex = getIntent().getIntExtra("OLD_TAB_INDEX", CURRENT_TAB_INDEX);
        View activeBubble = findViewById(R.id.activeBubble);
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNavLayout);

        if (activeBubble != null && bottomNavLayout != null) {
            bottomNavLayout.post(() -> {
                float tabWidth = bottomNavLayout.getWidth() / 5f;
                activeBubble.getLayoutParams().width = (int) tabWidth;
                activeBubble.requestLayout();
                activeBubble.setTranslationX(oldTabIndex * tabWidth);
                if (oldTabIndex != CURRENT_TAB_INDEX) {
                    activeBubble.animate().translationX(CURRENT_TAB_INDEX * tabWidth)
                            .setDuration(350).setInterpolator(new DecelerateInterpolator(1.5f)).start();
                }
            });
        }
    }
}