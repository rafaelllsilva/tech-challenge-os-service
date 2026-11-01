# language: pt
Funcionalidade: Saga da ordem de serviço - execução falhou
  Se a execução falha depois do pagamento, a OS é cancelada (o billing estorna o pagamento).

  Cenário: Falha na execução cancela a OS
    Dado um cliente e um veículo cadastrados
    Quando abro uma ordem de serviço
    E o billing publica ORCAMENTO_GERADO com valor 350.5
    Então a OS fica com status AGUARDANDO_APROVACAO
    Quando o cliente aprova o orçamento
    E o billing publica PAGAMENTO_CONFIRMADO
    Então a OS fica com status APROVADA
    Quando o execution publica EXECUCAO_FALHOU
    Então a OS fica com status CANCELADA
