package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalculadoraConsumo {

    private static final double FATOR_ECONOMIA =
            0.20;

    private static final SimpleDateFormat FORMATO_DATA =
            new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            );

    // =============================================================
    // VAZAMENTO
    // =============================================================

    private double aguaPerdida =
            0.0;

    private double aguaPerdidaFinalizada =
            0.0;

    private long ultimaLeituraVazamento =
            0;

    private boolean vazamentoAnterior =
            false;

    // =============================================================
    // CALCULAR
    // =============================================================

    public DadosEsp32 calcular(
            DadosEsp32 dados,
            ConsumoManager consumoManager,
            String dispositivoId
    ) {

        if (dados == null) {
            return null;
        }

        long agora =
                System.currentTimeMillis();

        // =========================================================
        // CONSUMO
        // =========================================================

        double litrosBrutoEsp =
                dados.getLitrosHoje();

        String hojeStr =
                FORMATO_DATA.format(
                        new Date()
                );

        String dataSalva =
                consumoManager.getData(
                        dispositivoId
                );

        double litrosHoje =
                consumoManager.getLitrosHoje(
                        dispositivoId
                );

        double ultimoRaw =
                consumoManager.getUltimoRaw(
                        dispositivoId
                );

        // =========================================================
        // PRIMEIRA LEITURA DO DIA
        // =========================================================

        if (
                !hojeStr.equals(dataSalva)
        ) {

            /*
             * Começamos um novo dia.
             *
             * O valor atual da ESP não é considerado consumo
             * retroativo do aplicativo.
             *
             * A partir desta leitura começamos a acompanhar
             * as diferenças.
             */

            litrosHoje = 0.0;

            consumoManager.salvarData(
                    dispositivoId,
                    hojeStr
            );

            consumoManager.salvarUltimoRaw(
                    dispositivoId,
                    litrosBrutoEsp
            );

            consumoManager.salvarLitrosHoje(
                    dispositivoId,
                    litrosHoje
            );

            LogHelper.log(
                    "Novo dia para a ESP "
                            + dispositivoId
                            + ". Leitura inicial: "
                            + litrosBrutoEsp
                            + " L"
            );

        } else {

            // =====================================================
            // MESMO DIA
            // =====================================================

            double diferenca =
                    litrosBrutoEsp - ultimoRaw;

            if (diferenca >= 0) {

                /*
                 * A ESP continuou contando normalmente.
                 */
                litrosHoje += diferenca;

            } else {

                /*
                 * O contador da ESP voltou para um valor menor.
                 *
                 * Isso normalmente significa que a ESP32 foi
                 * reiniciada.
                 *
                 * Nesse caso consideramos o valor atual como
                 * consumo desde o reinício.
                 */

                LogHelper.log(
                        "Reinício da ESP32 detectado."
                                + " Último valor: "
                                + ultimoRaw
                                + " L"
                                + " | Atual: "
                                + litrosBrutoEsp
                                + " L"
                );

                litrosHoje +=
                        Math.max(
                                litrosBrutoEsp,
                                0.0
                        );
            }

            consumoManager.salvarUltimoRaw(
                    dispositivoId,
                    litrosBrutoEsp
            );

            consumoManager.salvarLitrosHoje(
                    dispositivoId,
                    litrosHoje
            );
        }

        // =========================================================
        // SEGURANÇA
        // =========================================================

        if (litrosHoje < 0) {

            litrosHoje = 0.0;

            consumoManager.salvarLitrosHoje(
                    dispositivoId,
                    litrosHoje
            );
        }

        // =========================================================
        // ÁGUA POUPADA
        // =========================================================

        double aguaPoupada =
                litrosHoje
                        * FATOR_ECONOMIA;

        // =========================================================
        // ÁGUA PERDIDA
        // =========================================================

        if (dados.isVazamento()) {

            // =====================================================
            // INÍCIO DO VAZAMENTO
            // =====================================================

            if (!vazamentoAnterior) {

                aguaPerdida =
                        0.0;

                ultimaLeituraVazamento =
                        agora;

                LogHelper.log(
                        "Iniciando contador de água perdida."
                );

            } else {

                // =================================================
                // TEMPO DESDE A ÚLTIMA LEITURA
                // =================================================

                double segundos =
                        (
                                agora
                                        - ultimaLeituraVazamento
                        )
                                / 1000.0;

                double litrosPerdidos =
                        (
                                dados.getLitrosMinuto()
                                        / 60.0
                        )
                                * segundos;

                aguaPerdida +=
                        litrosPerdidos;

                ultimaLeituraVazamento =
                        agora;
            }

            vazamentoAnterior =
                    true;

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

            vazamentoAnterior =
                    false;

            ultimaLeituraVazamento =
                    0;

            aguaPerdida =
                    0.0;
        }

        // =========================================================
        // DEVOLVER RESULTADOS
        // =========================================================

        dados.setLitrosHoje(
                litrosHoje
        );

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
    // LOG
    // =============================================================

    private static class LogHelper {

        static void log(
                String mensagem
        ) {

            android.util.Log.d(
                    "CALCULADORA",
                    mensagem
            );
        }
    }
}