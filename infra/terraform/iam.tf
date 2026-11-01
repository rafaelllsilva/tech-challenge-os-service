# IRSA: o ServiceAccount do os-service assume esta role pelo provider OIDC do cluster EKS existente.
# O provider OIDC precisa estar associado ao cluster:
#   eksctl utils associate-iam-oidc-provider --cluster <cluster> --approve
data "aws_eks_cluster" "this" {
  name = var.eks_cluster_name
}

data "aws_iam_openid_connect_provider" "eks" {
  url = data.aws_eks_cluster.this.identity[0].oidc[0].issuer
}

locals {
  oidc_host = replace(data.aws_eks_cluster.this.identity[0].oidc[0].issuer, "https://", "")
}

data "aws_iam_policy_document" "confianca_irsa" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [data.aws_iam_openid_connect_provider.eks.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${local.oidc_host}:sub"
      values   = ["system:serviceaccount:${var.namespace}:${var.service_account}"]
    }

    condition {
      test     = "StringEquals"
      variable = "${local.oidc_host}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "os_service" {
  name               = var.nome_role_irsa
  assume_role_policy = data.aws_iam_policy_document.confianca_irsa.json
}

# Permissões mínimas: publicar em os-events e consumir as duas filas. Sem sqs:CreateQueue, porque
# as filas são criadas aqui (a aplicação usa queue-not-found-strategy=fail).
data "aws_iam_policy_document" "os_service" {
  statement {
    sid       = "PublicarEventosDaOs"
    effect    = "Allow"
    actions   = ["sns:Publish"]
    resources = [aws_sns_topic.os_events.arn]
  }

  statement {
    sid    = "ConsumirFilasDoOsService"
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:GetQueueUrl",
      "sqs:ChangeMessageVisibility",
    ]
    resources = [for fila in aws_sqs_queue.fila : fila.arn]
  }
}

resource "aws_iam_role_policy" "os_service" {
  name   = "os-service-mensageria"
  role   = aws_iam_role.os_service.id
  policy = data.aws_iam_policy_document.os_service.json
}
