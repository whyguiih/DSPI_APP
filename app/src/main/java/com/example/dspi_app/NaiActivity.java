package com.example.dspi_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class NaiActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;
    private ImageButton btnSend;
    private ScrollView chatScrollView;

    // Considerando que o ícone da MIA é o 3º no bottom_nav.xml (Index 2)
    private final int CURRENT_TAB_INDEX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuração para a interface respeitar os limites da tela (Notificações e Navegação)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_nai);

        // Aplica o padding para não invadir as barras do sistema
        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        // 1. Vincular componentes da interface
        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatScrollView = findViewById(R.id.chatScrollView);

        // 2. Configurar o Menu Inferior (Bottom Nav) e a Bolha Animada
        configurarBolhaAnimada();

        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        // 3. Ação de clique para enviar mensagem
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // Adiciona a mensagem do usuário na tela
                addMessage(message, true);
                etMessage.setText("");

                // Simulação simples de resposta da IA (MIA)
                chatContainer.postDelayed(() -> {
                    addMessage("Entendi! Estou processando as informações sobre os seus projetos. Como mais posso te ajudar?", false);
                }, 1000);
            }
        });

        // 4. Mensagem inicial de boas-vindas da MIA
        addMessage("Olá, sou a MIA! Sua inteligência artificial do Integra. Como posso te ajudar hoje?", false);
    }

    /**
     * Animação do botão ativo no menu inferior, idêntico à MainActivity
     */
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
                    activeBubble.animate()
                            .translationX(CURRENT_TAB_INDEX * tabWidth)
                            .setDuration(350)
                            .setInterpolator(new DecelerateInterpolator(1.5f))
                            .start();
                }
            });
        }
    }

    /**
     * Adiciona um "balão" de mensagem dinamicamente na tela
     */
    private void addMessage(String text, boolean isUser) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(15f);
        // Padding interno do balão
        textView.setPadding(40, 24, 40, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 16);

        if (isUser) {
            // Estilo para o Usuário (Alinhado à direita)
            params.gravity = Gravity.END;
            textView.setBackgroundResource(R.drawable.bg_active_bubble);
            params.setMarginStart(100);
        } else {
            // Estilo para a MIA (Alinhado à esquerda)
            params.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bg_glass);
            params.setMarginEnd(100);
        }

        textView.setLayoutParams(params);
        chatContainer.addView(textView);

        // Faz o Scroll rolar automaticamente para a mensagem mais recente
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }
}