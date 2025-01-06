# main.tf

provider "aws" {
  region = "me-south-1"  # Bahrain region
}

# Define the VPC
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  enable_dns_support = true
  enable_dns_hostnames = true
  tags = {
    Name = "main-vpc"
  }
}

# Define Subnet
resource "aws_subnet" "main_subnet" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "me-south-1a"
  map_public_ip_on_launch = true
  tags = {
    Name = "main-subnet"
  }
}

# Define an Internet Gateway
resource "aws_internet_gateway" "main_igw" {
  vpc_id = aws_vpc.main.id
  tags = {
    Name = "main-igw"
  }
}

# Define EC2 instance
resource "aws_instance" "web_server" {
  ami           = "ami-0c55b159cbfafe1f0"  # Example AMI ID for Amazon Linux 2 in Bahrain region
  instance_type = "t2.micro"
  subnet_id     = aws_subnet.main_subnet.id
  key_name      = "your-key-name"  # Replace with your SSH key name
  tags = {
    Name = "web-server"
  }
}

# Define the Security Group for the EC2 instance
resource "aws_security_group" "web_sg" {
  name        = "web-sg"
  description = "Allow inbound HTTP and SSH traffic"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Create an S3 bucket for storage
resource "aws_s3_bucket" "static_website" {
  bucket = "your-static-website-bucket"  # Replace with your unique bucket name
  acl    = "public-read"

  website {
    index_document = "index.html"
    # Optional: error_document = "error.html"
  }
}

# Create DynamoDB Table
resource "aws_dynamodb_table" "main_table" {
  name           = "main-table"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "id"
  attribute {
    name = "id"
    type = "S"
  }
}

# Create CloudWatch Log Group
resource "aws_cloudwatch_log_group" "log_group" {
  name = "/aws/logs/app-logs"
}

# Define Fargate cluster
resource "aws_eks_cluster" "fargate_cluster" {
  name     = "fargate-cluster"
  role_arn = aws_iam_role.eks_role.arn
  vpc_config {
    subnet_ids = [aws_subnet.main_subnet.id]
  }

  depends_on = [aws_internet_gateway.main_igw]
}

# IAM Role for EKS Cluster
resource "aws_iam_role" "eks_role" {
  name = "eks-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
      }
    ]
  })
}

# Define Fargate Profile for EKS
resource "aws_eks_fargate_profile" "fargate_profile" {
  cluster_name = aws_eks_cluster.fargate_cluster.name
  fargate_profile_name = "fargate-profile"
  pod_execution_role_arn = aws_iam_role.eks_role.arn
  subnet_ids = [aws_subnet.main_subnet.id]
}

# Lambda Function for auto-scaling or specific task
resource "aws_lambda_function" "auto_scaling" {
  function_name = "auto-scaling-function"
  role          = aws_iam_role.eks_role.arn
  handler       = "index.handler"
  runtime       = "nodejs14.x"
  # You will need to upload the function code to an S3 bucket or include inline code
}

output "ec2_instance_id" {
  value = aws_instance.web_server.id
}

output "eks_cluster_name" {
  value = aws_eks_cluster.fargate_cluster.name
}
