#!/bin/bash
# restore-database.sh
# Unified restore script for all environments

# Check if environment and backup file parameters are provided
if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Error: Missing required parameters"
  echo "Usage: ./restore-database.sh <environment> <backup_file>"
  echo "Example: ./restore-database.sh dev pharmacyhub_dev_20240106_020000.sql.gz"
  exit 1
fi

# Set environment variables
ENV="$1"
BACKUP_FILE="$2"
CRM_BASE="/opt/PharmacyHub"
ENV_DIR="$CRM_BASE/$ENV"
BACKUP_DIR="$ENV_DIR/data/backups/postgres"
DB_NAME="pharmacyhub_$ENV"
DOCKER_COMPOSE_FILE="$ENV_DIR/backend/docker-compose.yml"
CONTAINER_NAME="pharmacyhub-postgres-$ENV"

# Check if backup file exists
if [ ! -f "$BACKUP_DIR/$BACKUP_FILE" ]; then
  echo "Error: Backup file not found: $BACKUP_DIR/$BACKUP_FILE"
  echo "Please ensure the backup file exists in $BACKUP_DIR"
  exit 1
fi

echo "Starting database restore process for $ENV environment..."

# If file is gzipped, uncompress it
if [[ "$BACKUP_FILE" == *.gz ]]; then
  echo "Uncompressing and restoring gzipped backup..."
  gunzip < "$BACKUP_DIR/$BACKUP_FILE" | docker compose -f $DOCKER_COMPOSE_FILE exec -T $CONTAINER_NAME psql -U postgres -d $DB_NAME
else
  echo "Restoring uncompressed backup..."
  cat "$BACKUP_DIR/$BACKUP_FILE" | docker compose -f $DOCKER_COMPOSE_FILE exec -T $CONTAINER_NAME psql -U postgres -d $DB_NAME
fi

# Check if restore was successful
if [ $? -eq 0 ]; then
  echo "Database restore completed successfully for $ENV environment"
else
  echo "Error: Database restore failed for $ENV environment!"
  exit 1
fi
