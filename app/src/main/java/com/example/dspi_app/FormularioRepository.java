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

    //==================SALVAR FORMULARIO===============================
    public void salvarFormulario(String tipo, JSONObject campos, OnSalvoListener listener) {

        try {

            JSONObject body = new JSONObject();
            body.put("tipo", tipo);
            body.put("usuario", getEmailUsuario());
            body.put("campos", campos);

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                    body.toString(),
                    okhttp3.MediaType.parse("application/json")
            );

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL + "/salvar-dados")
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

                    String resposta = response.body().string();

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {

                        try {

                            JSONObject json = new JSONObject(resposta);

                            if (json.optBoolean("success")) {

                                listener.onSucesso();

                            } else {

                                listener.onErro(json.optString("error"));

                            }

                        } catch (Exception e) {

                            listener.onErro(e.getMessage());

                        }

                    });

                }

            });

        } catch (Exception e) {

            listener.onErro(e.getMessage());

        }

    }


    //==============BUSCAR FORMULARIO===================
    public void buscarFormulario(String tipo, OnBuscaListener listener) {

        try {

            JSONObject body = new JSONObject();

            body.put("tipo", tipo);
            body.put("usuario", getEmailUsuario());

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

                    String resposta = response.body().string();

                    android.util.Log.e("API_RESPOSTA", resposta);

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {

                        try {

                            JSONObject json = new JSONObject(resposta);

                            if (json.optBoolean("success")) {

                                listener.onSucesso(json.getJSONObject("dados"));

                            } else {

                                listener.onErro(json.optString("error"));

                            }

                        } catch (Exception e) {

                            listener.onErro(e.getMessage());

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