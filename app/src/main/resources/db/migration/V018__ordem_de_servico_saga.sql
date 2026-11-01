ALTER TABLE ordem_de_servico RENAME COLUMN orcamento TO valor_total;
ALTER TABLE ordem_de_servico ADD COLUMN orcamento_id VARCHAR(64) NULL;
ALTER TABLE ordem_de_servico ADD COLUMN tentativas_pagamento INT NOT NULL DEFAULT 0;
ALTER TABLE ordem_de_servico ALTER COLUMN status TYPE VARCHAR(30);
ALTER TABLE ordem_de_servico_status_historico ALTER COLUMN status TYPE VARCHAR(30);
