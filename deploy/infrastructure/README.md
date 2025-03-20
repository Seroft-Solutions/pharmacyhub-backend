# CRM Infrastructure

This directory contains shared infrastructure components for the CRM system. These components are common across all environments (dev, qa, prod).

## Components

### Proxy Infrastructure (`/proxy`)

The proxy infrastructure consists of:

- **Nginx Proxy Manager**: A user-friendly reverse proxy with SSL management
- **Portainer**: Docker container management UI
- **MariaDB**: Database for Nginx Proxy Manager

This setup provides:
- SSL termination for all environments
- Centralized domain management
- Docker container monitoring and management

## Environment Architecture

### Development Environment
- Frontend: `dev.crmcup.com` → `crm-frontend-dev:3000`
- Backend API: `api.dev.crmcup.com` → `crm-backend-dev:8081`
- Authentication: `auth.dev.crmcup.com` → `keycloak-dev:8080`

### QA Environment
- Frontend: `qa.crmcup.com` → `crm-frontend-qa:3000`
- Backend API: `api.qa.crmcup.com` → `crm-backend-qa:8082`
- Authentication: `auth.qa.crmcup.com` → `keycloak-qa:8080`

### Production Environment
- Frontend: `www.crmcup.com` → `crm-frontend-prod:3000`
- Backend API: `api.crmcup.com` → `crm-backend-prod:8080`
- Authentication: `auth.crmcup.com` → `keycloak-prod:8080`

### Infrastructure Management
- Portainer: `portainer.crmcup.com` → `portainer:9000`
- Nginx Proxy Manager: `nginx.crmcup.com` → `nginx-proxy-manager:81`

## Deployment Strategy

The infrastructure is deployed as a shared, environment-agnostic setup using GitHub Actions workflow. This provides:

1. **Centralized Management**: All environments are handled by a single infrastructure
2. **Data Persistence**: Configuration and SSL certificates are preserved across deployments
3. **Safe Updates**: Backup mechanisms ensure no data loss during updates
4. **Controlled Deployment**: Proper container lifecycle management (stop before update)

## Prerequisites

- SSH access to the target server
- Docker and Docker Compose installed on the target server
- GitHub repository secrets configured:
  - `SSH_PRIVATE_KEY`: SSH private key for server access
- Proper network configuration with `proxy-network` that all containers can join

## See Also

For detailed information about the proxy infrastructure, see the [Proxy README](./proxy/README.md).
