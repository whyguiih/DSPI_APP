package com.example.dspi_app;

public class Necessidade {
    private String nome;
    private String descricao;

    public Necessidade(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}