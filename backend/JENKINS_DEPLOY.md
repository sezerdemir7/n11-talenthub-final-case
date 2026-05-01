# Jenkins Deploy

This repository is a monorepo. Jenkins should use the root `Jenkinsfile`, not
`backend/Jenkinsfile`.

## Server Requirements

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

The Jenkins user must be able to run Docker commands.

If port `80` is already used by nginx/apache on the server, change
`FRONTEND_PORT` in the root `Jenkinsfile` or in `backend/.env.example`.

## Jenkins Credentials

Create these Jenkins Secret text credentials:

```text
ecommerce-postgres-password
ecommerce-rabbitmq-password
ecommerce-config-git-token
ecommerce-jwt-key
ecommerce-jwt-issuer-uri
ecommerce-jwt-client-secret
ecommerce-aws-access-key-id
ecommerce-aws-secret-access-key
```

## Pipeline Setup

Create a Multibranch Pipeline or Pipeline from SCM job.

Repository:

```text
<your-github-repo-url>
```

Script path:

```text
Jenkinsfile
```

The pipeline does this:

```text
checkout
create backend/.env from Jenkins credentials
validate backend/docker-compose.backend.yml
build and deploy backend services
build and deploy frontend nginx container
show docker compose status
```

Deploy command used by Jenkins:

```bash
cd backend
docker compose -f docker-compose.backend.yml --env-file .env up -d --build
```

If you configure a GitHub webhook, deploy starts after each push. Without a
webhook, the root `Jenkinsfile` polls the repository about every 2 minutes.
