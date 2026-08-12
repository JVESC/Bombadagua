package com.example.bombadagua.repository;

import android.util.Log;

import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.service.ConsumoManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {

    private static final long INTERVALO_GRAVACAO = 15000;

    private final FirebaseFirestore firestore;
    private final ConsumoManager consumoManager;

    public FirestoreRepository(ConsumoManager consumoManager) {

        firestore = FirebaseFirestore.getInstance();

        this.consumoManager = consumoManager;
    }

    /**
     * Salva o estado atual da ESP32.
     */
    public void salvar(DadosEsp32 dados) {

        Log.d(
                "FIRESTORE",
                "Entrou no salvar()"
        );

        long agora =
                System.currentTimeMillis();

        long ultima =
                consumoManager.getUltimaGravacao();

        Map<String, Object> mapa =
                new HashMap<>();

        mapa.put(
                "litrosMinuto",
                dados.getLitrosMinuto()
        );

        mapa.put(
                "litrosHoje",
                dados.getLitrosHoje()
        );

        mapa.put(
                "vazamento",
                dados.isVazamento()
        );

        mapa.put(
                "online",
                dados.isOnline()
        );

        mapa.put(
                "ultimaAtualizacao",
                dados.getUltimaAtualizacao()
        );

        mapa.put(
                "inicioVazamento",
                dados.getInicioVazamento()
        );

        mapa.put(
                "aguaPerdida",
                dados.getAguaPerdida()
        );

        Log.d(
                "FIRESTORE",
                "Gravando estado..."
        );

        firestore.collection("estado")
                .document("principal")
                .set(mapa)
                .addOnSuccessListener(unused ->
                        Log.d(
                                "FIRESTORE",
                                "Estado salvo"
                        )
                )
                .addOnFailureListener(e ->
                        Log.e(
                                "FIRESTORE",
                                "Erro ao salvar estado",
                                e
                        )
                );

        consumoManager.salvarUltimaGravacao(
                agora
        );
    }

    /**
     * Salva um vazamento finalizado no histórico.
     */
    public void salvarVazamento(
            long inicio,
            long fim,
            long duracao,
            double aguaPerdida
    ) {

        Map<String, Object> mapa =
                new HashMap<>();

        mapa.put(
                "inicioVazamento",
                inicio
        );

        mapa.put(
                "fimVazamento",
                fim
        );

        mapa.put(
                "duracao",
                duracao
        );

        mapa.put(
                "aguaPerdida",
                aguaPerdida
        );

        Log.d(
                "FIRESTORE",
                "Salvando histórico de vazamento..."
        );

        firestore.collection("vazamentos")
                .add(mapa)
                .addOnSuccessListener(
                        documentReference ->
                                Log.d(
                                        "FIRESTORE",
                                        "Vazamento salvo no histórico: "
                                                + documentReference.getId()
                                )
                )
                .addOnFailureListener(e ->
                        Log.e(
                                "FIRESTORE",
                                "Erro ao salvar vazamento",
                                e
                        )
                );
    }
}