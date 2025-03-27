# PharmacyHub Deployment

This directory contains scripts and configurations for deploying the PharmacyHub application to a VPS environment.

## Directory Structure

- `scripts/` - Contains deployment and management scripts
- `docker/` - Docker configurations for application components
- `env/` - Environment-specific configurations
- `infrastructure/` - Infrastructure setup scripts (Nginx Proxy Manager, Portainer)

## Recent Fixes

The following issues have been addressed in the infrastructure setup scripts:

1. **Sudoers File Syntax Error**
   - Fixed syntax in `/etc/sudoers.d/pharmacyhub-postgres` file
   - Implemented proper file creation with validation via visudo
   - Added error handling and backup process

2. **Nginx Proxy Manager and Portainer Deployment**
   - Updated to use the official MariaDB image for better compatibility
   - Fixed environment variable handling
   - Improved healthchecks using wget instead of curl
   - Added proper error handling and diagnostics

## Setting Up Infrastructure

### Prerequisites

- Ubuntu 20.04 or later VPS
- SSH access to the VPS
- Docker and Docker Compose installed on the VPS

### Deployment Methods

#### Method 1: Using GitHub Actions (Recommended)

1. Go to the GitHub repository "Actions" tab
2. Select the "Initialize PharmacyHub VPS Infrastructure" workflow
3. Click "Run workflow" 
4. Select the appropriate environment and click "Run workflow"

This will set up all required infrastructure on the VPS automatically.

#### Method 2: Manual Deployment

From the project root, run:

```bash
./deploy/scripts/deploy-infrastructure.sh <vps-ip> [username] [key-file]
```

This will:
- Create the necessary directory structure on the VPS
- Configure permissions for PostgreSQL directories
- Deploy Nginx Proxy Manager for reverse proxy
- Deploy Portainer for container management

### Deploying Application Components

After infrastructure setup, deploy the application components:

1. **Deploy Backend**

   ```bash
   ./deploy/scripts/deploy.sh <environment>
   ```

   Where `<environment>` is one of: dev, qa, prod

2. **Deploy Frontend**

   ```bash
   # Coming soon
   ```

## Infrastructure Components

### pgAdmin 4

Provides database management and administration capabilities:
- Web UI: Various ports per environment (default: 5050 for dev, 5051 for QA, 5052 for prod)
- Pre-configured connections to each database instance
- Environment variables required:
  - `DEV_PGADMIN_EMAIL`: Admin email for pgAdmin in dev environment (default: admin@pharmacyhub.com)
  - `DEV_PGADMIN_PASSWORD`: Admin password for pgAdmin in dev environment (default: admin)
  - `QA_PGADMIN_EMAIL`: Admin email for pgAdmin in QA environment
  - `QA_PGADMIN_PASSWORD`: Admin password for pgAdmin in QA environment
  - `PROD_PGADMIN_EMAIL`: Admin email for pgAdmin in production environment
  - `PROD_PGADMIN_PASSWORD`: Admin password for pgAdmin in production environment

### Nginx Proxy Manager

Provides reverse proxy capabilities with SSL support. Configuration:
- Admin UI: Port 81
- HTTP: Port 80
- HTTPS: Port 443
- Database: MariaDB 10.6

### Portainer

Docker container management UI:
- Admin UI: Port 9000
- Provides visual management of containers, networks, volumes, etc.

### Directory Layout

The VPS directory structure follows this pattern:
```
/opt/PharmacyHub/
├── dev/                    # Development environment
│   ├── backend/
│   │   └── logs/
│   ├── frontend/
│   │   └── logs/
│   └── data/
│       ├── postgres/       # PostgreSQL data
│       ├── redis/          # Redis data
│       ├── pgadmin/        # pgAdmin data
│       └── backups/        # Database backups
├── qa/                     # QA environment (same structure)
├── prod/                   # Production environment (same structure)
├── infrastructure/
│   └── proxy/              # Nginx Proxy Manager
└── nginx/
    └── logs/               # Nginx logs
```

## Troubleshooting

### Common Issues

1. **Sudoers File Syntax Error**

   If you see errors like `/etc/sudoers.d/pharmacyhub-postgres:1:86: syntax error`, run:
   ```bash
   # Use the GitHub Actions workflow "Fix PharmacyHub Infrastructure Issues"
   # Or manually fix:
   sudo bash -c 'echo "ubuntu ALL=(ALL) NOPASSWD:/usr/bin/chown -R 999:999 /opt/PharmacyHub/*/data/postgres" > /etc/sudoers.d/pharmacyhub-postgres'
   sudo bash -c 'echo "ubuntu ALL=(ALL) NOPASSWD:/usr/bin/chmod 750 /opt/PharmacyHub/*/data/postgres" >> /etc/sudoers.d/pharmacyhub-postgres'
   sudo chmod 440 /etc/sudoers.d/pharmacyhub-postgres
   ```

2. **Nginx Proxy Manager Container Issues**

   If the npm-db container is unhealthy or you see "bash executable not found" errors:
   ```bash
   # Use the GitHub Actions workflow "Fix PharmacyHub Infrastructure Issues"
   # Or manually fix:
   cd /opt/PharmacyHub/infrastructure/proxy
   docker-compose down
   rm -rf ./mysql
   ./run-custom-setup.sh
   ```

3. **Database Connection Issues**

   If containers can't connect to the database:
   ```bash
   # Check logs
   docker logs <container-name>
   
   # Restart the database
   docker restart <db-container-name>
   ```

### Getting Logs

```bash
# View logs for a specific container
docker logs <container-name>

# View logs for all containers in a compose file
cd /path/to/docker-compose/directory
docker-compose logs
```

## CI/CD Integration

This project uses GitHub Actions for automated deployment:

1. **setup-vps-infrastructure.yml** - Sets up the basic infrastructure
2. **deploy.yml** - Deploys the backend application 
3. **infrastructure-fix.yml** - Fixes infrastructure issues

These workflows handle directory creation, permissions, network setup, and container deployment.

## More Information

For more details on specific components:
- [Backend Deployment](./docker/README.md)
- [CI/CD Workflows](../.github/workflows/README.md)
