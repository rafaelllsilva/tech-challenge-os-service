package com.fiap.techchallenge.os.adapter.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdicionarServicosRequest {
    private List<Long> idTipoServicos;
}
