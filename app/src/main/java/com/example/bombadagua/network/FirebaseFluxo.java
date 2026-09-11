package com.example.bombadagua.network;

import android.util.Log;

import com.example.bombadagua.model.DadosEsp32;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class FirebaseFluxo implements FonteDadosFluxo {

    private static final String TAG = "FIREBASE_FLUXO";

    private final FirebaseFirestore firestore;

    private ListenerRegistration listenerRegistration;

    public FirebaseFluxo() {

        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void iniciarLeituraContinua(
            Callback callback
    ) {

        // Evita criar dois listeners ao mesmo tempo
        pararLeitura();

        Log.d(
                TAG,
                "Iniciando leitura do Firebase..."
        );

        listenerRegistration =
                firestore.collection("estado")
                        .document("principal")
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    // ==========================================
                                    // ERRO
                                    // ==========================================

                                    if (error != null) {

                                        Log.e(
                                                TAG,
                                                "Erro ao ler Firebase",
                                                error
                                        );

                                        callback.onErro(
                                                error.getMessage()
                                        );

                                        return;
                                    }

                                    // ==========================================
                                    // DOCUMENTO NÃO EXISTE
                                    // ==========================================

                                    if (snapshot == null ||
                                            !snapshot.exists()) {

                                        Log.d(
                                                TAG,
                                                "Documento estado/principal não existe."
                                        );

                                        callback.onErro(
                                                "Dados da ESP32 ainda não encontrados."
                                        );

                                        return;
                                    }

                                    // ==========================================
                                    // CONVERTER
                                    // ==========================================

                                    try {

                                        DadosEsp32 dados =
                                                converterDados(snapshot);

                                        Log.d(
                                                TAG,
                                                "Dados recebidos:"
                                        );

                                        Log.d(
                                                TAG,
                                                "Vazão: "
                                                        + dados.getLitrosMinuto()
                                                        + " L/min"
                                        );

                                        Log.d(
                                                TAG,
                                                "Litros hoje: "
                                                        + dados.getLitrosHoje()
                                        );

                                        callback.onSucesso(dados);

                                    } catch (Exception e) {

                                        Log.e(
                                                TAG,
                                                "Erro ao converter dados",
                                                e
                                        );

                                        callback.onErro(
                                                "Erro ao processar dados: "
                                                        + e.getMessage()
                                        );
                                    }
                                }
                        );
    }

    // =============================================================
    // FIRESTORE → DADOS ESP32
    // =============================================================

    private DadosEsp32 converterDados(
            DocumentSnapshot snapshot
    ) {

        DadosEsp32 dados =
                new DadosEsp32();

        // ==========================================
        // VAZÃO
        // ==========================================

        Double litrosMinuto =
                snapshot.getDouble("litrosMinuto");

        if (litrosMinuto != null) {

            dados.setLitrosMinuto(
                    litrosMinuto
            );
        }

        // ==========================================
        // LITROS HOJE
        // ==========================================

        Double litrosHoje =
                snapshot.getDouble("litrosHoje");

        if (litrosHoje != null) {

            dados.setLitrosHoje(
                    litrosHoje
            );
        }

        // ==========================================
        // ÁGUA POUPADA
        // ==========================================

        Double aguaPoupada =
                snapshot.getDouble("aguaPoupada");

        if (aguaPoupada != null) {

            dados.setAguaPoupada(
                    aguaPoupada
            );
        }

        // ==========================================
        // ÁGUA PERDIDA
        // ==========================================

        Double aguaPerdida =
                snapshot.getDouble("aguaPerdida");

        if (aguaPerdida != null) {

            dados.setAguaPerdida(
                    aguaPerdida
            );
        }

        // ==========================================
        // VAZAMENTO
        // ==========================================

        Boolean vazamento =
                snapshot.getBoolean("vazamento");

        if (vazamento != null) {

            dados.setVazamento(
                    vazamento
            );
        }

        // ==========================================
        // ONLINE
        // ==========================================

        Boolean online =
                snapshot.getBoolean("online");

        if (online != null) {

            dados.setOnline(online);

        } else {

            dados.setOnline(true);
        }

        // ==========================================
        // ÚLTIMA ATUALIZAÇÃO
        // ==========================================

        Long ultimaAtualizacao =
                snapshot.getLong("ultimaAtualizacao");

        if (ultimaAtualizacao != null) {

            dados.setUltimaAtualizacao(
                    ultimaAtualizacao
            );
        }

        // ==========================================
        // INÍCIO DO VAZAMENTO
        // ==========================================

        Long inicioVazamento =
                snapshot.getLong("inicioVazamento");

        if (inicioVazamento != null) {

            dados.setInicioVazamento(
                    inicioVazamento
            );
        }

        return dados;
    }

    // =============================================================
    // PARAR
    // =============================================================

    @Override
    public void pararLeitura() {

        if (listenerRegistration != null) {

            listenerRegistration.remove();

            listenerRegistration = null;

            Log.d(
                    TAG,
                    "Listener Firebase removido."
            );
        }
    }

    // =============================================================
    // FINALIZAR
    // =============================================================

    @Override
    public void finalizar() {

        pararLeitura();

        Log.d(
                TAG,
                "FirebaseFluxo finalizado."
        );
    }
}