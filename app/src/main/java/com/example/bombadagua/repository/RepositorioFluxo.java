package com.example.bombadagua.repository;

import android.content.Context;
import android.util.Log;

import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.network.FonteDadosFluxo;
import com.example.bombadagua.network.SimuladorFluxo;
import com.example.bombadagua.network.WifiHelper;
import com.example.bombadagua.service.CalculadoraConsumo;
import com.example.bombadagua.service.ConsumoManager;
import com.example.bombadagua.service.DetectorVazamento;

public class RepositorioFluxo {

    private static final boolean MODO_SIMULACAO = true;

    private final FonteDadosFluxo fonteDados;

    private final CalculadoraConsumo calculadora;

    private final ConsumoManager consumoManager;

    private final DetectorVazamento detectorVazamento;

    private final FirestoreRepository firestoreRepository;

    public RepositorioFluxo(Context context) {

        if (MODO_SIMULACAO) {

            fonteDados =
                    new SimuladorFluxo();

        } else {

            fonteDados =
                    new WifiHelper(context);
        }

        calculadora =
                new CalculadoraConsumo();

        detectorVazamento =
                new DetectorVazamento();

        consumoManager =
                new ConsumoManager(context);

        firestoreRepository =
                new FirestoreRepository(
                        consumoManager
                );
    }

    public interface Listener {

        void onNovoFluxo(DadosEsp32 dados);

        void onErro(String erro);
    }

    public void iniciar(Listener listener) {

        Log.d(
                "TESTE",
                "Repositorio iniciou"
        );

        fonteDados.iniciarLeituraContinua(
                new FonteDadosFluxo.Callback() {

                    @Override
                    public void onSucesso(
                            DadosEsp32 dados
                    ) {

                        Log.d(
                                "REPOSITORIO",
                                "Recebeu dados da fonte"
                        );

                        // =================================================
                        // 1. DETECTA VAZAMENTO
                        // =================================================

                        DadosEsp32 dadosAnalisados =
                                detectorVazamento.analisar(
                                        dados
                                );

                        Log.d(
                                "REPOSITORIO",
                                "Analisou vazamento"
                        );

                        // =================================================
                        // 2. CALCULA CONSUMO E ÁGUA PERDIDA
                        // =================================================

                        DadosEsp32 dadosCalculados =
                                calculadora.calcular(
                                        dadosAnalisados,
                                        consumoManager
                                );

                        Log.d(
                                "REPOSITORIO",
                                "Calculou consumo"
                        );

                        // =================================================
                        // 3. VERIFICA SE UM VAZAMENTO TERMINOU
                        // =================================================

                        if (
                                detectorVazamento
                                        .vazamentoFoiFinalizado()
                        ) {

                            long inicio =
                                    detectorVazamento
                                            .getInicioVazamento();

                            long fim =
                                    System.currentTimeMillis();

                            long duracao =
                                    fim - inicio;

                            double aguaPerdida =
                                    calculadora
                                            .getAguaPerdidaFinalizada();

                            Log.d(
                                    "REPOSITORIO",
                                    "================================="
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "💧 VAZAMENTO FINALIZADO"
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "Início: "
                                            + inicio
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "Fim: "
                                            + fim
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "Duração: "
                                            + duracao
                                            + " ms"
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "Água perdida: "
                                            + aguaPerdida
                                            + " L"
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "Salvando histórico..."
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "================================="
                            );

                            firestoreRepository
                                    .salvarVazamento(
                                            inicio,
                                            fim,
                                            duracao,
                                            aguaPerdida
                                    );
                        }

                        // =================================================
                        // 4. SALVA ESTADO ATUAL NO FIRESTORE
                        // =================================================

                        firestoreRepository.salvar(
                                dadosCalculados
                        );

                        Log.d(
                                "REPOSITORIO",
                                "Chamou FirestoreRepository"
                        );

                        // =================================================
                        // 5. ATUALIZA A TELA
                        // =================================================

                        listener.onNovoFluxo(
                                dadosCalculados
                        );
                    }

                    @Override
                    public void onErro(
                            String erro
                    ) {

                        listener.onErro(
                                erro
                        );
                    }
                }
        );
    }

    public void parar() {

        fonteDados.pararLeitura();
    }

    public void finalizar() {

        fonteDados.finalizar();
    }

    /**
     * Marca o alerta atual como resolvido.
     */
    public void marcarVazamentoComoResolvido() {

        detectorVazamento.marcarComoResolvido();

        Log.d(
                "REPOSITORIO",
                "Vazamento marcado como resolvido."
        );
    }
}