#!/bin/bash
# cleanup.sh
# Script to clean up Docker resources

# Check if environment parameter is provided
if [ -z "$1" ]; then
  echo "Error: Environment not specified"
  echo "Usage: ./cleanup.sh <environment> [--all]"
  echo "Examples:"
  echo "  ./cleanup.sh dev      # Clean unused resources for dev environment"
  echo "  ./cleanup.sh dev --all # Full cleanup of dev environment, including volumes"
  exit 1
fi

# Set environment variables
ENV="$1"
ALL_FLAG="$2"
CRM_BASE="/home/ubuntu/CRM"
ENV_DIR="$CRM_BASE/$ENV"
DOCKER_COMPOSE_PATH="$ENV_DIR/backend/docker-compose.yml"
FE_DOCKER_COMPOSE_PATH="$ENV_DIR/frontend/docker-compose.yml"

# Validate environment
if [ "$ENV" != "dev" ] && [ "$ENV" != "qa" ] && [ "$ENV" != "prod" ]; then
  echo "Error: Invalid environment. Must be one of: dev, qa, prod"
  exit 1
fi

echo "Starting cleanup for $ENV environment..."

# Check Docker Compose files exist
if [ ! -f "$DOCKER_COMPOSE_PATH" ]; then
  echo "Warning: Backend Docker Compose file not found: $DOCKER_COMPOSE_PATH"
fi

# Stop and remove containers
if [ -f "$DOCKER_COMPOSE_PATH" ]; then
  echo "Stopping backend containers..."
  docker compose -f $DOCKER_COMPOSE_PATH down --remove-orphans || true
fi

if [ -f "$FE_DOCKER_COMPOSE_PATH" ]; then
  echo "Stopping frontend containers..."
  docker compose -f $FE_DOCKER_COMPOSE_PATH down --remove-orphans || true
fi

# Prune unused Docker resources
echo "Cleaning up unused Docker resources..."
docker system prune -f

# If --all flag is specified, remove volumes
if [ "$ALL_FLAG" == "--all" ]; then
  echo "WARNING: About to remove all volumes for $ENV environment."
  echo "This will delete ALL DATA for this environment."
  read -p "Are you sure you want to continue? (y/n): " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]
  then
    echo "Removing Docker volumes..."
    docker volume rm $(docker volume ls -q | grep "crm.*$ENV") || true
    
    echo "Cleaning up data directories..."
    rm -rf "$ENV_DIR/data/postgres/*"
    rm -rf "$ENV_DIR/data/redis/*"
    rm -rf "$ENV_DIR/data/keycloak/*"
    
    echo "Keeping backup directory for safety."
  else
    echo "Volume removal aborted."
  fi
fi

echo "Cleanup for $ENV environment completed!"
