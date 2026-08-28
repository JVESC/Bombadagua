package com.example.bombadagua.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {

    private static final String TAG = "FIRESTORE";

    private final FirebaseFirestore firestore;

    public FirestoreRepository() {

        firestore =
                FirebaseFirestore.getInstance();
    }

    // =============================================================
    // SALVAR VAZAMENTO FINALIZADO
    // =============================================================

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
                TAG,
                "Salvando histórico de vazamento..."
        );

        firestore.collection("vazamentos")
                .add(mapa)
                .addOnSuccessListener(
                        documentReference ->
                                Log.d(
                                        TAG,
                                        "Vazamento salvo: "
                                                + documentReference.getId()
                                )
                )
                .addOnFailureListener(
                        e ->
                                Log.e(
                                        TAG,
                                        "Erro ao salvar vazamento",
                                        e
                                )
                );
    }
}