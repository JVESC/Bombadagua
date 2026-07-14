package com.example.bombadagua;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_PERMISSAO_BLUETOOTH = 100;

    // Troque pelo nome exato do seu módulo (aparece nas configs de Bluetooth do celular)
    private static final String NOME_HC05 = "HC-05";

    private BluetoothHelper bluetoothHelper;

    // Views que serão atualizadas com os dados recebidos do Arduino
    private TextView tvConsumoHoje;
    private TextView tvVazaoAtual;
    private TextView tvAguaPoupada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_fullscreen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvConsumoHoje = findViewById(R.id.tvConsumoHoje);
        tvVazaoAtual = findViewById(R.id.tvVazaoAtual);
        tvAguaPoupada = findViewById(R.id.tvAguaPoupada);

        // ===== DEBUG DO FIREBASE =====
        debugarFirestore();
        // =============================

        Button btnVerAlerta = findViewById(R.id.btnVerAlerta);
        Button btnVerHistorico = findViewById(R.id.btnVerHistorico);

        FirestoreHelper firestoreHelper = new FirestoreHelper();

        firestoreHelper.observarUltimaLeitura(new FirestoreHelper.OnLeituraListener() {
            @Override
            public void onLeituraRecebida(double vazaoAtual, double litrosHoje, double aguaPoupada) {
                android.util.Log.e("FIRESTORE", "onLeituraRecebida CHAMADO! vazao=" + vazaoAtual);
                tvVazaoAtual.setText(String.format("%.1f L/min", vazaoAtual));
                tvConsumoHoje.setText(String.format("%.0f", litrosHoje));
                tvAguaPoupada.setText(String.format("%.0f L", aguaPoupada));
                android.util.Log.e("FIRESTORE", "Texto definido: " + tvVazaoAtual.getText());
            }

            @Override
            public void onErro(String mensagem) {
                android.util.Log.e("FIRESTORE", mensagem);
            }
        });

        btnVerAlerta.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VazamentoActivity.class);
            startActivity(intent);
        });

        btnVerHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });

        // Cria o helper: sempre que uma linha de dado chegar do Arduino, processarDados() é chamado
        bluetoothHelper = new BluetoothHelper(this::processarDados);

        bluetoothHelper.setStatusListener(new BluetoothHelper.OnStatusListener() {
            @Override
            public void onConectado() {
                Toast.makeText(MainActivity.this, "Conectado ao Arduino!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onErro(String mensagem) {
                Toast.makeText(MainActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });

        verificarPermissoesEConectar();
    }

    /**
     * Debug: mostra a estrutura completa do documento no Firestore
     */
    private void debugarFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("leituras")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        android.util.Log.e("FIREBASE_DEBUG", "ESTRUTURA COMPLETA: " + doc.getData().toString());
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FIREBASE_DEBUG", "Erro: " + e.getMessage());
                });
    }

    /**
     * Verifica se as permissões de Bluetooth foram concedidas.
     * Se não, pede pro usuário. Se sim, já tenta conectar.
     */
    private void verificarPermissoesEConectar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            boolean temConnect = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;

            if (!temConnect) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        CODIGO_PERMISSAO_BLUETOOTH);
                return;
            }
        }
        // Em versões antigas (< Android 12), não precisa pedir permissão em tempo de execução
        // para Bluetooth clássico, então já pode conectar direto.
        conectarComArduino();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISSAO_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                conectarComArduino();
            } else {
                Toast.makeText(this, "Permissão de Bluetooth negada. Não é possível conectar ao Arduino.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void conectarComArduino() {
        if (!bluetoothHelper.bluetoothDisponivel()) {
            Toast.makeText(this, "Esse aparelho não tem Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bluetoothHelper.bluetoothLigado()) {
            Toast.makeText(this, "Ative o Bluetooth do celular e tente novamente", Toast.LENGTH_LONG).show();
            return;
        }
        bluetoothHelper.conectarPorNome(NOME_HC05);
    }

    /**
     * TODO: Preencher quando o formato de dados do Arduino for decidido.
     *
     * Esse método é chamado toda vez que uma linha completa chega do Arduino
     * (já filtrada, sem quebra de linha, sem espaços nas pontas).
     *
     * Exemplos de como ficaria dependendo do formato escolhido:
     *
     * — Se for um número só (ex: "4.2"):
     *     double vazao = Double.parseDouble(linha);
     *     tvVazaoAtual.setText(String.format("%.1f L/min", vazao));
     *
     * — Se for vários valores separados por vírgula (ex: "4.2,86,312"):
     *     String[] partes = linha.split(",");
     *     double vazao = Double.parseDouble(partes[0]);
     *     int litrosHoje = Integer.parseInt(partes[1]);
     *     int aguaPoupada = Integer.parseInt(partes[2]);
     *     tvVazaoAtual.setText(String.format("%.1f L/min", vazao));
     *     tvConsumoHoje.setText(String.valueOf(litrosHoje));
     *     tvAguaPoupada.setText(aguaPoupada + " L");
     */
    private void processarDados(String linha) {
        // Por enquanto, só mostra no Logcat pra você ver os dados chegando
        android.util.Log.d("BLUETOOTH_ARDUINO", "Dado recebido: " + linha);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothHelper != null) {
            bluetoothHelper.desconectar();
        }
    }
}
