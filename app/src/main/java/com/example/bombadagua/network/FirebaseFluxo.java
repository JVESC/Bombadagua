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

    // =============================================================
    // URL DO REALTIME DATABASE
    // =============================================================
    //
    // Pegue esse endereço em: Firebase Console → Realtime Database
    // → (o link mostrado no topo da página, algo como
    // "https://SEU-PROJETO-default-rtdb.firebaseio.com/").
    //
    // Tem que ser EXATAMENTE o mesmo endereço usado no firmware
    // (DATABASE_URL, no .ino).
    // =============================================================

    private static final String DATABASE_URL =
            "https://bombadagua-ac54e-default-rtdb.firebaseio.com/";

    private final DatabaseReference referencia;

    private ValueEventListener listener;

    public FirebaseFluxo() {

        referencia =
                FirebaseDatabase.getInstance(DATABASE_URL)
                        .getReference("estado");
    }

    @Override
    public void iniciarLeituraContinua(
            Callback callback
    ) {

        // Evita criar dois listeners ao mesmo tempo
        pararLeitura();

        Log.d(
                TAG,
                "Iniciando leitura do Realtime Database..."
        );

        listener = new ValueEventListener() {

            @Override
            public void onDataChange(
                    DataSnapshot snapshot
            ) {

                // ==========================================
                // NÓ NÃO EXISTE
                // ==========================================

                if (!snapshot.exists()) {

                    Log.d(
                            TAG,
                            "Nó \"estado\" ainda não existe."
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

        referencia.addValueEventListener(listener);
    }

    // =============================================================
    // REALTIME DATABASE → DADOS ESP32
    // =============================================================

    private DadosEsp32 converterDados(
            DataSnapshot snapshot
    ) {

        DadosEsp32 dados =
                new DadosEsp32();

        // ==========================================
        // VAZÃO
        // ==========================================

        Double litrosMinuto =
                snapshot.child("litrosMinuto")
                        .getValue(Double.class);

        if (litrosMinuto != null) {

            dados.setLitrosMinuto(
                    litrosMinuto
            );
        }

        // ==========================================
        // LITROS HOJE
        // ==========================================

        Double litrosHoje =
                snapshot.child("litrosHoje")
                        .getValue(Double.class);

        if (litrosHoje != null) {

            dados.setLitrosHoje(
                    litrosHoje
            );
        }

        // ==========================================
        // ONLINE
        // ==========================================

        Boolean online =
                snapshot.child("online")
                        .getValue(Boolean.class);

        if (online != null) {

            dados.setOnline(online);

        } else {

            dados.setOnline(true);
        }

        // ==========================================
        // ÚLTIMA ATUALIZAÇÃO
        // ==========================================

        Long ultimaAtualizacao =
                snapshot.child("ultimaAtualizacao")
                        .getValue(Long.class);

        if (ultimaAtualizacao != null) {

            dados.setUltimaAtualizacao(
                    ultimaAtualizacao
            );
        }

        // ==========================================
        // VAZAMENTO / ÁGUA POUPADA / ÁGUA PERDIDA /
        // INÍCIO DO VAZAMENTO
        // ==========================================
        //
        // A ESP32 não manda esses campos — eles são
        // calculados no próprio app (DetectorVazamento
        // e CalculadoraConsumo), então ficam com o valor
        // padrão aqui e são preenchidos depois, no
        // RepositorioFluxo.
        // ==========================================

        return dados;
    }

    // =============================================================
    // PARAR
    // =============================================================

    @Override
    public void pararLeitura() {

        if (listener != null) {

            referencia.removeEventListener(listener);

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