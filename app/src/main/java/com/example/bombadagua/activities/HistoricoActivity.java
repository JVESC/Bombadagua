package com.example.bombadagua.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bombadagua.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HistoricoActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private LinearLayout listaVazamentos;

    private TextView tvFiltroAtual;
    private TextView tvEventosResolvidos;
    private TextView tvAguaPoupadaHistorico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_historico);

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
        // FIRESTORE
        // ========================================================

        firestore =
                FirebaseFirestore.getInstance();

        // ========================================================
        // COMPONENTES
        // ========================================================

        LinearLayout btnVoltar =
                findViewById(R.id.btnVoltarHistorico);

        listaVazamentos =
                findViewById(R.id.listaVazamentos);

        tvFiltroAtual =
                findViewById(R.id.tvFiltroAtual);

        tvEventosResolvidos =
                findViewById(R.id.tvEventosResolvidos);

        tvAguaPoupadaHistorico =
                findViewById(R.id.tvAguaPoupadaHistorico);

        // ========================================================
        // VOLTAR
        // ========================================================

        btnVoltar.setOnClickListener(
                v -> finish()
        );

        // ========================================================
        // FILTROS
        // ========================================================

        TextView btnHoje =
                findViewById(R.id.btnFiltroHoje);

        TextView btnSeteDias =
                findViewById(R.id.btnFiltroSeteDias);

        TextView btnTrintaDias =
                findViewById(R.id.btnFiltroTrintaDias);

        TextView btnTodos =
                findViewById(R.id.btnFiltroTodos);

        btnHoje.setOnClickListener(
                v -> carregarHoje()
        );

        btnSeteDias.setOnClickListener(
                v -> carregarHistorico(
                        7,
                        "Últimos 7 dias"
                )
        );

        btnTrintaDias.setOnClickListener(
                v -> carregarHistorico(
                        30,
                        "Últimos 30 dias"
                )
        );

        btnTodos.setOnClickListener(
                v -> carregarTodos()
        );

        // ========================================================
        // FILTRO INICIAL
        // ========================================================

        carregarHistorico(
                7,
                "Últimos 7 dias"
        );
    }

    // ============================================================
    // HOJE
    // ============================================================

    private void carregarHoje() {

        tvFiltroAtual.setText(
                "Hoje"
        );

        listaVazamentos.removeAllViews();

        Calendar calendario =
                Calendar.getInstance();

        calendario.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendario.set(
                Calendar.MINUTE,
                0
        );

        calendario.set(
                Calendar.SECOND,
                0
        );

        calendario.set(
                Calendar.MILLISECOND,
                0
        );

        long inicio =
                calendario.getTimeInMillis();

        carregarPorPeriodo(
                inicio
        );
    }

    // ============================================================
    // CARREGAR HISTÓRICO POR DIAS
    // ============================================================

    private void carregarHistorico(
            int dias,
            String nomeFiltro
    ) {

        tvFiltroAtual.setText(
                nomeFiltro
        );

        listaVazamentos.removeAllViews();

        long agora =
                System.currentTimeMillis();

        long inicio =
                agora -
                        (dias * 24L * 60L * 60L * 1000L);

        carregarPorPeriodo(
                inicio
        );
    }

    // ============================================================
    // CARREGAR POR PERÍODO
    // ============================================================

    private void carregarPorPeriodo(
            long inicio
    ) {

        firestore.collection("vazamentos")
                .whereGreaterThanOrEqualTo(
                        "inicioVazamento",
                        inicio
                )
                .orderBy(
                        "inicioVazamento",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(
                        query -> {

                            atualizarResumo(
                                    query.size(),
                                    query
                            );

                            if (query.isEmpty()) {

                                mostrarMensagem(
                                        "Nenhum vazamento encontrado."
                                );

                                return;
                            }

                            for (
                                    QueryDocumentSnapshot document :
                                    query
                            ) {

                                adicionarVazamento(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            android.util.Log.e(
                                    "HISTORICO",
                                    "Erro ao carregar histórico",
                                    e
                            );

                            Toast.makeText(
                                    this,
                                    "Erro ao carregar histórico.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // ============================================================
    // CARREGAR TODOS
    // ============================================================

    private void carregarTodos() {

        tvFiltroAtual.setText(
                "Todos os vazamentos"
        );

        listaVazamentos.removeAllViews();

        firestore.collection("vazamentos")
                .orderBy(
                        "inicioVazamento",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(
                        query -> {

                            atualizarResumo(
                                    query.size(),
                                    query
                            );

                            if (query.isEmpty()) {

                                mostrarMensagem(
                                        "Nenhum vazamento encontrado."
                                );

                                return;
                            }

                            for (
                                    QueryDocumentSnapshot document :
                                    query
                            ) {

                                adicionarVazamento(
                                        document
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            android.util.Log.e(
                                    "HISTORICO",
                                    "Erro ao carregar histórico",
                                    e
                            );

                            Toast.makeText(
                                    this,
                                    "Erro ao carregar histórico.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }

    // ============================================================
    // ATUALIZAR RESUMO
    // ============================================================

    private void atualizarResumo(
            int quantidade,
            Iterable<QueryDocumentSnapshot> documentos
    ) {

        double totalAguaPerdida = 0.0;

        for (
                QueryDocumentSnapshot document :
                documentos
        ) {

            Double agua =
                    document.getDouble(
                            "aguaPerdida"
                    );

            if (agua != null) {

                totalAguaPerdida += agua;
            }
        }

        tvEventosResolvidos.setText(
                String.valueOf(quantidade)
        );

        tvAguaPoupadaHistorico.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f L",
                        totalAguaPerdida
                )
        );
    }

    // ============================================================
// ADICIONAR VAZAMENTO NA LISTA
// ============================================================

    private void adicionarVazamento(
            QueryDocumentSnapshot document
    ) {

        Long inicio =
                document.getLong(
                        "inicioVazamento"
                );

        Long fim =
                document.getLong(
                        "fimVazamento"
                );

        Long duracao =
                document.getLong(
                        "duracao"
                );

        Double aguaPerdida =
                document.getDouble(
                        "aguaPerdida"
                );

        if (inicio == null) {
            return;
        }

        // ========================================================
        // CONTAINER DO CARD
        // ========================================================

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                32,
                28,
                32,
                28
        );

        card.setBackgroundResource(
                R.drawable.bg_card_cinza
        );

        // ========================================================
        // FORMATOS DE DATA E HORA
        // ========================================================

        SimpleDateFormat formatoData =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        SimpleDateFormat formatoHora =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        SimpleDateFormat formatoDataHora =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                );

        // ========================================================
        // DATA + HORA
        // ========================================================

        LinearLayout linhaDataHora =
                new LinearLayout(this);

        linhaDataHora.setOrientation(
                LinearLayout.HORIZONTAL
        );

        linhaDataHora.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        // --------------------------------------------------------
        // DATA
        // --------------------------------------------------------

        TextView tvData =
                new TextView(this);

        tvData.setText(
                formatoData.format(
                        new Date(inicio)
                )
        );

        tvData.setTextSize(20);

        tvData.setTextColor(
                android.graphics.Color.rgb(
                        15,
                        15,
                        15
                )
        );

        tvData.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        // --------------------------------------------------------
        // HORA
        // --------------------------------------------------------

        TextView tvHora =
                new TextView(this);

        tvHora.setText(
                formatoHora.format(
                        new Date(inicio)
                )
        );

        tvHora.setTextSize(20);

        tvHora.setTextColor(
                android.graphics.Color.rgb(
                        15,
                        15,
                        15
                )
        );

        tvHora.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams horaParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        horaParams.setMargins(
                30,
                0,
                0,
                0
        );

        tvHora.setLayoutParams(
                horaParams
        );

        linhaDataHora.addView(
                tvData
        );

        linhaDataHora.addView(
                tvHora
        );

        // ========================================================
        // DURAÇÃO
        // ========================================================

        LinearLayout linhaDuracao =
                new LinearLayout(this);

        linhaDuracao.setOrientation(
                LinearLayout.HORIZONTAL
        );

        linhaDuracao.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        TextView tituloDuracao =
                new TextView(this);

        tituloDuracao.setText(
                "Duração:"
        );

        tituloDuracao.setTextSize(16);

        tituloDuracao.setTextColor(
                android.graphics.Color.rgb(
                        20,
                        20,
                        20
                )
        );

        tituloDuracao.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        tituloDuracao.setSingleLine(true);

        tituloDuracao.setWidth(
                (int) (130 * getResources().getDisplayMetrics().density)
        );

        TextView valorDuracao =
                new TextView(this);

        valorDuracao.setText(
                formatarDuracao(
                        duracao
                )
        );

        valorDuracao.setTextSize(16);

        valorDuracao.setTextColor(
                android.graphics.Color.rgb(
                        35,
                        35,
                        35
                )
        );

        linhaDuracao.addView(
                tituloDuracao
        );

        linhaDuracao.addView(
                valorDuracao
        );

        // ========================================================
        // ÁGUA PERDIDA
        // ========================================================

        LinearLayout linhaAgua =
                new LinearLayout(this);

        linhaAgua.setOrientation(
                LinearLayout.HORIZONTAL
        );

        linhaAgua.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        TextView tituloAgua =
                new TextView(this);

        tituloAgua.setText(
                "Água perdida:"
        );

        tituloAgua.setTextSize(16);

        tituloAgua.setTextColor(
                android.graphics.Color.rgb(
                        20,
                        20,
                        20
                )
        );

        tituloAgua.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        tituloAgua.setSingleLine(true);

        tituloAgua.setWidth(
                (int) (130 * getResources().getDisplayMetrics().density)
        );

        TextView valorAgua =
                new TextView(this);

        double litros =
                aguaPerdida != null
                        ? aguaPerdida
                        : 0.0;

        valorAgua.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f L",
                        litros
                )
        );

        valorAgua.setTextSize(16);

        valorAgua.setTextColor(
                android.graphics.Color.rgb(
                        35,
                        35,
                        35
                )
        );

        linhaAgua.addView(
                tituloAgua
        );

        linhaAgua.addView(
                valorAgua
        );

        // ========================================================
        // RESOLVIDO
        // ========================================================

        LinearLayout linhaResolvido =
                new LinearLayout(this);

        linhaResolvido.setOrientation(
                LinearLayout.HORIZONTAL
        );

        linhaResolvido.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        TextView tituloResolvido =
                new TextView(this);

        tituloResolvido.setText(
                "Resolvido:"
        );

        tituloResolvido.setTextSize(16);

        tituloResolvido.setTextColor(
                android.graphics.Color.rgb(
                        20,
                        20,
                        20
                )
        );

        tituloResolvido.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        tituloResolvido.setSingleLine(true);

        tituloResolvido.setWidth(
                (int) (130 * getResources().getDisplayMetrics().density)
        );

        TextView valorResolvido =
                new TextView(this);

        if (fim != null) {

            valorResolvido.setText(
                    formatoDataHora.format(
                            new Date(fim)
                    )
            );

        } else {

            valorResolvido.setText(
                    "Não informado"
            );
        }

        valorResolvido.setTextSize(16);

        valorResolvido.setTextColor(
                android.graphics.Color.rgb(
                        35,
                        35,
                        35
                )
        );

        linhaResolvido.addView(
                tituloResolvido
        );

        linhaResolvido.addView(
                valorResolvido
        );

        // ========================================================
        // ESPAÇAMENTO ENTRE AS LINHAS
        // ========================================================

        LinearLayout.LayoutParams espacamento =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        espacamento.setMargins(
                0,
                8,
                0,
                0
        );

        linhaDuracao.setLayoutParams(
                espacamento
        );

        linhaAgua.setLayoutParams(
                espacamento
        );

        linhaResolvido.setLayoutParams(
                espacamento
        );

        // ========================================================
        // ADICIONAR AO CARD
        // ========================================================

        card.addView(
                linhaDataHora
        );

        card.addView(
                linhaDuracao
        );

        card.addView(
                linhaAgua
        );

        card.addView(
                linhaResolvido
        );

        // ========================================================
        // ESPAÇAMENTO DO CARD
        // ========================================================

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                16
        );

        card.setLayoutParams(
                params
        );

        listaVazamentos.addView(
                card
        );

        // ========================================================
        // LINHA SEPARADORA
        // ========================================================

        View separador =
                new View(this);

        separador.setBackgroundColor(
                android.graphics.Color.rgb(
                        180,
                        180,
                        180
                )
        );

        LinearLayout.LayoutParams separadorParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                );

        separadorParams.setMargins(
                0,
                0,
                0,
                24
        );

        separador.setLayoutParams(
                separadorParams
        );

        listaVazamentos.addView(
                separador
        );
    }

    // ============================================================
    // FORMATAR DURAÇÃO
    // ============================================================

    private String formatarDuracao(
            Long duracao
    ) {

        if (duracao == null) {

            return "Não informado";
        }

        long segundos =
                duracao / 1000;

        long minutos =
                segundos / 60;

        segundos =
                segundos % 60;

        long horas =
                minutos / 60;

        minutos =
                minutos % 60;

        if (horas > 0) {

            return String.format(
                    Locale.getDefault(),
                    "%dh %02dmin",
                    horas,
                    minutos
            );
        }

        return String.format(
                Locale.getDefault(),
                "%dmin %02ds",
                minutos,
                segundos
        );
    }

    // ============================================================
    // NENHUM RESULTADO
    // ============================================================

    private void mostrarMensagem(
            String mensagem
    ) {

        TextView texto =
                new TextView(this);

        texto.setText(
                mensagem
        );

        texto.setTextSize(17);

        texto.setPadding(
                20,
                40,
                20,
                40
        );

        listaVazamentos.addView(
                texto
        );

    }

}