package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalculadoraConsumo {

    private static final double FATOR_ECONOMIA = 0.20;

    private static final SimpleDateFormat FORMATO_DATA =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
        // A ESP32 manda em "litrosHoje" um total ACUMULADO desde a
        // última vez que ela foi ligada — não zera à meia-noite.
        // Por isso calculamos aqui o "hoje de verdade": o valor
        // bruto da ESP menos o valor que ela já tinha no começo
        // do dia (baseline), guardado no celular.
        // =========================================================

        double litrosBrutoEsp =
                dados.getLitrosHoje();

        String hojeStr =
                FORMATO_DATA.format(new Date());

        String diaSalvo =
                consumoManager.getData();

        double baseline =
                consumoManager.getBaseline();

        if (!hojeStr.equals(diaSalvo)) {

            // =====================================================
            // VIROU O DIA (ou é a primeira leitura de sempre)
            //
            // Tudo que a ESP já tinha acumulado até agora passa a
            // ser "baseline": não conta como consumo de hoje.
            // =====================================================

            baseline = litrosBrutoEsp;

            consumoManager.salvarData(hojeStr);
            consumoManager.salvarBaseline(baseline);

            LogHelper.log(
                    "Novo dia detectado. Baseline: "
                            + baseline
                            + " L"
            );

        } else if (litrosBrutoEsp < baseline) {

            // =====================================================
            // A ESP32 REINICIOU NO MEIO DO DIA
            //
            // O contador dela voltou pra perto de zero, então o
            // valor atual já é, sozinho, o consumo de hoje a
            // partir daqui (perdemos só o que já tinha sido
            // contado antes do reinício, que é inevitável sem a
            // ESP guardar o "hoje" na própria memória dela).
            // =====================================================

            baseline = 0.0;

            consumoManager.salvarBaseline(baseline);

            LogHelper.log(
                    "ESP32 reiniciou durante o dia. "
                            + "Baseline zerada."
            );
        }

        double litrosHoje =
                litrosBrutoEsp - baseline;

        if (litrosHoje < 0) {
            litrosHoje = 0;
        }

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