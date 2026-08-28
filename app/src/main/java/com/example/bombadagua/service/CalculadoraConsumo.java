package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

public class CalculadoraConsumo {

    private static final double FATOR_ECONOMIA = 0.20;

    // Água perdida no vazamento atual
    private double aguaPerdida = 0.0;

    // Água perdida no último vazamento finalizado
    private double aguaPerdidaFinalizada = 0.0;

    // Última leitura durante o vazamento
    private long ultimaLeituraVazamento = 0;

    // Estado anterior do vazamento
    private boolean vazamentoAnterior = false;

    public DadosEsp32 calcular(
            DadosEsp32 dados,
            ConsumoManager consumoManager
    ) {

        long agora =
                System.currentTimeMillis();

        // =========================================================
        // LITROS HOJE
        // =========================================================
        //
        // IMPORTANTE:
        //
        // O valor vem da ESP32.
        //
        // O Android NÃO recalcula e NÃO salva litrosHoje.
        //
        double litrosHoje =
                dados.getLitrosHoje();

        // =========================================================
        // ÁGUA POUPADA
        // =========================================================

        double aguaPoupada =
                litrosHoje * FATOR_ECONOMIA;

        // =========================================================
        // ÁGUA PERDIDA
        // =========================================================

        if (dados.isVazamento()) {

            // =====================================================
            // INÍCIO DO VAZAMENTO
            // =====================================================

            if (!vazamentoAnterior) {

                aguaPerdida = 0.0;

                ultimaLeituraVazamento =
                        agora;

                LogHelper.log(
                        "Iniciando contador de água perdida."
                );

            } else {

                // =================================================
                // INTERVALO
                // =================================================

                double segundos =
                        (agora - ultimaLeituraVazamento)
                                / 1000.0;

                double litrosPerdidos =
                        (dados.getLitrosMinuto() / 60.0)
                                * segundos;

                aguaPerdida += litrosPerdidos;

                ultimaLeituraVazamento =
                        agora;
            }

            vazamentoAnterior = true;

        } else {

            // =====================================================
            // VAZAMENTO TERMINOU
            // =====================================================

            if (vazamentoAnterior) {

                aguaPerdidaFinalizada =
                        aguaPerdida;

                android.util.Log.d(
                        "AGUA_PERDIDA",
                        "Vazamento encerrado. Total: "
                                + aguaPerdidaFinalizada
                                + " L"
                );
            }

            vazamentoAnterior = false;

            ultimaLeituraVazamento = 0;

            aguaPerdida = 0.0;
        }

        // =========================================================
        // RESULTADOS
        // =========================================================

        dados.setAguaPoupada(
                aguaPoupada
        );

        dados.setAguaPerdida(
                aguaPerdida
        );

        return dados;
    }

    // =============================================================
    // ÚLTIMO VAZAMENTO FINALIZADO
    // =============================================================

    public double getAguaPerdidaFinalizada() {

        return aguaPerdidaFinalizada;
    }

    // =============================================================
    // PEQUENO HELPER DE LOG
    // =============================================================

    private static class LogHelper {

        static void log(String mensagem) {

            android.util.Log.d(
                    "CALCULADORA",
                    mensagem
            );
        }
    }
}