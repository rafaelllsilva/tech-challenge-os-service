variable "aws_region" {
  description = "Região da AWS (a mesma do cluster EKS e dos tópicos do billing e do execution)."
  type        = string
  default     = "us-east-1"
}

variable "eks_cluster_name" {
  description = "Nome do cluster EKS existente onde o os-service roda."
  type        = string
  default     = "tech-challenge"
}

variable "namespace" {
  description = "Namespace do os-service no cluster. Deve ser o mesmo de k8s/namespace.yaml."
  type        = string
  default     = "os-service"
}

variable "service_account" {
  description = "ServiceAccount do os-service. Deve ser o mesmo de k8s/serviceaccount.yaml."
  type        = string
  default     = "os-service"
}

variable "nome_role_irsa" {
  description = "Nome da role IAM assumida pelo ServiceAccount (annotation em k8s/serviceaccount.yaml)."
  type        = string
  default     = "os-service-irsa"
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
