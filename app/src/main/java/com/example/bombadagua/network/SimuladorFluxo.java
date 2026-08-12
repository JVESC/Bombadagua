package com.example.bombadagua.network;

import android.os.Handler;
import android.os.Looper;

import com.example.bombadagua.model.DadosEsp32;

import java.util.Random;

public class SimuladorFluxo implements FonteDadosFluxo {

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Random random =
            new Random();

    private boolean executando = false;

    private int contador = 0;

    @Override
    public void iniciarLeituraContinua(
            Callback callback
    ) {

        executando = true;

        contador = 0;

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                if (!executando) {
                    return;
                }

                DadosEsp32 dados =
                        new DadosEsp32();

                // =================================================
                // FASE 1 — FLUXO NORMAL
                // 5 leituras × 2 segundos = 10 segundos
                // =================================================

                if (contador < 5) {

                    dados.setLitrosMinuto(0.0);

                    android.util.Log.d(
                            "SIMULADOR",
                            "💧 NORMAL | "
                                    + contador
                                    + " | "
                                    + dados.getLitrosMinuto()
                                    + " L/min"
                    );

                }

                // =================================================
                // FASE 2 — VAZAMENTO
                // 10 leituras × 2 segundos = 20 segundos
                // =================================================

                else if (contador < 15) {

                    double vazao =
                            3 + random.nextDouble() * 12;

                    dados.setLitrosMinuto(
                            vazao
                    );

                    android.util.Log.d(
                            "SIMULADOR",
                            "🚨 VAZAMENTO | "
                                    + contador
                                    + " | "
                                    + vazao
                                    + " L/min"
                    );

                }

                // =================================================
                // FASE 3 — VAZAMENTO TERMINOU
                // 5 leituras × 2 segundos = 10 segundos
                // =================================================

                else if (contador < 20) {

                    dados.setLitrosMinuto(0.0);

                    android.util.Log.d(
                            "SIMULADOR",
                            "✅ NORMALIZOU | "
                                    + contador
                                    + " | "
                                    + dados.getLitrosMinuto()
                                    + " L/min"
                    );

                }

                // =================================================
                // REINICIA
                // =================================================

                else {

                    contador = 0;

                    dados.setLitrosMinuto(0.0);

                    android.util.Log.d(
                            "SIMULADOR",
                            "🔄 NOVO CICLO"
                    );
                }

                dados.setOnline(true);

                dados.setUltimaAtualizacao(
                        System.currentTimeMillis()
                );

                callback.onSucesso(
                        dados
                );

                contador++;

                handler.postDelayed(
                        this,
                        2000
                );
            }
        };

        handler.post(runnable);
    }

    @Override
    public void pararLeitura() {

        executando = false;

        handler.removeCallbacksAndMessages(
                null
        );
    }

    @Override
    public void finalizar() {

        pararLeitura();
    }
}