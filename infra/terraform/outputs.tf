output "topico_os_events_arn" {
  value = aws_sns_topic.os_events.arn
}

output "filas_urls" {
  description = "URLs das filas (OS_FILA_BILLING e OS_FILA_EXECUTION)."
  value       = { for chave, fila in aws_sqs_queue.fila : chave => fila.url }
}

output "dlqs_urls" {
  value = { for chave, fila in aws_sqs_queue.dlq : chave => fila.url }
}

output "role_irsa_arn" {
  description = "ARN para a annotation eks.amazonaws.com/role-arn de k8s/serviceaccount.yaml."
  value       = aws_iam_role.os_service.arn
}

output "repositorio_ecr_url" {
  value = aws_ecr_repository.os_service.repository_url
}
