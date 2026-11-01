terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Estado remoto. O bucket precisa existir antes do init (o Terraform não cria o bucket do próprio
  # estado); versionamento recomendado. use_lockfile é o lock nativo do S3 (Terraform >= 1.10).
  backend "s3" {
    bucket       = "archtechs-infra-fase4"
    key          = "infra-os-service"
    region       = "us-east-1"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Projeto = "tech-challenge-fase4"
      Servico = "os-service"
    }
  }
}
