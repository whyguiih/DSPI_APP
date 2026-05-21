package com.example.dspi_app;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class ConfiguradorMenu {

    public static void ativar(Activity activity, String nivel, int currentTabIndex) {

        // SE O NÍVEL VIER NULO DA INTENT, PEGAMOS O QUE JÁ ESTAVA NA ACTIVITY PARA NÃO PERDER
        if (nivel == null || nivel.isEmpty()) {
            nivel = activity.getIntent().getStringExtra("nivel_de_acesso");
        }

        // Garante uma String final para usar dentro dos cliques (Lambda do Java exige variável final ou efetivamente final)
        final String nivelFinal = (nivel != null) ? nivel : "6";

        // 1. Configura os botões normais de todas as páginas (INCLUINDO O INÍCIO)
        activity.findViewById(R.id.btnInicio).setOnClickListener(v ->
                navegarPara(activity, MainActivity.class, currentTabIndex, nivelFinal)
        );

        activity.findViewById(R.id.btnProjetos).setOnClickListener(v ->
                navegarPara(activity, ProjetosActivity.class, currentTabIndex, nivelFinal)
        );

        activity.findViewById(R.id.btnNai).setOnClickListener(v ->
                navegarPara(activity, NaiActivity.class, currentTabIndex, nivelFinal)
        );

        activity.findViewById(R.id.btnConta).setOnClickListener(v ->
                navegarPara(activity, ContaActivity.class, currentTabIndex, nivelFinal)
        );

        // 2. Aplica a Regra de Bloqueio do Nível 6 no botão Empresas
        View btnEmpresas = activity.findViewById(R.id.btnEmpresas);

        if (nivelFinal.equals("6")) {
            // Bloqueado para o nível 6
            btnEmpresas.setAlpha(0.4f);
            btnEmpresas.setOnClickListener(v -> {
                Toast.makeText(activity, "Acesso Negado.", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Liberado para os outros níveis
            btnEmpresas.setAlpha(1.0f);
            btnEmpresas.setOnClickListener(v ->
                    navegarPara(activity, EmpresasActivity.class, currentTabIndex, nivelFinal)
            );
        }
    }

    // Navegação centralizada e revisada
    private static void navegarPara(Activity activity, Class<?> targetActivity, int currentTabIndex, String nivel) {
        // Se o usuário já está na página que clicou, não faz nada
        if (activity.getClass() == targetActivity) return;

        Intent intent = new Intent(activity, targetActivity);
        intent.putExtra("OLD_TAB_INDEX", currentTabIndex);
        intent.putExtra("nivel_de_acesso", nivel); // Garante o envio do nível correto
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        activity.finish();
    }
}