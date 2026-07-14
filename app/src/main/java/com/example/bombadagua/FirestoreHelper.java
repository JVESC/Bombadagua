package com.example.bombadagua;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class FirestoreHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnLeituraListener {
        void onLeituraRecebida(double vazaoAtual, double litrosHoje, double aguaPoupada);
        void onErro(String mensagem);
    }

    public void observarUltimaLeitura(OnLeituraListener listener) {
        db.collection("leituras")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshots, erro) -> {
                    if (erro != null) {
                        listener.onErro("Erro ao buscar dados: " + erro.getMessage());
                        android.util.Log.e("FIRESTORE_ERRO", erro.getMessage(), erro);
                        return;
                    }
                    if (snapshots == null || snapshots.isEmpty()) {
                        listener.onErro("Nenhuma leitura encontrada ainda");
                        return;
                    }

                    QueryDocumentSnapshot documento = (QueryDocumentSnapshot) snapshots.getDocuments().get(0);

                    // Lê o documento inteiro como Map e ignora espaços em branco
                    // nos nomes dos campos (protege contra campos salvos com
                    // espaço acidental, ex: "litrosHoje " em vez de "litrosHoje").
                    Map<String, Object> dados = documento.getData();

                    double vazao = extrairDouble(dados, "vazaoAtual");
                    double litros = extrairDouble(dados, "litrosHoje");
                    double poupada = extrairDouble(dados, "aguaPoupada");

                    android.util.Log.d("FIRESTORE", "✓ Dados: vazao=" + vazao + ", litros=" + litros + ", poupada=" + poupada);
                    listener.onLeituraRecebida(vazao, litros, poupada);
                });
    }

    /**
     * Busca um campo numérico no mapa de dados do documento comparando os
     * nomes das chaves sem diferenciar maiúsculas/minúsculas e ignorando
     * espaços em branco no início/fim do nome do campo. Retorna 0.0 se o
     * campo não existir ou não for um número.
     */
    private double extrairDouble(Map<String, Object> dados, String nomeCampo) {
        if (dados == null) return 0.0;

        // Tenta primeiro o nome exato (caminho mais rápido e comum)
        Object valor = dados.get(nomeCampo);

        // Se não achou, procura alguma chave que bata ignorando espaços/caixa
        if (valor == null) {
            for (Map.Entry<String, Object> entrada : dados.entrySet()) {
                if (entrada.getKey() != null
                        && entrada.getKey().trim().equalsIgnoreCase(nomeCampo)) {
                    valor = entrada.getValue();
                    break;
                }
            }
        }

        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return 0.0;
    }
}