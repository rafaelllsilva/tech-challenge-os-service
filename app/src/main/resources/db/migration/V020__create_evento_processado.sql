CREATE TABLE evento_processado (
    event_id       UUID         PRIMARY KEY,
    tipo           VARCHAR(40)  NOT NULL,
    correlation_id VARCHAR(64)  NOT NULL,
    payload        JSONB        NOT NULL,
    ocorrido_em    TIMESTAMPTZ  NOT NULL,
    processado_em  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_evento_processado_correlation_id ON evento_processado (correlation_id);
