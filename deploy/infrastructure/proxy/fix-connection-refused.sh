#!/bin/bash
# fix-connection-refused.sh
# Fix connection refused issues with Nginx Proxy Manager

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Fixing Nginx Proxy Manager connection refused issue...${NC}"

# Step 1: Check if the container is running
NPM_CONTAINER=$(docker ps -a | grep nginx-proxy-manager)
if [[ -z "$NPM_CONTAINER" ]]; then
  echo -e "${RED}Nginx Proxy Manager container not found!${NC}"
  echo -e "${YELLOW}Starting all services...${NC}"
  
  # Start services from scratch
  cd /opt/PharmacyHub/infrastructure/proxy
  docker-compose up -d
  
  sleep 10
fi

# Step 2: Verify container status
echo -e "\n${YELLOW}Container status:${NC}"
docker ps -a | grep nginx-proxy-manager

# Check if container is running
CONTAINER_STATUS=$(docker inspect --format='{{.State.Status}}' nginx-proxy-manager 2>/dev/null || echo "not found")
if [[ "$CONTAINER_STATUS" != "running" ]]; then
  echo -e "${RED}Container is not running! Status: $CONTAINER_STATUS${NC}"
  echo -e "${YELLOW}Attempting to start container...${NC}"
  
  docker start nginx-proxy-manager
  sleep 5
  
  # Check again
  CONTAINER_STATUS=$(docker inspect --format='{{.State.Status}}' nginx-proxy-manager 2>/dev/null || echo "not found")
  if [[ "$CONTAINER_STATUS" != "running" ]]; then
    echo -e "${RED}Failed to start container!${NC}"
    exit 1
  fi
fi

# Step 3: Check for port binding issues
echo -e "\n${YELLOW}Port bindings:${NC}"
docker port nginx-proxy-manager

# Check if port 81 is properly bound
PORT_BINDING=$(docker port nginx-proxy-manager | grep '81/tcp')
if [[ -z "$PORT_BINDING" ]]; then
  echo -e "${RED}Port 81 is not properly bound!${NC}"
  echo -e "${YELLOW}Stopping container to fix port binding...${NC}"
  
  # Stop container
  docker stop nginx-proxy-manager
  
  # Modify docker-compose.yml to ensure explicit port binding
  echo -e "${YELLOW}Ensuring explicit port binding in docker-compose.yml...${NC}"
  cd /opt/PharmacyHub/infrastructure/proxy
  sed -i 's/- \'81:81\'/- \'0.0.0.0:81:81\'/g' docker-compose.yml
  
  # Start services again
  echo -e "${YELLOW}Starting services with fixed port binding...${NC}"
  docker-compose up -d
  
  sleep 10
fi

# Step 4: Check if nginx is running inside the container
echo -e "\n${YELLOW}Checking if nginx is running inside the container...${NC}"
NGINX_RUNNING=$(docker exec -i nginx-proxy-manager ps aux | grep nginx | grep -v grep || echo "not running")
if [[ "$NGINX_RUNNING" == "not running" ]]; then
  echo -e "${RED}Nginx is not running inside the container!${NC}"
  echo -e "${YELLOW}Attempting to start nginx...${NC}"
  
  # Start nginx
  docker exec -i nginx-proxy-manager nginx || echo -e "${RED}Failed to start nginx${NC}"
  
  sleep 5
fi

# Step 5: Check if the nginx configs are valid
echo -e "\n${YELLOW}Checking nginx configuration...${NC}"
docker exec -i nginx-proxy-manager nginx -t || echo -e "${RED}Invalid nginx configuration!${NC}"

# Step 6: Test network connectivity from inside the container
echo -e "\n${YELLOW}Testing network connectivity from inside the container...${NC}"
docker exec -i nginx-proxy-manager curl -I http://localhost:81 || echo -e "${RED}Failed to connect to port 81 inside the container${NC}"

# Step 7: Check host machine's listening ports
echo -e "\n${YELLOW}Checking host machine's listening ports...${NC}"
netstat -tuln | grep -E '81' || echo -e "${RED}Port 81 is not being listened on by any process!${NC}"

# Step 8: Check firewall settings
echo -e "\n${YELLOW}Checking firewall settings...${NC}"
if command -v ufw &> /dev/null; then
  ufw status | grep -E '81|80|443' || echo -e "${YELLOW}No firewall rules found for ports 80, 81, 443${NC}"
  
  # Ensure ports are allowed
  echo -e "${YELLOW}Ensuring ports are allowed through firewall...${NC}"
  ufw allow 80/tcp || true
  ufw allow 81/tcp || true
  ufw allow 443/tcp || true
elif command -v firewall-cmd &> /dev/null; then
  firewall-cmd --list-ports | grep -E '81|80|443' || echo -e "${YELLOW}No firewall rules found for ports 80, 81, 443${NC}"
  
  # Ensure ports are allowed
  echo -e "${YELLOW}Ensuring ports are allowed through firewall...${NC}"
  firewall-cmd --zone=public --add-port=80/tcp --permanent || true
  firewall-cmd --zone=public --add-port=81/tcp --permanent || true
  firewall-cmd --zone=public --add-port=443/tcp --permanent || true
  firewall-cmd --reload || true
else
  echo "No common firewall found (ufw/firewalld)"
fi

# Step 9: Create a simple nginx configuration to ensure it's working
echo -e "\n${YELLOW}Creating a simple nginx configuration to ensure it's working...${NC}"
docker exec -i nginx-proxy-manager bash -c "echo 'server { listen 81; location / { return 200 \"Nginx is working!\"; } }' > /etc/nginx/conf.d/test.conf"
docker exec -i nginx-proxy-manager nginx -t || echo -e "${RED}Invalid test configuration!${NC}"
docker exec -i nginx-proxy-manager nginx -s reload || echo -e "${RED}Failed to reload nginx with test configuration!${NC}"

echo -e "\n${YELLOW}Checking logs for nginx-proxy-manager...${NC}"
docker logs --tail 50 nginx-proxy-manager

echo -e "\n${GREEN}Fix attempts completed!${NC}"
echo -e "${YELLOW}Try accessing Nginx Proxy Manager at http://your-server-ip:81 again.${NC}"
echo -e "${YELLOW}If you still can't access it, consider the following:${NC}"
echo -e "1. Check if your hosting provider is blocking port 81"
echo -e "2. Try accessing from a different network to rule out local network issues"
echo -e "3. For a quick test, try creating a simple container to verify port binding: ${NC}"
echo -e "   docker run -d -p 8080:80 nginx && curl http://your-server-ip:8080"
echo -e "4. Try recreating everything from scratch:${NC}"
echo -e "   cd /opt/PharmacyHub/infrastructure/proxy && docker-compose down -v && docker-compose up -d"
