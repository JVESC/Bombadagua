package com.example.bombadagua.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.utils.ConfigManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WifiHelper implements FonteDadosFluxo {

    private final Context context;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Handler leituraHandler =
            new Handler(Looper.getMainLooper());

    private boolean lendo = false;

    private static final long INTERVALO_LEITURA = 2000; // 2 segundos

    public WifiHelper(Context context) {
        this.context = context.getApplicationContext();
    }


    private void buscarDados(FonteDadosFluxo.Callback callback) {

        android.util.Log.d("TESTE", "Entrou buscarDados");

        executor.execute(() -> {

            HttpURLConnection conexao = null;

            try {

                String ip = ConfigManager.obterIp(context);
                android.util.Log.d("TESTE", "IP: " + ip);

                URL url = new URL("http://" + ip + "/dados");
                android.util.Log.d("TESTE", "URL: " + url);

                conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(5000);
                conexao.setReadTimeout(5000);

                // ADICIONE ESTA LINHA
                int codigo = conexao.getResponseCode();
                android.util.Log.d("TESTE", "HTTP: " + codigo);

                BufferedReader leitor = new BufferedReader(
                        new InputStreamReader(conexao.getInputStream())
                );

                StringBuilder resposta = new StringBuilder();

                String linha;

                while ((linha = leitor.readLine()) != null) {
                    resposta.append(linha);
                }

                leitor.close();

                android.util.Log.d("TESTE", "JSON: " + resposta);

                JSONObject json = new JSONObject(resposta.toString());

                DadosEsp32 dados = new DadosEsp32();
                dados.setLitrosMinuto(json.getDouble("litros_minuto"));
                dados.setOnline(true);
                dados.setUltimaAtualizacao(System.currentTimeMillis());

                handler.post(() -> callback.onSucesso(dados));

            } catch (Exception e) {

                android.util.Log.e("TESTE", "ERRO", e);
                e.printStackTrace();

                handler.post(() -> callback.onErro(e.toString()));

            } finally {

                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        });
    }


    @Override
    public void iniciarLeituraContinua(FonteDadosFluxo.Callback callback) {

        android.util.Log.d("TESTE", "WifiHelper iniciou");

        if (lendo) {
            return;
        }

        lendo = true;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                android.util.Log.d("TESTE", "Runnable executou");

                if (!lendo) {
                    return;
                }

                android.util.Log.d("TESTE", "Chamando buscarDados");

                buscarDados(callback);

                leituraHandler.postDelayed(this, INTERVALO_LEITURA);
            }
        };

        leituraHandler.post(runnable);
    }
@Override
    public void pararLeitura() {

        lendo = false;

        leituraHandler.removeCallbacksAndMessages(null);

    }
@Override
    public void finalizar() {

        pararLeitura();

        executor.shutdownNow();

    }

}