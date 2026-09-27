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

import com.example.bombadagua.App;
import com.example.bombadagua.R;
import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.repository.RepositorioFluxo;

import java.util.Locale;

public class VazamentoActivity
        extends AppCompatActivity {

    private TextView tvDuracaoVazamento;
    private TextView tvAguaPerdida;
    private TextView tvVazaoAtualDetalhe;

    private RepositorioFluxo repositorioFluxo;

    // =============================================================
    // LISTENER
    // =============================================================
    //
    // Recebe o MESMO dado, já processado pelo DetectorVazamento,
    // que a MainActivity recebe. Antes, esta tela lia o valor
    // "cru" direto do Firestore (o que a ESP mandou), o que podia
    // divergir do que o app calculava internamente.
    // =============================================================

    private final RepositorioFluxo.Listener listener =
            new RepositorioFluxo.Listener() {

                @Override
                public void onNovoFluxo(DadosEsp32 dados) {
                    atualizarDados(dados);
                }

                @Override
                public void onErro(String erro) {

                    tvVazaoAtualDetalhe.setText("--");
                    tvAguaPerdida.setText("--");
                    tvDuracaoVazamento.setText("--");
                }
            };

    private final Handler handler =
            new Handler(
                    Looper.getMainLooper()
            );

    private long inicioVazamento = 0;

    private double aguaPerdida = 0.0;

    private double vazaoAtual = 0.0;

    private boolean vazamentoAtivo = false;

    // =============================================================
    // ATUALIZAÇÃO DA TELA
    // =============================================================

    private final Runnable atualizadorTela =
            new Runnable() {

                @Override
                public void run() {

                    if (
                            vazamentoAtivo &&
                                    inicioVazamento > 0
                    ) {

                        atualizarDuracao();

                        atualizarAguaPerdida();

                        handler.postDelayed(
                                this,
                                1000
                        );
                    }
                }
            };

    // =============================================================
    // ON CREATE
    // =============================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

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

        // =========================================================
        // COMPONENTES
        // =========================================================

        LinearLayout btnVoltar =
                findViewById(
                        R.id.btnVoltar
                );

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

        // =========================================================
        // VOLTAR
        // =========================================================

        btnVoltar.setOnClickListener(
                v -> finish()
        );

        // =========================================================
        // COMO CONSERTAR
        // =========================================================

        btnVerComoConsertar.setOnClickListener(
                v ->
                        Toast.makeText(
                                this,
                                "Abrir instruções de conserto",
                                Toast.LENGTH_SHORT
                        ).show()
        );

        // =========================================================
        // MARCAR RESOLVIDO
        // =========================================================
        //
        // NÃO ESCREVEMOS NO FIRESTORE.
        //
        // O DetectorVazamento controla o estado local.
        //
        // =========================================================

        btnMarcarResolvido.setOnClickListener(
                v -> {

                    App.getRepositorioFluxo()
                            .marcarVazamentoComoResolvido();

                    Toast.makeText(
                            this,
                            "Vazamento marcado como resolvido!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // =========================================================
        // INSCREVER NO REPOSITÓRIO COMPARTILHADO
        // =========================================================
        //
        // Usamos o mesmo RepositorioFluxo que a MainActivity usa,
        // em vez de abrir uma segunda conexão direta ao Firestore.
        // Assim os dois lugares mostram sempre o mesmo resultado.
        // =========================================================

        repositorioFluxo = App.getRepositorioFluxo();

        if (repositorioFluxo != null) {
            repositorioFluxo.adicionarListener(listener);
        }
    }

    // =============================================================
    // ATUALIZAR DADOS
    // =============================================================

    private void atualizarDados(
            DadosEsp32 dados
    ) {

        vazaoAtual =
                dados.getLitrosMinuto();

        tvVazaoAtualDetalhe.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f L/min",
                        vazaoAtual
                )
        );

        vazamentoAtivo =
                dados.isVazamento();

        inicioVazamento =
                dados.getInicioVazamento();

        aguaPerdida =
                dados.getAguaPerdida();

        if (vazamentoAtivo) {

            atualizarDuracao();

            atualizarAguaPerdida();

            handler.removeCallbacks(
                    atualizadorTela
            );

            handler.post(
                    atualizadorTela
            );

        } else {

            handler.removeCallbacks(
                    atualizadorTela
            );

            tvDuracaoVazamento.setText(
                    "0s"
            );

            tvAguaPerdida.setText(
                    "0.0 L"
            );
        }
    }

    // =============================================================
    // DURAÇÃO
    // =============================================================

    private void atualizarDuracao() {

        if (inicioVazamento <= 0) {

            tvDuracaoVazamento.setText(
                    "0s"
            );

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
                            "%dh %02dm %02ds",
                            horas,
                            minutos,
                            segundos
                    );

        } else if (minutos > 0) {

            texto =
                    String.format(
                            Locale.getDefault(),
                            "%dm %02ds",
                            minutos,
                            segundos
                    );

        } else {

            texto =
                    String.format(
                            Locale.getDefault(),
                            "%ds",
                            segundos
                    );
        }

        tvDuracaoVazamento.setText(
                texto
        );
    }

    // =============================================================
    // ÁGUA PERDIDA
    // =============================================================

    private void atualizarAguaPerdida() {

        tvAguaPerdida.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f L",
                        aguaPerdida
                )
        );
    }

    // =============================================================
    // ON DESTROY
    // =============================================================

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        if (repositorioFluxo != null) {
            repositorioFluxo.removerListener(listener);
        }

        super.onDestroy();
    }
}