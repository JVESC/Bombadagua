package com.example.bombadagua;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etSenha;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        TextView tvIrParaCadastro = findViewById(R.id.tvIrParaCadastro);
        android.widget.LinearLayout btnVoltar = findViewById(R.id.btnVoltarLogin);

        btnVoltar.setOnClickListener(v -> finish());

        btnEntrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            btnEntrar.setEnabled(false);

            firebaseAuth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this, task -> {
                        btnEntrar.setEnabled(true);

                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String mensagem = "Não foi possível entrar. Verifique e-mail e senha.";
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                android.util.Log.e("LOGIN", task.getException().getMessage());
                            }
                            Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvIrParaCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}