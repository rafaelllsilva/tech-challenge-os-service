-- Seed OS adicional
INSERT INTO ordem_de_servico (id, status, solicitacao, orcamento, id_cliente, id_veiculo)
VALUES
    -- Minhas
    (9, 'RECEBIDA', 'Troca de óleo e filtro', 0.00, 2, 2),
    (10, 'EM_DIAGNOSTICO', 'Barulho na suspensão dianteira', 852.50, 3, 3),
    (11, 'AGUARDANDO_APROVACAO', 'Revisão geral de 10k km', 1332.50, 4, 4),
    (12, 'APROVADA', 'Troca de pastilhas de freio', 685.00, 5, 5),
    (13, 'EM_EXECUCAO', 'Alinhamento e balanceamento', 429.40, 6, 6),
    (14, 'FINALIZADA', 'Troca de lâmpada do farol', 1295.00, 7, 7),
    (15, 'ENTREGUE', 'Limpeza de bicos injetores', 1205.00, 8, 8);

-- Peças
INSERT INTO peca (id, id_os, id_tipo_peca, quantidade) VALUES
    -- OS id 9 - Recebida (não possui peças)
    -- OS id 10 - Em Diagnóstico
    (18, 10, 1, 2),
    (19, 10, 4, 1),
    -- OS id 11 - Aguardando Aprovação
    (20, 11, 8, 2),
    (21, 11, 4, 1),
    -- OS id 12 - Aprovada
    (22, 12, 9, 1),
    -- OS id 13 - Em Execução
    (23, 13, 5, 1),
    (24, 13, 6, 4),
    -- OS id 14 - Finalizada
    (25, 14, 7, 1),
    (26, 14, 8, 1),
    -- OS id 15 - Entregue
    (27, 15, 1, 2),
    (28, 15, 2, 2);

-- Serviços
INSERT INTO servico (id, id_ordem_de_servico, id_tipo_servico, status, data_inicio, data_fim) VALUES
    -- OS id 9 - Recebida (não possui serviços)
    -- OS id 10 - Em Diagnóstico
    (20, 10, 4, 'AGUARDANDO_INICIO', NULL, NULL),
    (21, 10, 6, 'AGUARDANDO_INICIO', NULL, NULL),
    -- OS id 11 - Aguardando Aprovação
    (22, 11, 7, 'AGUARDANDO_INICIO', NULL, NULL),
    (23, 11, 8, 'AGUARDANDO_INICIO', NULL, NULL),
    -- OS id 12 - Aprovada
    (24, 12, 9, 'AGUARDANDO_INICIO', NULL, NULL),
    (25, 12, 5, 'AGUARDANDO_INICIO', NULL, NULL),
    -- OS id 13 - Em Execução
    (26, 13, 1, 'FINALIZADO', '2026-02-10 08:30:00', '2026-02-10 16:45:00'),
    (27, 13, 3, 'EM_EXECUCAO', '2026-02-10 14:00:00', NULL),
    (28, 13, 9, 'AGUARDANDO_INICIO', NULL, NULL),
    -- OS id 14 - Finalizada
    (29, 14, 2, 'FINALIZADO', '2026-03-01 10:00:00', '2026-03-01 12:00:00'),
    (30, 14, 4, 'FINALIZADO', '2026-03-01 14:30:00', '2026-03-04 11:15:00'),
    (31, 14, 7, 'FINALIZADO', '2026-03-04 13:00:00', '2026-03-04 18:00:00'),
    -- OS id 15 - Entregue
    (32, 15, 5, 'FINALIZADO', '2026-04-19 09:00:00', '2026-04-22 11:10:00'),
    (33, 15, 6, 'FINALIZADO', '2026-04-23 08:30:00', '2026-04-23 10:45:00');

-- Resetar sequences para evitar conflito em futuros inserts
SELECT setval(pg_get_serial_sequence('ordem_de_servico', 'id'), (SELECT MAX(id) FROM ordem_de_servico));
SELECT setval(pg_get_serial_sequence('peca', 'id'), (SELECT MAX(id) FROM peca));
SELECT setval('servico_id_seq', (SELECT MAX(id) FROM servico));
