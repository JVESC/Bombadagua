package com.example.bombadagua;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VazamentoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vazamento);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout btnVoltar = findViewById(R.id.btnVoltar);
        Button btnVerComoConsertar = findViewById(R.id.btnVerComoConsertar);
        Button btnMarcarResolvido = findViewById(R.id.btnMarcarResolvido);

        // Botão "Voltar" simplesmente fecha essa tela e volta pra anterior
        btnVoltar.setOnClickListener(v -> finish());

        btnVerComoConsertar.setOnClickListener(v -> {
            // Aqui depois você pode abrir uma tela de instruções,
            // por enquanto só um exemplo de feedback:
            Toast.makeText(this, "Abrir instruções de conserto", Toast.LENGTH_SHORT).show();
        });

        btnMarcarResolvido.setOnClickListener(v -> {
            // Aqui depois você vai atualizar o banco de dados marcando
            // esse vazamento como resolvido. Por enquanto, só volta pra tela principal:
            Toast.makeText(this, "Vazamento marcado como resolvido!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

}
