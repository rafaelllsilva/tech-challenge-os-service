# language: pt
Funcionalidade: Saga da ordem de serviço - pagamento recusado
  Falhas de pagamento devolvem a OS para nova aprovação e, no limite de tentativas, cancelam a OS.

  Contexto:
    Dado um cliente e um veículo cadastrados
    Quando abro uma ordem de serviço
    E o billing publica ORCAMENTO_GERADO com valor 350.5
    Então a OS fica com status AGUARDANDO_APROVACAO

  Cenário: Uma falha de pagamento volta a OS para a aprovação
    Quando o orçamento é aprovado e o pagamento falha 1 vez
    Então a OS fica com status AGUARDANDO_APROVACAO

  Esquema do Cenário: Falhas até o limite cancelam a OS
    Quando o orçamento é aprovado e o pagamento falha <falhas> vezes
    Então a OS fica com status CANCELADA
    E o evento OS_CANCELADA é publicado com motivo PAGAMENTO_RECUSADO

    Exemplos:
      | falhas |
      | 3      |
