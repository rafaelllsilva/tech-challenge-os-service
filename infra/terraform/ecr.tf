resource "aws_ecr_repository" "os_service" {
  name                 = var.repositorio_ecr
  image_tag_mutability = "MUTABLE" # o deploy.yml também publica a tag latest

  image_scanning_configuration {
    scan_on_push = true
  }
}
