package com.example.bombadagua.network;

import android.os.Handler;
import android.os.Looper;

import com.example.bombadagua.model.DadosEsp32;

import java.util.Random;

public class SimuladorFluxo implements FonteDadosFluxo {

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Random random = new Random();

    private boolean executando = false;

    @Override
    public void iniciarLeituraContinua(Callback callback) {

        executando = true;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                if (!executando) {
                    return;
                }

                DadosEsp32 dados = new DadosEsp32();

                dados.setLitrosMinuto(
                        3 + random.nextDouble() * 12
                );

                dados.setOnline(true);

                dados.setUltimaAtualizacao(
                        System.currentTimeMillis()
                );
                android.util.Log.d(
                        "SIMULADOR",
                        "Enviando " + dados.getLitrosMinuto()
                );
                callback.onSucesso(dados);

                handler.postDelayed(this, 2000);

            }

        };

        handler.post(runnable);

    }

    @Override
    public void pararLeitura() {

        executando = false;

        handler.removeCallbacksAndMessages(null);

    }

    @Override
    public void finalizar() {

        pararLeitura();

    }

}