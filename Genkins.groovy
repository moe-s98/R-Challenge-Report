pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials') // Jenkins credential ID for Docker Hub
        EC2_KEY_PATH = '/path/to/your/key.pem'
        BACKEND_NODE = 'ec2-node-1-address'
        FRONTEND_NODE = 'ec2-node-2-address'
        MOBILE_NODE = 'ec2-node-3-address'
    }

    stages {
        stage('Backend: Build, Dockerize, and Deploy') {
            steps {
                script {
                    dir('backend') {
                        sh '''
                        # Install dependencies
                        composer install
                        
                        # Build Docker image
                        docker build -t ${DOCKERHUB_CREDENTIALS_USR}/backend:latest .
                        
                        # Push to Docker Hub
                        echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                        docker push ${DOCKERHUB_CREDENTIALS_USR}/backend:latest
                        
                        # Deploy on EC2
                        ssh -i ${EC2_KEY_PATH} ec2-user@${BACKEND_NODE} << EOF
                        docker pull ${DOCKERHUB_CREDENTIALS_USR}/backend:latest
                        docker stop backend || true && docker rm backend || true
                        docker run -d --name backend -p 80:80 ${DOCKERHUB_CREDENTIALS_USR}/backend:latest
                        EOF
                        '''
                    }
                }
            }
        }

        stage('Frontend: Build, Dockerize, and Deploy') {
            steps {
                script {
                    dir('frontend') {
                        sh '''
                        # Install dependencies and build
                        npm install
                        npm run build
                        
                        # Build Docker image
                        docker build -t ${DOCKERHUB_CREDENTIALS_USR}/frontend:latest .
                        
                        # Push to Docker Hub
                        echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                        docker push ${DOCKERHUB_CREDENTIALS_USR}/frontend:latest
                        
                        # Deploy on EC2
                        ssh -i ${EC2_KEY_PATH} ec2-user@${FRONTEND_NODE} << EOF
                        docker pull ${DOCKERHUB_CREDENTIALS_USR}/frontend:latest
                        docker stop frontend || true && docker rm frontend || true
                        docker run -d --name frontend -p 3000:3000 ${DOCKERHUB_CREDENTIALS_USR}/frontend:latest
                        EOF
                        '''
                    }
                }
            }
        }

        stage('Mobile App: Build, Dockerize, and Deploy') {
            steps {
                script {
                    dir('mobile-app') {
                        sh '''
                        # Install dependencies
                        npm install
                        
                        # Build Docker image
                        docker build -t ${DOCKERHUB_CREDENTIALS_USR}/mobile-app:latest .
                        
                        # Push to Docker Hub
                        echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                        docker push ${DOCKERHUB_CREDENTIALS_USR}/mobile-app:latest
                        
                        # Deploy on EC2
                        ssh -i ${EC2_KEY_PATH} ec2-user@${MOBILE_NODE} << EOF
                        docker pull ${DOCKERHUB_CREDENTIALS_USR}/mobile-app:latest
                        docker stop mobile-app || true && docker rm mobile-app || true
                        docker run -d --name mobile-app -p 8080:8080 ${DOCKERHUB_CREDENTIALS_USR}/mobile-app:latest
                        EOF
                        '''
                    }
                }
            }
        }
    }
}
