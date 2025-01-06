# 1. Public Subnet
resource "aws_subnet" "public_subnet2" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "172.32.1.0/24"
  availability_zone       = "me-south-1b"
  map_public_ip_on_launch = true
  tags = {
    Name = "Public Subnet"
  }
}

# 2. Private Subnet
resource "aws_subnet" "private_subnet2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "172.32.2.0/24"
  availability_zone = "me-south-1b"
  tags = {
    Name = "Private Subnet"
  }
}

# 3. EC2 Instance for Backend (Laravel) in Private Subnet
resource "aws_instance" "backend_instance2" {
  ami             = "ami-064ca081dffe98dc2"
  instance_type   = "t3.micro"
  subnet_id       = aws_subnet.private_subnet2.id
  key_name        = "mykey"
  security_groups = [aws_security_group.sg.id]
  tags = {
    Name = "Backend Server (Laravel)"
  }
}

# 4. EC2 Instance for Frontend (React) in Public Subnet
resource "aws_instance" "frontend_instance2" {
  ami             = "ami-064ca081dffe98dc2"
  instance_type   = "t3.micro"
  subnet_id       = aws_subnet.public_subnet2.id
  key_name        = "mykey"
  security_groups = [aws_security_group.sg.id]
  tags = {
    Name = "Frontend Server (React)"
  }
}

# 5. EC2 Instance for Mobile (React Native) in Private Subnet
resource "aws_instance" "mobile_instance2" {
  ami             = "ami-064ca081dffe98dc2"
  instance_type   = "t3.micro"
  subnet_id       = aws_subnet.private_subnet2.id
  key_name        = "mykey"
  security_groups = [aws_security_group.sg.id]
  tags = {
    Name = "Mobile App Server (React Native)"
  }
}

# 6. New Public Subnet (Optional)
resource "aws_subnet" "public_subnet3" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "172.32.3.0/24"
  availability_zone       = "me-south-1b"
  map_public_ip_on_launch = true
  tags = {
    Name = "Public Subnet 3"
  }
}

# 7. New Private Subnet (Optional)
resource "aws_subnet" "private_subnet3" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "172.32.4.0/24"
  availability_zone = "me-south-1b"
  tags = {
    Name = "Private Subnet 3"
  }
}
