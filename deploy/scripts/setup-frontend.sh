#!/bin/bash
# setup-frontend.sh
# Sets up frontend environment files and Docker Compose

# Check if environment parameter is provided
if [ -z "$1" ]; then
  echo "Error: Environment not specified"
  echo "Usage: ./setup-frontend.sh <environment>"
  echo "Example: ./setup-frontend.sh dev"
  exit 1
fi

# Set environment
ENV="$1"
CRM_BASE="/opt/PharmacyHub"
ENV_DIR="$CRM_BASE/$ENV"
BE_ENV_FILE="$ENV_DIR/backend/.env.$ENV"
FE_DIR="$ENV_DIR/frontend"
FE_ENV_FILE="$FE_DIR/.env.$ENV"
DOCKER_COMPOSE_PATH="$FE_DIR/docker-compose.yml"

# Validate environment
if [ "$ENV" != "dev" ] && [ "$ENV" != "qa" ] && [ "$ENV" != "prod" ]; then
  echo "Error: Invalid environment. Must be one of: dev, qa, prod"
  exit 1
fi

# Create frontend directory if it doesn't exist
mkdir -p "$FE_DIR"

# Load backend environment variables
echo "Loading backend environment variables..."
if [ ! -f "$BE_ENV_FILE" ]; then
  echo "Error: Backend environment file not found: $BE_ENV_FILE"
  exit 1
fi

set -a
source "$BE_ENV_FILE"
set +a

# Create frontend environment file
echo "Creating frontend environment file..."
cat << EOF > "$FE_ENV_FILE"
# PharmacyHub Frontend Environment Configuration for $ENV
NEXT_PUBLIC_API_BASE_URL=${ENV}_API_BASE_URL
NODE_ENV=$ENV
EOF

# Create frontend Docker Compose file
echo "Creating frontend Docker Compose file..."
cat << EOF > "$DOCKER_COMPOSE_PATH"
version: '3.8'

services:
  pharmacyhub-frontend-$ENV:
    image: \${${ENV}_FRONTEND_DOCKER_IMAGE}
    container_name: pharmacyhub-frontend-$ENV
    env_file: .env.$ENV
    environment:
      NODE_ENV: $ENV
    ports:
      - "\${${ENV}_FRONTEND_PORT}:3000"
    networks:
      - $ENV-network
      - proxy-network
    restart: unless-stopped
    deploy:
      restart_policy:
        condition: on-failure
        max_attempts: 3
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  $ENV-network:
    name: pharmacyhub-$ENV-network
    external: true
  proxy-network:
    name: proxy-network
    external: true
EOF

echo "PharmacyHub frontend setup for $ENV environment completed successfully!"
