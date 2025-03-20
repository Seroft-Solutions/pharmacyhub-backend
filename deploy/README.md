# Backend Deployment System

This directory contains the deployment configuration and scripts for the CRM backend services.

## Directory Structure

- `docker/` - Docker Compose files for each environment
- `env/` - Environment configuration files
- `scripts/` - Deployment and utility scripts

## Deployment Process

The deployment system is designed to handle multiple environments:

1. **Development** - Automatically deployed from the `dev` branch
2. **QA** - Automatically deployed from the `qa` branch
3. **Production** - Automatically deployed from the `main` branch

## Docker Compose Files

Each environment has its own Docker Compose configuration:

- `docker/docker-compose.dev.yml` - Development environment
- `docker/docker-compose.qa.yml` - QA environment
- `docker/docker-compose.prod.yml` - Production environment
- `docker/docker-compose.local.yml` - Local development (for reference)

## Environment Files

Environment-specific variables are stored in:

- `env/.env.dev` - Development settings
- `env/.env.qa` - QA settings
- `env/.env.prod` - Production settings
- `env/.env.local` - Local development (for reference)

## Deployment Scripts

- `scripts/deploy.sh` - Main deployment script for all environments
- `scripts/backup-database.sh` - Database backup script
- `scripts/restore-database.sh` - Database restore script
- `scripts/setup-directories.sh` - Script to set up required directory structure

## Port Allocation

To avoid conflicts, each environment uses different port mappings:

| Service | Dev | QA | Prod |
|---------|-----|-----|------|
| PostgreSQL | 5432 | 5433 | 5434 |
| Redis | 6379 | 6380 | 6381 |
| Backend | 8081 | 8082 | 8080 |
| Cal.com | 3001 | 3002 | 3003 |

## VPS Directory Structure

The deployment creates a standardized directory structure on the VPS:

```
/opt/CRM/
├── dev/
│   ├── backend/
│   │   ├── docker-compose.yml
│   │   ├── .env
│   │   ├── deploy.sh
│   │   └── logs/
│   └── data/
│       ├── postgres/
│       ├── redis/
│       ├── keycloak/
│       └── backups/postgres/
├── qa/
│   ├── ...same structure as above
└── prod/
    ├── ...same structure as above
```

## Manual Deployment

To manually deploy to a specific environment:

```bash
# SSH to the VPS
ssh root103.135.45.76 -p 22

# Navigate to the environment directory
cd /opt/CRM/dev/backend

# Run the deployment script
./deploy.sh dev
```
