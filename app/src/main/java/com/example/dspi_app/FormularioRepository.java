package com.example.dspi_app;

import android.content.Context;
import android.net.Uri;
import org.json.JSONObject;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class FormularioRepository {

    private final Context context;
    private final String BASE_URL = "https://api-dspi.whyguiih.workers.dev";

    public interface OnUploadProgressListener {
        void onProgress(int progress);
        void onSucesso(String videoUrl);
        void onErro(String erro);
    }

    public interface OnSalvoListener {
        void onSucesso();
        void onErro(String erro);
    }

    public interface OnBuscaListener {
        void onSucesso(JSONObject dados);
        void onErro(String erro);
    }

    public interface OnListarDocumentosListener {
        void onSucesso(org.json.JSONArray documentos);
        void onErro(String erro);
    }

    // ================== LISTAR DOCUMENTOS ===================
    public void listarDocumentos(String usuario, OnListarDocumentosListener listener) {
        String url = BASE_URL + "/listar-documentos?usuario=" + usuario;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        listener.onErro(e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String resposta = response.body() != null ? response.body().string() : "";
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    try {
                        JSONObject json = new JSONObject(resposta);
                        if (json.optBoolean("success")) {
                            listener.onSucesso(json.optJSONArray("data"));
                        } else {
                            listener.onErro(json.optString("error", "Erro ao listar documentos"));
                        }
                    } catch (Exception e) {
                        listener.onErro("Erro ao processar lista: " + e.getMessage());
                    }
                });
            }
        });
    }

    //==================SALVAR FORMULARIO===============================
    public void salvarFormulario(String tipo, String usuario, JSONObject campos, OnSalvoListener listener) {

        try {
            String urlFinal = BASE_URL + "/salvar-dados";
            JSONObject body = new JSONObject();

            if (tipo.equals("feedback")) {
                urlFinal = BASE_URL + "/salvar-comentario";
                body.put("nome_equipe", usuario);
                body.put("comentario", campos.optString("comentario"));
            } else {
                body.put("tipo", tipo);
                body.put("usuario", usuario);
                body.put("campos", campos);
            }

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                    body.toString(),
                    okhttp3.MediaType.parse("application/json")
            );

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(urlFinal)
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            listener.onErro(e.getMessage()));

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String resposta = response.body() != null ? response.body().string() : "";
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        try {
                            // Proteção: Se não começar com '{', não é JSON, é um erro de rota/servidor
                            if (!resposta.trim().startsWith("{")) {
                                listener.onErro("O servidor retornou um erro inesperado: " + resposta);
                                return;
                            }
                            JSONObject json = new JSONObject(resposta);
                            if (json.optBoolean("success")) {
                                listener.onSucesso();
                            } else {
                                listener.onErro(json.optString("message", json.optString("error", "Erro na API")));
                            }
                        } catch (Exception e) {
                            listener.onErro("Erro ao ler resposta: " + e.getMessage());
                        }
                    });
                }

            });

        } catch (Exception e) {

            listener.onErro(e.getMessage());

        }

    }


    //==============BUSCAR FORMULARIO===================
    public void buscarFormulario(String tipo, String usuario, OnBuscaListener listener) {

        try {

            JSONObject body = new JSONObject();

            body.put("tipo", tipo);
            body.put("usuario", usuario);

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                    body.toString(),
                    okhttp3.MediaType.parse("application/json")
            );

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL + "/buscar-dados")
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {

                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            listener.onErro(e.getMessage()));

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {

                    String resposta = response.body() != null ? response.body().string() : "";

                    android.util.Log.e("API_RESPOSTA", resposta);

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {

                        try {
                            if (!resposta.trim().startsWith("{")) {
                                listener.onErro("Resposta do servidor inválida (não JSON): " + resposta);
                                return;
                            }

                            JSONObject json = new JSONObject(resposta);

                            if (json.optBoolean("success")) {
                                // Verifica se 'dados' é um objeto antes de converter
                                Object dadosObj = json.opt("dados");
                                if (dadosObj instanceof JSONObject) {
                                    listener.onSucesso((JSONObject) dadosObj);
                                } else {
                                    // Se 'dados' for nulo ou outro tipo, retorna um objeto vazio para não quebrar
                                    listener.onSucesso(new JSONObject());
                                }
                            } else {
                                String erroMsg = json.optString("error", json.optString("message", "Erro ao buscar dados"));
                                listener.onErro(erroMsg);
                            }

                        } catch (Exception e) {
                            listener.onErro("Erro de processamento: " + e.getMessage());
                        }

                    });

                }

            });

        } catch (Exception e) {

            listener.onErro(e.getMessage());

        }

    }

    public FormularioRepository(Context context) {
        this.context = context;
    }

    private String getEmailUsuario() {
        return context.getSharedPreferences("SESSAO_USER", Context.MODE_PRIVATE)
                .getString("email_logado", "");
    }

    // =========================================================
    // UPLOAD DE VÍDEO COM PROGRESSO
    // =========================================================
    public void uploadVideo(Uri videoUri, OnUploadProgressListener listener) {
        String email = getEmailUsuario();
        if (email.isEmpty()) {
            listener.onErro("Usuário não autenticado.");
            return;
        }

        // Configurar timeout maior para vídeos (2 minutos)
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        try {
            // Tentar obter o tamanho do arquivo de forma mais robusta
            long size = -1;
            try (android.content.res.AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(videoUri, "r")) {
                if (fd != null) size = fd.getLength();
            } catch (Exception ignored) {}

            final long totalBytes = size;
            String mimeType = context.getContentResolver().getType(videoUri);
            if (mimeType == null) mimeType = "video/mp4";

            RequestBody fileBody = new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.parse(context.getContentResolver().getType(videoUri));
                }

                @Override
                public long contentLength() {
                    return totalBytes;
                }

                @Override
                public void writeTo(BufferedSink sink) throws java.io.IOException {
                    try (InputStream inputStream = context.getContentResolver().openInputStream(videoUri)) {
                        if (inputStream == null) throw new java.io.IOException("Não foi possível abrir o arquivo.");

                        byte[] buffer = new byte[8192];
                        long uploaded = 0;
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            sink.write(buffer, 0, read);
                            uploaded += read;

                            if (totalBytes > 0) {
                                final int progress = (int) (100 * uploaded / totalBytes);
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                        listener.onProgress(progress));
                            }
                        }
                    }
                }
            };

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("usuario", email)
                    .addFormDataPart("video", "pitch_video.mp4", fileBody)
                    .build();

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL + "/upload-video")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            listener.onErro("Falha na conexão: " + e.getMessage()));
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(body);
                                String url = json.optString("video_url", "");
                                listener.onSucesso(url);
                            } catch (Exception e) {
                                listener.onSucesso("");
                            }
                        } else {
                            listener.onErro("Erro " + response.code() + ": " + body);
                        }
                    });
                }
            });
        } catch (Exception e) {
            listener.onErro("Erro ao acessar vídeo: " + e.getMessage());
        }
    }
}