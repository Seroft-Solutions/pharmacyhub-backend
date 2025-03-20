# PharmacyHub Infrastructure Setup

This directory contains the stable, production-ready infrastructure setup for the PharmacyHub application. The setup includes Nginx Proxy Manager as a reverse proxy and Portainer for container management.

## Components

- **Nginx Proxy Manager**: Provides an easy-to-use admin interface for managing Nginx as a reverse proxy
- **Portainer**: Web-based interface for managing Docker containers
- **MariaDB**: Backend database for Nginx Proxy Manager

## Quick Start

To set up the infrastructure:

```bash
# Navigate to the infrastructure directory
cd /opt/PharmacyHub/infrastructure/proxy

# Make scripts executable
chmod +x *.sh

# Run the setup script
./setup.sh
```

Once setup is complete, you can access:

- **Nginx Proxy Manager**: `http://your-server-ip:81`
  - Default login: `admin@example.com` / `changeme`
- **Portainer**: `http://your-server-ip:9000`
  - Set admin credentials on first login

## Management

The `manage.sh` script provides easy management of the infrastructure:

```bash
# Get help
./manage.sh help

# Start all services
./manage.sh start

# Stop all services
./manage.sh stop

# Restart all services
./manage.sh restart

# Check status
./manage.sh status

# View logs
./manage.sh logs

# View logs for a specific service
./manage.sh logs npm

# Backup
./manage.sh backup

# Restore from backup
./manage.sh restore ./backups/backup_20250320_123456.tar.gz

# Complete reset (warning: destroys all data)
./manage.sh reset
```

## Configuration

The setup uses the following specific versions to ensure stability:

- **Nginx Proxy Manager**: v2.10.3 (stable)
- **MariaDB**: v10.6.16
- **Portainer**: v2.19.4 (Community Edition)

## Troubleshooting

If you experience issues:

1. Check service status:
   ```bash
   ./manage.sh status
   ```

2. View logs:
   ```bash
   ./manage.sh logs
   ```

3. Restart services:
   ```bash
   ./manage.sh restart
   ```

4. If problems persist, you can perform a complete reset:
   ```bash
   ./manage.sh reset
   ```
   **Note**: This will remove all configuration data

## Network Configuration

The services use a Docker network named `proxy-network` which must be created before the services are started (the setup script handles this automatically).

## Security Notes

- After first login to Nginx Proxy Manager, immediately change the default admin password
- Configure Nginx Proxy Manager with proper SSL certificates for production use
- Consider restricting access to the admin interfaces (ports 81 and 9000) to trusted IP addresses
