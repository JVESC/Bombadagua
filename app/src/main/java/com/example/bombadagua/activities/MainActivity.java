package com.example.bombadagua.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bombadagua.App;
import com.example.bombadagua.R;
import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.model.DispositivoEsp32;
import com.example.bombadagua.repository.DispositivoRepository;
import com.example.bombadagua.repository.RepositorioFluxo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    // =============================================================
    // COMPONENTES DA TELA
    // =============================================================

    private TextView tvConsumoHoje;
    private TextView tvVazaoAtual;
    private TextView tvStatusEsp;
    private TextView tvStatusVazamento;

    private AlertDialog dialogAtual;
    private TextView tvDispositivoSelecionado;

    private Button btnSelecionarEsp32;
    private Button btnAdicionarEsp32;

    private DrawerLayout drawerLayout;

    private Button btnConfigWifi;
    private TextView tvDicaConfigWifi;

    // =============================================================
    // REPOSITÓRIOS
    // =============================================================

    private RepositorioFluxo repositorioFluxo;

    private DispositivoRepository dispositivoRepository;

    // =============================================================
    // DISPOSITIVOS
    // =============================================================

    private final List<DispositivoEsp32> dispositivos =
            new ArrayList<>();

    private DispositivoEsp32 dispositivoSelecionado;

    // =============================================================
    // REDE
    // =============================================================

    private ConnectivityManager connectivityManager;

    private ConnectivityManager.NetworkCallback networkCallback;

    // =============================================================
    // LISTENER DO FLUXO
    // =============================================================


    private LinearLayout criarCardDispositivo(
            DispositivoEsp32 dispositivo,
            boolean selecionado
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(14)
        );

        // =============================================================
        // FUNDO DO CARD
        // =============================================================

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setCornerRadius(
                dp(16)
        );

        if (selecionado) {

            fundo.setColor(
                    Color.rgb(232, 244, 255)
            );

            fundo.setStroke(
                    dp(2),
                    Color.rgb(80, 150, 220)
            );

        } else {

            fundo.setColor(
                    Color.WHITE
            );

            fundo.setStroke(
                    dp(1),
                    Color.rgb(225, 225, 225)
            );
        }

        card.setBackground(
                fundo
        );

        // =============================================================
        // ÍCONE
        // =============================================================

        TextView icone =
                new TextView(this);

        icone.setText("💧");

        icone.setTextSize(25);

        icone.setGravity(
                Gravity.CENTER
        );

        GradientDrawable fundoIcone =
                new GradientDrawable();

        fundoIcone.setShape(
                GradientDrawable.OVAL
        );

        fundoIcone.setColor(
                Color.rgb(225, 241, 255)
        );

        icone.setBackground(
                fundoIcone
        );

        LinearLayout.LayoutParams paramsIcone =
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                );

        card.addView(
                icone,
                paramsIcone
        );

        // =============================================================
        // INFORMAÇÕES
        // =============================================================

        LinearLayout informacoes =
                new LinearLayout(this);

        informacoes.setOrientation(
                LinearLayout.VERTICAL
        );

        informacoes.setPadding(
                dp(14),
                0,
                dp(8),
                0
        );

        LinearLayout.LayoutParams paramsInfo =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        // NOME

        TextView nome =
                new TextView(this);

        String nomeDispositivo =
                dispositivo.getNome();

        if (nomeDispositivo == null
                || nomeDispositivo.trim().isEmpty()) {

            nomeDispositivo = "ESP32";
        }

        nome.setText(
                nomeDispositivo
        );

        nome.setTextSize(16);
        nome.setTextColor(Color.BLACK);
        nome.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        informacoes.addView(
                nome
        );

        // LOCAL

        TextView local =
                new TextView(this);

        String localDispositivo =
                dispositivo.getLocal();

        if (localDispositivo == null
                || localDispositivo.trim().isEmpty()) {

            localDispositivo =
                    "Local não informado";
        }

        local.setText(
                localDispositivo
        );

        local.setTextSize(13);
        local.setTextColor(
                Color.rgb(110, 110, 110)
        );

        local.setPadding(
                0,
                dp(3),
                0,
                0
        );

        informacoes.addView(
                local
        );

        // ID

        TextView id =
                new TextView(this);

        id.setText(
                dispositivo.getDispositivoId()
        );

        id.setTextSize(11);
        id.setTextColor(
                Color.rgb(150, 150, 150)
        );

        id.setPadding(
                0,
                dp(4),
                0,
                0
        );

        informacoes.addView(
                id
        );

        card.addView(
                informacoes,
                paramsInfo
        );

        // =============================================================
        // INDICADOR DE SELEÇÃO
        // =============================================================

        TextView check =
                new TextView(this);

        check.setText(
                selecionado ? "✓" : "›"
        );

        check.setTextSize(
                selecionado ? 24 : 28
        );

        check.setTextColor(
                selecionado
                        ? Color.rgb(45, 125, 70)
                        : Color.rgb(140, 140, 140)
        );

        check.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams paramsCheck =
                new LinearLayout.LayoutParams(
                        dp(36),
                        dp(48)
                );

        card.addView(
                check,
                paramsCheck
        );

        return card;
    }
    private final RepositorioFluxo.Listener listener =
            new RepositorioFluxo.Listener() {

                @Override
                public void onNovoFluxo(
                        DadosEsp32 dados
                ) {

                    if (dados == null) {
                        return;
                    }

                    // -------------------------------------------------
                    // VAZÃO
                    // -------------------------------------------------

                    tvVazaoAtual.setText(
                            String.format(
                                    "%.2f L/min",
                                    dados.getLitrosMinuto()
                            )
                    );

                    // -------------------------------------------------
                    // CONSUMO
                    // -------------------------------------------------

                    tvConsumoHoje.setText(
                            String.format(
                                    "%.2f L",
                                    dados.getLitrosHoje()
                            )
                    );

                    // -------------------------------------------------
                    // STATUS ESP32
                    // -------------------------------------------------

                    if (tvStatusEsp != null) {

                        if (dados.isOnline()) {

                            tvStatusEsp.setText(
                                    "🟢 Online"
                            );

                            tvStatusEsp.setTextColor(
                                    0xFF2E7D32
                            );

                        } else {

                            tvStatusEsp.setText(
                                    "🔴 Offline — verifique a ESP32"
                            );

                            tvStatusEsp.setTextColor(
                                    0xFFC62828
                            );
                        }
                    }

                    // -------------------------------------------------
                    // VAZAMENTO
                    // -------------------------------------------------

                    if (tvStatusVazamento != null) {

                        if (dados.isVazamento()) {

                            tvStatusVazamento.setText(
                                    "Detectado"
                            );

                            tvStatusVazamento.setTextColor(
                                    0xFFC62828
                            );

                        } else {

                            tvStatusVazamento.setText(
                                    "Nenhum"
                            );

                            tvStatusVazamento.setTextColor(
                                    0xFF2E7D32
                            );
                        }
                    }
                }

                @Override
                public void onErro(
                        String erro
                ) {

                    Log.e(
                            "MAIN_ACTIVITY",
                            "Erro no fluxo: " + erro
                    );

                    tvVazaoAtual.setText(
                            "--"
                    );

                    if (tvStatusEsp != null) {

                        tvStatusEsp.setText(
                                "🔴 Offline — verifique a ESP32"
                        );

                        tvStatusEsp.setTextColor(
                                0xFFC62828
                        );
                    }

                    if (tvStatusVazamento != null) {

                        tvStatusVazamento.setText(
                                "--"
                        );

                        tvStatusVazamento.setTextColor(
                                0xFF000000
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

        Log.d(
                "TESTE",
                "MainActivity criada"
        );

        EdgeToEdge.enable(
                this
        );

        setContentView(
                R.layout.activity_main_fullscreen
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

        tvConsumoHoje =
                findViewById(
                        R.id.tvConsumoHoje
                );

        tvVazaoAtual =
                findViewById(
                        R.id.tvVazaoAtual
                );

        tvStatusEsp =
                findViewById(
                        R.id.tvStatusEsp
                );

        tvStatusVazamento =
                findViewById(
                        R.id.tvStatusVazamento
                );

        tvDispositivoSelecionado =
                findViewById(
                        R.id.tvDispositivoSelecionado
                );

        btnSelecionarEsp32 =
                findViewById(
                        R.id.btnSelecionarEsp32
                );

        btnAdicionarEsp32 =
                findViewById(
                        R.id.btnAdicionarEsp32
                );

        drawerLayout =
                findViewById(
                        R.id.drawerLayout
                );

        ImageView btnMenu =
                findViewById(
                        R.id.btnMenu
                );

        Button btnSairConta =
                findViewById(
                        R.id.btnSairConta
                );

        btnConfigWifi =
                findViewById(
                        R.id.btnConfigWifi
                );

        tvDicaConfigWifi =
                findViewById(
                        R.id.tvDicaConfigWifi
                );

        // =========================================================
        // REPOSITÓRIOS
        // =========================================================

        repositorioFluxo =
                App.getRepositorioFluxo();

        dispositivoRepository =
                new DispositivoRepository();

        // =========================================================
        // CONECTIVIDADE
        // =========================================================

        connectivityManager =
                (ConnectivityManager)
                        getSystemService(
                                Context.CONNECTIVITY_SERVICE
                        );

        atualizarDisponibilidadeConfigWifi();

        // =========================================================
        // MENU
        // =========================================================

        btnMenu.setOnClickListener(
                v -> drawerLayout.openDrawer(
                        GravityCompat.END
                )
        );

        // =========================================================
        // SAIR DA CONTA
        // =========================================================

        btnSairConta.setOnClickListener(
                v -> {

                    FirebaseAuth
                            .getInstance()
                            .signOut();

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    BoasVindasActivity.class
                            );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(
                            intent
                    );

                    finish();
                }
        );

        // =========================================================
        // CONFIGURAR WI-FI
        // =========================================================

        btnConfigWifi.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    ConfigWifiActivity.class
                            );

                    startActivity(
                            intent
                    );
                }
        );

        // =========================================================
        // SELECIONAR ESP32
        // =========================================================

        btnSelecionarEsp32.setOnClickListener(
                v -> mostrarSeletorEsp32()
        );

        // =========================================================
        // ADICIONAR ESP32
        // =========================================================

        btnAdicionarEsp32.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    VincularEsp32Activity.class
                            );

                    startActivityForResult(
                            intent,
                            1001
                    );
                }
        );

        // =========================================================
        // ALERTA
        // =========================================================

        Button btnVerAlerta =
                findViewById(
                        R.id.btnVerAlerta
                );

        btnVerAlerta.setOnClickListener(
                v ->
                        startActivity(
                                new Intent(
                                        this,
                                        VazamentoActivity.class
                                )
                        )
        );

        // =========================================================
        // HISTÓRICO
        // =========================================================

        Button btnVerHistorico =
                findViewById(
                        R.id.btnVerHistorico
                );

        btnVerHistorico.setOnClickListener(
                v ->
                        startActivity(
                                new Intent(
                                        this,
                                        HistoricoActivity.class
                                )
                        )
        );

        // =========================================================
        // FIREBASE MESSAGING
        // =========================================================

        FirebaseMessaging
                .getInstance()
                .getToken()
                .addOnCompleteListener(
                        task -> {

                            if (!task.isSuccessful()) {

                                Log.e(
                                        "FCM",
                                        "Erro ao obter token",
                                        task.getException()
                                );

                                return;
                            }

                            String token =
                                    task.getResult();

                            Log.d(
                                    "FCM",
                                    "Token: " + token
                            );

                            FirebaseUser usuario =
                                    FirebaseAuth
                                            .getInstance()
                                            .getCurrentUser();

                            if (usuario != null) {

                                FirebaseFirestore
                                        .getInstance()
                                        .collection("usuarios")
                                        .document(
                                                usuario.getUid()
                                        )
                                        .update(
                                                "tokenFCM",
                                                token
                                        )
                                        .addOnSuccessListener(
                                                unused ->
                                                        Log.d(
                                                                "FCM",
                                                                "Token salvo no Firestore"
                                                        )
                                        )
                                        .addOnFailureListener(
                                                e ->
                                                        Log.e(
                                                                "FCM",
                                                                "Erro ao salvar token",
                                                                e
                                                        )
                                        );
                            }
                        }
                );

        // =========================================================
        // CARREGAR ESP32
        // =========================================================

        carregarDispositivos();
    }

    // =============================================================
    // CARREGAR DISPOSITIVOS DA CONTA
    // =============================================================

    private void carregarDispositivos() {

        dispositivoRepository.buscarDispositivos(
                new DispositivoRepository
                        .OnDispositivosCarregadosListener() {

                    @Override
                    public void onSucesso(
                            List<DispositivoEsp32> lista
                    ) {

                        dispositivos.clear();

                        dispositivos.addAll(
                                lista
                        );

                        Log.d(
                                "MAIN_ACTIVITY",
                                "Dispositivos carregados: "
                                        + dispositivos.size()
                        );

                        if (dispositivos.isEmpty()) {

                            mostrarSemDispositivos();

                            return;
                        }

                        restaurarDispositivoSelecionado();
                    }

                    @Override
                    public void onErro(
                            String mensagem
                    ) {

                        Log.e(
                                "MAIN_ACTIVITY",
                                mensagem
                        );

                        Toast.makeText(
                                MainActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =============================================================
    // RESTAURAR DISPOSITIVO SELECIONADO
    // =============================================================

    private void restaurarDispositivoSelecionado() {

        String idSalvo =
                getSharedPreferences(
                        "dispositivo_selecionado",
                        MODE_PRIVATE
                )
                        .getString(
                                "dispositivoId",
                                null
                        );

        if (idSalvo != null) {

            for (
                    DispositivoEsp32 dispositivo :
                    dispositivos
            ) {

                if (
                        idSalvo.equals(
                                dispositivo.getDispositivoId()
                        )
                ) {

                    selecionarDispositivo(
                            dispositivo
                    );

                    return;
                }
            }
        }

        selecionarDispositivo(
                dispositivos.get(0)
        );
    }

    // =============================================================
    // SELECIONAR DISPOSITIVO
    // =============================================================

    private void selecionarDispositivo(
            DispositivoEsp32 dispositivo
    ) {

        if (dispositivo == null) {
            return;
        }

        dispositivoSelecionado = dispositivo;

        // =============================================================
        // SALVAR ESP SELECIONADA
        // =============================================================

        getSharedPreferences(
                "dispositivo_selecionado",
                MODE_PRIVATE
        )
                .edit()
                .putString(
                        "dispositivoId",
                        dispositivo.getDispositivoId()
                )
                .apply();

        // =============================================================
        // MOSTRAR ESP NO BOTÃO PRINCIPAL
        // =============================================================

        String nome = dispositivo.getNome();

        if (nome == null || nome.trim().isEmpty()) {
            nome = "ESP32";
        }

        btnSelecionarEsp32.setText(
                "💧  " + nome + "   ▼"
        );

        // =============================================================
        // INFORMAÇÃO SECUNDÁRIA
        // =============================================================

        String local = dispositivo.getLocal();

        if (local != null && !local.trim().isEmpty()) {

            tvDispositivoSelecionado.setText(
                    "Monitorando • " + local
            );

        } else {

            tvDispositivoSelecionado.setText(
                    "ESP32 selecionada"
            );
        }

        // =============================================================
        // INFORMAR AO REPOSITÓRIO
        // =============================================================

        repositorioFluxo.selecionarDispositivo(
                dispositivo.getDispositivoId()
        );

        Log.d(
                "MAIN_ACTIVITY",
                "ESP32 selecionada: "
                        + dispositivo.getDispositivoId()
        );
    }

    // =============================================================
    // SELETOR DE ESP32
    // =============================================================

    private void mostrarSeletorEsp32() {

        if (dispositivos.isEmpty()) {
            mostrarSemDispositivos();
            return;
        }

        // =============================================================
        // CONTAINER PRINCIPAL
        // =============================================================

        LinearLayout container = new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dp(20),
                dp(8),
                dp(20),
                dp(8)
        );

        // =============================================================
        // SUBTÍTULO
        // =============================================================

        TextView subtitulo = new TextView(this);

        subtitulo.setText(
                "Escolha qual ESP32 você deseja monitorar"
        );

        subtitulo.setTextSize(14);
        subtitulo.setTextColor(
                Color.rgb(100, 100, 100)
        );

        subtitulo.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(16)
        );

        container.addView(
                subtitulo
        );

        // =============================================================
        // CARDS DAS ESP32
        // =============================================================

        for (DispositivoEsp32 dispositivo : dispositivos) {

            boolean selecionado =
                    dispositivoSelecionado != null
                            &&
                            dispositivo.getDispositivoId()
                                    .equals(
                                            dispositivoSelecionado
                                                    .getDispositivoId()
                                    );

            LinearLayout card =
                    criarCardDispositivo(
                            dispositivo,
                            selecionado
                    );

            card.setOnClickListener(v -> {

                selecionarDispositivo(
                        dispositivo
                );

                if (dialogAtual != null) {
                    dialogAtual.dismiss();
                }
            });

            container.addView(card);

            // Espaço entre os cards
            LinearLayout.LayoutParams espaco =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(10)
                    );

            View separador = new View(this);

            separador.setLayoutParams(
                    espaco
            );

            container.addView(
                    separador
            );
        }

        // =============================================================
        // DIALOG
        // =============================================================

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Selecionar ESP32")
                        .setView(container)
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .create();

        dialogAtual = dialog;

        dialog.setOnDismissListener(
                d -> dialogAtual = null
        );

        dialog.show();
    }

    // =============================================================
    // SEM DISPOSITIVOS
    // =============================================================

    private void mostrarSemDispositivos() {

        tvDispositivoSelecionado.setText(
                "Nenhuma ESP32 vinculada"
        );

        btnSelecionarEsp32.setEnabled(
                false
        );

        new AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Nenhuma ESP32"
                )
                .setMessage(
                        "Você ainda não possui uma ESP32 vinculada à sua conta."
                )
                .setPositiveButton(
                        "Vincular ESP32",
                        (dialog, which) -> {

                            startActivityForResult(
                                    new Intent(
                                            this,
                                            VincularEsp32Activity.class
                                    ),
                                    1001
                            );
                        }
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .show();
    }

    // =============================================================
    // RESULTADO DA TELA DE VINCULAÇÃO
    // =============================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == 1001
                        &&
                        resultCode == RESULT_OK
        ) {

            btnSelecionarEsp32.setEnabled(
                    true
            );

            carregarDispositivos();
        }
    }

    // =============================================================
    // DISPONIBILIDADE DO CONFIGURAR WI-FI
    // =============================================================

    private boolean estaConectadoWifi() {

        if (connectivityManager == null) {
            return false;
        }

        Network rede =
                connectivityManager.getActiveNetwork();

        if (rede == null) {
            return false;
        }

        NetworkCapabilities capacidades =
                connectivityManager
                        .getNetworkCapabilities(
                                rede
                        );

        return capacidades != null
                &&
                capacidades.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                );
    }

    private void atualizarDisponibilidadeConfigWifi() {

        boolean conectadoWifi =
                estaConectadoWifi();

        if (btnConfigWifi != null) {

            btnConfigWifi.setEnabled(
                    conectadoWifi
            );

            btnConfigWifi.setAlpha(
                    conectadoWifi
                            ? 1f
                            : 0.4f
            );
        }

        if (tvDicaConfigWifi != null) {

            tvDicaConfigWifi.setVisibility(
                    conectadoWifi
                            ? View.GONE
                            : View.VISIBLE
            );
        }
    }

    // =============================================================
    // CICLO DE VIDA
    // =============================================================

    @Override
    protected void onStart() {

        super.onStart();

        Log.d(
                "TESTE",
                "onStart"
        );

        /*
         * O listener da Activity deve ser registrado sempre que
         * a Activity estiver visível.
         *
         * O RepositorioFluxo decide se já existe uma ESP32
         * selecionada e, se existir, inicia o monitoramento.
         */
        if (repositorioFluxo != null) {

            repositorioFluxo.adicionarListener(
                    listener
            );
        }

        // =========================================================
        // MONITORAR CONECTIVIDADE
        // =========================================================

        if (connectivityManager != null) {

            networkCallback =
                    new ConnectivityManager
                            .NetworkCallback() {

                        @Override
                        public void onAvailable(
                                Network network
                        ) {

                            runOnUiThread(
                                    MainActivity.this::
                                            atualizarDisponibilidadeConfigWifi
                            );
                        }

                        @Override
                        public void onLost(
                                Network network
                        ) {

                            runOnUiThread(
                                    MainActivity.this::
                                            atualizarDisponibilidadeConfigWifi
                            );
                        }

                        @Override
                        public void onCapabilitiesChanged(
                                Network network,
                                NetworkCapabilities capacidades
                        ) {

                            runOnUiThread(
                                    MainActivity.this::
                                            atualizarDisponibilidadeConfigWifi
                            );
                        }
                    };

            NetworkRequest pedido =
                    new NetworkRequest.Builder()
                            .build();

            connectivityManager
                    .registerNetworkCallback(
                            pedido,
                            networkCallback
                    );

            atualizarDisponibilidadeConfigWifi();
        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (repositorioFluxo != null) {

            repositorioFluxo.removerListener(
                    listener
            );
        }

        if (
                connectivityManager != null
                        &&
                        networkCallback != null
        ) {

            connectivityManager
                    .unregisterNetworkCallback(
                            networkCallback
                    );

            networkCallback = null;
        }
    }
    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}