package com.example.bombadagua.repository;

import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.service.ConsumoManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {

    private static final long INTERVALO_GRAVACAO = 1000;

    private final FirebaseFirestore firestore;

    private final ConsumoManager consumoManager;

    public FirestoreRepository(ConsumoManager consumoManager){

        firestore = FirebaseFirestore.getInstance();

        this.consumoManager = consumoManager;

    }

    public void salvar(DadosEsp32 dados){

        android.util.Log.d("FIRESTORE", "Entrou no salvar()");

        long agora = System.currentTimeMillis();

        long ultima =
                consumoManager.getUltimaGravacao();

       /*testes
        if(!dados.isVazamento()

                && agora - ultima < INTERVALO_GRAVACAO){
            android.util.Log.d("FIRESTORE",
                    "Ignorando gravação. Tempo restante: "
                            + (INTERVALO_GRAVACAO - (agora - ultima)));
            return;

        }
        */
        Map<String,Object> mapa = new HashMap<>();

        mapa.put("litrosMinuto",dados.getLitrosMinuto());
        mapa.put("litrosHoje",dados.getLitrosHoje());
        mapa.put("aguaPoupada",dados.getAguaPoupada());
        mapa.put("vazamento",dados.isVazamento());
        mapa.put("online",dados.isOnline());
        mapa.put("ultimaAtualizacao",dados.getUltimaAtualizacao());

        android.util.Log.d("FIRESTORE","Gravando...");
        firestore.collection("estado")
                .document("principal")
                .set(mapa)
                .addOnSuccessListener(unused ->
                        android.util.Log.d("FIRESTORE",
                                "Estado salvo"))
                .addOnFailureListener(e ->
                        android.util.Log.e("FIRESTORE",
                                "Erro ao salvar estado", e));

        firestore.collection("historico")
                .add(mapa)
                .addOnSuccessListener(documentReference ->
                        android.util.Log.d("FIRESTORE",
                                "Histórico salvo"))
                .addOnFailureListener(e ->
                        android.util.Log.e("FIRESTORE",
                                "Erro ao salvar histórico", e));

        consumoManager.salvarUltimaGravacao(agora);

    }

}