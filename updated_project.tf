provider "aws" {
  region = "me-south-1"
  access_key = ""  # Your AWS Access Key ID
  secret_key = ""  # Your AWS Secret Access Key
}

# 1. VPC
resource "aws_vpc" "main" {
  cidr_block = "172.31.0.0/16"
  
}

# 2. Internet Gateway for Public Subnet
resource "aws_internet_gateway" "gateway" {
  vpc_id = aws_vpc.main.id
}

# 3. Public Subnet
resource "aws_subnet" "public_subnet" {
  vpc_id = aws_vpc.main.id
  cidr_block = "172.31.1.0/24"
  availability_zone = "me-south-1a"
  map_public_ip_on_launch = true
  tags = {
    Name = "Public Subnet"
  }
}

# 4. Private Subnet
resource "aws_subnet" "private_subnet" {
  vpc_id = aws_vpc.main.id
  cidr_block = "172.31.2.0/24"
  availability_zone = "me-south-1a"
  tags = {
    Name = "Private Subnet"
  }
}

# 5. Route Table for Public Subnet
resource "aws_route_table" "public_route_table" {
  vpc_id = aws_vpc.main.id
}

# 6. Route for Public Subnet (Internet Gateway)
resource "aws_route" "public_route" {
  route_table_id = aws_route_table.public_route_table.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = aws_internet_gateway.gateway.id
}

# 7. Associate Public Subnet with Route Table
resource "aws_route_table_association" "public_subnet_association" {
  subnet_id = aws_subnet.public_subnet.id
  route_table_id = aws_route_table.public_route_table.id
}

# 8. NAT Gateway for Private Subnet
resource "aws_eip" "nat_ip" {
  
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat_ip.id
  subnet_id = aws_subnet.public_subnet.id
}

# 9. Route Table for Private Subnet
resource "aws_route_table" "private_route_table" {
  vpc_id = aws_vpc.main.id
}

# 10. Route for Private Subnet (NAT Gateway)
resource "aws_route" "private_route" {
  route_table_id = aws_route_table.private_route_table.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = aws_nat_gateway.nat.id
}

# 11. Associate Private Subnet with Route Table
resource "aws_route_table_association" "private_subnet_association" {
  subnet_id = aws_subnet.private_subnet.id
  route_table_id = aws_route_table.private_route_table.id
}

# 12. EC2 Instance for Backend (Laravel) in Private Subnet
resource "aws_instance" "backend_instance" {
  ami = "ami-064ca081dffe98dc2" # Amazon Linux 2 AMI
  instance_type = "t3.micro"
  subnet_id = aws_subnet.private_subnet.id
  key_name = "mykey" # Make sure you specify your key pair
  security_groups = [aws_security_group.sg.id]
  tags = {
    Name = "Backend Server (Laravel)"
  }
}

# 13. EC2 Instance for Frontend (React) in Public Subnet
resource "aws_instance" "frontend_instance" {
  ami = "ami-064ca081dffe98dc2" # Amazon Linux 2 AMI
  instance_type = "t3.micro"
  subnet_id = aws_subnet.public_subnet.id
  key_name = "mykey" # Make sure you specify your key pair
  security_groups = [aws_security_group.sg.id]
  tags = {
    Name = "Frontend Server (React)"
  }
}

# 14. EC2 Instance for Mobile (React Native) in Private Subnet
resource "aws_instance" "mobile_instance" {
  ami = "ami-064ca081dffe98dc2" # Amazon Linux 2 AMI
  instance_type = "t3.micro"
  subnet_id = aws_subnet.private_subnet.id
  key_name = "mykey" # Make sure you specify your key pair
  security_groups = [aws_security_group.sg.id]
  tags = {
    Name = "Mobile App Server (React Native)"
  }
}

# 15. Security Group for EC2 Instances
resource "aws_security_group" "sg" {
  name        = "sg"
  description = "Allow HTTP, HTTPS, SSH access"
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

  ingress {
    from_port   = 443
    to_port     = 443
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
