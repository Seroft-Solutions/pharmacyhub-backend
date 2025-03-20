#!/bin/bash
# backup-database.sh
# Enhanced backup script for all environments with data integrity checks

# Check if environment parameter is provided
if [ -z "$1" ]; then
  echo "Error: Environment not specified"
  echo "Usage: ./backup-database.sh <environment>"
  echo "Example: ./backup-database.sh dev"
  exit 1
fi

# Set environment variables
ENV="$1"
CRM_BASE="/opt/PharmacyHub"
ENV_DIR="$CRM_BASE/$ENV"
BACKUP_DIR="$ENV_DIR/data/backups/postgres"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DB_NAME="pharmacyhub_$ENV"
DOCKER_COMPOSE_FILE="$ENV_DIR/backend/docker-compose.yml"
CONTAINER_NAME="pharmacyhub-postgres-$ENV"

# Ensure backup directory exists
mkdir -p "$BACKUP_DIR"

# Check if container is running
if ! docker ps | grep -q "$CONTAINER_NAME"; then
  echo "Warning: Database container '$CONTAINER_NAME' is not running."
  
  # Check if the container exists but is stopped
  if docker ps -a | grep -q "$CONTAINER_NAME"; then
    echo "Container exists but is not running. Attempting to start it temporarily for backup..."
    docker start "$CONTAINER_NAME"
    
    # Wait for container to be ready
    echo "Waiting for database to be ready..."
    max_attempts=10
    counter=0
    
    while [ $counter -lt $max_attempts ]; do
      if docker exec "$CONTAINER_NAME" pg_isready -U postgres; then
        echo "Database is ready for backup!"
        DB_STARTED=true
        break
      fi
      counter=$((counter + 1))
      if [ $counter -eq $max_attempts ]; then
        echo "Database failed to start after $max_attempts attempts"
        echo "Cannot create backup at this time."
        exit 1
      fi
      echo "Attempt $counter/$max_attempts: Database not ready yet..."
      sleep 5
    done
  else
    echo "Container does not exist. Cannot create backup at this time."
    exit 1
  fi
fi

# Create backup
echo "Creating database backup for $ENV environment..."
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.sql"

# Try using docker-compose first, fall back to direct docker command
if [ -f "$DOCKER_COMPOSE_FILE" ]; then
  echo "Using docker-compose for backup..."
  docker compose -f "$DOCKER_COMPOSE_FILE" exec -T "$CONTAINER_NAME" pg_dump -U postgres "$DB_NAME" > "$BACKUP_FILE"
  BACKUP_EXIT_CODE=$?
else
  echo "Docker-compose file not found, using direct docker command..."
  docker exec -t "$CONTAINER_NAME" pg_dump -U postgres "$DB_NAME" > "$BACKUP_FILE"
  BACKUP_EXIT_CODE=$?
fi

# Stop container if we started it temporarily
if [ "$DB_STARTED" = true ]; then
  echo "Stopping temporarily started container..."
  docker stop "$CONTAINER_NAME"
fi

# Check if backup was successful and has content
if [ $BACKUP_EXIT_CODE -eq 0 ] && [ -s "$BACKUP_FILE" ]; then
  echo "Database backup created successfully."
  
  # Verify the backup file has valid SQL content (basic check)
  if grep -q "CREATE TABLE" "$BACKUP_FILE" || grep -q "PostgreSQL database dump" "$BACKUP_FILE"; then
    echo "Backup content verified successfully."
    
    # Compress backup
    echo "Compressing backup file..."
    gzip "$BACKUP_FILE"
    
    # Keep only last 7 days of backups, but always keep the most recent 5 backups
    echo "Cleaning up old backups..."
    find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +7 | sort | head -n -5 | xargs rm -f 2>/dev/null || true
    
    # List available backups
    BACKUP_COUNT=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" | wc -l)
    echo "Backup process completed successfully. You now have $BACKUP_COUNT backups for $ENV environment."
    echo "Latest backup: ${BACKUP_FILE}.gz"
    echo "Backup directory: $BACKUP_DIR"
  else
    echo "Warning: Backup file does not appear to contain valid PostgreSQL dump content."
    echo "Keeping backup file for investigation: $BACKUP_FILE"
    exit 1
  fi
else
  echo "Error: Database backup failed for $ENV environment!"
  echo "Please check the database container is running and accessible."
  exit 1
fi
