# Tópico do os-service: publica aqui os eventos da OS (via relay do outbox).
resource "aws_sns_topic" "os_events" {
  name = "os-events"
}

# Filas de entrada do os-service, uma por serviço de origem. Os nomes são o contrato com a aplicação
# (OS_FILA_BILLING e OS_FILA_EXECUTION no ConfigMap). Elas são criadas mesmo sem a integração com os
# tópicos abaixo: a aplicação usa queue-not-found-strategy=fail e não sobe se as filas não existirem.
locals {
  filas = {
    billing   = "os-service-billing-events"
    execution = "os-service-execution-events"
  }
}

resource "aws_sqs_queue" "dlq" {
  for_each = local.filas

  name                      = "${each.value}-dlq"
  message_retention_seconds = 1209600 # 14 dias, para dar tempo de investigar e reprocessar
}

resource "aws_sqs_queue" "fila" {
  for_each = local.filas

  name = each.value

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq[each.key].arn
    maxReceiveCount     = var.max_receive_count
  })
}

# -----------------------------------------------------------------------------------------------
# INTEGRAÇÃO COM OS TÓPICOS DO BILLING E DO EXECUTION: DESABILITADA POR ENQUANTO
#
# Decisão: esses tópicos devem ser criados pelos repositórios do billing-service e do execution-service e
# ainda não existem na conta. O `data "aws_sns_topic"` falha no plan se o tópico não existir, o que
# impediria o primeiro deploy. Por isso o bloco abaixo fica comentado, para validar o restante da
# infraestrutura (os-events, filas, DLQs, IAM, ECR) e o deploy no EKS.
#
# Consequência: até habilitar, as filas não recebem eventos, então a saga não avança (o os-service
# publica em os-events, mas não recebe orçamento, pagamento nem execução).
#
# Para habilitar (depois que billing-events e execution-events existirem na conta):
#   1. Descomente o bloco abaixo.
#   2. terraform apply
# -----------------------------------------------------------------------------------------------

# data "aws_sns_topic" "billing_events" {
#   name = "billing-events"
# }
#
# data "aws_sns_topic" "execution_events" {
#   name = "execution-events"
# }
#
# # Cada entrada liga uma fila ao seu único tópico de origem: billing com billing, execution com
# # execution. Todos os recursos abaixo iteram sobre este mapa, então não há combinação cruzada.
# locals {
#   integracoes = {
#     billing = {
#       fila_arn   = aws_sqs_queue.fila["billing"].arn
#       fila_url   = aws_sqs_queue.fila["billing"].id
#       topico_arn = data.aws_sns_topic.billing_events.arn
#     }
#     execution = {
#       fila_arn   = aws_sqs_queue.fila["execution"].arn
#       fila_url   = aws_sqs_queue.fila["execution"].id
#       topico_arn = data.aws_sns_topic.execution_events.arn
#     }
#   }
# }
#
# # Permite que somente o tópico de origem envie mensagens para a sua fila.
# data "aws_iam_policy_document" "fila" {
#   for_each = local.integracoes
#
#   statement {
#     sid       = "PermitirEnvioDoSns"
#     effect    = "Allow"
#     actions   = ["sqs:SendMessage"]
#     resources = [each.value.fila_arn]
#
#     principals {
#       type        = "Service"
#       identifiers = ["sns.amazonaws.com"]
#     }
#
#     condition {
#       test     = "ArnEquals"
#       variable = "aws:SourceArn"
#       values   = [each.value.topico_arn]
#     }
#   }
# }
#
# resource "aws_sqs_queue_policy" "fila" {
#   for_each = local.integracoes
#
#   queue_url = each.value.fila_url
#   policy    = data.aws_iam_policy_document.fila[each.key].json
# }
#
# # Assina a fila no seu tópico de origem. raw_message_delivery: a aplicação lê o envelope JSON
# # direto, sem o wrapper do SNS.
# resource "aws_sns_topic_subscription" "fila" {
#   for_each = local.integracoes
#
#   topic_arn            = each.value.topico_arn
#   protocol             = "sqs"
#   endpoint             = each.value.fila_arn
#   raw_message_delivery = true
#
#   depends_on = [aws_sqs_queue_policy.fila]
# }
