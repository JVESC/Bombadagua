package com.example.bombadagua.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {

    private static final String PREFS = "configuracoes";

    private static final String KEY_IP = "ip_esp32";

    private static final String IP_PADRAO = "192.168.0.100";

    public static void salvarIp(Context context, String ip){

        SharedPreferences preferences =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        preferences.edit().putString(KEY_IP, ip).apply();

    }

    public static String obterIp(Context context){

        SharedPreferences preferences =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        return preferences.getString(KEY_IP, IP_PADRAO);

    }

}