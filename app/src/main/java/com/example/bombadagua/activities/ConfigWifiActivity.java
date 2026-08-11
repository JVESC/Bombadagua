package com.example.bombadagua.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bombadagua.R;

public class ConfigWifiActivity extends AppCompatActivity {

    private EditText etWifi;
    private EditText etSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi);

        etWifi = findViewById(R.id.etWifi);
        etSenha = findViewById(R.id.etSenha);

        Button btnSalvarWifi = findViewById(R.id.btnSalvarWifi);
        Button btnCancelarWifi = findViewById(R.id.btnCancelarWifi);

        btnSalvarWifi.setOnClickListener(v -> {

            String wifi = etWifi.getText().toString().trim();
            String senha = etSenha.getText().toString();

            if (wifi.isEmpty()) {
                Toast.makeText(
                        this,
                        "Digite o nome do Wi-Fi",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (senha.isEmpty()) {
                Toast.makeText(
                        this,
                        "Digite a senha do Wi-Fi",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Por enquanto apenas vamos testar a tela.
            // Depois vamos enviar esses dados para a ESP32.

            Toast.makeText(
                    this,
                    "Dados preenchidos corretamente!",
                    Toast.LENGTH_SHORT
            ).show();
        });

        btnCancelarWifi.setOnClickListener(v -> finish());
        LinearLayout btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> finish());
    }
}