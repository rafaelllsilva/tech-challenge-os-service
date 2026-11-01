#!/bin/bash
# Cria topicos, filas, DLQs e assinaturas usados pelo os-service (executado pelo LocalStack ao ficar pronto).
set -euo pipefail

MAX_RECEIVE_COUNT=5

criar_topico() {
  awslocal sns create-topic --name "$1" --query TopicArn --output text
}

criar_fila_com_dlq() {
  local fila="$1"
  local dlq_url dlq_arn politica
  dlq_url=$(awslocal sqs create-queue --queue-name "${fila}-dlq" --query QueueUrl --output text)
  dlq_arn=$(awslocal sqs get-queue-attributes --queue-url "$dlq_url" --attribute-names QueueArn --query Attributes.QueueArn --output text)
  politica=$(mktemp)
  cat > "$politica" <<JSON
{"RedrivePolicy": "{\"deadLetterTargetArn\":\"${dlq_arn}\",\"maxReceiveCount\":\"${MAX_RECEIVE_COUNT}\"}"}
JSON
  local url
  url=$(awslocal sqs create-queue --queue-name "$fila" --attributes "file://${politica}" --query QueueUrl --output text)
  rm -f "$politica"
  awslocal sqs get-queue-attributes --queue-url "$url" --attribute-names QueueArn --query Attributes.QueueArn --output text
}

assinar() {
  awslocal sns subscribe --topic-arn "$1" --protocol sqs --notification-endpoint "$2" \
    --attributes RawMessageDelivery=true > /dev/null
}

OS_EVENTS_ARN=$(criar_topico os-events)
BILLING_EVENTS_ARN=$(criar_topico billing-events)
EXECUTION_EVENTS_ARN=$(criar_topico execution-events)

BILLING_FILA_ARN=$(criar_fila_com_dlq os-service-billing-events)
EXECUTION_FILA_ARN=$(criar_fila_com_dlq os-service-execution-events)
assinar "$BILLING_EVENTS_ARN" "$BILLING_FILA_ARN"
assinar "$EXECUTION_EVENTS_ARN" "$EXECUTION_FILA_ARN"

# Fila usada pelos testes (IT e BDD) para observar o que o os-service publica
TESTE_FILA_ARN=$(criar_fila_com_dlq test-os-events)
assinar "$OS_EVENTS_ARN" "$TESTE_FILA_ARN"

echo "Recursos de mensageria criados."
