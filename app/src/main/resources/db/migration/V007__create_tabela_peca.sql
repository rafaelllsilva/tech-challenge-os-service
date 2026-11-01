CREATE TABLE IF NOT EXISTS peca (
    id SERIAL PRIMARY KEY,
    id_os BIGINT NOT NULL,
    id_tipo_peca BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    CONSTRAINT fk_peca_os FOREIGN KEY (id_os) REFERENCES ordem_de_servico(id),
    CONSTRAINT fk_peca_tipo_peca FOREIGN KEY (id_tipo_peca) REFERENCES tipo_peca(id)
);
