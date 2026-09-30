package com.example.bombadagua.activities;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Gravity;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bombadagua.R;
import com.example.bombadagua.model.DadosEsp32;
import com.example.bombadagua.repository.RepositorioFluxo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.bombadagua.App;

public class MainActivity extends AppCompatActivity {

    private TextView tvConsumoHoje;
    private TextView tvVazaoAtual;
    private TextView tvStatusEsp;
    private TextView tvStatusVazamento;

    private RepositorioFluxo repositorioFluxo;
    private DrawerLayout drawerLayout;

    // Guardamos a referência para poder remover exatamente
    // este mesmo listener depois (em onStop).
    private final RepositorioFluxo.Listener listener =
            new RepositorioFluxo.Listener() {

                @Override
                public void onNovoFluxo(DadosEsp32 dados) {

                    tvVazaoAtual.setText(
                            String.format("%.2f L/min",
                                    dados.getLitrosMinuto()));

                    tvConsumoHoje.setText(
                            String.format("%.2f L",
                                    dados.getLitrosHoje()));

                    if (tvStatusEsp != null) {

                        if (dados.isOnline()) {

                            tvStatusEsp.setText("🟢 Online");
                            tvStatusEsp.setTextColor(0xFF2E7D32);

                        } else {

                            tvStatusEsp.setText("🔴 Offline — verifique a ESP32");
                            tvStatusEsp.setTextColor(0xFFC62828);
                        }
                    }

                    if (tvStatusVazamento != null) {

                        if (dados.isVazamento()) {

                            tvStatusVazamento.setText("Detectado");
                            tvStatusVazamento.setTextColor(0xFFC62828);

                        } else {

                            tvStatusVazamento.setText("Nenhum");
                            tvStatusVazamento.setTextColor(0xFF2E7D32);
                        }
                    }
                }

                @Override
                public void onErro(String erro) {

                    tvVazaoAtual.setText("--");

                    if (tvStatusEsp != null) {
                        tvStatusEsp.setText("🔴 Offline — verifique a ESP32");
                        tvStatusEsp.setTextColor(0xFFC62828);
                    }

                    if (tvStatusVazamento != null) {
                        tvStatusVazamento.setText("--");
                        tvStatusVazamento.setTextColor(0xFF000000);
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        android.util.Log.d("TESTE", "MainActivity criada");
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_fullscreen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;

                });

        tvConsumoHoje = findViewById(R.id.tvConsumoHoje);
        tvVazaoAtual = findViewById(R.id.tvVazaoAtual);
        tvStatusEsp = findViewById(R.id.tvStatusEsp);
        tvStatusVazamento = findViewById(R.id.tvStatusVazamento);
        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        Button btnSairConta = findViewById(R.id.btnSairConta);
        Button btnConfigWifi = findViewById(R.id.btnConfigWifi);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        btnSairConta.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, BoasVindasActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnConfigWifi.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigWifiActivity.class);
            startActivity(intent);
        });

        Button btnVerAlerta = findViewById(R.id.btnVerAlerta);
        Button btnVerHistorico = findViewById(R.id.btnVerHistorico);

        btnVerAlerta.setOnClickListener(v ->
                startActivity(new Intent(this, VazamentoActivity.class)));

        btnVerHistorico.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));

        repositorioFluxo = App.getRepositorioFluxo();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e("FCM", "Erro ao obter token", task.getException());
                        return;
                    }

                    String token = task.getResult();

                    Log.d("FCM", "Token: " + token);

                    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

                    if (usuario != null) {

                        FirebaseFirestore.getInstance()
                                .collection("usuarios")
                                .document(usuario.getUid())
                                .update("tokenFCM", token)
                                .addOnSuccessListener(unused ->
                                        Log.d("FCM", "Token salvo no Firestore"))
                                .addOnFailureListener(e ->
                                        Log.e("FCM", "Erro ao salvar token", e));
                    }
                });

    }

    private void debugarFirestore() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("leituras")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {

                    if (!query.isEmpty()) {

                        QueryDocumentSnapshot doc =
                                (QueryDocumentSnapshot)
                                        query.getDocuments().get(0);

                        android.util.Log.d(
                                "FIREBASE",
                                doc.getData().toString());

                    }

                });

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Deixa de ouvir os dados. Se a VazamentoActivity ainda
        // estiver na tela, o monitoramento continua rodando pra ela.
        if (repositorioFluxo != null) {
            repositorioFluxo.removerListener(listener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        android.util.Log.d("TESTE", "onStart");

        if (repositorioFluxo != null) {
            repositorioFluxo.adicionarListener(listener);
        }
    }

}