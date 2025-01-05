# Challenge Report
## Project Demo for the challenge and giving solutions for every requirement.   

This challenge involves the design and operation of a real-time data platform dedicated to tracking and safeguarding food quality. Our objective is to deliver innovative technical solutions that not only enhance the platform's performance but also ensure its robustness during peak period.

## First solution 

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


