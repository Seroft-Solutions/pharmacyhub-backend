#!/bin/bash
# Script for fixing Nginx Proxy Manager configuration issues

# Exit script on error, but allow commands with || true to continue
set -e

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting advanced Nginx Proxy Manager configuration fix...${NC}"

# Check if proxy network exists, create it if it doesn't
if ! docker network inspect proxy-network >/dev/null 2>&1; then
  echo -e "${YELLOW}Creating proxy-network...${NC}"
  docker network create proxy-network
  echo -e "${GREEN}Successfully created proxy-network${NC}"
else
  echo -e "${GREEN}proxy-network already exists${NC}"
fi

# Create a function to wait for services to be up
wait_for_service() {
  local service=$1
  local max_attempts=30
  local wait_seconds=5
  local attempts=0
  
  echo -e "${YELLOW}Waiting for $service to be ready...${NC}"
  
  while [ $attempts -lt $max_attempts ]; do
    if [ "$service" == "db" ]; then
      # Test connection using root user with explicit password
      if docker exec npm-db mysqladmin ping -h localhost -u root -pnpm &> /dev/null; then
        echo -e "${GREEN}Database is ready!${NC}"
        return 0
      fi
    elif [ "$service" == "npm" ]; then
      # Use wget instead of curl for Alpine-based images
      if docker exec nginx-proxy-manager wget -q --spider http://localhost:81 &> /dev/null; then
        echo -e "${GREEN}Nginx Proxy Manager is ready!${NC}"
        return 0
      fi
    fi
    
    attempts=$((attempts+1))
    echo "Attempt $attempts/$max_attempts, waiting $wait_seconds seconds..."
    sleep $wait_seconds
  done
  
  echo -e "${RED}Service $service did not become ready in time.${NC}"
  return 1
}

# Stop services to start fresh
echo -e "${YELLOW}Stopping services...${NC}"
docker-compose down

# Clean existing data to avoid authorization issues
echo -e "${YELLOW}Cleaning existing database files...${NC}"
rm -rf ./mysql/*

# Create required directories
echo -e "${YELLOW}Creating required directories...${NC}"
mkdir -p ./mysql ./data ./data/nginx ./letsencrypt

# Ensure config.json exists
if [ ! -f ./data/config.json ]; then
  echo -e "${YELLOW}Creating default config.json...${NC}"
  cat > ./data/config.json << EOF
{
  "database": {
    "engine": "mysql",
    "host": "db",
    "name": "npm",
    "user": "npm",
    "password": "npm",
    "port": 3306
  }
}
EOF
fi

# Start database first
echo -e "${YELLOW}Starting database service...${NC}"
docker-compose up -d db

# Wait for database to be ready with error handling
if ! wait_for_service "db"; then
  echo -e "${RED}Database failed to start properly. Checking logs:${NC}"
  docker-compose logs db
  echo -e "${RED}Trying to fix common issues...${NC}"
  
  # Fix common permissions issues
  echo -e "${YELLOW}Fixing potential permissions issues...${NC}"
  if [ "$CI" != "true" ]; then
    sudo chown -R 999:999 ./mysql || true
  else
    chown -R 999:999 ./mysql 2>/dev/null || echo "Skipping permissions fix in CI mode"
  fi
  
  # Try starting the database again
  echo -e "${YELLOW}Restarting database service...${NC}"
  docker-compose down
  docker-compose up -d db
  
  # Wait again for database
  if ! wait_for_service "db"; then
    echo -e "${RED}Database still failed to start. Exiting.${NC}"
    exit 1
  fi
fi

# Initialize database and create npm user
echo -e "${YELLOW}Initializing database and creating npm user...${NC}"
# Use root user to create the npm user and database
docker exec -i npm-db mysql -u root -pnpm << EOF
CREATE DATABASE IF NOT EXISTS npm;
CREATE USER IF NOT EXISTS 'npm'@'%' IDENTIFIED BY 'npm';
GRANT ALL PRIVILEGES ON npm.* TO 'npm'@'%';
FLUSH PRIVILEGES;
EOF

# Check if users table exists, if not create basic schema
echo -e "${YELLOW}Creating basic schema...${NC}"
docker exec -i npm-db mysql -u root -pnpm npm << EOF
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  name VARCHAR(100) NOT NULL,
  nickname VARCHAR(100),
  email VARCHAR(100) NOT NULL,
  roles JSON NOT NULL
);

CREATE TABLE IF NOT EXISTS hosts (
  id INT AUTO_INCREMENT PRIMARY KEY,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  owner_user_id INT NOT NULL,
  domain_names JSON NOT NULL,
  forward_host VARCHAR(100) NOT NULL,
  forward_port INT NOT NULL,
  access_list_id INT DEFAULT 0,
  certificate_id INT DEFAULT 0,
  ssl_forced BOOLEAN DEFAULT FALSE,
  caching_enabled BOOLEAN DEFAULT FALSE,
  block_exploits BOOLEAN DEFAULT FALSE,
  advanced_config TEXT,
  meta JSON,
  allow_websocket_upgrade BOOLEAN DEFAULT TRUE, 
  http2_support BOOLEAN DEFAULT FALSE,
  forward_scheme VARCHAR(50) DEFAULT "http",
  enabled BOOLEAN DEFAULT TRUE,
  locations JSON
);

-- Insert default admin user if not exists
INSERT IGNORE INTO users (id, name, nickname, email, roles)
VALUES (1, 'Administrator', 'Admin', 'admin@example.com', '["admin"]');
EOF

# Start Nginx Proxy Manager
echo -e "${YELLOW}Starting Nginx Proxy Manager...${NC}"
docker-compose up -d npm

# Wait for Nginx Proxy Manager to be ready
if ! wait_for_service "npm"; then
  echo -e "${RED}Nginx Proxy Manager failed to start. Checking logs:${NC}"
  docker-compose logs npm
  echo -e "${RED}Attempting to restart...${NC}"
  
  # Try to restart the service
  docker-compose restart npm
  
  # Wait again
  if ! wait_for_service "npm"; then
    echo -e "${RED}Nginx Proxy Manager still failed to start. Exiting.${NC}"
    exit 1
  fi
fi

# Start Portainer
echo -e "${YELLOW}Starting Portainer...${NC}"
docker-compose up -d portainer

echo -e "${GREEN}Nginx Proxy Manager configuration fix completed successfully!${NC}"
echo -e "${GREEN}You can now access the admin UI at http://your-server-ip:81${NC}"
echo -e "${GREEN}Default login: admin@example.com / changeme${NC}"
