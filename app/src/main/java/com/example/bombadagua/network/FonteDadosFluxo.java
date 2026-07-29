package com.example.bombadagua.network;

import com.example.bombadagua.model.DadosEsp32;

public interface FonteDadosFluxo {

    interface Callback {

        void onSucesso(DadosEsp32 dados);

        void onErro(String erro);

    }

    void iniciarLeituraContinua(Callback callback);

    void pararLeitura();

    void finalizar();

}