pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        BACKEND_DIR = 'backend'
        COMPOSE_FILE = 'docker-compose.backend.yml'
        CONFIG_GIT_URI = 'https://github.com/sezerdemir7/ecommerce-config-repo.git'
        CONFIG_GIT_BRANCH = 'main'
        GIT_USERNAME = 'sezerdemir7'
        AWS_S3_BUCKET_NAME = 'capsuleteam'
        FRONTEND_PORT = '80'
        VITE_API_BASE_URL = '/api'
        VITE_APP_NAME = 'N11 Marketplace'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Create backend env file') {
            steps {
                withCredentials([
                    string(credentialsId: 'ecommerce-postgres-password', variable: 'POSTGRES_PASSWORD'),
                    string(credentialsId: 'ecommerce-rabbitmq-password', variable: 'RABBITMQ_PASSWORD'),
                    string(credentialsId: 'ecommerce-config-git-token', variable: 'GIT_TOKEN'),
                    string(credentialsId: 'ecommerce-jwt-key', variable: 'JWT_KEY'),
                    string(credentialsId: 'ecommerce-jwt-issuer-uri', variable: 'JWT_ISSUER_URI'),
                    string(credentialsId: 'ecommerce-jwt-client-secret', variable: 'JWT_CLIENT_SECRET'),
                    string(credentialsId: 'ecommerce-aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'ecommerce-aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    writeFile file: "${env.BACKEND_DIR}/.env", text: """POSTGRES_USER=postgres
POSTGRES_PASSWORD=${env.POSTGRES_PASSWORD}

RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=${env.RABBITMQ_PASSWORD}

CONFIG_GIT_URI=${env.CONFIG_GIT_URI}
CONFIG_GIT_BRANCH=${env.CONFIG_GIT_BRANCH}
GIT_USERNAME=${env.GIT_USERNAME}
GIT_TOKEN=${env.GIT_TOKEN}

JWT_KEY=${env.JWT_KEY}
JWT_ISSUER_URI=${env.JWT_ISSUER_URI}
JWT_CLIENT_SECRET=${env.JWT_CLIENT_SECRET}

AWS_ACCESS_KEY_ID=${env.AWS_ACCESS_KEY_ID}
AWS_SECRET_ACCESS_KEY=${env.AWS_SECRET_ACCESS_KEY}
AWS_S3_BUCKET_NAME=${env.AWS_S3_BUCKET_NAME}

FRONTEND_PORT=${env.FRONTEND_PORT}
VITE_API_BASE_URL=${env.VITE_API_BASE_URL}
VITE_APP_NAME=${env.VITE_APP_NAME}
"""
                    sh "chmod 600 ${env.BACKEND_DIR}/.env"
                }
            }
        }

        stage('Validate compose') {
            steps {
                dir(env.BACKEND_DIR) {
                    sh 'docker compose -f ${COMPOSE_FILE} --env-file .env config --quiet'
                }
            }
        }

        stage('Deploy full stack') {
            steps {
                dir(env.BACKEND_DIR) {
                    sh 'docker compose -f ${COMPOSE_FILE} --env-file .env up -d --build'
                }
            }
        }

        stage('Show status') {
            steps {
                dir(env.BACKEND_DIR) {
                    sh 'docker compose -f ${COMPOSE_FILE} --env-file .env ps'
                }
            }
        }
    }
}
