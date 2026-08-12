package com.example.bombadagua.service;

/**
 * Gerencia o estado compartilhado do alerta de vazamento.
 *
 * É Singleton para que MainActivity, RepositorioFluxo
 * e VazamentoActivity utilizem o mesmo estado.
 */
public class AlertaVazamentoManager {

    private static AlertaVazamentoManager instancia;

    private boolean alertaResolvido = false;

    private AlertaVazamentoManager() {
    }

    public static synchronized AlertaVazamentoManager getInstance() {

        if (instancia == null) {
            instancia = new AlertaVazamentoManager();
        }

        return instancia;
    }

    /**
     * Marca o alerta atual como resolvido.
     */
    public synchronized void marcarComoResolvido() {

        alertaResolvido = true;

        android.util.Log.d(
                "ALERTA_MANAGER",
                "Alerta marcado como resolvido."
        );
    }

    /**
     * Verifica se o alerta atual foi resolvido.
     */
    public synchronized boolean isAlertaResolvido() {

        return alertaResolvido;
    }

    /**
     * Permite que um novo vazamento seja detectado.
     */
    public synchronized void permitirNovoAlerta() {

        alertaResolvido = false;

        android.util.Log.d(
                "ALERTA_MANAGER",
                "Novo alerta permitido."
        );
    }

    /**
     * Reseta completamente o estado.
     */
    public synchronized void resetar() {

        alertaResolvido = false;

        android.util.Log.d(
                "ALERTA_MANAGER",
                "Estado do alerta resetado."
        );
    }
}