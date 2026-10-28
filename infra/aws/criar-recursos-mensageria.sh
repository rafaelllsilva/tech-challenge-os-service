#!/bin/bash
# Cria na AWS os tópicos SNS, as filas SQS (com DLQ), as assinaturas com raw delivery e a policy que
# permite ao SNS enviar para as filas. Idempotente: pode ser executado várias vezes.
# Uso: AWS_REGION=us-east-1 ./criar-recursos-mensageria.sh
set -euo pipefail

REGION="${AWS_REGION:-us-east-1}"
MAX_RECEIVE_COUNT="${MAX_RECEIVE_COUNT:-5}"
ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"

# create-topic e create-queue (com os mesmos atributos) retornam o recurso existente, sem erro.
criar_topico() {
  aws sns create-topic --region "$REGION" --name "$1" --query TopicArn --output text
}

url_da_fila() {
  aws sqs get-queue-url --region "$REGION" --queue-name "$1" --query QueueUrl --output text 2>/dev/null || true
}

garantir_fila() {
  local nome="$1" url
  url="$(url_da_fila "$nome")"
  if [ -z "$url" ] || [ "$url" = "None" ]; then
    url="$(aws sqs create-queue --region "$REGION" --queue-name "$nome" --query QueueUrl --output text)"
  fi
  echo "$url"
}

arn_da_fila() {
  aws sqs get-queue-attributes --region "$REGION" --queue-url "$1" \
    --attribute-names QueueArn --query Attributes.QueueArn --output text
}

# Fila com DLQ: a RedrivePolicy é reaplicada a cada execução (set-queue-attributes é idempotente).
criar_fila_com_dlq() {
  local fila="$1" dlq_url dlq_arn url
  dlq_url="$(garantir_fila "${fila}-dlq")"
  dlq_arn="$(arn_da_fila "$dlq_url")"
  url="$(garantir_fila "$fila")"
  local atributos
  atributos="$(mktemp)"
  cat > "$atributos" <<JSON
{"RedrivePolicy": "{\"deadLetterTargetArn\":\"${dlq_arn}\",\"maxReceiveCount\":\"${MAX_RECEIVE_COUNT}\"}"}
JSON
  aws sqs set-queue-attributes --region "$REGION" --queue-url "$url" --attributes "file://${atributos}"
  rm -f "$atributos"
  echo "$url"
}

# Permite que o tópico envie mensagens para a fila.
permitir_envio_do_topico() {
  local fila_url="$1" fila_arn="$2" topico_arn="$3" atributos
  atributos="$(mktemp)"
  python3 - "$fila_arn" "$topico_arn" > "$atributos" <<'PY'
import json, sys
fila_arn, topico_arn = sys.argv[1], sys.argv[2]
politica = {
    "Version": "2012-10-17",
    "Statement": [{
        "Sid": "PermitirEnvioDoSns",
        "Effect": "Allow",
        "Principal": {"Service": "sns.amazonaws.com"},
        "Action": "sqs:SendMessage",
        "Resource": fila_arn,
        "Condition": {"ArnEquals": {"aws:SourceArn": topico_arn}},
    }],
}
print(json.dumps({"Policy": json.dumps(politica)}))
PY
  aws sqs set-queue-attributes --region "$REGION" --queue-url "$fila_url" --attributes "file://${atributos}"
  rm -f "$atributos"
}

# subscribe é idempotente para a mesma combinação tópico/endpoint; RawMessageDelivery é reaplicado.
assinar() {
  local topico_arn="$1" fila_arn="$2" assinatura_arn
  assinatura_arn="$(aws sns subscribe --region "$REGION" --topic-arn "$topico_arn" --protocol sqs \
    --notification-endpoint "$fila_arn" --query SubscriptionArn --output text)"
  aws sns set-subscription-attributes --region "$REGION" --subscription-arn "$assinatura_arn" \
    --attribute-name RawMessageDelivery --attribute-value true
}

echo "Conta ${ACCOUNT_ID}, região ${REGION}"

OS_EVENTS_ARN="$(criar_topico os-events)"
BILLING_EVENTS_ARN="$(criar_topico billing-events)"
EXECUTION_EVENTS_ARN="$(criar_topico execution-events)"

BILLING_FILA_URL="$(criar_fila_com_dlq os-service-billing-events)"
EXECUTION_FILA_URL="$(criar_fila_com_dlq os-service-execution-events)"
BILLING_FILA_ARN="$(arn_da_fila "$BILLING_FILA_URL")"
EXECUTION_FILA_ARN="$(arn_da_fila "$EXECUTION_FILA_URL")"

permitir_envio_do_topico "$BILLING_FILA_URL" "$BILLING_FILA_ARN" "$BILLING_EVENTS_ARN"
permitir_envio_do_topico "$EXECUTION_FILA_URL" "$EXECUTION_FILA_ARN" "$EXECUTION_EVENTS_ARN"
assinar "$BILLING_EVENTS_ARN" "$BILLING_FILA_ARN"
assinar "$EXECUTION_EVENTS_ARN" "$EXECUTION_FILA_ARN"

echo "Tópicos: ${OS_EVENTS_ARN} ${BILLING_EVENTS_ARN} ${EXECUTION_EVENTS_ARN}"
echo "Filas:   ${BILLING_FILA_URL} ${EXECUTION_FILA_URL}"
echo "Recursos de mensageria prontos. Aplique infra/aws/politica-iam-os-service.json à role do os-service."
