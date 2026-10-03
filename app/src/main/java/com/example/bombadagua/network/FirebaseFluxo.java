package com.example.bombadagua.network;

import android.util.Log;

import com.example.bombadagua.model.DadosEsp32;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseFluxo implements FonteDadosFluxo {

    private static final String TAG = "FIREBASE_FLUXO";

    private static final String DATABASE_URL =
            "https://bombadagua-ac54e-default-rtdb.firebaseio.com/";

    private DatabaseReference referencia;

    private ValueEventListener listener;

    private String dispositivoId;

    public FirebaseFluxo() {

        /*
         * Não iniciamos a referência aqui porque ainda não
         * sabemos qual ESP32 o usuário escolheu.
         */
        referencia = null;
    }

    // =============================================================
    // DEFINIR ESP32
    // =============================================================

    public void selecionarDispositivo(
            String dispositivoId
    ) {

        pararLeitura();

        this.dispositivoId = dispositivoId;

        if (
                dispositivoId == null
                        ||
                        dispositivoId.trim().isEmpty()
        ) {

            referencia = null;

            Log.e(
                    TAG,
                    "ID do dispositivo inválido."
            );

            return;
        }

        referencia =
                FirebaseDatabase
                        .getInstance(DATABASE_URL)
                        .getReference("dispositivos")
                        .child(dispositivoId)
                        .child("estado");

        Log.d(
                TAG,
                "ESP32 selecionada: "
                        + dispositivoId
        );

        Log.d(
                TAG,
                "Caminho do Firebase: "
                        + "dispositivos/"
                        + dispositivoId
                        + "/estado"
        );
    }

    // =============================================================
    // OBTER DISPOSITIVO ATUAL
    // =============================================================

    public String getDispositivoId() {

        return dispositivoId;
    }

    // =============================================================
    // INICIAR LEITURA
    // =============================================================

    @Override
    public void iniciarLeituraContinua(
            Callback callback
    ) {

        pararLeitura();

        if (referencia == null) {

            Log.e(
                    TAG,
                    "Não é possível iniciar leitura: "
                            + "nenhuma ESP32 foi selecionada."
            );

            callback.onErro(
                    "Nenhuma ESP32 selecionada."
            );

            return;
        }

        Log.d(
                TAG,
                "Iniciando leitura do Realtime Database..."
        );

        listener = new ValueEventListener() {

            @Override
            public void onDataChange(
                    DataSnapshot snapshot
            ) {

                // =================================================
                // NÓ NÃO EXISTE
                // =================================================

                if (!snapshot.exists()) {

                    Log.d(
                            TAG,
                            "Nó da ESP32 ainda não existe."
                    );

                    callback.onErro(
                            "Dados da ESP32 ainda não encontrados."
                    );

                    return;
                }

                // =================================================
                // CONVERTER
                // =================================================

                try {

                    DadosEsp32 dados =
                            converterDados(snapshot);

                    Log.d(
                            TAG,
                            "Dados recebidos da ESP32 "
                                    + dispositivoId
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

                    callback.onSucesso(
                            dados
                    );

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

            @Override
            public void onCancelled(
                    DatabaseError error
            ) {

                Log.e(
                        TAG,
                        "Erro ao ler Realtime Database: "
                                + error.getMessage()
                );

                callback.onErro(
                        error.getMessage()
                );
            }
        };

        referencia.addValueEventListener(
                listener
        );
    }

    // =============================================================
    // REALTIME DATABASE → DADOS ESP32
    // =============================================================

    private DadosEsp32 converterDados(
            DataSnapshot snapshot
    ) {

        DadosEsp32 dados =
                new DadosEsp32();

        // =========================================================
        // VAZÃO
        // =========================================================

        Double litrosMinuto =
                snapshot
                        .child("litrosMinuto")
                        .getValue(Double.class);

        if (litrosMinuto != null) {

            dados.setLitrosMinuto(
                    litrosMinuto
            );
        }

        // =========================================================
        // LITROS HOJE
        // =========================================================

        Double litrosHoje =
                snapshot
                        .child("litrosHoje")
                        .getValue(Double.class);

        if (litrosHoje != null) {

            dados.setLitrosHoje(
                    litrosHoje
            );
        }

        // =========================================================
        // ONLINE
        // =========================================================

        Boolean online =
                snapshot
                        .child("online")
                        .getValue(Boolean.class);

        if (online != null) {

            dados.setOnline(
                    online
            );

        } else {

            dados.setOnline(
                    true
            );
        }

        // =========================================================
        // ÚLTIMA ATUALIZAÇÃO
        // =========================================================

        Long ultimaAtualizacao =
                snapshot
                        .child("ultimaAtualizacao")
                        .getValue(Long.class);

        if (ultimaAtualizacao != null) {

            dados.setUltimaAtualizacao(
                    ultimaAtualizacao
            );
        }

        /*
         * Vazamento, água poupada e água perdida continuam
         * sendo tratados pelo RepositorioFluxo.
         */

        return dados;
    }

    // =============================================================
    // PARAR
    // =============================================================

    @Override
    public void pararLeitura() {

        if (listener != null && referencia != null) {

            referencia.removeEventListener(
                    listener
            );

            listener = null;

            Log.d(
                    TAG,
                    "Listener Realtime Database removido."
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