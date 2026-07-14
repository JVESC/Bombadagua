package com.example.bombadagua; // ajuste para o seu pacote

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirestoreHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnLeituraListener {
        void onLeituraRecebida(double vazaoAtual, double litrosHoje, double aguaPoupada);
        void onErro(String mensagem);
    }

    /**
     * Busca a leitura mais recente da coleção "leituras" e mantém
     * a tela atualizada automaticamente (listener em tempo real).
     */
    public void observarUltimaLeitura(OnLeituraListener listener) {
        db.collection("leituras")
                .limit(1)
                .addSnapshotListener((snapshots, erro) -> {
                    if (erro != null) {
                        listener.onErro("Erro ao buscar dados: " + erro.getMessage());
                        return;
                    }
                    if (snapshots == null || snapshots.isEmpty()) {
                        listener.onErro("Nenhuma leitura encontrada ainda");
                        return;
                    }

                    QueryDocumentSnapshot documento = (QueryDocumentSnapshot) snapshots.getDocuments().get(0);

                    Double vazao = documento.getDouble("vazaoAtual");
                    Double litros = documento.getDouble("litrosHoje");
                    Double poupada = documento.getDouble("aguaPoupada");

                    if (vazao != null && litros != null && poupada != null) {
                        listener.onLeituraRecebida(vazao, litros, poupada);
                    }
                });
    }
}