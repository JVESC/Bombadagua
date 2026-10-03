package com.example.bombadagua.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.bombadagua.model.DispositivoEsp32;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DispositivoRepository {

    private static final String TAG = "DISPOSITIVO_REPO";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public DispositivoRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // =============================================================
    // BUSCAR OS ESP32 DA CONTA LOGADA
    // =============================================================

    public void buscarDispositivos(
            OnDispositivosCarregadosListener listener
    ) {

        if (auth.getCurrentUser() == null) {

            Log.e(
                    TAG,
                    "Nenhum usuário está logado."
            );

            listener.onErro(
                    "Nenhum usuário está logado."
            );

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Log.d(
                TAG,
                "Buscando dispositivos do usuário: " + uid
        );

        firestore.collection("dispositivos")
                .whereEqualTo("donoUid", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<DispositivoEsp32> dispositivos =
                            new ArrayList<>();

                    for (
                            QueryDocumentSnapshot document :
                            querySnapshot
                    ) {

                        DispositivoEsp32 dispositivo =
                                document.toObject(
                                        DispositivoEsp32.class
                                );

                        /*
                         * Caso o documento não tenha o ID salvo
                         * dentro do próprio objeto, usamos o ID
                         * do documento do Firestore.
                         */
                        if (
                                dispositivo.getDispositivoId() == null
                                        ||
                                        dispositivo.getDispositivoId().isEmpty()
                        ) {

                            dispositivo.setDispositivoId(
                                    document.getId()
                            );
                        }

                        dispositivos.add(dispositivo);

                        Log.d(
                                TAG,
                                "Dispositivo encontrado: "
                                        + dispositivo.getNome()
                                        + " - "
                                        + dispositivo.getLocal()
                        );
                    }

                    Log.d(
                            TAG,
                            "Total de dispositivos encontrados: "
                                    + dispositivos.size()
                    );

                    listener.onSucesso(dispositivos);
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Erro ao buscar dispositivos.",
                            e
                    );

                    listener.onErro(
                            "Erro ao buscar dispositivos: "
                                    + e.getMessage()
                    );
                });
    }

    // =============================================================
    // SALVAR UM NOVO ESP32 VINCULADO
    // =============================================================

    public void salvarDispositivo(
            DispositivoEsp32 dispositivo,
            OnOperacaoListener listener
    ) {

        if (auth.getCurrentUser() == null) {

            listener.onErro(
                    "Nenhum usuário está logado."
            );

            return;
        }

        String uid = auth.getCurrentUser().getUid();

        dispositivo.setDonoUid(uid);

        if (dispositivo.getCriadoEm() == 0) {

            dispositivo.setCriadoEm(
                    System.currentTimeMillis()
            );
        }

        String dispositivoId =
                dispositivo.getDispositivoId();

        if (
                dispositivoId == null
                        ||
                        dispositivoId.trim().isEmpty()
        ) {

            listener.onErro(
                    "O ID do ESP32 não foi informado."
            );

            return;
        }

        Log.d(
                TAG,
                "Salvando dispositivo: " + dispositivoId
        );

        firestore.collection("dispositivos")
                .document(dispositivoId)
                .set(dispositivo)
                .addOnSuccessListener(unused -> {

                    Log.d(
                            TAG,
                            "Dispositivo salvo com sucesso."
                    );

                    listener.onSucesso();
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Erro ao salvar dispositivo.",
                            e
                    );

                    listener.onErro(
                            "Erro ao salvar dispositivo: "
                                    + e.getMessage()
                    );
                });
    }

    // =============================================================
    // EXCLUIR DISPOSITIVO
    // =============================================================

    public void excluirDispositivo(
            String dispositivoId,
            OnOperacaoListener listener
    ) {

        if (auth.getCurrentUser() == null) {

            listener.onErro(
                    "Nenhum usuário está logado."
            );

            return;
        }

        if (
                dispositivoId == null
                        ||
                        dispositivoId.trim().isEmpty()
        ) {

            listener.onErro(
                    "ID do dispositivo inválido."
            );

            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        Log.d(
                TAG,
                "Tentando excluir dispositivo: "
                        + dispositivoId
        );

        firestore.collection("dispositivos")
                .document(dispositivoId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        listener.onErro(
                                "Dispositivo não encontrado."
                        );

                        return;
                    }

                    String donoUid =
                            document.getString("donoUid");

                    if (
                            donoUid == null
                                    ||
                                    !donoUid.equals(uid)
                    ) {

                        listener.onErro(
                                "Esse dispositivo não pertence à sua conta."
                        );

                        return;
                    }

                    firestore.collection("dispositivos")
                            .document(dispositivoId)
                            .delete()
                            .addOnSuccessListener(unused -> {

                                Log.d(
                                        TAG,
                                        "Dispositivo excluído."
                                );

                                listener.onSucesso();
                            })
                            .addOnFailureListener(e -> {

                                Log.e(
                                        TAG,
                                        "Erro ao excluir dispositivo.",
                                        e
                                );

                                listener.onErro(
                                        "Erro ao excluir dispositivo: "
                                                + e.getMessage()
                                );
                            });
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Erro ao verificar proprietário.",
                            e
                    );

                    listener.onErro(
                            "Erro ao verificar dispositivo: "
                                    + e.getMessage()
                    );
                });
    }

    // =============================================================
    // INTERFACES
    // =============================================================

    public interface OnDispositivosCarregadosListener {

        void onSucesso(
                List<DispositivoEsp32> dispositivos
        );

        void onErro(
                String mensagem
        );
    }

    public interface OnOperacaoListener {

        void onSucesso();

        void onErro(
                String mensagem
        );
    }
}