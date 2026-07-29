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

        executor.execute(() -> {

            HttpURLConnection conexao = null;

            try {

                String ip = ConfigManager.obterIp(context);

                URL url = new URL("http://" + ip + "/dados");

                conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(5000);
                conexao.setReadTimeout(5000);

                BufferedReader leitor = new BufferedReader(
                        new InputStreamReader(conexao.getInputStream())
                );

                StringBuilder resposta = new StringBuilder();

                String linha;

                while ((linha = leitor.readLine()) != null) {
                    resposta.append(linha);
                }

                leitor.close();

                JSONObject json = new JSONObject(resposta.toString());

                DadosEsp32 dados = new DadosEsp32();
                dados.setLitrosMinuto(
                        json.getDouble("litros_minuto")
                );

                dados.setOnline(true);

                dados.setUltimaAtualizacao(
                        System.currentTimeMillis()
                );


                handler.post(() -> callback.onSucesso(dados));

            } catch (Exception e) {

                handler.post(() -> callback.onErro(e.getMessage()));

            } finally {

                if (conexao != null) {
                    conexao.disconnect();
                }

            }

        });

    }


    @Override
    public void iniciarLeituraContinua(FonteDadosFluxo.Callback callback) {

        if (lendo) {
            return;
        }

        lendo = true;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (!lendo) {
                    return;
                }

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