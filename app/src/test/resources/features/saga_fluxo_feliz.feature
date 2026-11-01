# language: pt
Funcionalidade: Saga da ordem de serviço - fluxo feliz
  A OS percorre orçamento, aprovação, pagamento e execução trocando eventos com o billing e o execution.

  Cenário: Da abertura à entrega do veículo
    Dado um cliente e um veículo cadastrados
    Quando abro uma ordem de serviço
    Então a OS fica com status RECEBIDA
    E o evento OS_CRIADA é publicado
    Quando o billing publica ORCAMENTO_GERADO com valor 350.5
    Então a OS fica com status AGUARDANDO_APROVACAO
    Quando o cliente aprova o orçamento
    Então a OS fica com status AGUARDANDO_PAGAMENTO
    E o evento ORCAMENTO_APROVADO é publicado
    Quando o billing publica PAGAMENTO_CONFIRMADO
    Então a OS fica com status APROVADA
    Quando o execution publica EXECUCAO_INICIADA
    Então a OS fica com status EM_EXECUCAO
    Quando o execution publica EXECUCAO_FINALIZADA
    Então a OS fica com status FINALIZADA
    Quando entrego o veículo
    Então a OS fica com status ENTREGUE
    E o histórico de status da OS é "RECEBIDA, AGUARDANDO_APROVACAO, AGUARDANDO_PAGAMENTO, APROVADA, EM_EXECUCAO, FINALIZADA, ENTREGUE"
    E o histórico registra os eventos publicados "OS_CRIADA, ORCAMENTO_APROVADO" e os recebidos "ORCAMENTO_GERADO, PAGAMENTO_CONFIRMADO, EXECUCAO_INICIADA, EXECUCAO_FINALIZADA"
