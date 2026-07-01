package com.example.dspi_app;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.util.Log;
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
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NaiActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText etMessage;
    private ImageButton btnSend;
    private ScrollView chatScrollView;

    private final int CURRENT_TAB_INDEX = 2; // Ícone da MIA

    // ==========================================
    // CONFIGURAÇÕES DO ANYTHING LLM
    // ==========================================
    private final String BASE_URL = "https://ought-debtor-reprocess.ngrok-free.dev";
    private final String API_KEY = "77SDBH4-8BEM47G-JTQCEAH-79PPHCT";
    private final String WORKSPACE_SLUG = "mia";
    // ==========================================

    private OkHttpClient client;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_nai);

        View mainLayout = findViewById(R.id.mainLayout);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        chatContainer = findViewById(R.id.chatContainer);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        chatScrollView = findViewById(R.id.chatScrollView);

        configurarBolhaAnimada();
        String nivel = getIntent().getStringExtra("nivel_de_acesso");
        ConfiguradorMenu.ativar(this, nivel, CURRENT_TAB_INDEX);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        mainHandler = new Handler(Looper.getMainLooper());

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                // 1. Cria a bolha do usuário formatada
                TextView userBubble = createMessageBubble(true);
                Spanned formattedUserText = HtmlCompat.fromHtml(formatMarkdownToHtml(message), HtmlCompat.FROM_HTML_MODE_COMPACT);
                userBubble.setText(formattedUserText);
                etMessage.setText("");
                scrollChatToBottom();

                // 2. Cria a bolha de "pensando..." da MIA
                TextView loadingBubble = createMessageBubble(false);
                startLoadingAnimation(loadingBubble);
                scrollChatToBottom();

                // 3. Envia requisição para a IA
                sendMessageToAnythingLLM(message, loadingBubble);
            }
        });

        // Mensagem inicial de boas-vindas da MIA
        TextView welcomeBubble = createMessageBubble(false);
        animateTypewriter(welcomeBubble, "Olá, sou a MIA! Sua inteligência artificial do Integra. Como posso te ajudar hoje?");
    }

    private void sendMessageToAnythingLLM(String userMessage, TextView loadingBubble) {
        String url = BASE_URL + "/api/v1/workspace/" + WORKSPACE_SLUG + "/chat";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("message", userMessage);
            jsonBody.put("mode", "chat");

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("accept", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("API_MIA", "Erro na requisição: " + e.getMessage());
                    mainHandler.post(() -> {
                        stopLoadingAnimation(loadingBubble);
                        loadingBubble.setText("Ops, estou com dificuldades de conexão no momento. 🥺");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);

                            String aiText = jsonResponse.optString("textResponse", "Erro ao obter resposta.");

                            mainHandler.post(() -> {
                                stopLoadingAnimation(loadingBubble);
                                animateTypewriter(loadingBubble, aiText);
                            });
                        } catch (Exception e) {
                            Log.e("API_MIA", "Erro no JSON: " + e.getMessage());
                            mainHandler.post(() -> {
                                stopLoadingAnimation(loadingBubble);
                                loadingBubble.setText("Desculpe, não entendi a resposta do servidor.");
                            });
                        }
                    } else {
                        mainHandler.post(() -> {
                            stopLoadingAnimation(loadingBubble);
                            loadingBubble.setText("Erro " + response.code() + ": " + response.message());
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextView createMessageBubble(boolean isUser) {
        TextView textView = new TextView(this);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(15f);
        textView.setPadding(40, 24, 40, 24);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 16);

        if (isUser) {
            params.gravity = Gravity.END;
            textView.setBackgroundResource(R.drawable.bg_active_bubble);
            params.setMarginStart(100);
        } else {
            params.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bg_glass);
            params.setMarginEnd(100);
        }

        textView.setLayoutParams(params);
        chatContainer.addView(textView);
        return textView;
    }

    private Runnable loadingRunnable;
    private void startLoadingAnimation(TextView loadingBubble) {
        loadingRunnable = new Runnable() {
            int dotCount = 0;
            @Override
            public void run() {
                dotCount++;
                if (dotCount > 3) dotCount = 1;

                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < dotCount; i++) dots.append(".");

                loadingBubble.setText(dots.toString());
                mainHandler.postDelayed(this, 400);
            }
        };
        mainHandler.post(loadingRunnable);
    }

    private void stopLoadingAnimation(TextView loadingBubble) {
        if (loadingRunnable != null) {
            mainHandler.removeCallbacks(loadingRunnable);
        }
        loadingBubble.setText("");
    }

    /**
     * Efeito de Máquina de Escrever corrigido:
     * Faz a bolha crescer dinamicamente enquanto preserva a formatação visual.
     */
    private void animateTypewriter(TextView textView, String fullText) {
        final long DELAY_MS = 20;

        // 1. Converte o texto Markdown para HTML e gera um objeto Spanned (que contém o texto + formatações)
        String htmlText = formatMarkdownToHtml(fullText);
        Spanned spannedText = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT);
        int totalChars = spannedText.length();

        Runnable typewriterRunnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (index <= totalChars) {
                    // O subSequence corta o texto até o index atual, mas CARREGA JUNTO
                    // as formatações (negrito, itálico) correspondentes a esse trecho.
                    // Isso faz a bolha expandir fisicamente a cada letra inserida.
                    CharSequence currentText = spannedText.subSequence(0, index);

                    textView.setText(currentText);
                    index++;
                    mainHandler.postDelayed(this, DELAY_MS);

                    scrollChatToBottom();
                }
            }
        };
        mainHandler.post(typewriterRunnable);
    }

    /**
     * Converte caracteres de formatação Markdown da IA para marcações HTML.
     */
    private String formatMarkdownToHtml(String text) {
        if (text == null) return "";

        text = text.replaceAll("(?s)```(.*?)```", "<br><tt>$1</tt><br>");
        text = text.replaceAll("(?s)\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        text = text.replaceAll("(?s)(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)", "<i>$1</i>");
        text = text.replaceAll("(?s)`(.*?)`", "<tt>$1</tt>");
        text = text.replaceAll("(?m)^- (.*)$", "&#8226; $1");
        text = text.replace("\n", "<br>");

        return text;
    }

    private void scrollChatToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
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
                    activeBubble.animate()
                            .translationX(CURRENT_TAB_INDEX * tabWidth)
                            .setDuration(350)
                            .setInterpolator(new DecelerateInterpolator(1.5f))
                            .start();
                }
            });
        }
    }
}