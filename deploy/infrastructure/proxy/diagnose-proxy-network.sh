#!/bin/bash
# diagnose-proxy-network.sh
# Diagnose Nginx Proxy Manager connection issues

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Diagnosing Nginx Proxy Manager network issues...${NC}"

# Check if containers are running
echo -e "\n${YELLOW}Checking container status:${NC}"
docker ps -a | grep -E 'nginx-proxy-manager|portainer|npm-db'

# Get container IDs
NPM_ID=$(docker ps -a --filter "name=nginx-proxy-manager" -q)
DB_ID=$(docker ps -a --filter "name=npm-db" -q)
PORTAINER_ID=$(docker ps -a --filter "name=portainer" -q)

if [ -z "$NPM_ID" ]; then
  echo -e "${RED}Nginx Proxy Manager container not found!${NC}"
else
  echo -e "\n${YELLOW}Checking Nginx Proxy Manager container details:${NC}"
  
  # Check container status
  echo -e "\n${YELLOW}Container state:${NC}"
  docker inspect --format='{{.State.Status}}' "$NPM_ID"
  
  # Check health status
  echo -e "\n${YELLOW}Health status:${NC}"
  docker inspect --format='{{.State.Health.Status}}' "$NPM_ID"
  
  # Check last health check result
  echo -e "\n${YELLOW}Last health check result:${NC}"
  docker inspect --format='{{range .State.Health.Log}}{{.Output}}{{end}}' "$NPM_ID" | tail -n 1
  
  # Check port mappings
  echo -e "\n${YELLOW}Port mappings:${NC}"
  docker port "$NPM_ID"
  
  # Check network settings
  echo -e "\n${YELLOW}Network settings:${NC}"
  docker inspect --format='{{range $k, $v := .NetworkSettings.Ports}}{{$k}} -> {{$v}}{{println}}{{end}}' "$NPM_ID"
  
  # Check logs for any errors
  echo -e "\n${YELLOW}Recent logs:${NC}"
  docker logs --tail 50 "$NPM_ID"
  
  # Try to connect to the service from inside the container
  echo -e "\n${YELLOW}Testing internal connectivity:${NC}"
  docker exec "$NPM_ID" curl -I --max-time 5 http://localhost:81 || echo -e "${RED}Failed to connect internally${NC}"
fi

# Check network configuration
echo -e "\n${YELLOW}Docker networks:${NC}"
docker network ls

# Check if proxy-network exists
echo -e "\n${YELLOW}Checking proxy-network:${NC}"
docker network inspect proxy-network || echo -e "${RED}proxy-network does not exist!${NC}"

# Check host networking
echo -e "\n${YELLOW}Host network interfaces:${NC}"
ip addr show

# Check if ports are being listened on
echo -e "\n${YELLOW}Checking for listening ports:${NC}"
netstat -tuln | grep -E '80|81|443|9000' || echo -e "${RED}No matching listening ports found!${NC}"

# Check if firewall might be blocking
echo -e "\n${YELLOW}Checking firewall status:${NC}"
if command -v ufw &> /dev/null; then
  ufw status
elif command -v firewall-cmd &> /dev/null; then
  firewall-cmd --list-all
else
  echo "No common firewall found (ufw/firewalld)"
fi

# Attempt to fix common issues
echo -e "\n${YELLOW}Attempting to fix common issues...${NC}"

# Recreate container with explicit port bindings
echo -e "\n${YELLOW}Do you want to recreate the Nginx Proxy Manager container with explicit port bindings? (y/n)${NC}"
read -r RECREATE
if [[ "$RECREATE" =~ ^[Yy]$ ]]; then
  echo -e "${YELLOW}Stopping containers...${NC}"
  docker-compose down
  
  echo -e "${YELLOW}Modifying docker-compose.yml to ensure explicit port bindings...${NC}"
  # Create a backup
  cp docker-compose.yml docker-compose.yml.bak
  
  # Modify the file to ensure explicit host binding for ports
  sed -i 's/- \'80:80\'/- \'0.0.0.0:80:80\'/g' docker-compose.yml
  sed -i 's/- \'443:443\'/- \'0.0.0.0:443:443\'/g' docker-compose.yml
  sed -i 's/- \'81:81\'/- \'0.0.0.0:81:81\'/g' docker-compose.yml
  
  echo -e "${YELLOW}Starting containers with explicit port bindings...${NC}"
  docker-compose up -d
  
  echo -e "${GREEN}Containers recreated with explicit port bindings. Please try accessing the service again.${NC}"
else
  echo -e "${YELLOW}Skipping container recreation.${NC}"
fi

echo -e "\n${GREEN}Diagnostics complete. See above for details.${NC}"
echo -e "${YELLOW}If you still can't access Nginx Proxy Manager, consider the following:${NC}"
echo -e "1. Ensure your server's firewall allows access to port 81"
echo -e "2. Check if your hosting provider blocks these ports"
echo -e "3. Try accessing from a different network to rule out local network issues"
echo -e "4. Check for any Docker network conflicts or overlapping port bindings"
