package com.example.bombadagua.service;

import android.content.Context;
import android.content.SharedPreferences;

public class ConsumoManager {

    private static final String PREFS = "consumo";

    private static final String KEY_LITROS_HOJE = "litrosHoje";
    private static final String KEY_ULTIMA_LEITURA = "ultimaLeitura";
    private static final String KEY_ULTIMA_GRAVACAO = "ultimaGravacao";
    private static final String KEY_DATA = "data";

    private final SharedPreferences preferences;

    public ConsumoManager(Context context) {

        preferences = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );

    }

    //==========================
    // LITROS HOJE
    //==========================

    public double getLitrosHoje() {

        return Double.longBitsToDouble(

                preferences.getLong(
                        KEY_LITROS_HOJE,
                        Double.doubleToLongBits(0)
                )

        );

    }

    public void salvarLitrosHoje(double litros) {

        preferences.edit()

                .putLong(
                        KEY_LITROS_HOJE,
                        Double.doubleToLongBits(litros)
                )

                .apply();

    }

    //==========================
    // ÚLTIMA LEITURA
    //==========================

    public long getUltimaLeitura() {

        return preferences.getLong(
                KEY_ULTIMA_LEITURA,
                0
        );

    }

    public void salvarUltimaLeitura(long tempo) {

        preferences.edit()

                .putLong(
                        KEY_ULTIMA_LEITURA,
                        tempo
                )

                .apply();

    }

    //==========================
    // ÚLTIMA GRAVAÇÃO FIREBASE
    //==========================

    public long getUltimaGravacao() {

        return preferences.getLong(
                KEY_ULTIMA_GRAVACAO,
                0
        );

    }

    public void salvarUltimaGravacao(long tempo) {

        preferences.edit()

                .putLong(
                        KEY_ULTIMA_GRAVACAO,
                        tempo
                )

                .apply();

    }

    //==========================
    // DATA
    //==========================

    public String getData() {

        return preferences.getString(
                KEY_DATA,
                ""
        );

    }

    public void salvarData(String data) {

        preferences.edit()

                .putString(
                        KEY_DATA,
                        data
                )

                .apply();

    }

    //==========================
    // RESET
    //==========================

    public void limparConsumo() {

        preferences.edit()

                .remove(KEY_LITROS_HOJE)
                .remove(KEY_ULTIMA_LEITURA)
                .remove(KEY_ULTIMA_GRAVACAO)
                .remove(KEY_DATA)

                .apply();

    }

}