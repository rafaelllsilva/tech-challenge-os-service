CREATE TABLE outbox_evento (
    id             UUID         PRIMARY KEY,
    tipo           VARCHAR(40)  NOT NULL,
    topico         VARCHAR(60)  NOT NULL,
    correlation_id VARCHAR(64)  NOT NULL,
    payload        JSONB        NOT NULL,
    ocorrido_em    TIMESTAMPTZ  NOT NULL,
    status         VARCHAR(15)  NOT NULL DEFAULT 'PENDENTE',
    tentativas     INT          NOT NULL DEFAULT 0,
    ultimo_erro    TEXT         NULL,
    enviado_em     TIMESTAMPTZ  NULL
);

CREATE INDEX idx_outbox_evento_status_ocorrido_em ON outbox_evento (status, ocorrido_em);
