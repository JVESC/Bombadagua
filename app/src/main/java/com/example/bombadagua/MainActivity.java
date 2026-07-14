package com.example.bombadagua;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

    private static final int CODIO_PERMISSAO_BLUETOOTH = 100;
    private static final String NOME_HC05 = "HC-05";

    private BluetoothHelper bluetoothHelper;
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

        debugarFirestore();

        Button btnVerAlerta = findViewById(R.id.btnVerAlerta);
        Button btnVerHistorico = findViewById(R.id.btnVerHistorico);

        FirestoreHelper firestoreHelper = new FirestoreHelper();
        firestoreHelper.observarUltimaLeitura(new FirestoreHelper.OnLeituraListener() {
            @SuppressLint("DefaultLocale")
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

    private void debugarFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("leituras")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        android.util.Log.e("FIREBASE_DEBUG", "ESTRUTURA COMPLETA: " + doc.getData());
                    }
                })
                .addOnFailureListener(e -> android.util.Log.e("FIREBASE_DEBUG", "Erro ao buscar: " + e.getMessage()));
    }

    private void verificarPermissoesEConectar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean temConnect = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;

            if (!temConnect) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        CODIO_PERMISSAO_BLUETOOTH);
                return;
            }
        }
        conectarComArduino();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIO_PERMISSAO_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                conectarComArduino();
            } else {
                Toast.makeText(this, "Permissão de Bluetooth negada.",
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

    private void processarDados(String linha) {
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