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
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Load backend env file') {
            steps {
                withCredentials([
                    file(credentialsId: 'ecommerce-backend-env', variable: 'BACKEND_ENV_FILE')
                ]) {
                    sh '''
                        cp "$BACKEND_ENV_FILE" "$BACKEND_DIR/.env"
                        chmod 600 "$BACKEND_DIR/.env"
                    '''
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
