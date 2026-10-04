package com.example.bombadagua.service;

import android.content.Context;
import android.content.SharedPreferences;

public class ConsumoManager {

    private static final String PREFS =
            "consumo";

    private static final String KEY_LITROS_HOJE =
            "litrosHoje_";

    private static final String KEY_ULTIMA_LEITURA =
            "ultimaLeitura_";

    private static final String KEY_ULTIMA_GRAVACAO =
            "ultimaGravacao_";

    private static final String KEY_DATA =
            "data_";

    private static final String KEY_ULTIMO_RAW =
            "ultimoRaw_";

    private final SharedPreferences preferences;

    public ConsumoManager(Context context) {

        preferences =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    // =============================================================
    // CHAVE POR DISPOSITIVO
    // =============================================================

    private String chave(
            String chaveBase,
            String dispositivoId
    ) {

        if (
                dispositivoId == null
                        ||
                        dispositivoId.trim().isEmpty()
        ) {

            return chaveBase + "sem_dispositivo";
        }

        return chaveBase
                + dispositivoId.trim();
    }

    // =============================================================
    // LITROS HOJE
    // =============================================================

    public double getLitrosHoje(
            String dispositivoId
    ) {

        return Double.longBitsToDouble(

                preferences.getLong(

                        chave(
                                KEY_LITROS_HOJE,
                                dispositivoId
                        ),

                        Double.doubleToLongBits(0.0)
                )
        );
    }

    public void salvarLitrosHoje(
            String dispositivoId,
            double litros
    ) {

        preferences.edit()

                .putLong(

                        chave(
                                KEY_LITROS_HOJE,
                                dispositivoId
                        ),

                        Double.doubleToLongBits(
                                litros
                        )
                )

                .apply();
    }

    // =============================================================
    // ÚLTIMA LEITURA DO APLICATIVO
    // =============================================================

    public long getUltimaLeitura(
            String dispositivoId
    ) {

        return preferences.getLong(

                chave(
                        KEY_ULTIMA_LEITURA,
                        dispositivoId
                ),

                0
        );
    }

    public void salvarUltimaLeitura(
            String dispositivoId,
            long tempo
    ) {

        preferences.edit()

                .putLong(

                        chave(
                                KEY_ULTIMA_LEITURA,
                                dispositivoId
                        ),

                        tempo
                )

                .apply();
    }

    // =============================================================
    // ÚLTIMA GRAVAÇÃO
    // =============================================================

    public long getUltimaGravacao(
            String dispositivoId
    ) {

        return preferences.getLong(

                chave(
                        KEY_ULTIMA_GRAVACAO,
                        dispositivoId
                ),

                0
        );
    }

    public void salvarUltimaGravacao(
            String dispositivoId,
            long tempo
    ) {

        preferences.edit()

                .putLong(

                        chave(
                                KEY_ULTIMA_GRAVACAO,
                                dispositivoId
                        ),

                        tempo
                )

                .apply();
    }

    // =============================================================
    // DATA
    // =============================================================

    public String getData(
            String dispositivoId
    ) {

        return preferences.getString(

                chave(
                        KEY_DATA,
                        dispositivoId
                ),

                ""
        );
    }

    public void salvarData(
            String dispositivoId,
            String data
    ) {

        preferences.edit()

                .putString(

                        chave(
                                KEY_DATA,
                                dispositivoId
                        ),

                        data
                )

                .apply();
    }

    // =============================================================
    // ÚLTIMA LEITURA BRUTA DA ESP32
    // =============================================================

    public double getUltimoRaw(
            String dispositivoId
    ) {

        return Double.longBitsToDouble(

                preferences.getLong(

                        chave(
                                KEY_ULTIMO_RAW,
                                dispositivoId
                        ),

                        Double.doubleToLongBits(0.0)
                )
        );
    }

    public void salvarUltimoRaw(
            String dispositivoId,
            double valor
    ) {

        preferences.edit()

                .putLong(

                        chave(
                                KEY_ULTIMO_RAW,
                                dispositivoId
                        ),

                        Double.doubleToLongBits(
                                valor
                        )
                )

                .apply();
    }

    // =============================================================
    // RESET DE UM DISPOSITIVO
    // =============================================================

    public void limparConsumo(
            String dispositivoId
    ) {

        preferences.edit()

                .remove(
                        chave(
                                KEY_LITROS_HOJE,
                                dispositivoId
                        )
                )

                .remove(
                        chave(
                                KEY_ULTIMA_LEITURA,
                                dispositivoId
                        )
                )

                .remove(
                        chave(
                                KEY_ULTIMA_GRAVACAO,
                                dispositivoId
                        )
                )

                .remove(
                        chave(
                                KEY_DATA,
                                dispositivoId
                        )
                )

                .remove(
                        chave(
                                KEY_ULTIMO_RAW,
                                dispositivoId
                        )
                )

                .apply();
    }
}