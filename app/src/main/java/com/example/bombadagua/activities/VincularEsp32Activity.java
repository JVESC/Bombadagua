package com.example.bombadagua.activities;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bombadagua.R;
import com.example.bombadagua.model.DispositivoEsp32;
import com.example.bombadagua.repository.DispositivoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class VincularEsp32Activity extends AppCompatActivity {

    private EditText etCodigo;
    private EditText etNome;
    private EditText etLocal;
    private Button btnVincular;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DispositivoRepository dispositivoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_vincular_esp32);

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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dispositivoRepository = new DispositivoRepository();

        etCodigo = findViewById(R.id.etCodigoEsp32);
        etNome = findViewById(R.id.etNomeEsp32);
        etLocal = findViewById(R.id.etLocalEsp32);
        btnVincular = findViewById(R.id.btnVincularEsp32);

        LinearLayout btnVoltar =
                findViewById(R.id.btnVoltarVincular);

        configurarCampoCodigo();

        btnVoltar.setOnClickListener(v -> finish());

        btnVincular.setOnClickListener(v -> vincularEsp32());
    }

    // =============================================================
    // CONFIGURAÇÃO DO CAMPO DE CÓDIGO
    // =============================================================

    private void configurarCampoCodigo() {

        etCodigo.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        etCodigo.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(6)
                }
        );
    }

    // =============================================================
    // VINCULAR ESP32
    // =============================================================

    private void vincularEsp32() {

        String codigo =
                etCodigo.getText()
                        .toString()
                        .trim();

        String nome =
                etNome.getText()
                        .toString()
                        .trim();

        String local =
                etLocal.getText()
                        .toString()
                        .trim();

        // ---------------------------------------------------------
        // VALIDAÇÕES
        // ---------------------------------------------------------

        if (codigo.isEmpty()) {

            etCodigo.setError(
                    "Digite o código da ESP32"
            );

            etCodigo.requestFocus();

            return;
        }

        if (codigo.length() != 6) {

            etCodigo.setError(
                    "O código precisa ter 6 números"
            );

            etCodigo.requestFocus();

            return;
        }

        if (nome.isEmpty()) {

            etNome.setError(
                    "Digite um nome para a ESP32"
            );

            etNome.requestFocus();

            return;
        }

        if (local.isEmpty()) {

            etLocal.setError(
                    "Digite o local da ESP32"
            );

            etLocal.requestFocus();

            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Você precisa estar logado.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // ---------------------------------------------------------
        // DESABILITA BOTÃO
        // ---------------------------------------------------------

        btnVincular.setEnabled(false);
        btnVincular.setText("Procurando ESP32...");

        procurarCodigo(codigo, nome, local);
    }

    // =============================================================
    // PROCURAR CÓDIGO NO REALTIME DATABASE
    // =============================================================

    private void procurarCodigo(
            String codigo,
            String nome,
            String local
    ) {

        DatabaseReference referencia =
                firebaseDatabase
                        .getReference(
                                "dispositivos_pendentes"
                        )
                        .child(codigo);

        referencia.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            DataSnapshot snapshot
                    ) {

                        if (!snapshot.exists()) {

                            mostrarErro(
                                    "Código não encontrado. " +
                                            "Verifique o código mostrado pela ESP32."
                            );

                            return;
                        }

                        String status =
                                snapshot
                                        .child("status")
                                        .getValue(String.class);

                        String dispositivoId =
                                snapshot
                                        .child("dispositivoId")
                                        .getValue(String.class);

                        // -------------------------------------------------
                        // VERIFICA SE O DISPOSITIVO ESTÁ DISPONÍVEL
                        // -------------------------------------------------

                        if (
                                status == null
                                        ||
                                        !"disponivel".equals(status)
                        ) {

                            mostrarErro(
                                    "Essa ESP32 não está disponível " +
                                            "para vinculação."
                            );

                            return;
                        }

                        if (
                                dispositivoId == null
                                        ||
                                        dispositivoId.trim().isEmpty()
                        ) {

                            mostrarErro(
                                    "A ESP32 não informou um ID válido."
                            );

                            return;
                        }

                        salvarDispositivo(
                                referencia,
                                dispositivoId,
                                nome,
                                local
                        );
                    }

                    @Override
                    public void onCancelled(
                            DatabaseError error
                    ) {

                        mostrarErro(
                                "Erro ao consultar a ESP32: "
                                        + error.getMessage()
                        );
                    }
                }
        );
    }

    // =============================================================
    // SALVAR NO FIRESTORE
    // =============================================================

    private void salvarDispositivo(
            DatabaseReference referenciaPendente,
            String dispositivoId,
            String nome,
            String local
    ) {

        btnVincular.setText(
                "Vinculando..."
        );

        String uid =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        DispositivoEsp32 dispositivo =
                new DispositivoEsp32(
                        dispositivoId,
                        uid,
                        nome,
                        local,
                        System.currentTimeMillis()
                );

        dispositivoRepository.salvarDispositivo(
                dispositivo,
                new DispositivoRepository.OnOperacaoListener() {

                    @Override
                    public void onSucesso() {

                        marcarComoVinculado(
                                referenciaPendente,
                                dispositivoId
                        );
                    }

                    @Override
                    public void onErro(
                            String mensagem
                    ) {

                        mostrarErro(
                                mensagem
                        );
                    }
                }
        );
    }

    // =============================================================
    // MARCAR ESP32 COMO VINCULADA
    // =============================================================

    private void marcarComoVinculado(
            DatabaseReference referenciaPendente,
            String dispositivoId
    ) {

        Map<String, Object> atualizacao =
                new HashMap<>();

        atualizacao.put(
                "status",
                "vinculado"
        );

        atualizacao.put(
                "donoUid",
                firebaseAuth
                        .getCurrentUser()
                        .getUid()
        );

        atualizacao.put(
                "dispositivoId",
                dispositivoId
        );

        atualizacao.put(
                "ultimaAtualizacao",
                System.currentTimeMillis()
        );

        referenciaPendente
                .updateChildren(atualizacao)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "ESP32 vinculada com sucesso!",
                            Toast.LENGTH_LONG
                    ).show();

                    setResult(
                            RESULT_OK
                    );

                    finish();
                })
                .addOnFailureListener(e -> {

                    /*
                     * O dispositivo já foi salvo no Firestore.
                     * Aqui só houve problema ao atualizar o
                     * status no Realtime Database.
                     */
                    btnVincular.setEnabled(true);
                    btnVincular.setText(
                            "Vincular ESP32"
                    );

                    Toast.makeText(
                            this,
                            "ESP32 salva, mas não foi possível " +
                                    "finalizar o vínculo: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =============================================================
    // ERRO
    // =============================================================

    private void mostrarErro(
            String mensagem
    ) {

        btnVincular.setEnabled(true);
        btnVincular.setText(
                "Vincular ESP32"
        );

        Toast.makeText(
                this,
                mensagem,
                Toast.LENGTH_LONG
        ).show();
    }
}