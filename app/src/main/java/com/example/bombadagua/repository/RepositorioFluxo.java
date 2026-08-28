package com.example.bombadagua.repository;

import android.content.Context;
import android.util.Log;

import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.network.FirebaseFluxo;
import com.example.bombadagua.network.FonteDadosFluxo;
import com.example.bombadagua.network.SimuladorFluxo;
import com.example.bombadagua.service.CalculadoraConsumo;
import com.example.bombadagua.service.ConsumoManager;
import com.example.bombadagua.service.DetectorVazamento;

public class RepositorioFluxo {

    // =============================================================
    // CONFIGURAÇÃO
    // =============================================================

    private static final boolean MODO_SIMULACAO = false;

    // =============================================================
    // COMPONENTES
    // =============================================================

    private final FonteDadosFluxo fonteDados;

    private final CalculadoraConsumo calculadora;

    private final ConsumoManager consumoManager;

    private final DetectorVazamento detectorVazamento;

    private final FirestoreRepository firestoreRepository;

    // =============================================================
    // CONTROLE
    // =============================================================

    private boolean iniciado = false;

    // =============================================================
    // CONSTRUTOR
    // =============================================================

    public RepositorioFluxo(
            Context context
    ) {

        if (MODO_SIMULACAO) {

            fonteDados =
                    new SimuladorFluxo();

            Log.d(
                    "REPOSITORIO",
                    "Modo SIMULAÇÃO ativado."
            );

        } else {

            fonteDados =
                    new FirebaseFluxo();

            Log.d(
                    "REPOSITORIO",
                    "Modo FIREBASE ativado."
            );
        }

        calculadora =
                new CalculadoraConsumo();

        detectorVazamento =
                new DetectorVazamento();

        consumoManager =
                new ConsumoManager(context);

        firestoreRepository =
                new FirestoreRepository();
    }

    // =============================================================
    // LISTENER
    // =============================================================

    public interface Listener {

        void onNovoFluxo(
                DadosEsp32 dados
        );

        void onErro(
                String erro
        );
    }

    // =============================================================
    // INICIAR
    // =============================================================

    public void iniciar(
            Listener listener
    ) {

        // Evita criar múltiplos listeners
        if (iniciado) {

            Log.d(
                    "REPOSITORIO",
                    "Monitoramento já está iniciado."
            );

            return;
        }

        iniciado = true;

        Log.d(
                "REPOSITORIO",
                "================================="
        );

        Log.d(
                "REPOSITORIO",
                "Monitoramento iniciado."
        );

        Log.d(
                "REPOSITORIO",
                "Fonte: "
                        + (
                        MODO_SIMULACAO
                                ? "SIMULADOR"
                                : "FIREBASE"
                )
        );

        Log.d(
                "REPOSITORIO",
                "================================="
        );

        fonteDados.iniciarLeituraContinua(
                new FonteDadosFluxo.Callback() {

                    @Override
                    public void onSucesso(
                            DadosEsp32 dados
                    ) {

                        Log.d(
                                "REPOSITORIO",
                                "Dados recebidos da ESP32."
                        );

                        Log.d(
                                "REPOSITORIO",
                                "Vazão: "
                                        + dados.getLitrosMinuto()
                                        + " L/min"
                        );

                        Log.d(
                                "REPOSITORIO",
                                "Litros hoje: "
                                        + dados.getLitrosHoje()
                        );

                        // =================================================
                        // 1. DETECTOR DE VAZAMENTO
                        // =================================================

                        DadosEsp32 dadosAnalisados =
                                detectorVazamento.analisar(
                                        dados
                                );

                        Log.d(
                                "REPOSITORIO",
                                "Vazamento: "
                                        + dadosAnalisados.isVazamento()
                        );

                        // =================================================
                        // 2. CÁLCULOS LOCAIS
                        // =================================================

                        DadosEsp32 dadosCalculados =
                                calculadora.calcular(
                                        dadosAnalisados,
                                        consumoManager
                                );

                        // =================================================
                        // 3. VAZAMENTO FINALIZADO
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
                                    "VAZAMENTO FINALIZADO"
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "Água perdida: "
                                            + aguaPerdida
                                            + " L"
                            );

                            // =================================================
                            // SALVA SOMENTE O HISTÓRICO
                            // =================================================

                            firestoreRepository
                                    .salvarVazamento(
                                            inicio,
                                            fim,
                                            duracao,
                                            aguaPerdida
                                    );

                            Log.d(
                                    "REPOSITORIO",
                                    "Histórico salvo."
                            );

                            Log.d(
                                    "REPOSITORIO",
                                    "================================="
                            );
                        }

                        // =================================================
                        // 4. ATUALIZA A INTERFACE
                        // =================================================

                        listener.onNovoFluxo(
                                dadosCalculados
                        );
                    }

                    @Override
                    public void onErro(
                            String erro
                    ) {

                        Log.e(
                                "REPOSITORIO",
                                "Erro: " + erro
                        );

                        listener.onErro(
                                erro
                        );
                    }
                }
        );
    }

    // =============================================================
    // PARAR
    // =============================================================

    public void parar() {

        Log.d(
                "REPOSITORIO",
                "Parando monitoramento."
        );

        iniciado = false;

        fonteDados.pararLeitura();
    }

    // =============================================================
    // FINALIZAR
    // =============================================================

    public void finalizar() {

        Log.d(
                "REPOSITORIO",
                "Finalizando repositorio."
        );

        iniciado = false;

        fonteDados.finalizar();
    }

    // =============================================================
    // MARCAR VAZAMENTO COMO RESOLVIDO
    // =============================================================

    public void marcarVazamentoComoResolvido() {

        detectorVazamento.marcarComoResolvido();

        Log.d(
                "REPOSITORIO",
                "Vazamento marcado como resolvido."
        );
    }
}