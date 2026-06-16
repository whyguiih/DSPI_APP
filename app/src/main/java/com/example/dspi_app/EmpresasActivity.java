package com.example.dspi_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class EmpresasActivity extends AppCompatActivity {
    private final int CURRENT_TAB_INDEX = 3; // 3 = Empresas

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

        // Chamada da função que carrega a lista do banco de dados
        carregarListaDeEmpresas();
    }

    private void carregarListaDeEmpresas() {
        LinearLayout listaEmpresasLayout = findViewById(R.id.listaEmpresasLayout);
        listaEmpresasLayout.removeAllViews(); // Limpa antes de preencher

        try {
            // OBS: Se você usa uma classe DatabaseHelper específica, substitua a linha abaixo!
            // Exemplo: SQLiteDatabase db = new MeuDatabaseHelper(this).getReadableDatabase();
            // Estou abrindo direto baseado no nome usado no seu arquivo SQL.
            SQLiteDatabase db = openOrCreateDatabase("db_dspi", MODE_PRIVATE, null);

            // Consulta cruzando tb_empresas com tb_cadastros para pegar as contas Nível 4
            String query = "SELECT e.nome_empresa, e.endereco, e.foto_perfil " +
                    "FROM tb_empresas e ";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String nome = cursor.getString(0);
                    String endereco = cursor.getString(1);
                    String fotoPerfil = cursor.getString(2);

                    // Inflar (criar) uma cópia do nosso item_empresa.xml
                    View itemEmpresa = getLayoutInflater().inflate(R.layout.item_empresa, listaEmpresasLayout, false);

                    TextView txtNome = itemEmpresa.findViewById(R.id.txtNomeEmpresa);
                    TextView txtEndereco = itemEmpresa.findViewById(R.id.txtEnderecoEmpresa);
                    ImageView imgEmpresa = itemEmpresa.findViewById(R.id.imgEmpresa);

                    // Setar os textos
                    txtNome.setText(nome);
                    txtEndereco.setText(endereco != null ? endereco : "Endereço não informado");

                    // Truque para carregar a imagem dinamicamente a partir do caminho (ex: '/drawable/threeeo.png')
                    if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                        // Limpa o caminho do banco deixando apenas o nome, ex: "threeeo"
                        String nomeImagem = fotoPerfil.replace("/drawable/", "").replace(".png", "").replace(".jpg", "");

                        // Busca o ID do recurso na pasta drawable
                        int resourceId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());

                        if (resourceId != 0) {
                            imgEmpresa.setImageResource(resourceId);
                        }
                    }

                    // Adicionar na tela
                    listaEmpresasLayout.addView(itemEmpresa);

                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

        } catch (Exception e) {
            e.printStackTrace();
            // Em caso de erro, a lista simplesmente não carregará e não dará crash.
        }
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