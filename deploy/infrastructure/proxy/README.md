# CRM Proxy Infrastructure

This directory contains the Nginx Proxy Manager and Portainer setup for the CRM system.

## Components

- **Nginx Proxy Manager**: A user-friendly reverse proxy with SSL management
- **Portainer**: Docker container management UI
- **MariaDB**: Database for Nginx Proxy Manager

## Directory Structure

```
/proxy
├── data/                                  # Nginx Proxy Manager data
│   ├── nginx/                             # Nginx configurations
│   │   └── proxy_host/                    # Individual proxy host configurations (1.conf, 2.conf, etc.)
│   ├── letsencrypt-acme-challenge/        # ACME challenge for SSL verification
│   └── keys.json                          # Encryption keys
├── letsencrypt/                           # SSL certificates
│   └── renewal/                           # SSL renewal configurations
├── mysql/                                 # MariaDB database files
├── docker-compose.yml                     # Deployment configuration
└── docker-cleanup.sh                      # Utility script for Docker cleanup
```

## Proxy Host Configurations

The proxy host configurations are stored in the `data/nginx/proxy_host/` directory:

| Config   | Domain                | Service            | Port | Environment |
|----------|----------------------|-------------------|------|-------------|
| 1.conf   | dev.crmcup.com       | crm-frontend-dev  | 3000 | Development |
| 2.conf   | auth.dev.crmcup.com  | keycloak-dev      | 8080 | Development |
| 3.conf   | api.dev.crmcup.com   | crm-backend-dev   | 8081 | Development |
| 4.conf   | portainer.crmcup.com | portainer         | 9000 | Shared      |
| 5.conf   | nginx.crmcup.com     | nginx-proxy-manager| 81   | Shared      |
| 6.conf   | auth.qa.crmcup.com   | keycloak-qa       | 8080 | QA          |
| 7.conf   | qa.crmcup.com        | crm-frontend-qa   | 3000 | QA          |
| 8.conf   | api.qa.crmcup.com    | crm-backend-qa    | 8082 | QA          |
| 9.conf   | www.crmcup.com       | crm-frontend-prod | 3000 | Production  |
| 10.conf  | api.crmcup.com       | crm-backend-prod  | 8080 | Production  |
| 11.conf  | auth.crmcup.com      | keycloak-prod     | 8080 | Production  |

## Environment Port Mapping

### Development Environment
- Frontend: Container `crm-frontend-dev` on port 3000
- Backend: Container `crm-backend-dev` on port 8081
- Keycloak: Container `keycloak-dev` on port 8080

### QA Environment
- Frontend: Container `crm-frontend-qa` on port 3000 (external 3010)
- Backend: Container `crm-backend-qa` on port 8082
- Keycloak: Container `keycloak-qa` on port 8080

### Production Environment
- Frontend: Container `crm-frontend-prod` on port 3000 (external 3020)
- Backend: Container `crm-backend-prod` on port 8080
- Keycloak: Container `keycloak-prod` on port 8080

## SSL Certificates

The SSL certificates are managed by Let's Encrypt and configured in the `letsencrypt/renewal/` directory.

## Data Persistence

All configuration data is preserved in Docker volumes:

- Nginx Proxy Manager data: `./data` directory
- MariaDB database: `./mysql` directory
- Let's Encrypt certificates: `./letsencrypt` directory
- Portainer data: Docker named volume `portainer_data`

## Deployment

The infrastructure is deployed using GitHub Actions. The workflow:

1. Stops any running containers to prevent conflicts
2. Creates a backup of existing configurations before deployment
3. Deploys the updated configuration while preserving data
4. Verifies that deployment was successful

See the `.github/workflows/deploy-nginx-portainer.yml` file for details.

## Access 

- Nginx Proxy Manager UI: `http://<server-ip>:81` or `https://nginx.crmcup.com`
  - Default login: `admin@example.com` / `changeme`
- Portainer UI: `http://<server-ip>:9000` or `https://portainer.crmcup.com`
