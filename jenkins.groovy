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
 
