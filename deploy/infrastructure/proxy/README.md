# Nginx Proxy Manager Configuration

This directory contains the Docker Compose configuration and related scripts for deploying Nginx Proxy Manager, which serves as a reverse proxy for the PharmacyHub application.

## Overview

- **Nginx Proxy Manager**: Used for managing NGINX proxy hosts with an easy-to-use admin interface
- **MariaDB**: Stores proxy configurations
- **Portainer**: Web-based container management

## Automatic Proxy Loading

The system has been enhanced to ensure proxy configurations are properly loaded at startup, fixing previous issues where proxies would not load automatically.

### Key Improvements

1. **Service Startup Order**: Added `depends_on.condition: service_healthy` to ensure the database is fully ready before Nginx Proxy Manager starts
2. **Database Health Checks**: Improved health checks for MariaDB with better timing and retry parameters
3. **Configuration Persistence**: Enhanced volume mapping to ensure data persists correctly
4. **Config.json**: Explicitly creates a `config.json` file with database connection details
5. **Permission Management**: Sets proper file permissions on all data directories
6. **Enhanced Startup Process**: Added proper sequence for container restart after config changes

## Usage

The infrastructure is automatically deployed by the GitHub Actions workflow, but can also be managed manually:

### Manual Steps

1. **Start Services**:
   ```bash
   cd /opt/PharmacyHub/infrastructure/proxy
   docker-compose up -d
   ```

2. **Generate Proxy Configurations**:
   ```bash
   ./setup-proxy-configs.sh
   ```

3. **Check Status**:
   ```bash
   docker ps | grep nginx-proxy-manager
   ```

### Troubleshooting

If proxies are not loading after restart:

1. **Check Database Connection**:
   ```bash
   docker exec npm-db mysqladmin ping -h localhost -u npm -pnpm
   ```

2. **Verify Config Files**:
   ```bash
   ls -la /opt/PharmacyHub/infrastructure/proxy/data/
   cat /opt/PharmacyHub/infrastructure/proxy/data/config.json
   ```

3. **Check Permissions**:
   ```bash
   ls -la /opt/PharmacyHub/infrastructure/proxy/data/
   ```

4. **View Container Logs**:
   ```bash
   docker logs nginx-proxy-manager
   ```

5. **Force Regenerate Configurations**:
   ```bash
   ./setup-proxy-configs.sh
   ```

## Additional Information

- Default admin credentials: admin@example.com / changeme (change after first login)
- Admin interface: http://<server-ip>:81
- All proxy configurations are stored in `data/nginx/proxy_host/` directory
