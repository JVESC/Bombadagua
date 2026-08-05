package com.example.bombadagua.repository;

import android.content.Context;

import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.network.FonteDadosFluxo;
import com.example.bombadagua.network.SimuladorFluxo;
import com.example.bombadagua.network.WifiHelper;
import com.example.bombadagua.service.CalculadoraConsumo;
import com.example.bombadagua.service.ConsumoManager;
import com.example.bombadagua.service.DetectorVazamento;

public class RepositorioFluxo {

    private static final boolean MODO_SIMULACAO = false;

    private final FonteDadosFluxo fonteDados;

    private final CalculadoraConsumo calculadora;

    private final ConsumoManager consumoManager;
    private final DetectorVazamento detectorVazamento;

    // Na próxima etapa vamos implementar de verdade
    private final FirestoreRepository firestoreRepository;

    public RepositorioFluxo(Context context) {

        if (MODO_SIMULACAO) {

            fonteDados = new SimuladorFluxo();

        } else {

            fonteDados = new WifiHelper(context);

        }

        calculadora = new CalculadoraConsumo();

        detectorVazamento = new DetectorVazamento();

        consumoManager = new ConsumoManager(context);

        firestoreRepository = new FirestoreRepository(consumoManager);

    }

    public interface Listener {

        void onNovoFluxo(DadosEsp32 dados);

        void onErro(String erro);

    }

    public void iniciar(Listener listener) {

        android.util.Log.d("TESTE", "Repositorio iniciou");
        fonteDados.iniciarLeituraContinua(new FonteDadosFluxo.Callback() {

            @Override
            public void onSucesso(DadosEsp32 dados) {

                android.util.Log.d("REPOSITORIO", "Recebeu dados da fonte");

                DadosEsp32 dadosCalculados =
                        calculadora.calcular(dados, consumoManager);

                android.util.Log.d("REPOSITORIO", "Calculou consumo");

                DadosEsp32 dadosAnalisados =
                        detectorVazamento.analisar(dadosCalculados);

                android.util.Log.d("REPOSITORIO", "Analisou vazamento");

                firestoreRepository.salvar(dadosAnalisados);

                android.util.Log.d("REPOSITORIO", "Chamou FirestoreRepository");

                listener.onNovoFluxo(dadosAnalisados);

            }

            @Override
            public void onErro(String erro) {

                listener.onErro(erro);

            }

        });

    }

    public void parar() {

        fonteDados.pararLeitura();

    }

    public void finalizar() {

        fonteDados.finalizar();

    }

}