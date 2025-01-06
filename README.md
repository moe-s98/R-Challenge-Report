# Challenge Report
## Project Demo for the challenge and giving solutions for every requirement.   

This challenge involves the design and operation of a real-time data platform dedicated to tracking and safeguarding food quality. Our objective is to deliver innovative technical solutions that not only enhance the platform's performance but also ensure its robustness during peak period.

# First solution 

<div>
  <img src="https://github.com/user-attachments/assets/d63214ea-3486-406b-b722-6e66b7c01b03" >
</div>

### Tools/Services:

- DevOps Tools

  - Jenkins : automates the build, test, and deployment processes in CI/CD pipelines. It streamlines continuous integration and delivery, ensuring faster and more reliable software releases.
  - Dacker : Docker automates the deployment of applications in lightweight, portable containers. This ensures consistent environments across development, testing, and production, improving efficiency and scalability
  - Ansible : I will use Ansible for server management when needed, as it is agentless and simplifies remote automation. This ensures efficient and streamlined control over our infrastructure.
    
  - Terraform : Terraform automates infrastructure management using a declarative configuration language. It ensures consistent, repeatable deployments across multiple cloud providers.
 
  - Kubernetes : as (EKS) is a managed Kubernetes service that simplifies running Kubernetes on AWS without the need to install and operate your own Kubernetes control plane or nodes. It offers integrated features for scalability, security, and high availability.


   
-  AWS Services

   - AWS Region: AWS Middle East (Bahrain) Region : Region Bahrain is a great choice due to its compliance with local regulations and proximity to Saudi Arabia. AWS is superior to Google Cloud in terms of 
 services and support. Additionally, the Saudi region will open soon, within a months.

   - VPC (Virtual Private Cloud): The architecture is within a VPC

   - Internet Gateway: Provides internet access to the VPC

   - Availability Zones: The architecture spans multiple availability zones enhance high availability and fault tolerance by distributing resources across separate locations. This ensures minimal downtime and provides a resilient infrastructure.

   - Subnets: Subnetting EC2 instances enhances security and improves network traffic management
     - Private Subnet: Used for backend and mobile app components

     - Public Subnet: Used for frontend components

   - Amazon EC2 Instances: Used for backend, frontend, and mobile app components

   - Amazon DynamoDB: Used for database services

   - Amazon ElastiCache for Redis: Used for caching services

   - Amazon EKS (Elastic Kubernetes Service): Manages containerized applications


   - Load Balancer: Distributes incoming traffic across multiple instances

   - CloudWatch: Used for monitoring and logging
 
### Project details and  Workflow : 
Our project architecture consists of three EC2 instances designated for the back-end, front-end, and mobile application. The back-end is built with Laravel and connected to a Redis cache and DynamoDB. The front-end leverages React.js, while the mobile server hosts React Native with Android Build Tools.

The design of this project emphasizes flexibility and adherence to modern DevOps practices. For instance, we have implemented a highly available setup by utilizing two Availability Zones to ensure continued operation in case one zone becomes unavailable. Additionally, we deployed the infrastructure on Amazon EKS (Elastic Kubernetes Service) to enable seamless scalability of nodes based on workload demands.

Considering the periodic high traffic load  mentioned in the chalange , I configured the system to dynamically respond to CPU utilization. When CPU usage reaches 80%, a new node is automatically provisioned, and as the load decreases, the additional node is terminated. This approach ensures a highly flexible, cost-effective, and efficient solution tailored to meet varying workload demands.

### Let's dive more into details 🔍🧾 

#### Explanation of the Pipeline:
 

<div>
<img src="https://github.com/user-attachments/assets/f94d6189-7f50-4683-9856-2a9b21f31da7" width=600 height=250 >
<div>


- Environment Variables:
   DOCKERHUB_CREDENTIALS: Jenkins credentials used for Docker Hub login.
   AWS_REGION: The AWS region for your EKS cluster.
   EKS_CLUSTER_NAME: The name of your EKS cluster.
   BACKEND_IMAGE, FRONTEND_IMAGE, MOBILE_APP_IMAGE: Docker Hub image names for the backend, frontend, and mobile apps.
- Stages:
  -Checkout Code:
   Pulls the latest code from your repository.

  - Build Docker Images:

     For each of the backend, frontend, and mobile apps, a Docker image is built using the respective Dockerfile.
     Make sure that Dockerfile.backend, Dockerfile.frontend, and Dockerfile.mobile exist in your project.
  -Login to Docker Hub:
   Logs into Docker Hub using Jenkins credentials.

  - Push Docker Images:
    Pushes the Docker images to Docker Hub for the backend, frontend, and mobile app.

  - Configure kubectl for EKS:
    Configures the kubectl tool to interact with your EKS cluster.

  - Deploy Applications to EKS:

    For each component (backend, frontend, and mobile app), deploys it to EKS using the respective Kubernetes deployment and service YAML files.
    Make sure the Kubernetes YAML files (backend-deployment.yaml, frontend-deployment.yaml, mobile-app-deployment.yaml) are included in your project.
  - Clean Up:
    Optionally cleans up unused Docker images and caches to save space.
    
  - Post Block:
     always: This block runs after all stages, no matter the result.
     success: This block runs if the pipeline finishes successfully.
     failure: This block runs if the pipeline encounters an error during execution.
  - Prerequisites:
     Ensure you have Docker and AWS CLI installed on the Jenkins server.
     Jenkins must have kubectl and eksctl configured for interacting with your EKS cluster.
     Docker Hub credentials configured in Jenkins.

  Jenkins PipeLine GROOVY:

```
 pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')  // Jenkins Docker Hub credentials
        AWS_REGION = 'us-west-2'
        EKS_CLUSTER_NAME = 'my-cluster'
        BACKEND_IMAGE = '<dockerhub-user>/backend'
        FRONTEND_IMAGE = '<dockerhub-user>/frontend'
        MOBILE_APP_IMAGE = '<dockerhub-user>/mobile-app'
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm  // Checkout the repository code
            }
        }

        stage('Build Backend Docker Image') {
            steps {
                script {
                    // Build backend Docker image using Dockerfile.backend
                    sh '''
                    docker build -t ${BACKEND_IMAGE}:latest -f Dockerfile.backend .
                    '''
                }
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                script {
                    // Build frontend Docker image using Dockerfile.frontend
                    sh '''
                    docker build -t ${FRONTEND_IMAGE}:latest -f Dockerfile.frontend .
                    '''
                }
            }
        }

        stage('Build Mobile App Docker Image') {
            steps {
                script {
                    // Build mobile app Docker image using Dockerfile.mobile
                    sh '''
                    docker build -t ${MOBILE_APP_IMAGE}:latest -f Dockerfile.mobile .
                    '''
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    // Login to Docker Hub using Jenkins credentials
                    docker.withRegistry('https://registry.hub.docker.com', DOCKERHUB_CREDENTIALS) {
                        // Automatically logs in using the provided credentials
                    }
                }
            }
        }

        stage('Push Docker Images to Docker Hub') {
            steps {
                script {
                    // Push the backend, frontend, and mobile app images to Docker Hub
                    sh '''
                    docker push ${BACKEND_IMAGE}:latest
                    docker push ${FRONTEND_IMAGE}:latest
                    docker push ${MOBILE_APP_IMAGE}:latest
                    '''
                }
            }
        }

        stage('Configure kubectl for EKS') {
            steps {
                script {
                    // Set up the kubeconfig file to interact with the EKS cluster
                    sh '''
                    aws eks --region ${AWS_REGION} update-kubeconfig --name ${EKS_CLUSTER_NAME}
                    '''
                }
            }
        }

        stage('Deploy Backend to EKS') {
            steps {
                script {
                    // Deploy the backend app to EKS
                    sh '''
                    kubectl apply -f backend-deployment.yaml
                    kubectl apply -f backend-service.yaml
                    '''
                }
            }
        }

        stage('Deploy Frontend to EKS') {
            steps {
                script {
                    // Deploy the frontend app to EKS
                    sh '''
                    kubectl apply -f frontend-deployment.yaml
                    kubectl apply -f frontend-service.yaml
                    '''
                }
            }
        }

        stage('Deploy Mobile App to EKS') {
            steps {
                script {
                    // Deploy the mobile app (React Native) to EKS
                    sh '''
                    kubectl apply -f mobile-app-deployment.yaml
                    kubectl apply -f mobile-app-service.yaml
                    '''
                }
            }
        }

        stage('Clean Up') {
            steps {
                script {
                    // Optionally, clean up unused Docker images and caches
                    sh 'docker system prune -f'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
        success {
            echo 'Deployment and Docker Push Successful.'
        }
        failure {
            echo 'There was an error during the pipeline execution.'
        }
    }
}
 
 ```

### Dockerization 

<div>
<img src="https://github.com/user-attachments/assets/5d958c39-3b6c-4ba9-bed8-c463ce8645f4" width=600 height=250 >
</div>




 - Backend (Laravel): Dockerfile

```
 # Dockerfile for Laravel (Node 1)
FROM php:8.1-fpm
WORKDIR /var/www
RUN apt-get update && apt-get install -y \
    zip unzip curl \
    && docker-php-ext-install pdo_mysql
COPY . /var/www
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer
RUN composer install
CMD ["php-fpm"]

```  

  - Frontend (React.js): Dockerfile

```

# Dockerfile for React.js (Node 2)
FROM node:18
WORKDIR /app
COPY . /app
RUN npm install
RUN npm run build
CMD ["npm", "start"]

```

  - Dockerfile for React Native with Android Build Tools

```
# Use a base image with Node.js
FROM node:16-bullseye

# Install Java (required for Android builds)
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk \
    && apt-get clean

# Set environment variables for Java
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Android SDK and build tools
RUN mkdir -p /opt/android-sdk && \
    cd /opt/android-sdk && \
    curl -o commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip commandlinetools.zip -d /opt/android-sdk && \
    rm commandlinetools.zip

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$PATH

# Accept Android SDK licenses
RUN yes | sdkmanager --licenses

# Install required Android SDK components
RUN sdkmanager \
    "platform-tools" \
    "platforms;android-33" \
    "build-tools;33.0.2"

# Create app directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install app dependencies
RUN npm install

# Copy the rest of the app files
COPY . .

# Build the React Native app
RUN npm run android

# Default command
CMD ["npm", "start"]

```

### Considerations
Resource Requirements:
Building Android apps is resource-intensive. Ensure your Docker container has sufficient memory and CPU.

Caching:
Use Docker's caching mechanism effectively by copying package.json first and running npm install before copying the rest of the files.


<div>
<img src="https://github.com/user-attachments/assets/506e35b4-22ba-4f3d-af5e-8aba1d08a4bc" width=600 height=250>
</div>

### K8'S

 - Using Amazon EKS with eksctl ensures seamless scalability for your React Native app, Laravel backend, and React frontend. With Horizontal Pod Autoscaling, workloads automatically adjust to traffic spikes, 
   ensuring consistent performance. Cluster Autoscaler dynamically scales worker nodes to match resource demands, reducing costs during low usage.
   
 - For each of the three components (backend, frontend, mobile app), you need to create Kubernetes deployment YAML files. These files describe how your containers will be deployed and exposed on EKS.

Backend Deployment.yaml

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
      - name: backend
        image: <dockerhub-user>/backend:latest
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: backend-service
spec:
  selector:
    app: backend
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
  type: LoadBalancer
```

Frontend Deployment.yaml

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: <dockerhub-user>/frontend:latest
        ports:
        - containerPort: 3000
---
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
spec:
  selector:
    app: frontend
  ports:
    - protocol: TCP
      port: 3000
      targetPort: 3000
  type: LoadBalancer
```
Mobile App Deployment.yaml

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mobile-app-deployment
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mobile-app
  template:
    metadata:
      labels:
        app: mobile-app
    spec:
      containers:
      - name: mobile-app
        image: <dockerhub-user>/mobile-app:latest
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: mobile-app-service
spec:
  selector:
    app: mobile-app
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: LoadBalancer
```
Apply the Kubernetes Configurations
Once you have your Kubernetes deployment YAML files for backend, frontend, and mobile apps, you can apply them to your EKS cluster using kubectl:

```
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f mobile-app-deployment.yaml

```

### Cluster Autoscaler on EKS
   Cluster Autoscaler automatically adjusts the size of your Kubernetes cluster based on the resource demands. If the node utilization is high, it will scale out by adding more nodes, and if there's 
   underutilization, it will scale in by removing nodes.

Steps to Set Up Cluster Autoscaler:
1- Install Cluster Autoscaler: You can deploy Cluster Autoscaler to your EKS cluster using the following steps:

Download the Cluster Autoscaler YAML file for your EKS version:

```
wget https://github.com/kubernetes/autoscaler/releases/download/cluster-autoscaler-<version>/cluster-autoscaler-aws.yaml
Replace <version> with the version that corresponds to your EKS version.
```
Edit the cluster-autoscaler-aws.yaml file to set your EKS cluster name:

```
Copy code
- --cluster-name=<EKS_CLUSTER_NAME>
Replace <EKS_CLUSTER_NAME> with your actual EKS cluster name.
```

Apply the Cluster Autoscaler configuration to your EKS cluster:

```
kubectl apply -f cluster-autoscaler-aws.yaml
Set Permissions for Cluster Autoscaler: Ensure the IAM role used by your EKS nodes has the necessary permissions to allow autoscaling. This typically includes autoscaling:DescribeAutoScalingGroups, autoscaling:SetDesiredCapacity, and ec2:DescribeInstances permissions.
```

If using an IAM role for Service Account (IRSA) with EKS, ensure that the role has the correct trust relationships and policies for the Cluster Autoscaler.

2. Configure Auto Scaling Group (ASG)
The AWS Auto Scaling Group (ASG) automatically adjusts the number of EC2 instances in your EKS node group based on the scaling policies you define.

To configure auto-scaling based on CPU/Memory utilization:

Set Up an Auto Scaling Group for Your EKS Worker Nodes: When you create an EKS worker node group, ensure that the Auto Scaling Group is set up to handle auto-scaling. AWS will automatically create the Auto Scaling Group when you use eksctl or the EKS management console.

Define Scaling Policies: Define the scaling policies based on CPU utilization or other resource metrics (e.g., Memory utilization).

<div>
<img src="https://github.com/user-attachments/assets/c6b4dd4a-f6f8-41bf-b227-4a1e99cd04a8" width=600 height=250>
</div>



### For example, to scale up when CPU utilization reaches 80%, set a scale-up policy:

```
ScalingAdjustment: 1
Cooldown: 300
MetricType: CPUUtilization
Threshold: 80

```
### You can also configure a scale-down policy to reduce the number of nodes when CPU utilization is below a certain threshold, like 50%:

```

ScalingAdjustment: -1
Cooldown: 300
MetricType: CPUUtilization
Threshold: 50
Enable Metrics Collection: Ensure your EC2 instances are reporting their metrics to CloudWatch. This is crucial for the scaling to function correctly. You can enable CloudWatch monitoring when creating the node group.
```

3. Connect Cluster Autoscaler with Auto Scaling Group
The Cluster Autoscaler will monitor the Kubernetes resource utilization and determine when new nodes need to be added. When the resources of a node in your EKS cluster exceed the 80% threshold (for CPU or Memory), Cluster Autoscaler will request the Auto Scaling Group to add more EC2 instances.

4. Verify and Monitor the Setup
Verify Cluster Autoscaler Logs: After applying the Cluster Autoscaler configuration, check the logs to ensure that the autoscaler is working correctly.


```
kubectl -n kube-system logs deployment/cluster-autoscaler
```

Test the Scaling:

Deploy some workloads to increase the CPU or Memory usage on your nodes.
Monitor the Auto Scaling Group to verify that the scaling process works and additional EC2 nodes are provisioned.
CloudWatch Metrics: You can also use AWS CloudWatch to monitor CPU and Memory utilization on your EC2 nodes. You can create CloudWatch alarms to notify you when scaling events occur.

###  Terraform IAC


<div>
<img src="https://github.com/user-attachments/assets/b0657256-11f4-4209-9a83-4930ae8596a9" width=600 height=250>
<img src="https://github.com/user-attachments/assets/71ded2d0-37bb-43e9-b831-a80440a665d2" width=600 height=250>
</div>

Terraform Files and .tfstate attached in repo.

## Cost 
### Below is a detailed cost breakdown and comparison of the two architectures to help you decide the best option. Both setups include EKS, Redis (ElastiCache), DynamoDB, and an Application Load Balancer (ALB), with the difference being the number of Availability Zones (AZs) and EC2 instances.

1. Single Availability Zone Setup
 - 3 EC2 instances in 1 AZ (t3.medium).
 - EKS cluster for container orchestration.
 - ALB, Redis (ElastiCache), and DynamoDB
   
   Cost Breakdown

<div>
<img src="https://github.com/user-attachments/assets/e685198e-8713-46ff-8336-da13d550b2e0">
</div>




2. Double Availability Zone Setup
- 6 EC2 instances (3 in each AZ).
Cost Breakdown

<div>
<img src="https://github.com/user-attachments/assets/d2468661-1014-4e68-8b9d-b9294ca7ee88">
</div>

### Recommendation
 - Single AZ Setup: Cost-effective for smaller workloads and non-critical applications where high availability is not a priority.
 - Double AZ Setup: Suitable for production-grade environments requiring high fault tolerance and redundancy but incurs additional costs.
🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁🏁

# Second solution

<div>
<img src="https://github.com/user-attachments/assets/a75e6259-6cbc-4bc7-b3f8-41fed2d7fe99" width=600 height=250>
</div>


Amazon Fargate is a serverless compute engine for containers that allows you to run containers without managing servers. It automatically provisions and scales the compute resources required to run containerized applications. This project leverages Amazon EKS with Fargate to orchestrate and scale Dockerized applications. It includes a backend built with PHP (Laravel), a frontend developed in React.js, and mobile applications using React Native. The infrastructure is managed via Terraform, with services running in a secure VPC across multiple availability zones. An Application Load Balancer (ALB) is used for traffic distribution, while a Jenkins CI/CD pipeline automates the build, testing, and deployment processes to EKS. The use of Fargate enables automatic scaling and serverless compute for containerized workloads, optimizing cost and performance.

## Project Overview

- The architecture will include:

  - Amazon EKS with Fargate for container orchestration.
  - ALB (Application Load Balancer) for load balancing between services.
  - RDS for database management (optional).
  - Dockerized backend (PHP with Laravel), frontend (React.js), and mobile app (React Native).
  - Jenkins CI/CD Pipeline to automate the build, test, and deployment processes.

Step 1: Set up Infrastructure with Terraform : .tf in the repo 

Step 2: Create RDS (Optional)
If you plan to use RDS, configuration for MySQL

Step 3: Kubernetes Deployments (EKS)
Now that EKS is set up, let's deploy your backend, frontend, and mobile apps using Kubernetes manifests.

Step 4: Jenkins CI/CD Pipeline
We’ll now create the Jenkins Declarative Pipeline for your project to build, test, and deploy it to EKS.

.groovy
```
pipeline {
    agent any
    environment {
        AWS_ACCESS_KEY_ID = credentials('aws-access-key-id')
        AWS_SECRET_ACCESS_KEY = credentials('aws-secret-access-key')
        ECR_REPOSITORY = "<AWS_ACCOUNT_ID>.dkr.ecr.us-west-2.amazonaws.com"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Backend Docker Image') {
            steps {
                script {
                    docker.build("backend:latest")
                }
            }
        }

        stage('Push Backend Image to ECR') {
            steps {
                script {
                    sh "aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin $ECR_REPOSITORY"
                    sh "docker tag backend:latest $ECR_REPOSITORY/backend:latest"
                    sh "docker push $ECR_REPOSITORY/backend:latest"
                }
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                script {
                    docker.build("frontend:latest")
                }
            }
        }

        stage('Push Frontend Image to ECR') {
            steps {
                script {
                    sh "docker tag frontend:latest $ECR_REPOSITORY/frontend:latest"
                    sh "docker push $ECR_REPOSITORY/frontend:latest"
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                script {
                    sh """
                        eksctl utils write-kubeconfig --region us-west-2 --cluster production-cluster
                        kubectl apply -f kubernetes-manifests/backend-deployment.yaml
                        kubectl apply -f kubernetes-manifests/frontend-deployment.yaml
                    """
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
```
Step 5: CI/CD Workflow Overview
Code Commit: Developers push code to the repository.
Build: Jenkins fetches the code, builds the Docker images for both backend and frontend.
Push to ECR: The Docker images are pushed to AWS ECR.
Deploy to EKS: Jenkins deploys the Docker containers to the Amazon EKS Cluster.
ALB Load Balancer: The ALB routes traffic to the services running in EKS.

## Conclusion
Infrastructure: We use Amazon EKS with Fargate to scale our applications automatically. The backend (Laravel), frontend (React), and optional RDS are set up in a secure, scalable environment.
CI/CD: A Jenkins pipeline automates the process of building, testing, and deploying applications, ensuring a smooth flow from code commit to production.

## Cost 

 - Fargate Costs (EKS):
  Fargate Tasks (Compute Costs):

  Pricing is based on the CPU and memory resources allocated to each Fargate task.
  For example, if you use 2 vCPU and 4GB memory per task:
  2 vCPU x $0.04048/hr = $0.08096/hr

  4GB x $0.004445/hr = $0.01778/hr

  Total per task = $0.09874/hr

  Monthly cost (730 hours) = $72.05/task/month

  Assuming 3 tasks running continuously for a month:

  Total for 3 tasks = $72.05 x 3 = $216.15/month

 - EKS Costs:
   
  EKS Cluster Management Fee: $0.10 per hour
  Hourly cost = $0.10/hr
  Monthly cost = $0.10 x 730 hours = $73/month

 - Application Load Balancer (ALB):

  Pricing: $0.0225 per LCU (Load Balancer Capacity Unit) hour

  Assuming 5 LCUs used per month = $0.0225 x 5 x 730 hours = $82.125/month

 - Data Transfer Costs:
   Data transfer costs typically involve outbound data from your application to the internet or other regions. Assuming 100GB of outbound data:

   First 1GB free, then $0.09 per GB

   100GB - 1GB = 99GB x $0.09 = $8.91/month

- Storage (EFS or S3 for Persistent Storage):
   
  EFS (Elastic File System): $0.30 per GB/month (assuming 50GB of storage)

  50GB x $0.30 = $15/month

  OR S3: Depending on usage, but assume 50GB storage:

  $0.023 per GB for the first 50TB = $1.15/month

- Total Estimated Monthly Cost:

  Fargate Tasks: $216.15

  EKS Cluster: $73
  ALB: $82.125

  Data Transfer: $8.91

  Storage (EFS/S3): $15 or $1.15

 Total Cost (Using EFS): $395.185/month

Total Cost (Using S3): $371.35/month
