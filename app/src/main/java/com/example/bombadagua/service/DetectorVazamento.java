package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

public class DetectorVazamento {

    private static final double LIMITE_VAZAMENTO = 1.0;

    // 5 minutos em milissegundos
    private static final long TEMPO_MINIMO_VAZAMENTO = 10 * 1000;

    // Momento em que a vazão acima do limite começou
    private long inicioVazamento = 0;

    public DadosEsp32 analisar(DadosEsp32 dados) {

        double vazao = dados.getLitrosMinuto();

        if (vazao > LIMITE_VAZAMENTO) {

            // Começou uma possível vazão contínua
            if (inicioVazamento == 0) {
                inicioVazamento = System.currentTimeMillis();
            }

            long tempoDecorrido =
                    System.currentTimeMillis() - inicioVazamento;

            // Só considera vazamento depois de 5 minutos
            if (tempoDecorrido >= TEMPO_MINIMO_VAZAMENTO) {
                dados.setVazamento(true);

                android.util.Log.d(
                        "VAZAMENTO",
                        "🚨 Vazamento detectado! Vazão: " + vazao + " L/min"
                );
            } else {
                dados.setVazamento(false);
            }

        } else {

            // Vazão voltou ao normal, então reinicia a contagem
            inicioVazamento = 0;
            dados.setVazamento(false);
        }

        return dados;
    }
}