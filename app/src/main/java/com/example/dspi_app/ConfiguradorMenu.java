package com.example.dspi_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

public class ConfiguradorMenu {

    public static void ativar(Activity activity, String nivel, int currentTabIndex) {

        SharedPreferences prefs = activity.getSharedPreferences("SESSAO_USER", Context.MODE_PRIVATE);
        long expiracaoAtual = prefs.getLong("tempo_expiracao", 0);
        if (expiracaoAtual > 0) {
            prefs.edit().putLong("tempo_expiracao", System.currentTimeMillis() + (10 * 60 * 1000)).apply();
        }

        if (nivel == null || nivel.isEmpty()) {
            nivel = activity.getIntent().getStringExtra("nivel_de_acesso");
        }

        final String nivelFinal = (nivel != null) ? nivel : "6";

        activity.findViewById(R.id.btnInicio).setOnClickListener(v ->
                navegarPara(activity, MainActivity.class, currentTabIndex, nivelFinal)
        );

        activity.findViewById(R.id.btnProjetos).setOnClickListener(v ->
                navegarPara(activity, ProjetosActivity.class, currentTabIndex, nivelFinal)
        );

        View btnNai = activity.findViewById(R.id.btnNai);
        if (nivelFinal.equals("6") || nivelFinal.equals("4")) {
            btnNai.setAlpha(0.4f);
            btnNai.setOnClickListener(v -> {
                Toast.makeText(activity, "Acesso Negado à área Mia.", Toast.LENGTH_SHORT).show();
            });
        } else {
            btnNai.setAlpha(1.0f);
            btnNai.setOnClickListener(v ->
                    navegarPara(activity, NaiActivity.class, currentTabIndex, nivelFinal)
            );
        }

        activity.findViewById(R.id.btnConta).setOnClickListener(v ->
                navegarPara(activity, ContaActivity.class, currentTabIndex, nivelFinal)
        );

        View btnEmpresas = activity.findViewById(R.id.btnEmpresas);
        // Todos os usuários agora têm acesso às Empresas
        btnEmpresas.setAlpha(1.0f);
        btnEmpresas.setOnClickListener(v ->
                navegarPara(activity, EmpresasActivity.class, currentTabIndex, nivelFinal)
        );

    }

    private static void navegarPara(Activity activity, Class<?> targetActivity, int currentTabIndex, String nivel) {
        if (activity.getClass() == targetActivity) return;

        Intent intent = new Intent(activity, targetActivity);
        intent.putExtra("OLD_TAB_INDEX", currentTabIndex);
        intent.putExtra("nivel_de_acesso", nivel);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }
}