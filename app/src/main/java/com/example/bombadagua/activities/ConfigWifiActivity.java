package com.example.bombadagua.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bombadagua.R;

public class ConfigWifiActivity extends AppCompatActivity {

    // =============================================================
    // ENDEREÇO DA ESP32 EM MODO DE CONFIGURAÇÃO
    // =============================================================
    //
    // Quando a ESP32 não tem Wi-Fi salvo (ou não consegue
    // conectar), ela cria seu próprio ponto de acesso
    // (WiFi.softAP, veja iniciarModoConfiguracao() no firmware):
    //
    //   Rede:  AGUA_SOB_CONTROLE
    //   Senha: 12345678
    //
    // Nesse modo, o endereço IP da ESP32 é sempre o padrão do
    // ESP32 em modo AP: 192.168.4.1. É diferente do IP que ela
    // recebe depois, quando já está conectada na rede Wi-Fi de
    // casa (esse é dinâmico, dado pelo roteador).
    // =============================================================

    private static final String IP_CONFIGURACAO_ESP = "192.168.4.1";

    private static final String URL_CONFIGURACAO_ESP =
            "http://" + IP_CONFIGURACAO_ESP + "/config";

    private static final String NOME_REDE_ESP = "AGUA_SOB_CONTROLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi);

        LinearLayout btnVoltar = findViewById(R.id.btnVoltar);

        Button btnAbrirWifiCelular =
                findViewById(R.id.btnAbrirWifiCelular);

        Button btnSalvarWifi =
                findViewById(R.id.btnSalvarWifi);

        Button btnCancelarWifi =
                findViewById(R.id.btnCancelarWifi);

        // =========================================================
        // PASSO 1: ABRIR O WI-FI DO CELULAR
        // =========================================================
        //
        // O Android não deixa um app conectar o celular numa rede
        // Wi-Fi sozinho sem interação do usuário. Então abrimos a
        // tela de Wi-Fi do sistema e o usuário escolhe a rede
        // "AGUA_SOB_CONTROLE" manualmente.
        // =========================================================

        btnAbrirWifiCelular.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Selecione a rede \"" + NOME_REDE_ESP + "\"",
                    Toast.LENGTH_LONG
            ).show();

            startActivity(
                    new Intent(Settings.ACTION_WIFI_SETTINGS)
            );
        });

        // =========================================================
        // PASSO 2: ABRIR A PÁGINA DE CONFIGURAÇÃO DA ESP32
        // =========================================================
        //
        // A própria ESP32 serve a página com o formulário de
        // SSID/senha (ver paginaConfiguracao()/salvarWiFi() no
        // firmware). O app só precisa abrir esse endereço no
        // navegador — quem recebe e salva o Wi-Fi é a ESP32.
        // =========================================================

        btnSalvarWifi.setOnClickListener(v -> {

            try {

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(URL_CONFIGURACAO_ESP)
                );

                startActivity(intent);

            } catch (ActivityNotFoundException e) {

                Toast.makeText(
                        this,
                        "Não encontrei um navegador para abrir "
                                + URL_CONFIGURACAO_ESP,
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        btnCancelarWifi.setOnClickListener(v -> finish());

        btnVoltar.setOnClickListener(v -> finish());
    }
}