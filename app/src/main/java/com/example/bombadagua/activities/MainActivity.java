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

public class MainActivity extends AppCompatActivity {

    // =============================================================
    // COMPONENTES DA TELA
    // =============================================================

    private TextView tvConsumoHoje;
    private TextView tvVazaoAtual;
    private TextView tvStatusEsp;
    private TextView tvStatusVazamento;

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

        dispositivoSelecionado =
                dispositivo;

        // ---------------------------------------------------------
        // SALVAR PREFERÊNCIA
        // ---------------------------------------------------------

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

        // ---------------------------------------------------------
        // ATUALIZAR NOME NA TELA
        // ---------------------------------------------------------

        tvDispositivoSelecionado.setText(
                dispositivo.getNome()
                        + " • "
                        + dispositivo.getLocal()
        );

        // ---------------------------------------------------------
        // INFORMAR AO REPOSITÓRIO
        // ---------------------------------------------------------

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

        String[] nomes =
                new String[
                        dispositivos.size()
                        ];

        int dispositivoAtual = 0;

        for (
                int i = 0;
                i < dispositivos.size();
                i++
        ) {

            DispositivoEsp32 dispositivo =
                    dispositivos.get(i);

            nomes[i] =
                    dispositivo.getNome()
                            + "\n"
                            + dispositivo.getLocal();

            if (
                    dispositivoSelecionado != null
                            &&
                            dispositivo.getDispositivoId()
                                    .equals(
                                            dispositivoSelecionado
                                                    .getDispositivoId()
                                    )
            ) {

                dispositivoAtual = i;
            }
        }

        AlertDialog dialog =
                new AlertDialog.Builder(
                        this
                )
                        .setTitle(
                                "Selecionar ESP32"
                        )
                        .setSingleChoiceItems(
                                nomes,
                                dispositivoAtual,
                                (dialogInterface, which) -> {

                                    selecionarDispositivo(
                                            dispositivos.get(which)
                                    );

                                    dialogInterface.dismiss();
                                }
                        )
                        .setNegativeButton(
                                "Cancelar",
                                null
                        )
                        .create();

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

        if (repositorioFluxo != null) {

            /*
             * Só começa a leitura depois que uma ESP32
             * tiver sido selecionada.
             */
            if (
                    dispositivoSelecionado != null
            ) {

                repositorioFluxo
                        .adicionarListener(
                                listener
                        );
            }
        }

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
}