package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

public class DetectorVazamento {

    private static final double LIMITE_VAZAMENTO = 1.0;

    public DadosEsp32 analisar(DadosEsp32 dados) {

        boolean vazamento =
                dados.getLitrosMinuto() > LIMITE_VAZAMENTO;

        dados.setVazamento(vazamento);

        return dados;

    }

}