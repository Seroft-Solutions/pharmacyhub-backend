#!/bin/bash
# Backend deployment script for all environments
# Enhanced for data persistence and zero data loss

# Check if environment parameter is provided
if [ -z "$1" ]; then
  echo "Error: Environment not specified"
  echo "Usage: ./deploy.sh <environment>"
  echo "Example: ./deploy.sh dev"
  exit 1
fi

# Set environment variables
ENV="$1"
CRM_BASE="/home/ubuntu/CRM"
ENV_DIR="$CRM_BASE/$ENV"
BE_DIR="$ENV_DIR/backend"
ENV_FILE="$BE_DIR/.env"
DOCKER_COMPOSE_FILE="$BE_DIR/docker-compose.yml"

# Validate environment
if [ "$ENV" != "dev" ] && [ "$ENV" != "qa" ] && [ "$ENV" != "prod" ]; then
  echo "Error: Invalid environment. Must be one of: dev, qa, prod"
  exit 1
fi

echo "Starting backend deployment for $ENV environment..."

# Ensure directory structure exists
mkdir -p "$BE_DIR"
mkdir -p "$BE_DIR/logs"
mkdir -p "$ENV_DIR/data/postgres"
mkdir -p "$ENV_DIR/data/redis"
mkdir -p "$ENV_DIR/data/keycloak"
mkdir -p "$ENV_DIR/data/backups/postgres"

# Permissions management with safer error handling
echo "Setting up directory permissions..."
# For user-owned directories, set permissions directly
chown -R ubuntu:ubuntu "$ENV_DIR/data/redis" 2>/dev/null || true
chown -R ubuntu:ubuntu "$ENV_DIR/data/keycloak" 2>/dev/null || true
chown -R ubuntu:ubuntu "$ENV_DIR/data/backups" 2>/dev/null || true
chown -R ubuntu:ubuntu "$BE_DIR/logs" 2>/dev/null || true

# If not running in GitHub Actions (CI=true is set by GitHub), try to set postgres permissions
if [ "$CI" != "true" ]; then
  echo "Setting Postgres data directory permissions..."
  # Try without sudo first - might work if permissions are already correct
  chown -R 999:999 "$ENV_DIR/data/postgres" 2>/dev/null || \
  # Try non-interactive sudo
  sudo -n chown -R 999:999 "$ENV_DIR/data/postgres" 2>/dev/null || \
  # Try regular sudo as last resort
  sudo chown -R 999:999 "$ENV_DIR/data/postgres" 2>/dev/null || \
  echo "Warning: Could not set Postgres directory ownership. You may need to set it manually."
  
  # Similar approach for chmod
  chmod 750 "$ENV_DIR/data/postgres" 2>/dev/null || \
  sudo -n chmod 750 "$ENV_DIR/data/postgres" 2>/dev/null || \
  sudo chmod 750 "$ENV_DIR/data/postgres" 2>/dev/null || \
  echo "Warning: Could not set Postgres directory permissions. You may need to set them manually."
else
  echo "Running in CI environment, skipping sudo operations."
fi

# Verify env file exists
if [ ! -f "$ENV_FILE" ]; then
  echo "Error: Environment file not found: $ENV_FILE"
  exit 1
fi

# Verify Docker Compose file exists
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
  echo "Error: Docker Compose file not found: $DOCKER_COMPOSE_FILE"
  exit 1
fi

# Create additional backup right before deployment
if [ -d "$ENV_DIR/data/postgres" ] && [ "$(ls -A $ENV_DIR/data/postgres)" ]; then
  echo "Creating pre-deployment database backup..."
  # Check if backup script exists and is executable
  if [ -f "./backup-database.sh" ] && [ -x "./backup-database.sh" ]; then
    ./backup-database.sh "$ENV"
  else
    # Manual backup if script is not available
    BACKUP_DIR="$ENV_DIR/data/backups/postgres"
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    DB_NAME="crm_$ENV"
    CONTAINER_NAME="crm-postgres-$ENV"
    
    # Check if container is running before attempting backup
    if docker ps | grep -q "$CONTAINER_NAME"; then
      echo "Creating manual backup of database before deployment..."
      BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_pre_deploy_${TIMESTAMP}.sql"
      docker exec $CONTAINER_NAME pg_dump -U postgres $DB_NAME > "$BACKUP_FILE"
      
      # Check if backup was successful
      if [ $? -eq 0 ]; then
        echo "Pre-deployment backup created successfully."
        # Compress backup
        gzip "$BACKUP_FILE"
      else
        echo "Warning: Pre-deployment backup failed, proceeding with caution."
      fi
    else
      echo "Database container not running, cannot create backup"
    fi
  fi
fi

# Load environment variables
echo "Loading environment variables..."
set -a
source "$ENV_FILE"
set +a

# Deploy
echo "Deploying backend for $ENV environment..."
cd "$BE_DIR"

# Login to Docker registry if credentials are provided
if [ ! -z "$DOCKER_USERNAME" ] && [ ! -z "$DOCKER_PASSWORD" ]; then
  echo "Logging in to Docker registry..."
  echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
fi

# Check if containers are already running
CONTAINERS_RUNNING=$(docker compose -f docker-compose.yml ps -q 2>/dev/null | wc -l)

if [ "$CONTAINERS_RUNNING" -gt 0 ]; then
  echo "Detected running containers. Stopping them gracefully first..."
  # Stop containers gracefully with a timeout
  echo "Gracefully stopping containers (30 second timeout)..."
  docker compose -f docker-compose.yml stop -t 30 || true
  
  # Down but preserve volumes to ensure data persistence
  echo "Taking down containers but preserving volumes..."
  docker compose -f docker-compose.yml down --remove-orphans --volumes=false || true
  
  echo "Existing containers gracefully stopped."
else
  echo "No existing containers detected."
fi

# Pull latest images
echo "Pulling latest images..."
docker compose -f docker-compose.yml pull

# Start services
echo "Starting services..."
docker compose -f docker-compose.yml up -d

# Only verify database if not in CI mode
if [ "$CI" != "true" ]; then
  echo "Verifying database is running..."
  max_attempts=30
  counter=0
  db_container="crm-postgres-$ENV"

  while [ $counter -lt $max_attempts ]; do
    if docker compose -f docker-compose.yml exec -T $db_container pg_isready -U postgres; then
      echo "Database is ready!"
      break
    fi
    counter=$((counter + 1))
    if [ $counter -eq $max_attempts ]; then
      echo "Database failed to start after $max_attempts attempts"
      exit 1
    fi
    echo "Attempt $counter/$max_attempts: Database not ready yet..."
    sleep 10
  done

  # Verify all services are running
  echo "Verifying all services..."
  sleep 30
  if ! docker compose -f docker-compose.yml ps | grep -q "Exit"; then
    echo "All backend services are running successfully!"
  else
    echo "Some services failed to start:"
    docker compose -f docker-compose.yml ps
    exit 1
  fi
else
  echo "CI mode detected, skipping database and service verification."
  echo "Services are starting up in background. Please check their status manually."
fi

# Display access URLs
if [ "$ENV" == "dev" ]; then
  echo "Backend API is now accessible at https://api.dev.crmcup.com"
  echo "Keycloak is now accessible at https://auth.dev.crmcup.com"
  echo "Cal.com is now accessible at https://cal.dev.crmcup.com"
elif [ "$ENV" == "qa" ]; then
  echo "Backend API is now accessible at https://api.qa.crmcup.com"
  echo "Keycloak is now accessible at https://auth.qa.crmcup.com"
  echo "Cal.com is now accessible at https://cal.qa.crmcup.com"
elif [ "$ENV" == "prod" ]; then
  echo "Backend API is now accessible at https://api.crmcup.com"
  echo "Keycloak is now accessible at https://auth.crmcup.com"
  echo "Cal.com is now accessible at https://cal.crmcup.com"
fi

echo "Backend deployment for $ENV environment completed successfully!"
