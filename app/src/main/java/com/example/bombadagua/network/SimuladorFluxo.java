package com.example.bombadagua.network;

import android.os.Handler;
import android.os.Looper;

import com.example.bombadagua.model.DadosEsp32;

public class SimuladorFluxo implements FonteDadosFluxo {

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean executando = false;

    // Contador das leituras
    private int contador = 0;

    @Override
    public void iniciarLeituraContinua(Callback callback) {

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

                /*
                 * Cada leitura acontece a cada 2 segundos.
                 *
                 * 0 - 4  = vazão normal
                 * 5 - 12 = vazamento contínuo
                 * 13 - 16 = vazão normal
                 *
                 * Depois o ciclo reinicia.
                 */

                if (contador >= 5 && contador <= 12) {

                    // Vazamento
                    dados.setLitrosMinuto(8.0);

                    android.util.Log.d(
                            "SIMULADOR",
                            "🚨 SIMULANDO VAZAMENTO - "
                                    + "Leitura: "
                                    + contador
                                    + " | Vazão: 8.0 L/min"
                    );

                } else {

                    // Vazão normal
                    dados.setLitrosMinuto(0.5);

                    android.util.Log.d(
                            "SIMULADOR",
                            "💧 Vazão normal - "
                                    + "Leitura: "
                                    + contador
                                    + " | Vazão: 0.5 L/min"
                    );
                }

                dados.setOnline(true);

                dados.setUltimaAtualizacao(
                        System.currentTimeMillis()
                );

                callback.onSucesso(dados);

                contador++;

                // Reinicia o ciclo
                if (contador > 16) {
                    contador = 0;
                }

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

        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void finalizar() {

        pararLeitura();
    }
}