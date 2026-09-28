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

    // A ESP32 envia dados pro Firestore a cada ~5 segundos
    // (veja intervaloFirebase no firmware). Se não chegar nada
    // novo por mais tempo que isso, consideramos que ela está
    // offline — mesmo que o campo "online" do Firestore ainda
    // diga true (a ESP não avisa quando perde conexão, ela
    // simplesmente para de mandar dado).
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

    // Indica se a fonte de dados (Firebase/Simulador) está
    // efetivamente conectada/ouvindo.
    private boolean iniciado = false;

    // Todas as telas que estão "inscritas" para receber os dados
    // já processados (DetectorVazamento + CalculadoraConsumo).
    //
    // IMPORTANTE: existe UM ÚNICO pipeline de processamento.
    // MainActivity, VazamentoActivity etc. recebem sempre o
    // mesmo resultado, nunca versões conflitantes dos dados.
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
    // ADICIONAR LISTENER (uma tela passa a "ouvir" os dados)
    // =============================================================
    //
    // Pode ser chamado por quantas telas quiser (MainActivity,
    // VazamentoActivity, ...) ao mesmo tempo. Todas recebem os
    // MESMOS dados, já processados pelo DetectorVazamento e pela
    // CalculadoraConsumo — não existe mais uma tela lendo o dado
    // "cru" da ESP enquanto outra lê o dado "calculado".
    // =============================================================

    public void adicionarListener(
            Listener listener
    ) {

        if (listener == null) {
            return;
        }

        listeners.add(listener);

        Log.d(
                "REPOSITORIO",
                "Listener adicionado. Total: " + listeners.size()
        );

        // A fonte de dados só precisa ser iniciada uma vez,
        // mesmo com vários listeners inscritos.
        if (iniciado) {
            return;
        }

        iniciarFonteDados();
    }

    // =============================================================
    // REMOVER LISTENER (uma tela para de "ouvir" os dados)
    // =============================================================
    //
    // A fonte de dados só é realmente parada quando NENHUMA tela
    // mais precisa dela — assim, sair da VazamentoActivity não
    // interrompe o monitoramento que a MainActivity ainda usa,
    // e vice-versa.
    // =============================================================

    public void removerListener(
            Listener listener
    ) {

        listeners.remove(listener);

        Log.d(
                "REPOSITORIO",
                "Listener removido. Total: " + listeners.size()
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
                        // 0. VERIFICA SE O DADO ESTÁ "FRESCO"
                        // =================================================
                        //
                        // Importante pro uso fora de casa: sem isso, se a
                        // ESP perder Wi-Fi, o app continuaria mostrando os
                        // últimos números como se fossem em tempo real.
                        // =================================================

                        long diferencaAtualizacao =
                                System.currentTimeMillis()
                                        - dados.getUltimaAtualizacao();

                        if (
                                dados.getUltimaAtualizacao() > 0 &&
                                        diferencaAtualizacao > LIMITE_OFFLINE_MS
                        ) {

                            dados.setOnline(false);

                            Log.d(
                                    "REPOSITORIO",
                                    "ESP32 sem atualizar há "
                                            + (diferencaAtualizacao / 1000)
                                            + "s — marcando como offline."
                            );
                        }

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
                        // 4. ATUALIZA TODAS AS TELAS INSCRITAS
                        // =================================================
                        //
                        // Todo mundo recebe o MESMO objeto, já processado.
                        // =================================================

                        for (Listener l : listeners) {
                            l.onNovoFluxo(dadosCalculados);
                        }
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
                            l.onErro(erro);
                        }
                    }
                }
        );
    }

    // =============================================================
    // PARAR (uso interno — chamado quando o último listener sai)
    // =============================================================

    private void parar() {

        Log.d(
                "REPOSITORIO",
                "Parando monitoramento (nenhuma tela precisa mais)."
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