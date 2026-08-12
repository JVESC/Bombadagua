package com.example.bombadagua.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bombadagua.R;
import com.example.bombadagua.service.AlertaVazamentoManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class VazamentoActivity extends AppCompatActivity {

    private TextView tvDuracaoVazamento;
    private TextView tvAguaPerdida;
    private TextView tvVazaoAtualDetalhe;

    private FirebaseFirestore firestore;

    private ListenerRegistration listenerEstado;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private long inicioVazamento = 0;

    private double aguaPerdida = 0.0;

    private boolean vazamentoAtivo = false;

    // Manager compartilhado com o DetectorVazamento
    private AlertaVazamentoManager alertaManager;

    // ============================================================
    // ATUALIZAÇÃO DA TELA
    // ============================================================

    private final Runnable atualizadorTela =
            new Runnable() {

                @Override
                public void run() {

                    if (vazamentoAtivo
                            && inicioVazamento > 0) {

                        atualizarDuracao();

                        atualizarAguaPerdida();

                        handler.postDelayed(
                                this,
                                1000
                        );
                    }
                }
            };

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_vazamento
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // ========================================================
        // COMPONENTES
        // ========================================================

        LinearLayout btnVoltar =
                findViewById(R.id.btnVoltar);

        Button btnVerComoConsertar =
                findViewById(
                        R.id.btnVerComoConsertar
                );

        Button btnMarcarResolvido =
                findViewById(
                        R.id.btnMarcarResolvido
                );

        tvDuracaoVazamento =
                findViewById(
                        R.id.tvDuracaoVazamento
                );

        tvAguaPerdida =
                findViewById(
                        R.id.tvAguaPerdida
                );

        tvVazaoAtualDetalhe =
                findViewById(
                        R.id.tvVazaoAtualDetalhe
                );

        firestore =
                FirebaseFirestore.getInstance();

        alertaManager =
                AlertaVazamentoManager
                        .getInstance();

        // ========================================================
        // VOLTAR
        // ========================================================

        btnVoltar.setOnClickListener(
                v -> finish()
        );

        // ========================================================
        // VER COMO CONSERTAR
        // ========================================================

        btnVerComoConsertar.setOnClickListener(
                v -> {

                    Toast.makeText(
                            this,
                            "Abrir instruções de conserto",
                            Toast.LENGTH_SHORT
                    ).show();

                }
        );

        // ========================================================
        // MARCAR COMO RESOLVIDO
        // ========================================================

        btnMarcarResolvido.setOnClickListener(
                v -> marcarComoResolvido()
        );

        // ========================================================
        // FIRESTORE
        // ========================================================

        ouvirEstado();
    }

    // ============================================================
    // MARCAR COMO RESOLVIDO
    // ============================================================

    private void marcarComoResolvido() {

        android.util.Log.d(
                "VAZAMENTO",
                "Botão Marcar como Resolvido pressionado."
        );

        // ========================================================
        // 1. AVISA O DETECTOR / MANAGER
        // ========================================================

        alertaManager.marcarComoResolvido();

        // ========================================================
        // 2. PARA A ATUALIZAÇÃO DA TELA
        // ========================================================

        pararAtualizacaoTela();

        vazamentoAtivo = false;

        inicioVazamento = 0;

        aguaPerdida = 0.0;

        // ========================================================
        // 3. ATUALIZA O FIRESTORE
        // ========================================================

        firestore.collection("estado")
                .document("principal")
                .update(
                        "vazamento",
                        false,

                        "inicioVazamento",
                        0L,

                        "aguaPerdida",
                        0.0
                )
                .addOnSuccessListener(
                        unused -> {

                            android.util.Log.d(
                                    "VAZAMENTO",
                                    "Firestore atualizado: "
                                            + "vazamento=false"
                            );

                            Toast.makeText(
                                    this,
                                    "Vazamento marcado como resolvido!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // =================================================
                            // REMOVE O LISTENER
                            // =================================================

                            if (listenerEstado != null) {

                                listenerEstado.remove();

                                listenerEstado = null;
                            }

                            // =================================================
                            // FECHA A TELA
                            // =================================================

                            finish();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            android.util.Log.e(
                                    "VAZAMENTO",
                                    "Erro ao atualizar Firestore",
                                    e
                            );

                            Toast.makeText(
                                    this,
                                    "Erro ao marcar como resolvido.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Caso dê erro, voltamos a considerar
                            // o vazamento ativo na tela.

                            vazamentoAtivo = true;

                            if (inicioVazamento == 0) {

                                inicioVazamento =
                                        System.currentTimeMillis();
                            }

                            iniciarAtualizacaoTela();
                        }
                );
    }

    // ============================================================
    // OUVIR FIRESTORE EM TEMPO REAL
    // ============================================================

    private void ouvirEstado() {

        listenerEstado =
                firestore.collection("estado")
                        .document("principal")
                        .addSnapshotListener(
                                (document, error) -> {

                                    if (error != null) {

                                        android.util.Log.e(
                                                "VAZAMENTO",
                                                "Erro ao ouvir estado",
                                                error
                                        );

                                        return;
                                    }

                                    if (document == null
                                            || !document.exists()) {

                                        return;
                                    }

                                    // =================================
                                    // VAZÃO
                                    // =================================

                                    Double vazao =
                                            document.getDouble(
                                                    "litrosMinuto"
                                            );

                                    if (vazao != null) {

                                        tvVazaoAtualDetalhe
                                                .setText(
                                                        String.format(
                                                                Locale.getDefault(),
                                                                "%.1f L/min",
                                                                vazao
                                                        )
                                                );
                                    }

                                    // =================================
                                    // VAZAMENTO
                                    // =================================

                                    Boolean vazamento =
                                            document.getBoolean(
                                                    "vazamento"
                                            );

                                    boolean novoEstado =
                                            Boolean.TRUE.equals(
                                                    vazamento
                                            );

                                    // =================================
                                    // ÁGUA PERDIDA
                                    // =================================

                                    Double agua =
                                            document.getDouble(
                                                    "aguaPerdida"
                                            );

                                    if (agua != null) {

                                        aguaPerdida =
                                                agua;

                                        tvAguaPerdida
                                                .setText(
                                                        String.format(
                                                                Locale.getDefault(),
                                                                "%.1f L",
                                                                aguaPerdida
                                                        )
                                                );
                                    }

                                    // =================================
                                    // INÍCIO DO VAZAMENTO
                                    // =================================

                                    Long inicio =
                                            document.getLong(
                                                    "inicioVazamento"
                                            );

                                    if (inicio != null
                                            && inicio > 0) {

                                        inicioVazamento =
                                                inicio;
                                    }

                                    // =================================
                                    // ESTADO DA TELA
                                    // =================================

                                    if (novoEstado) {

                                        vazamentoAtivo =
                                                true;

                                        tvDuracaoVazamento
                                                .setText(
                                                        "Vazamento detectado"
                                                );

                                        iniciarAtualizacaoTela();

                                    } else {

                                        vazamentoAtivo =
                                                false;

                                        inicioVazamento =
                                                0;

                                        aguaPerdida =
                                                0.0;

                                        tvDuracaoVazamento
                                                .setText(
                                                        "Nenhum vazamento"
                                                );

                                        tvAguaPerdida
                                                .setText(
                                                        "0.0 L"
                                                );

                                        pararAtualizacaoTela();
                                    }
                                }
                        );
    }

    // ============================================================
    // INICIA ATUALIZAÇÃO
    // ============================================================

    private void iniciarAtualizacaoTela() {

        handler.removeCallbacks(
                atualizadorTela
        );

        handler.post(
                atualizadorTela
        );
    }

    // ============================================================
    // PARA ATUALIZAÇÃO
    // ============================================================

    private void pararAtualizacaoTela() {

        handler.removeCallbacks(
                atualizadorTela
        );
    }

    // ============================================================
    // ATUALIZA DURAÇÃO
    // ============================================================

    private void atualizarDuracao() {

        if (inicioVazamento <= 0) {
            return;
        }

        long agora =
                System.currentTimeMillis();

        long diferenca =
                agora - inicioVazamento;

        long segundos =
                diferenca / 1000;

        long minutos =
                segundos / 60;

        segundos =
                segundos % 60;

        long horas =
                minutos / 60;

        minutos =
                minutos % 60;

        String texto;

        if (horas > 0) {

            texto =
                    String.format(
                            Locale.getDefault(),
                            "%dh %02dmin",
                            horas,
                            minutos
                    );

        } else {

            texto =
                    String.format(
                            Locale.getDefault(),
                            "%dmin %02ds",
                            minutos,
                            segundos
                    );
        }

        tvDuracaoVazamento.setText(
                texto
        );
    }

    // ============================================================
    // ÁGUA PERDIDA
    // ============================================================

    private void atualizarAguaPerdida() {

        tvAguaPerdida.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f L",
                        aguaPerdida
                )
        );
    }

    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @Override
    protected void onDestroy() {

        pararAtualizacaoTela();

        if (listenerEstado != null) {

            listenerEstado.remove();

            listenerEstado = null;
        }

        super.onDestroy();
    }
}