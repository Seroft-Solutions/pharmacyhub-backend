#!/bin/bash
# PharmacyHub Infrastructure Setup Script
# A complete, reliable setup for Nginx Proxy Manager and Portainer

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║             PharmacyHub Infrastructure Setup               ║${NC}"
echo -e "${YELLOW}╚════════════════════════════════════════════════════════════╝${NC}"

# Create proxy network if it doesn't exist
echo -e "\n${YELLOW}[1/7] Creating Docker network...${NC}"
if ! docker network inspect proxy-network &>/dev/null; then
  docker network create proxy-network
  echo -e "${GREEN}✓ proxy-network created${NC}"
else
  echo -e "${GREEN}✓ proxy-network already exists${NC}"
fi

# Stop any existing containers
echo -e "\n${YELLOW}[2/7] Stopping any existing containers...${NC}"
docker-compose down -v || true
echo -e "${GREEN}✓ Existing containers stopped${NC}"

# Prepare directories
echo -e "\n${YELLOW}[3/7] Creating required directories...${NC}"
mkdir -p ./data ./mysql ./letsencrypt
mkdir -p ./data/nginx
mkdir -p ./data/logs
echo -e "${GREEN}✓ Directories created${NC}"

# Create config file
echo -e "\n${YELLOW}[4/7] Creating configuration file...${NC}"
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
echo -e "${GREEN}✓ Configuration file created${NC}"

# Start the database container first
echo -e "\n${YELLOW}[5/7] Starting database container...${NC}"
docker-compose up -d db
echo -e "${GREEN}✓ Database container started${NC}"

# Wait for database to be ready
echo -e "\n${YELLOW}[6/7] Waiting for database to be ready...${NC}"
COUNTER=0
MAX_TRIES=30
until docker-compose exec -T db mysqladmin ping -h localhost -u root -pnpm --silent || [ $COUNTER -eq $MAX_TRIES ]; do
  echo -ne "."
  sleep 2
  COUNTER=$((COUNTER+1))
done

if [ $COUNTER -eq $MAX_TRIES ]; then
  echo -e "\n${RED}✗ Database did not become ready in time. Please check logs with: docker-compose logs db${NC}"
  exit 1
fi
echo -e "\n${GREEN}✓ Database is ready${NC}"

# Start the remaining containers
echo -e "\n${YELLOW}[7/7] Starting Nginx Proxy Manager and Portainer...${NC}"
docker-compose up -d
echo -e "${GREEN}✓ All containers started${NC}"

echo -e "\n${YELLOW}Waiting for services to initialize (90 seconds)...${NC}"
for i in {1..18}; do
  echo -ne "."
  sleep 5
done
echo -e "\n"

# Check container status
echo -e "${YELLOW}Checking container status:${NC}"
docker-compose ps

# Display login information
echo -e "\n${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║             Setup Complete! Access Details:                 ║${NC}"
echo -e "${GREEN}╠════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║ Nginx Proxy Manager: http://$(hostname -I | awk '{print $1}'):81    ║${NC}"
echo -e "${GREEN}║ Default login: admin@example.com / changeme                 ║${NC}"
echo -e "${GREEN}║                                                             ║${NC}"
echo -e "${GREEN}║ Portainer: http://$(hostname -I | awk '{print $1}'):9000                    ║${NC}"
echo -e "${GREEN}║ (create admin credentials on first login)                   ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo -e "${YELLOW}Note: It may take a minute or two for services to fully initialize.${NC}"
echo -e "${YELLOW}If you can't access them immediately, please wait and try again.${NC}"
