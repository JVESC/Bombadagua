package com.example.bombadagua;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + 40,
                    systemBars.top,
                    systemBars.right + 40,
                    systemBars.bottom + 40
            );
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        TextView tvIrParaCadastro = findViewById(R.id.tvIrParaCadastro);
        android.widget.LinearLayout btnVoltar = findViewById(R.id.btnVoltarLogin);

        btnVoltar.setOnClickListener(v -> finish());

        // Alterna mostrar/esconder a senha ao tocar no ícone do olho
        etSenha.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // índice do drawable à direita (start=0, top=1, end=2, bottom=3)
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etSenha.getCompoundDrawables()[DRAWABLE_END] != null) {
                    float areaToque = etSenha.getWidth() - etSenha.getPaddingEnd()
                            - etSenha.getCompoundDrawables()[DRAWABLE_END].getBounds().width();
                    if (event.getX() >= areaToque) {
                        boolean mostrandoSenha = etSenha.getInputType() ==
                                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                        if (mostrandoSenha) {
                            // Esconde a senha de novo
                            etSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_olho_fechado, 0);
                        } else {
                            // Mostra a senha em texto puro
                            etSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            etSenha.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_olho_aberto, 0);
                        }
                        etSenha.setSelection(etSenha.getText().length());
                        v.performClick();
                        return true;
                    }
                }
            }
            return false;
        });

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