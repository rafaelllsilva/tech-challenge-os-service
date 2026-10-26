-- Seed OS
INSERT INTO ordem_de_servico (id, status, solicitacao, orcamento, id_cliente, id_veiculo)
VALUES
    (1, 'AGUARDANDO_APROVACAO', 'Resolva o barulho no lado do motorista.', 1352.80, 1, 1),
    (2, 'ENTREGUE', 'Troca de óleo e filtro', 60.00, 2, 2),
    (3, 'ENTREGUE', 'Barulho na suspensão dianteira', 852.50, 3, 3),
    (4, 'EM_EXECUCAO', 'Revisão geral de 10k km', 1332.50, 4, 4),
    (5, 'FINALIZADA', 'Troca de pastilhas de freio', 685.00, 5, 5),
    (6, 'EM_EXECUCAO', 'Alinhamento e balanceamento', 429.40, 6, 6),
    (7, 'EM_EXECUCAO', 'Troca de lâmpada do farol', 1295.00, 7, 7),
    (8, 'EM_EXECUCAO', 'Limpeza de bicos injetores', 1205.00, 8, 8);

-- Peças
INSERT INTO peca (id, id_os, id_tipo_peca, quantidade) VALUES
    -- OS id 1 - Aguardando Aprovação
    (1, 1, 2,  1),
    (2, 1, 3,  1),
    (3, 1, 4,  1),
    (4, 1, 1,  4),
    (5, 1, 10, 3),
    (6, 1, 5,  1),
    -- OS id 2 - Entregue (não possui peças)
    -- OS id 3 - Entregue
    (7, 3, 1, 2),
    (8, 3, 4, 1),
    -- OS id 4 - Em Execução
    (9, 4, 8, 2),
    (10, 4, 4, 1),
    -- OS id 5 - Finalizada
    (11, 5, 9, 1),
    -- OS id 6 - Em Execução
    (12, 6, 5, 1),
    (13, 6, 6, 4),
    -- OS id 7 - Em Execução
    (14, 7, 7, 1),
    (15, 7, 8, 1),
    -- OS id 8 - Em Execução
    (16, 8, 1, 2),
    (17, 8, 2, 2);

-- Serviços
INSERT INTO servico (id, id_ordem_de_servico, id_tipo_servico, status, data_inicio, data_fim) VALUES
    -- OS id 1 - Aguardando Aprovação
    (1, 1, 1, 'AGUARDANDO_INICIO', NULL, NULL),
    (2, 1, 2, 'AGUARDANDO_INICIO', NULL, NULL),
    (3, 1, 2, 'AGUARDANDO_INICIO', NULL, NULL),
    (4, 1, 1, 'AGUARDANDO_INICIO', NULL, NULL),
    -- OS id 2 - Entregue
    (5, 2, 3, 'FINALIZADO', '2026-01-10 09:15:00', '2026-01-10 11:45:00'),
    -- OS id 3 - Entregue
    (6, 3, 4, 'FINALIZADO', '2026-01-15 13:30:00', '2026-01-17 17:45:00'),
    (7, 3, 6, 'DELETADO', '2026-01-15 10:00:00', NULL),
    -- OS id 4 - Em Execução
    (8, 4, 7, 'FINALIZADO', '2026-01-20 08:45:00', '2026-01-21 12:00:00'),
    (9, 4, 8, 'EM_EXECUCAO', '2026-04-19 11:20:00', NULL),
    -- OS id 5 - Finalizada
    (10, 5, 9, 'FINALIZADO', '2026-02-01 09:00:00', '2026-02-01 13:30:00'),
    (11, 5, 5, 'FINALIZADO', '2026-02-01 15:00:00', '2026-02-03 10:00:00'),
    -- OS id 6 - Em Execução
    (12, 6, 1, 'FINALIZADO', '2026-02-10 08:30:00', '2026-02-10 16:45:00'),
    (13, 6, 3, 'DELETADO', '2026-02-10 14:00:00', NULL),
    (14, 6, 9, 'AGUARDANDO_INICIO',  NULL, NULL),
    -- OS id 7 - Em Execução
    (15, 7, 2, 'FINALIZADO', '2026-03-01 10:00:00', '2026-03-01 12:00:00'),
    (16, 7, 4, 'FINALIZADO', '2026-03-01 14:30:00', '2026-03-04 11:15:00'),
    (17, 7, 7, 'AGUARDANDO_INICIO', NULL, NULL),
    -- OS id 8 - Em Execução
    (18, 8, 5, 'EM_EXECUCAO', '2026-04-19 09:00:00', NULL),
    (19, 8, 6, 'AGUARDANDO_INICIO', NULL, NULL);

-- Resetar sequences para evitar conflito em futuros inserts
SELECT setval(pg_get_serial_sequence('ordem_de_servico', 'id'), (SELECT MAX(id) FROM ordem_de_servico));
SELECT setval(pg_get_serial_sequence('peca', 'id'), (SELECT MAX(id) FROM peca));
SELECT setval('servico_id_seq', (SELECT MAX(id) FROM servico));
