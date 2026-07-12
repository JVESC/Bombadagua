package com.example.bombadagua; // ajuste para o seu pacote

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Classe responsável por toda a comunicação Bluetooth com o módulo HC-05 do Arduino.
 *
 * Como usar (dentro de uma Activity):
 *
 *   BluetoothHelper bt = new BluetoothHelper(linhaRecebida -> {
 *       // esse código roda na UI thread, pode atualizar TextViews aqui
 *       processarDados(linhaRecebida);
 *   });
 *
 *   bt.conectar("HC-05"); // ou o nome exato do seu módulo
 *
 * Não esquece de chamar bt.desconectar() no onDestroy() da Activity.
 */
public class BluetoothHelper {

    // UUID padrão para comunicação serial via Bluetooth clássico (SPP)
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter adapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OnDadosRecebidosListener listener;

    private BluetoothSocket socket;
    private Thread threadLeitura;
    private volatile boolean lendo = false;

    public interface OnDadosRecebidosListener {
        void onDadosRecebidos(String linha);
    }

    public interface OnStatusListener {
        void onConectado();
        void onErro(String mensagem);
    }

    private OnStatusListener statusListener;

    public BluetoothHelper(OnDadosRecebidosListener listener) {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.listener = listener;
    }

    public void setStatusListener(OnStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    public boolean bluetoothDisponivel() {
        return adapter != null;
    }

    public boolean bluetoothLigado() {
        return adapter != null && adapter.isEnabled();
    }

    /**
     * Retorna a lista de dispositivos já pareados (a pessoa precisa ter pareado
     * o HC-05 manualmente nas configurações do Android antes de usar o app).
     */
    @SuppressLint("MissingPermission") // permissão já deve ter sido pedida antes de chamar isso
    public Set<BluetoothDevice> listarDispositivosPareados() {
        if (adapter == null) return null;
        return adapter.getBondedDevices();
    }

    /**
     * Conecta a um dispositivo pareado pelo nome exato (ex: "HC-05").
     * Roda em uma thread separada, pois conexão Bluetooth é uma operação bloqueante.
     */
    @SuppressLint("MissingPermission")
    public void conectarPorNome(String nomeDispositivo) {
        if (adapter == null) {
            notificarErro("Bluetooth não disponível nesse aparelho");
            return;
        }

        Set<BluetoothDevice> pareados = adapter.getBondedDevices();
        BluetoothDevice dispositivoAlvo = null;

        for (BluetoothDevice device : pareados) {
            if (device.getName() != null && device.getName().equals(nomeDispositivo)) {
                dispositivoAlvo = device;
                break;
            }
        }

        if (dispositivoAlvo == null) {
            notificarErro("Dispositivo \"" + nomeDispositivo + "\" não encontrado nos pareados. " +
                    "Pareie o HC-05 nas configurações de Bluetooth do celular primeiro.");
            return;
        }

        conectar(dispositivoAlvo);
    }

    @SuppressLint("MissingPermission")
    public void conectar(BluetoothDevice device) {
        new Thread(() -> {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                adapter.cancelDiscovery(); // importante: cancela busca ativa antes de conectar
                socket.connect();

                mainHandler.post(() -> {
                    if (statusListener != null) statusListener.onConectado();
                });

                iniciarLeitura();

            } catch (IOException e) {
                notificarErro("Falha ao conectar: " + e.getMessage());
                fecharSocket();
            }
        }).start();
    }

    private void iniciarLeitura() {
        lendo = true;
        threadLeitura = new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                StringBuilder buffer = new StringBuilder();
                byte[] bytesTemp = new byte[1024];

                while (lendo) {
                    int quantidadeBytes = inputStream.read(bytesTemp);
                    if (quantidadeBytes == -1) break;

                    String pedaco = new String(bytesTemp, 0, quantidadeBytes);
                    buffer.append(pedaco);

                    // Arduino manda cada leitura terminada em \n — processamos linha por linha
                    int indiceQuebraLinha;
                    while ((indiceQuebraLinha = buffer.indexOf("\n")) != -1) {
                        String linha = buffer.substring(0, indiceQuebraLinha).trim();
                        buffer.delete(0, indiceQuebraLinha + 1);

                        if (!linha.isEmpty()) {
                            mainHandler.post(() -> listener.onDadosRecebidos(linha));
                        }
                    }
                }
            } catch (IOException e) {
                if (lendo) { // só reporta erro se não foi um "desconectar" intencional
                    notificarErro("Conexão perdida: " + e.getMessage());
                }
            }
        });
        threadLeitura.start();
    }

    public void desconectar() {
        lendo = false;
        fecharSocket();
    }

    private void fecharSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    private void notificarErro(String mensagem) {
        mainHandler.post(() -> {
            if (statusListener != null) statusListener.onErro(mensagem);
        });
    }
}
