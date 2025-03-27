# pgAdmin 4 Setup and Configuration

This guide explains how to set up and use pgAdmin 4 in the PharmacyHub environment.

## Overview

pgAdmin 4 is a comprehensive database management and administration tool for PostgreSQL databases. It provides a web-based interface for managing your databases, tables, views, and more.

## Environment Configuration

pgAdmin has been added to all environment configurations (dev, local, qa, prod) with pre-configured connections to the respective PostgreSQL instances.

### Port Assignments

| Environment | Port | URL (when running locally) |
|-------------|------|----------------------------|
| Development | 5050 | http://localhost:5050 |
| Local | 5050 | http://localhost:5050 |
| QA | 5051 | http://localhost:5051 |
| Production | 5052 | http://localhost:5052 |

## Authentication

Default credentials are specified through environment variables. For security reasons, it's recommended to change these from the defaults.

### Required Environment Variables

Add these to your environment configuration files (.env):

```
# Development environment
DEV_PGADMIN_EMAIL=admin@pharmacyhub.com  # Default value if not set
DEV_PGADMIN_PASSWORD=admin  # Default value if not set

# QA environment
QA_PGADMIN_EMAIL=admin@pharmacyhub.com
QA_PGADMIN_PASSWORD=admin

# Production environment
PROD_PGADMIN_EMAIL=admin@pharmacyhub.com
PROD_PGADMIN_PASSWORD=admin
```

## Accessing pgAdmin

1. Start the environment using docker-compose:
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. Navigate to the appropriate URL (e.g., http://localhost:5050 for development)

3. Log in using the credentials specified in the environment variables

## Pre-configured Database Connections

The pgAdmin service is pre-configured to connect to the PostgreSQL database in each environment. The connections are automatically created using the following details:

### Development Environment
- Name: PharmacyHub Dev DB
- Host: postgres-dev
- Port: 5432
- Database: pharmacyhub_dev
- Username: postgres
- Password: Value of DEV_DB_PASSWORD

### QA Environment
- Name: PharmacyHub QA DB 
- Host: postgres-qa
- Port: 5432
- Database: pharmacyhub_qa
- Username: postgres
- Password: Value of QA_DB_PASSWORD

### Production Environment
- Name: PharmacyHub Production DB
- Host: postgres-prod
- Port: 5432
- Database: pharmacyhub_prod
- Username: postgres
- Password: Value of PROD_DB_PASSWORD

## Managing Databases through pgAdmin

With pgAdmin, you can:

- Create and manage databases, schemas, tables, views, and functions
- Execute and analyze SQL queries
- Import and export data
- Create and manage users and permissions
- Configure backups and maintenance tasks
- View database server statistics

## Security Considerations

1. **Change Default Credentials**: Always change the default admin email and password for production environments
2. **Network Access**: By default, pgAdmin is accessible from any IP. For production, consider adding network restrictions
3. **Regular Updates**: Keep pgAdmin updated to receive security patches

## Troubleshooting

### Common Issues

1. **Can't connect to pgAdmin**
   - Verify the container is running: `docker ps | grep pgadmin`
   - Check container logs: `docker logs pharmacyhub-pgadmin-dev`
   - Ensure the port is accessible and not blocked by a firewall

2. **Can't connect to the database**
   - Ensure the PostgreSQL container is running
   - Verify the database credentials in the environment variables
   - Check the network connectivity between containers

3. **pgAdmin seems slow**
   - pgAdmin may require more resources than allocated. Consider increasing memory limit
   - Large query results can slow down pgAdmin, consider limiting result sets

### Resetting pgAdmin

If you need to reset pgAdmin to its default state:

```bash
# Stop the container
docker-compose -f docker-compose.dev.yml stop pgadmin-dev

# Remove the volume
docker volume rm pharmacyhub-pgadmin-data-dev

# Start pgAdmin again
docker-compose -f docker-compose.dev.yml up -d pgadmin-dev
```

## Backing Up pgAdmin Data

pgAdmin configuration and settings are stored in the mapped volume. To back up this data:

```bash
# For development environment
cp -r ${CRM_BASE_PATH}/dev/data/pgadmin /path/to/backup/location
```

## Additional Resources

- [pgAdmin 4 Documentation](https://www.pgadmin.org/docs/pgadmin4/latest/index.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
