package com.example.bombadagua.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bombadagua.R;
import com.example.bombadagua.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MeuFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static final String CHANNEL_ID = "alertas_vazamento";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(TAG, "Novo token: " + token);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario != null) {

            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(usuario.getUid())
                    .update("tokenFCM", token)
                    .addOnSuccessListener(unused ->
                            Log.d(TAG, "Token FCM atualizado no Firestore"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Erro ao atualizar token FCM", e));
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "Notificação recebida!");

        String titulo = "🚨 Vazamento detectado";
        String texto = "Foi detectado um fluxo contínuo de água.";

        if (message.getNotification() != null) {

            if (message.getNotification().getTitle() != null) {
                titulo = message.getNotification().getTitle();
            }

            if (message.getNotification().getBody() != null) {
                texto = message.getNotification().getBody();
            }
        }

        criarCanalNotificacao();
        mostrarNotificacao(titulo, texto);
    }

    private void criarCanalNotificacao() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Alertas de vazamento",
                    NotificationManager.IMPORTANCE_HIGH
            );

            canal.setDescription(
                    "Notificações sobre possíveis vazamentos de água"
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    private void mostrarNotificacao(String titulo, String texto) {

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_gota)
                        .setContentTitle(titulo)
                        .setContentText(texto)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat manager =
                NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Permissão de notificação não concedida.");
            return;
        }

        manager.notify(1001, builder.build());
    }
}