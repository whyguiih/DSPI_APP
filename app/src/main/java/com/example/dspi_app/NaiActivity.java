package com.example.dspi_app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NaiActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;
    private ImageButton btnSend;
    private ScrollView chatScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nai);

        // 1. Vincular componentes da interface
        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatScrollView = findViewById(R.id.chatScrollView);

        // 2. Configurar o Menu Inferior (Bottom Nav)
        // Certifique-se de que o método no seu ConfiguradorMenu suporta a seleção da MIA
        // Exemplo: Substitua o ID abaixo pelo ID correto do ícone da MIA no seu bottom_nav.xml
        ConfiguradorMenu configurador = new ConfiguradorMenu(this);
        // configurador.configurar(R.id.nav_mia); // Descomente e ajuste conforme a sua lógica do ConfiguradorMenu

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
            textView.setBackgroundResource(R.drawable.bg_active_bubble); // Usa seu drawable de botão ativo
            params.setMarginStart(100); // Evita que grude do lado esquerdo
        } else {
            // Estilo para a MIA (Alinhado à esquerda)
            params.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bg_glass); // Usa o fundo de vidro padrão
            params.setMarginEnd(100); // Evita que grude do lado direito
        }

        textView.setLayoutParams(params);
        chatContainer.addView(textView);

        // Faz o Scroll rolar automaticamente para a mensagem mais recente
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }
}