pipeline {
    agent any

    tools {
        jdk 'jdk17'
        gradle 'gradle8'
    }

    environment {
        SONAR_TOKEN = credentials('sonarqube-token')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/rutaRoze/chat.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh './gradlew clean test jacocoTestReport'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "./gradlew sonar -Dsonar.token=${SONAR_TOKEN}"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t chat-app:\${BUILD_NUMBER} ."
                sh "docker tag chat-app:\${BUILD_NUMBER} chat-app:latest"
            }
        }

        stage('Run Docker Container') {
            steps {
                echo 'Running Docker container...'
                sh '''#!/bin/bash
        docker stop chat-app || true
        docker rm chat-app || true
        docker run -d --name chat-app -p 8080:8080 chat-app:${BUILD_NUMBER}
        '''
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully.'
        }
        failure {
            echo '❌ Pipeline failed. Please check the logs and SonarQube dashboard.'
        }
        always {
            echo '🧹 Cleaning up...'
            sh './gradlew --stop || true'
            sh 'docker image prune -a -f || true'
        }
    }
}