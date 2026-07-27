package com.example.dspi_app;

import java.io.Serializable;

public class Projeto implements Serializable {
    private String nomeProjeto, nomeEquipe, status, integrantes, orientador, nomeCoorientador, usuario;
    private String propostaChave, segmentosClientes, atividadesChaves, recursosChaves;
    private String relacionamentosClientes, canais, estruturaCustos, fluxoReceita, parceirosChaves;
    private String tarefas, dificuldadesEnxergadas;

    private String empresaVinculada;
    private String empresaVinculadaEmail;
    private String comentarioEmpresa;
    private String videoUrl;

    public Projeto(String nomeProjeto, String nomeEquipe, String status, String integrantes, String orientador,
                   String propostaChave, String segmentosClientes, String atividadesChaves, String recursosChaves,
                   String relacionamentosClientes, String canais, String estruturaCustos, String fluxoReceita,
                   String parceirosChaves, String tarefas, String dificuldadesEnxergadas, String empresaVinculada,
                   String videoUrl, String nomeCoorientador, String usuario, String empresaVinculadaEmail) {
        this.nomeProjeto = nomeProjeto;
        this.nomeEquipe = nomeEquipe;
        this.status = status;
        this.integrantes = integrantes;
        this.orientador = orientador;
        this.propostaChave = propostaChave;
        this.segmentosClientes = segmentosClientes;
        this.atividadesChaves = atividadesChaves;
        this.recursosChaves = recursosChaves;
        this.relacionamentosClientes = relacionamentosClientes;
        this.canais = canais;
        this.estruturaCustos = estruturaCustos;
        this.fluxoReceita = fluxoReceita;
        this.parceirosChaves = parceirosChaves;
        this.tarefas = tarefas;
        this.dificuldadesEnxergadas = dificuldadesEnxergadas;
        this.empresaVinculada = empresaVinculada;
        this.videoUrl = videoUrl;
        this.nomeCoorientador = nomeCoorientador;
        this.usuario = usuario;
        this.empresaVinculadaEmail = empresaVinculadaEmail;
    }

    public String getNomeProjeto() { return nomeProjeto; }
    public String getNomeEquipe() { return nomeEquipe; }
    public String getStatus() { return status; }
    public String getIntegrantes() { return integrantes; }
    public String getOrientador() { return orientador; }
    public String getNomeCoorientador() { return nomeCoorientador; }
    public String getUsuario() { return usuario; }
    public String getPropostaChave() { return propostaChave; }
    public String getSegmentosClientes() { return segmentosClientes; }
    public String getAtividadesChaves() { return atividadesChaves; }
    public String getRecursosChaves() { return recursosChaves; }
    public String getRelacionamentosClientes() { return relacionamentosClientes; }
    public String getCanais() { return canais; }
    public String getEstruturaCustos() { return estruturaCustos; }
    public String getFluxoReceita() { return fluxoReceita != null ? fluxoReceita : "Não informado"; }
    public String getParceirosChaves() { return parceirosChaves; }
    public String getTarefas() { return tarefas; }
    public String getDificuldadesEnxergadas() { return dificuldadesEnxergadas; }
    public String getEmpresaVinculada() { return empresaVinculada; }
    public String getEmpresaVinculadaEmail() { return empresaVinculadaEmail; }
    public String getVideoUrl() { return videoUrl; }

    public String getComentarioEmpresa() {
        return comentarioEmpresa;
    }

    public void setComentarioEmpresa(String comentarioEmpresa) {
        this.comentarioEmpresa = comentarioEmpresa;
    }
}