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
`FRONTEND_PORT` in the `.env` file uploaded to Jenkins.

## Jenkins Credential

Create one Jenkins credential:

```text
Kind: Secret file
ID: ecommerce-backend-env
File: your production .env file
```

The uploaded `.env` file must contain all values from `backend/.env.example`,
including Iyzico values:

```text
IYZICO_API_KEY
IYZICO_SECRET_KEY
IYZICO_BASE_URL
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
copy Jenkins secret file to backend/.env
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
