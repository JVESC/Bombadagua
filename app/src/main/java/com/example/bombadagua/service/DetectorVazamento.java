package com.example.bombadagua.service;

import com.example.bombadagua.model.DadosEsp32;

public class DetectorVazamento {

    private static final double LIMITE_VAZAMENTO = 1.0;

    // Para teste: 10 segundos
    private static final long TEMPO_MINIMO_VAZAMENTO = 10 * 1000;

    // Momento em que começou a vazão contínua
    private long inicioVazamento = 0;

    // Indica se existe um vazamento confirmado atualmente
    private boolean vazamentoConfirmado = false;

    // Indica que um vazamento terminou nesta análise
    private boolean vazamentoFinalizado = false;

    private final AlertaVazamentoManager alertaManager;

    public DetectorVazamento() {

        alertaManager =
                AlertaVazamentoManager.getInstance();
    }

    public DadosEsp32 analisar(DadosEsp32 dados) {

        // A cada nova análise, assumimos que nenhum
        // vazamento terminou até que seja detectado.
        vazamentoFinalizado = false;

        double vazao =
                dados.getLitrosMinuto();

        // =========================================================
        // VAZÃO NORMAL
        // =========================================================

        if (vazao <= LIMITE_VAZAMENTO) {

            /*
             * Se havia um vazamento confirmado e agora
             * a vazão voltou ao normal, o vazamento terminou.
             */
            if (vazamentoConfirmado) {

                android.util.Log.d(
                        "VAZAMENTO",
                        "✅ Vazamento finalizado."
                );

                dados.setVazamento(false);

                /*
                 * Mantemos o início do vazamento nos dados
                 * para que o RepositorioFluxo possa registrar
                 * o histórico.
                 */
                dados.setInicioVazamento(
                        inicioVazamento
                );

                vazamentoFinalizado = true;

                vazamentoConfirmado = false;

                return dados;
            }

            // Não havia vazamento confirmado.
            inicioVazamento = 0;

            alertaManager.resetar();

            dados.setVazamento(false);

            dados.setInicioVazamento(0);

            android.util.Log.d(
                    "VAZAMENTO",
                    "Vazão normal. Detector resetado."
            );

            return dados;
        }

        // =========================================================
        // VAZÃO ACIMA DO LIMITE
        // =========================================================

        if (inicioVazamento == 0) {

            inicioVazamento =
                    System.currentTimeMillis();

            android.util.Log.d(
                    "VAZAMENTO",
                    "Possível vazamento iniciado. Vazão: "
                            + vazao
                            + " L/min"
            );
        }

        dados.setInicioVazamento(
                inicioVazamento
        );

        long agora =
                System.currentTimeMillis();

        long tempoDecorrido =
                agora - inicioVazamento;

        // =========================================================
        // ALERTA FOI MARCADO COMO RESOLVIDO
        // =========================================================

        if (alertaManager.isAlertaResolvido()) {

            /*
             * O usuário resolveu o alerta atual.
             *
             * Se a água continuar passando por tempo suficiente,
             * começamos um novo período de vazamento.
             */
            if (tempoDecorrido >= TEMPO_MINIMO_VAZAMENTO) {

                alertaManager.permitirNovoAlerta();

                inicioVazamento = agora;

                dados.setInicioVazamento(
                        inicioVazamento
                );

                dados.setVazamento(true);

                vazamentoConfirmado = true;

                android.util.Log.d(
                        "VAZAMENTO",
                        "🚨 Novo vazamento detectado! Vazão: "
                                + vazao
                                + " L/min"
                );

            } else {

                dados.setVazamento(false);

                android.util.Log.d(
                        "VAZAMENTO",
                        "Alerta resolvido. "
                                + "Aguardando novo período de "
                                + "vazão contínua."
                );
            }

            return dados;
        }

        // =========================================================
        // TEMPO MÍNIMO ATINGIDO
        // =========================================================

        if (tempoDecorrido >= TEMPO_MINIMO_VAZAMENTO) {

            dados.setVazamento(true);

            vazamentoConfirmado = true;

            android.util.Log.d(
                    "VAZAMENTO",
                    "🚨 Vazamento detectado! Vazão: "
                            + vazao
                            + " L/min"
            );

        } else {

            dados.setVazamento(false);

            android.util.Log.d(
                    "VAZAMENTO",
                    "Possível vazamento. "
                            + "Tempo: "
                            + (tempoDecorrido / 1000)
                            + " segundos."
            );
        }

        return dados;
    }

    /**
     * Informa se um vazamento acabou nesta análise.
     */
    public boolean vazamentoFoiFinalizado() {

        return vazamentoFinalizado;
    }

    /**
     * Marca o alerta atual como resolvido.
     */
    public void marcarComoResolvido() {

        alertaManager.marcarComoResolvido();

        android.util.Log.d(
                "VAZAMENTO",
                "Alerta atual marcado como resolvido."
        );
    }

    public long getInicioVazamento() {

        return inicioVazamento;
    }
}