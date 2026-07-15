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
import com.google.firebase.auth.UserProfileChangeRequest;

public class CadastroActivity extends AppCompatActivity {

    private EditText etNome;
    private EditText etEmail;
    private EditText etSenha;
    private EditText etConfirmarSenha;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);
        TextView tvIrParaLogin = findViewById(R.id.tvIrParaLogin);
        android.widget.LinearLayout btnVoltar = findViewById(R.id.btnVoltarCadastro);

        btnVoltar.setOnClickListener(v -> finish());

        btnCadastrar.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();
            String confirmarSenha = etConfirmarSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }

            if (senha.length() < 6) {
                Toast.makeText(this, "A senha precisa ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCadastrar.setEnabled(false);

            firebaseAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            // Salva o nome no perfil do usuário do Firebase
                            UserProfileChangeRequest perfil = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nome)
                                    .build();

                            firebaseAuth.getCurrentUser().updateProfile(perfil)
                                    .addOnCompleteListener(perfilTask -> {
                                        btnCadastrar.setEnabled(true);
                                        Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        } else {
                            btnCadastrar.setEnabled(true);
                            String mensagem = "Não foi possível cadastrar.";
                            if (task.getException() != null && task.getException().getMessage() != null) {
                                mensagem = task.getException().getMessage();
                            }
                            Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvIrParaLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}