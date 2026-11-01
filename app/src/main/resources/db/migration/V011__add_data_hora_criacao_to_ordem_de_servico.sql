-- Adicionar coluna data_hora_criacao à tabela ordem_de_servico
ALTER TABLE ordem_de_servico
ADD COLUMN data_hora_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
