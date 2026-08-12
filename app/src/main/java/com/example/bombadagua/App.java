package com.example.bombadagua;

import android.app.Application;

import com.example.bombadagua.repository.RepositorioFluxo;

public class App extends Application {

    private static RepositorioFluxo repositorioFluxo;

    public static RepositorioFluxo getRepositorioFluxo() {
        return repositorioFluxo;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        repositorioFluxo = new RepositorioFluxo(this);
    }
}