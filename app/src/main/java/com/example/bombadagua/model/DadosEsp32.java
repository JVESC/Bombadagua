package com.example.bombadagua.model;

public class DadosEsp32 {

    private double litrosMinuto;

    private double litrosHoje;

    private double aguaPoupada;

    private boolean online;

    private boolean vazamento;

    private long ultimaAtualizacao;

    public DadosEsp32() {
    }

    public DadosEsp32(double litrosMinuto) {
        this.litrosMinuto = litrosMinuto;
        this.online = true;
        this.ultimaAtualizacao = System.currentTimeMillis();
    }

    public double getLitrosMinuto() {
        return litrosMinuto;
    }

    public void setLitrosMinuto(double litrosMinuto) {
        this.litrosMinuto = litrosMinuto;
    }

    public double getLitrosHoje() {
        return litrosHoje;
    }

    public void setLitrosHoje(double litrosHoje) {
        this.litrosHoje = litrosHoje;
    }

    public double getAguaPoupada() {
        return aguaPoupada;
    }

    public void setAguaPoupada(double aguaPoupada) {
        this.aguaPoupada = aguaPoupada;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isVazamento() {
        return vazamento;
    }

    public void setVazamento(boolean vazamento) {
        this.vazamento = vazamento;
    }

    public long getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(long ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

}