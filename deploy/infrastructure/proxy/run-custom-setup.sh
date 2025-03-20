#!/bin/bash
# Script for setting up Nginx Proxy Manager and Portainer

# Exit script on error, but allow commands with || true to continue
set -e

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting Nginx Proxy Manager and Portainer setup...${NC}"

# Check if proxy network exists, create it if it doesn't
if ! docker network inspect proxy-network >/dev/null 2>&1; then
  echo -e "${YELLOW}Creating proxy-network...${NC}"
  docker network create proxy-network
  echo -e "${GREEN}Successfully created proxy-network${NC}"
else
  echo -e "${GREEN}proxy-network already exists${NC}"
fi

# Stop services to start fresh
echo -e "${YELLOW}Stopping services...${NC}"
docker-compose down || true

# Clean existing data to avoid authentication issues
echo -e "${YELLOW}Cleaning existing database files...${NC}"
rm -rf ./mysql/*

# Create required directories
echo -e "${YELLOW}Creating required directories...${NC}"
mkdir -p ./mysql ./data ./letsencrypt
mkdir -p ./data/nginx/include
mkdir -p ./data/nginx/proxy_host
mkdir -p ./data/nginx/redirection_host
mkdir -p ./data/nginx/stream
mkdir -p ./data/nginx/dead_host
mkdir -p ./data/nginx/temp

# Create required configuration files
echo -e "${YELLOW}Creating necessary configuration files...${NC}"

# Create resolvers.conf
cat > ./data/nginx/include/resolvers.conf << EOF
resolver 127.0.0.11 valid=10s;
EOF

# Create log.conf
cat > ./data/nginx/include/log.conf << EOF
log_format proxy '[\$time_local] \$remote_addr - \$remote_user - \$server_name to: \$upstream_addr: \$request upstream_response_time \$upstream_response_time msec \$msec request_time \$request_time';
access_log /var/log/nginx/access.log proxy;
error_log /var/log/nginx/error.log warn;
EOF

# Create default locations conf
cat > ./data/nginx/include/default-locations.conf << EOF
location /stub_status {
    allow 127.0.0.1;
    deny all;
    stub_status;
}
EOF

# Create gzip.conf
cat > ./data/nginx/include/gzip.conf << EOF
gzip on;
gzip_disable "msie6";
gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_buffers 16 8k;
gzip_http_version 1.1;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
EOF

# Create websocket.conf
cat > ./data/nginx/include/websocket.conf << EOF
proxy_set_header Upgrade \$http_upgrade;
proxy_set_header Connection \$http_connection;
proxy_http_version 1.1;
EOF

# Create proxy.conf
cat > ./data/nginx/include/proxy.conf << EOF
proxy_set_header Host \$host;
proxy_set_header X-Real-IP \$remote_addr;
proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto \$scheme;
proxy_set_header X-Forwarded-Host \$host;
proxy_set_header X-Forwarded-Port \$server_port;
EOF

# Create production.json config file
echo -e "${YELLOW}Creating production.json config file...${NC}"
cat > ./data/production.json << EOF
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

# Create a symbolic link from config.json to production.json
echo -e "${YELLOW}Linking config.json to production.json...${NC}"
ln -sf ./data/production.json ./data/config.json || true

# Start database first
echo -e "${YELLOW}Starting database service...${NC}"
docker-compose up -d db

# Define function to wait for database readiness
wait_for_db() {
  local max_attempts=20
  local attempt=0
  local wait_seconds=5
  
  echo -e "${YELLOW}Waiting for database to be ready...${NC}"
  
  while [ $attempt -lt $max_attempts ]; do
    if docker exec npm-db mysqladmin ping -h localhost -u root -pnpm &> /dev/null; then
      echo -e "${GREEN}Database is ready!${NC}"
      return 0
    fi
    
    attempt=$((attempt + 1))
    echo "Attempt $attempt/$max_attempts, waiting $wait_seconds seconds..."
    sleep $wait_seconds
  done
  
  echo -e "${RED}Database did not become ready in time.${NC}"
  return 1
}

# Wait for database to be ready
wait_for_db

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

# Start Nginx Proxy Manager and Portainer
echo -e "${YELLOW}Starting all services...${NC}"
docker-compose up -d

# Function to check container logs for errors
check_container_logs() {
  local container=$1
  local max_lines=50
  
  echo -e "${YELLOW}Checking logs for $container:${NC}"
  docker logs --tail $max_lines $container
}

# Wait for containers to be healthy
echo -e "${YELLOW}Waiting for services to be ready (30 seconds)...${NC}"
sleep 30

# Check container status
echo -e "${YELLOW}Checking container status:${NC}"
docker ps | grep -E 'nginx-proxy-manager|portainer|npm-db'

# Check logs if there are issues
check_container_logs nginx-proxy-manager
check_container_logs npm-db

echo -e "${GREEN}Setup completed! You can access:${NC}"
echo -e "${GREEN}- Nginx Proxy Manager: http://your-server-ip:81${NC}"
echo -e "${GREEN}  Default login: admin@example.com / changeme${NC}"
echo -e "${GREEN}- Portainer: http://your-server-ip:9000${NC}"
