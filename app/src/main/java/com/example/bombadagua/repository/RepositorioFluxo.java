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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RepositorioFluxo {

    // =============================================================
    // CONFIGURAÇÃO
    // =============================================================

    private static final boolean MODO_SIMULACAO = false;

    private static final long LIMITE_OFFLINE_MS = 30_000;

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

    private String dispositivoSelecionadoId = null;

    private final List<Listener> listeners =
            new CopyOnWriteArrayList<>();

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
    // SELECIONAR ESP32
    // =============================================================

    public void selecionarDispositivo(
            String dispositivoId
    ) {

        if (
                dispositivoId == null
                        ||
                        dispositivoId.trim().isEmpty()
        ) {

            Log.e(
                    "REPOSITORIO",
                    "ID da ESP32 inválido."
            );

            return;
        }

        dispositivoSelecionadoId =
                dispositivoId.trim();

        Log.d(
                "REPOSITORIO",
                "================================="
        );

        Log.d(
                "REPOSITORIO",
                "ESP32 selecionada:"
        );

        Log.d(
                "REPOSITORIO",
                dispositivoSelecionadoId
        );

        Log.d(
                "REPOSITORIO",
                "================================="
        );

        /*
         * Quando estamos usando FirebaseFluxo, informamos
         * qual ESP32 deverá ser lida.
         */
        if (fonteDados instanceof FirebaseFluxo) {

            FirebaseFluxo firebaseFluxo =
                    (FirebaseFluxo) fonteDados;

            firebaseFluxo.selecionarDispositivo(
                    dispositivoSelecionadoId
            );
        }

        /*
         * Se o monitoramento já estava funcionando,
         * reiniciamos a leitura para usar a nova ESP32.
         */
        if (iniciado) {

            fonteDados.pararLeitura();

            fonteDados.iniciarLeituraContinua(
                    criarCallback()
            );
        }
    }

    // =============================================================
    // OBTER ESP32 SELECIONADA
    // =============================================================

    public String getDispositivoSelecionadoId() {

        return dispositivoSelecionadoId;
    }

    // =============================================================
    // ADICIONAR LISTENER
    // =============================================================

    public void adicionarListener(
            Listener listener
    ) {

        if (listener == null) {
            return;
        }

        if (!listeners.contains(listener)) {

            listeners.add(listener);
        }

        Log.d(
                "REPOSITORIO",
                "Listener adicionado. Total: "
                        + listeners.size()
        );

        if (iniciado) {
            return;
        }

        iniciarFonteDados();
    }

    // =============================================================
    // REMOVER LISTENER
    // =============================================================

    public void removerListener(
            Listener listener
    ) {

        listeners.remove(listener);

        Log.d(
                "REPOSITORIO",
                "Listener removido. Total: "
                        + listeners.size()
        );

        if (listeners.isEmpty()) {

            parar();
        }
    }

    // =============================================================
    // INICIAR FONTE DE DADOS
    // =============================================================

    private void iniciarFonteDados() {

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
                "ESP32: "
                        + (
                        dispositivoSelecionadoId == null
                                ? "NENHUMA"
                                : dispositivoSelecionadoId
                )
        );

        Log.d(
                "REPOSITORIO",
                "=================================");

        fonteDados.iniciarLeituraContinua(
                criarCallback()
        );
    }

    // =============================================================
    // CALLBACK DA FONTE DE DADOS
    // =============================================================

    private FonteDadosFluxo.Callback criarCallback() {

        return new FonteDadosFluxo.Callback() {

            @Override
            public void onSucesso(
                    DadosEsp32 dados
            ) {

                processarDados(
                        dados
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

                for (Listener l : listeners) {

                    l.onErro(
                            erro
                    );
                }
            }
        };
    }

    // =============================================================
    // PROCESSAR DADOS
    // =============================================================

    private void processarDados(
            DadosEsp32 dados
    ) {

        if (dados == null) {

            Log.e(
                    "REPOSITORIO",
                    "Dados recebidos são nulos."
            );

            return;
        }

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

        // =========================================================
        // VERIFICAR SE O DADO ESTÁ FRESCO
        // =========================================================

        long diferencaAtualizacao =
                System.currentTimeMillis()
                        - dados.getUltimaAtualizacao();

        if (
                dados.getUltimaAtualizacao() > 0
                        &&
                        diferencaAtualizacao > LIMITE_OFFLINE_MS
        ) {

            dados.setOnline(false);

            Log.d(
                    "REPOSITORIO",
                    "ESP32 sem atualizar há "
                            + (
                            diferencaAtualizacao / 1000
                    )
                            + "s — marcando como offline."
            );
        }

        // =========================================================
        // DETECTOR DE VAZAMENTO
        // =========================================================

        DadosEsp32 dadosAnalisados =
                detectorVazamento.analisar(
                        dados
                );

        Log.d(
                "REPOSITORIO",
                "Vazamento: "
                        + dadosAnalisados.isVazamento()
        );

        // =========================================================
        // CÁLCULOS LOCAIS
        // =========================================================

        DadosEsp32 dadosCalculados =
                calculadora.calcular(
                        dadosAnalisados,
                        consumoManager
                );

        // =========================================================
        // VAZAMENTO FINALIZADO
        // =========================================================

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

        // =========================================================
        // ATUALIZAR TODAS AS TELAS
        // =========================================================

        for (Listener l : listeners) {

            l.onNovoFluxo(
                    dadosCalculados
            );
        }
    }

    // =============================================================
    // PARAR
    // =============================================================

    private void parar() {

        Log.d(
                "REPOSITORIO",
                "Parando monitoramento."
        );

        iniciado = false;

        fonteDados.pararLeitura();
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