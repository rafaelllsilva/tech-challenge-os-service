variable "aws_region" {
  description = "Região da AWS (a mesma do cluster EKS e dos tópicos do billing e do execution)."
  type        = string
  default     = "us-east-1"
}

variable "max_receive_count" {
  description = "Quantas vezes uma mensagem é entregue antes de ir para a DLQ."
  type        = number
  default     = 5
}

variable "repositorio_ecr" {
  description = "Nome do repositório ECR da imagem (usado pelo deploy.yml)."
  type        = string
  default     = "os-service"
}
