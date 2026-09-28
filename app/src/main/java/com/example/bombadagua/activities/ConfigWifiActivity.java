package com.example.bombadagua.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

    private View containerInstrucoes;
    private View containerWebView;
    private View containerErro;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_wifi);

        LinearLayout btnVoltar = findViewById(R.id.btnVoltar);

        Button btnAbrirWifiCelular =
                findViewById(R.id.btnAbrirWifiCelular);

        Button btnAbrirConfigEsp =
                findViewById(R.id.btnSalvarWifi);

        Button btnCancelarWifi =
                findViewById(R.id.btnCancelarWifi);

        containerInstrucoes = findViewById(R.id.containerInstrucoes);
        containerWebView = findViewById(R.id.containerWebView);
        containerErro = findViewById(R.id.containerErro);

        Button btnAbrirWifiErro = findViewById(R.id.btnAbrirWifiErro);
        Button btnTentarNovamente = findViewById(R.id.btnTentarNovamente);
        View btnVoltarErro = findViewById(R.id.btnVoltarErro);

        LinearLayout btnVoltarWebView =
                findViewById(R.id.btnVoltarWebView);

        ProgressBar progressWebView =
                findViewById(R.id.progressWebView);

        webView = findViewById(R.id.webViewEsp);

        // =========================================================
        // CONFIGURAÇÃO DO WEBVIEW
        // =========================================================
        //
        // setWebViewClient garante que os links/formulários da
        // própria página da ESP32 continuem abrindo AQUI DENTRO,
        // em vez de mandar pra um navegador externo.
        // =========================================================

        webView.getSettings().setJavaScriptEnabled(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressWebView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    int errorCode,
                    String description,
                    String failingUrl
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                progressWebView.setVisibility(View.GONE);

                mostrarTelaErro();
            }
        });

        webView.setWebChromeClient(new android.webkit.WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressWebView.setProgress(newProgress);
            }
        });

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
        // PASSO 2: MOSTRAR A PÁGINA DE CONFIGURAÇÃO DA ESP32
        // =========================================================
        //
        // A própria ESP32 serve a página com o formulário de
        // SSID/senha (ver paginaConfiguracao()/salvarWiFi() no
        // firmware). O app só carrega esse endereço dentro do
        // WebView — quem recebe e salva o Wi-Fi é a ESP32.
        // =========================================================

        btnAbrirConfigEsp.setOnClickListener(v -> abrirTelaWebView());

        btnCancelarWifi.setOnClickListener(v -> finish());

        btnVoltar.setOnClickListener(v -> finish());

        btnVoltarWebView.setOnClickListener(v -> fecharTelaWebView());

        // =========================================================
        // TELA DE ERRO
        // =========================================================

        btnAbrirWifiErro.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Selecione a rede \"" + NOME_REDE_ESP + "\"",
                    Toast.LENGTH_LONG
            ).show();

            startActivity(
                    new Intent(Settings.ACTION_WIFI_SETTINGS)
            );
        });

        btnTentarNovamente.setOnClickListener(v -> {

            containerErro.setVisibility(View.GONE);
            containerWebView.setVisibility(View.VISIBLE);

            progressWebView.setVisibility(View.VISIBLE);
            progressWebView.setProgress(0);

            webView.loadUrl(URL_CONFIGURACAO_ESP);
        });

        btnVoltarErro.setOnClickListener(v -> {

            containerErro.setVisibility(View.GONE);
            containerInstrucoes.setVisibility(View.VISIBLE);
        });

        // =========================================================
        // BOTÃO FÍSICO/GESTO DE VOLTAR
        // =========================================================

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (containerErro.getVisibility() == View.VISIBLE) {

                            containerErro.setVisibility(View.GONE);
                            containerInstrucoes.setVisibility(View.VISIBLE);

                        } else if (containerWebView.getVisibility() == View.VISIBLE) {

                            if (webView.canGoBack()) {
                                webView.goBack();
                            } else {
                                fecharTelaWebView();
                            }

                        } else {

                            finish();
                        }
                    }
                }
        );
    }

    private void abrirTelaWebView() {

        containerInstrucoes.setVisibility(View.GONE);
        containerErro.setVisibility(View.GONE);
        containerWebView.setVisibility(View.VISIBLE);

        webView.loadUrl(URL_CONFIGURACAO_ESP);
    }

    private void fecharTelaWebView() {

        containerWebView.setVisibility(View.GONE);
        containerInstrucoes.setVisibility(View.VISIBLE);

        // Limpa a página pra não deixar carregando em segundo
        // plano nem manter estado da tentativa anterior.
        webView.loadUrl("about:blank");
    }

    private void mostrarTelaErro() {

        containerWebView.setVisibility(View.GONE);
        containerErro.setVisibility(View.VISIBLE);

        webView.loadUrl("about:blank");
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}