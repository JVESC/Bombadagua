package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

public class CalculadoraConsumo {

    private static final double FATOR_ECONOMIA = 0.20;

    public DadosEsp32 calcular(DadosEsp32 dados,
                               ConsumoManager consumoManager) {

        long agora = System.currentTimeMillis();

        double litrosHoje = consumoManager.getLitrosHoje();

        long ultimaLeitura = consumoManager.getUltimaLeitura();

        if (ultimaLeitura != 0) {

            double segundos =
                    (agora - ultimaLeitura) / 1000.0;

            double litrosConsumidos =
                    (dados.getLitrosMinuto() / 60.0) * segundos;

            litrosHoje += litrosConsumidos;

        }

        double aguaPoupada =
                litrosHoje * FATOR_ECONOMIA;

        consumoManager.salvarLitrosHoje(litrosHoje);
        consumoManager.salvarUltimaLeitura(agora);

        dados.setLitrosHoje(litrosHoje);
        dados.setUltimaAtualizacao(agora);

        return dados;

    }

}