#!/bin/bash
# Script to clean up Docker containers and volumes related to Nginx Proxy Manager

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}[INFO]${NC} Cleaning up Nginx Proxy Manager and related containers..."

# Stop containers
echo -e "${YELLOW}[INFO]${NC} Stopping containers..."
docker-compose down

# Get container IDs
NPM_ID=$(docker ps -a --filter "name=nginx-proxy-manager" -q)
DB_ID=$(docker ps -a --filter "name=npm-db" -q)
PORTAINER_ID=$(docker ps -a --filter "name=portainer" -q)

# Remove containers if they exist
if [ ! -z "$NPM_ID" ]; then
  echo -e "${YELLOW}[INFO]${NC} Removing Nginx Proxy Manager container..."
  docker rm -f $NPM_ID
fi

if [ ! -z "$DB_ID" ]; then
  echo -e "${YELLOW}[INFO]${NC} Removing MariaDB container..."
  docker rm -f $DB_ID
fi

if [ ! -z "$PORTAINER_ID" ]; then
  echo -e "${YELLOW}[INFO]${NC} Removing Portainer container..."
  docker rm -f $PORTAINER_ID
fi

# Ask for confirmation before removing volumes
read -p "Do you want to remove all data volumes as well? This will delete all proxy configurations. (y/N): " CONFIRM
if [[ $CONFIRM =~ ^[Yy]$ ]]; then
  echo -e "${YELLOW}[INFO]${NC} Removing data volumes..."
  
  # Remove proxy data
  if [ -d "./data" ]; then
    echo -e "${YELLOW}[INFO]${NC} Moving ./data to ./data_backup_$(date +%Y%m%d_%H%M%S)..."
    mv ./data "./data_backup_$(date +%Y%m%d_%H%M%S)"
    mkdir -p ./data
    mkdir -p ./data/nginx
    mkdir -p ./data/nginx/proxy_host
    mkdir -p ./data/nginx/custom
    mkdir -p ./data/logs
    
    # Create initial config.json
    echo '{
      "database": {
        "engine": "mysql",
        "host": "db",
        "name": "npm",
        "user": "npm",
        "password": "npm",
        "port": 3306
      }
    }' > ./data/config.json
    
    echo '{
      "key": "npm",
      "iv": "npm"
    }' > ./data/keys.json
  fi
  
  # Remove MySQL data
  if [ -d "./mysql" ]; then
    echo -e "${YELLOW}[INFO]${NC} Moving ./mysql to ./mysql_backup_$(date +%Y%m%d_%H%M%S)..."
    mv ./mysql "./mysql_backup_$(date +%Y%m%d_%H%M%S)"
    mkdir -p ./mysql
  fi
  
  # Remove Let's Encrypt data
  if [ -d "./letsencrypt" ]; then
    echo -e "${YELLOW}[INFO]${NC} Moving ./letsencrypt to ./letsencrypt_backup_$(date +%Y%m%d_%H%M%S)..."
    mv ./letsencrypt "./letsencrypt_backup_$(date +%Y%m%d_%H%M%S)"
    mkdir -p ./letsencrypt
  fi
  
  # Remove Docker named volumes
  PORTAINER_VOLUME=$(docker volume ls -q | grep "portainer_data")
  if [ ! -z "$PORTAINER_VOLUME" ]; then
    echo -e "${YELLOW}[INFO]${NC} Removing portainer_data Docker volume..."
    docker volume rm $PORTAINER_VOLUME
  fi
  
  echo -e "${GREEN}[SUCCESS]${NC} All data volumes have been backed up and reset."
else
  echo -e "${YELLOW}[INFO]${NC} Keeping data volumes."
fi

echo -e "${GREEN}[SUCCESS]${NC} Cleanup completed successfully."
echo -e "${YELLOW}[INFO]${NC} You can now run './run-custom-setup.sh' to start with a clean environment."
