package com.example.bombadagua.model;

public class DispositivoEsp32 {

    private String dispositivoId;
    private String donoUid;
    private String nome;
    private String local;
    private long criadoEm;

    public DispositivoEsp32() {
        // Necessário para o Firebase
    }

    public DispositivoEsp32(
            String dispositivoId,
            String donoUid,
            String nome,
            String local,
            long criadoEm
    ) {
        this.dispositivoId = dispositivoId;
        this.donoUid = donoUid;
        this.nome = nome;
        this.local = local;
        this.criadoEm = criadoEm;
    }

    public String getDispositivoId() {
        return dispositivoId;
    }

    public void setDispositivoId(String dispositivoId) {
        this.dispositivoId = dispositivoId;
    }

    public String getDonoUid() {
        return donoUid;
    }

    public void setDonoUid(String donoUid) {
        this.donoUid = donoUid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(long criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {
        return nome + " - " + local;
    }
}