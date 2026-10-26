package com.fiap.techchallenge.os.inputdata;

public class AdicionarPecaItemInputData {
    private Long idTipoPeca;
    private int quantidade;

    public AdicionarPecaItemInputData(Long idTipoPeca, int quantidade) {
        this.idTipoPeca = idTipoPeca;
        this.quantidade = quantidade;
    }

    public Long getIdTipoPeca() {
        return idTipoPeca;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setIdTipoPeca(Long idTipoPeca) {
        this.idTipoPeca = idTipoPeca;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
