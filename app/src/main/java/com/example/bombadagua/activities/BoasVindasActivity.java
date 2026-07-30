package com.example.bombadagua.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bombadagua.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BoasVindasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se já existe um usuário logado antes de mostrar a tela.
        // Se existir, pula direto pro dashboard, sem passar pelo login.
        FirebaseUser usuarioAtual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAtual != null) {
            Intent intent = new Intent(BoasVindasActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boas_vindas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(BoasVindasActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(BoasVindasActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}