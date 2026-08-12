package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

public class CalculadoraConsumo {

    private static final double FATOR_ECONOMIA = 0.20;

    // Água perdida acumulada no vazamento atual
    private double aguaPerdida = 0.0;

    // Água perdida no último vazamento finalizado
    private double aguaPerdidaFinalizada = 0.0;

    // Usado para calcular o intervalo entre as leituras
    private long ultimaLeituraVazamento = 0;

    // Indica se já estamos acompanhando um vazamento
    private boolean vazamentoAnterior = false;

    public DadosEsp32 calcular(
            DadosEsp32 dados,
            ConsumoManager consumoManager
    ) {

        long agora =
                System.currentTimeMillis();

        double litrosHoje =
                consumoManager.getLitrosHoje();

        long ultimaLeitura =
                consumoManager.getUltimaLeitura();

        // ==========================================
        // CÁLCULO DO CONSUMO NORMAL
        // ==========================================

        if (ultimaLeitura != 0) {

            double segundos =
                    (agora - ultimaLeitura) / 1000.0;

            double litrosConsumidos =
                    (dados.getLitrosMinuto() / 60.0)
                            * segundos;

            litrosHoje += litrosConsumidos;
        }

        // ==========================================
        // CÁLCULO DA ÁGUA PERDIDA
        // ==========================================

        if (dados.isVazamento()) {

            // ==========================================
            // PRIMEIRO MOMENTO DO VAZAMENTO
            // ==========================================

            if (!vazamentoAnterior) {

                aguaPerdida = 0.0;

                ultimaLeituraVazamento = agora;

                android.util.Log.d(
                        "AGUA_PERDIDA",
                        "Iniciando contador de água perdida"
                );

            } else {

                // ==========================================
                // INTERVALO DESDE A ÚLTIMA LEITURA
                // ==========================================

                double segundos =
                        (agora - ultimaLeituraVazamento)
                                / 1000.0;

                // Litros perdidos neste intervalo
                double litrosPerdidos =
                        (dados.getLitrosMinuto() / 60.0)
                                * segundos;

                aguaPerdida += litrosPerdidos;

                ultimaLeituraVazamento = agora;
            }

            vazamentoAnterior = true;

        } else {

            // ==========================================
            // VAZAMENTO ENCERRADO
            // ==========================================

            if (vazamentoAnterior) {

                /*
                 * Guarda o valor final antes de zerar
                 * o contador do vazamento atual.
                 */
                aguaPerdidaFinalizada =
                        aguaPerdida;

                android.util.Log.d(
                        "AGUA_PERDIDA",
                        "Vazamento encerrado. Total perdido: "
                                + aguaPerdidaFinalizada
                                + " L"
                );
            }

            vazamentoAnterior = false;

            ultimaLeituraVazamento = 0;

            /*
             * Zera somente o contador do vazamento atual.
             *
             * O valor de aguaPerdidaFinalizada continua
             * disponível para o RepositorioFluxo.
             */
            aguaPerdida = 0.0;
        }

        // ==========================================
        // ÁGUA POUPADA
        // ==========================================

        double aguaPoupada =
                litrosHoje * FATOR_ECONOMIA;

        // ==========================================
        // SALVA OS DADOS
        // ==========================================

        consumoManager.salvarLitrosHoje(
                litrosHoje
        );

        consumoManager.salvarUltimaLeitura(
                agora
        );

        // ==========================================
        // COLOCA OS RESULTADOS NO OBJETO
        // ==========================================

        dados.setLitrosHoje(
                litrosHoje
        );

        dados.setAguaPoupada(
                aguaPoupada
        );

        dados.setUltimaAtualizacao(
                agora
        );

        dados.setAguaPerdida(
                aguaPerdida
        );

        android.util.Log.d(
                "AGUA_PERDIDA",
                "Água perdida atual: "
                        + aguaPerdida
                        + " L"
        );

        return dados;
    }

    /**
     * Retorna a quantidade de água perdida
     * no último vazamento finalizado.
     */
    public double getAguaPerdidaFinalizada() {

        return aguaPerdidaFinalizada;
    }
}