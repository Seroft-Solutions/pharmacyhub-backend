#!/bin/bash
# migrate-database.sh
# Automated database migration script for all environments

# Check if environment parameter is provided
if [ -z "$1" ]; then
  echo "Error: Environment not specified"
  echo "Usage: ./migrate-database.sh <environment>"
  echo "Example: ./migrate-database.sh dev"
  exit 1
fi

# Set environment variables
ENV="$1"
CRM_BASE="/home/ubuntu/PharmacyHub"
ENV_DIR="$CRM_BASE/$ENV"
BE_DIR="$ENV_DIR/backend"
ENV_FILE="$BE_DIR/.env"
DOCKER_COMPOSE_FILE="$BE_DIR/docker-compose.yml"
DB_NAME="pharmacyhub_$ENV"
CONTAINER_NAME="pharmacyhub-postgres-$ENV"
MIGRATIONS_DIR="$BE_DIR/migrations"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Validate environment
if [ "$ENV" != "dev" ] && [ "$ENV" != "qa" ] && [ "$ENV" != "prod" ]; then
  echo "Error: Invalid environment. Must be one of: dev, qa, prod"
  exit 1
fi

echo "Starting database migration for $ENV environment..."

# Check if container is running
if ! docker ps | grep -q "$CONTAINER_NAME"; then
  echo "Error: Database container '$CONTAINER_NAME' is not running."
  echo "Please ensure the database is running before attempting migrations."
  exit 1
fi

# Create migrations directory if it doesn't exist
mkdir -p "$MIGRATIONS_DIR"

# Create a backup before migration
echo "Creating pre-migration database backup..."
BACKUP_DIR="$ENV_DIR/data/backups/postgres"
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_pre_migration_${TIMESTAMP}.sql"

# Ensure backup directory exists
mkdir -p "$BACKUP_DIR"

# Create the backup
docker exec "$CONTAINER_NAME" pg_dump -U postgres "$DB_NAME" > "$BACKUP_FILE"

# Check if backup was successful
if [ $? -eq 0 ] && [ -s "$BACKUP_FILE" ]; then
  echo "Pre-migration backup created successfully."
  # Compress backup
  gzip "$BACKUP_FILE"
else
  echo "Error: Pre-migration backup failed."
  echo "Do you want to proceed without a backup? (y/n)"
  read -r response
  if [[ "$response" != "y" ]]; then
    echo "Migration aborted."
    exit 1
  fi
  echo "Proceeding without backup..."
fi

# Check for migration SQL files
MIGRATION_FILES=($(ls -1 "$MIGRATIONS_DIR"/*.sql 2>/dev/null || echo ""))
if [ ${#MIGRATION_FILES[@]} -eq 0 ]; then
  echo "No migration SQL files found in $MIGRATIONS_DIR"
  echo "Migration completed (no changes)."
  exit 0
fi

# Apply migrations in order
echo "Applying database migrations..."
for migration_file in "${MIGRATION_FILES[@]}"; do
  filename=$(basename "$migration_file")
  echo "Running migration: $filename"
  
  # Apply the migration
  cat "$migration_file" | docker exec -i "$CONTAINER_NAME" psql -U postgres -d "$DB_NAME"
  
  # Check if migration was successful
  if [ $? -eq 0 ]; then
    echo "Migration $filename applied successfully."
    
    # Move applied migration to completed directory
    mkdir -p "$MIGRATIONS_DIR/completed"
    mv "$migration_file" "$MIGRATIONS_DIR/completed/${TIMESTAMP}_${filename}"
  else
    echo "Error: Migration $filename failed."
    echo "Restore the database from backup? (y/n)"
    read -r response
    if [[ "$response" == "y" ]]; then
      echo "Restoring from backup..."
      gunzip -c "${BACKUP_FILE}.gz" | docker exec -i "$CONTAINER_NAME" psql -U postgres -d "$DB_NAME"
      if [ $? -eq 0 ]; then
        echo "Restore completed successfully."
      else
        echo "Error: Restore failed. Please check your backup file and restore manually."
      fi
    else
      echo "Continuing without restore. Database may be in an inconsistent state."
    fi
    exit 1
  fi
done

echo "All database migrations completed successfully!"
