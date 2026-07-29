package com.example.bombadagua.activities;

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

public class MainActivity extends AppCompatActivity {

    private TextView tvConsumoHoje;
    private TextView tvVazaoAtual;
    private TextView tvAguaPoupada;

    private RepositorioFluxo repositorioFluxo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        tvAguaPoupada = findViewById(R.id.tvAguaPoupada);

        debugarFirestore();

        Button btnVerAlerta = findViewById(R.id.btnVerAlerta);
        Button btnVerHistorico = findViewById(R.id.btnVerHistorico);

        btnVerAlerta.setOnClickListener(v ->
                startActivity(new Intent(this, VazamentoActivity.class)));

        btnVerHistorico.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoActivity.class)));

        repositorioFluxo = new RepositorioFluxo(this);


    }

    private void iniciarMonitoramento() {


        repositorioFluxo.iniciar(new RepositorioFluxo.Listener() {

            @Override
            public void onNovoFluxo(DadosEsp32 dados) {

                tvVazaoAtual.setText(
                        String.format("%.2f L/min",
                                dados.getLitrosMinuto()));

                tvConsumoHoje.setText(
                        String.format("%.2f L",
                                dados.getLitrosHoje()));

                tvAguaPoupada.setText(
                        String.format("%.2f L",
                                dados.getAguaPoupada()));

            }

            @Override
            public void onErro(String erro) {

                tvVazaoAtual.setText("--");

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
    protected void onDestroy() {
        super.onDestroy();

        if (repositorioFluxo != null) {
            repositorioFluxo.finalizar();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        iniciarMonitoramento();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (repositorioFluxo != null) {
            repositorioFluxo.parar();
        }
    }

}