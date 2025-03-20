#!/bin/bash
# restart-proxy-services.sh
# Restart and verify the health of Nginx Proxy Manager and Portainer

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Restarting and verifying Nginx Proxy Manager and Portainer...${NC}"

# Stop the containers gracefully
echo -e "${YELLOW}Stopping containers...${NC}"
docker-compose stop -t 30 || true

# Start the containers
echo -e "${YELLOW}Starting containers...${NC}"
docker-compose up -d

# Function to check if a container is healthy
check_container_health() {
  local container_name=$1
  local max_attempts=12
  local wait_seconds=10
  local attempt=1
  
  echo -e "${YELLOW}Checking health of ${container_name}...${NC}"
  
  while [ $attempt -le $max_attempts ]; do
    echo -e "Attempt $attempt/$max_attempts..."
    
    # Get container health status
    local health_status=$(docker inspect --format='{{.State.Health.Status}}' ${container_name} 2>/dev/null || echo "container not found")
    
    if [ "$health_status" = "healthy" ]; then
      echo -e "${GREEN}${container_name} is healthy!${NC}"
      return 0
    elif [ "$health_status" = "container not found" ]; then
      echo -e "${RED}${container_name} container not found!${NC}"
      return 1
    else
      echo -e "${YELLOW}${container_name} status: ${health_status}, waiting ${wait_seconds}s...${NC}"
      
      # Show recent health check logs
      echo -e "${YELLOW}Recent health check logs:${NC}"
      docker inspect --format='{{range .State.Health.Log}}{{.Output}}{{end}}' ${container_name} | tail -n 1
      
      sleep $wait_seconds
      attempt=$((attempt + 1))
    fi
  done
  
  echo -e "${RED}${container_name} did not become healthy after $max_attempts attempts.${NC}"
  return 1
}

# Wait a moment before checking health
echo -e "${YELLOW}Waiting 30 seconds for initial startup...${NC}"
sleep 30

# Check status of containers
echo -e "${YELLOW}Current container status:${NC}"
docker ps | grep -E 'nginx-proxy-manager|portainer|npm-db'

# Check the health of each container
check_container_health "nginx-proxy-manager"
npm_status=$?

check_container_health "portainer"
portainer_status=$?

check_container_health "npm-db"
db_status=$?

# Summary
echo -e "\n${YELLOW}Health check summary:${NC}"
[ $npm_status -eq 0 ] && echo -e "${GREEN}✅ Nginx Proxy Manager: Healthy${NC}" || echo -e "${RED}❌ Nginx Proxy Manager: Unhealthy${NC}"
[ $portainer_status -eq 0 ] && echo -e "${GREEN}✅ Portainer: Healthy${NC}" || echo -e "${RED}❌ Portainer: Unhealthy${NC}"
[ $db_status -eq 0 ] && echo -e "${GREEN}✅ Database: Healthy${NC}" || echo -e "${RED}❌ Database: Unhealthy${NC}"

# Show container logs for unhealthy containers
if [ $npm_status -ne 0 ]; then
  echo -e "\n${YELLOW}Nginx Proxy Manager logs:${NC}"
  docker logs --tail 50 nginx-proxy-manager
fi

if [ $portainer_status -ne 0 ]; then
  echo -e "\n${YELLOW}Portainer logs:${NC}"
  docker logs --tail 50 portainer
fi

if [ $db_status -ne 0 ]; then
  echo -e "\n${YELLOW}Database logs:${NC}"
  docker logs --tail 50 npm-db
fi

# Overall status
if [ $npm_status -eq 0 ] && [ $portainer_status -eq 0 ] && [ $db_status -eq 0 ]; then
  echo -e "\n${GREEN}All services are healthy!${NC}"
  echo -e "${GREEN}You can access:${NC}"
  echo -e "${GREEN}- Nginx Proxy Manager: http://your-server-ip:81${NC}"
  echo -e "${GREEN}  Default login: admin@example.com / changeme${NC}"
  echo -e "${GREEN}- Portainer: http://your-server-ip:9000${NC}"
  exit 0
else
  echo -e "\n${RED}Some services are still unhealthy. Please check the logs above for details.${NC}"
  exit 1
fi
