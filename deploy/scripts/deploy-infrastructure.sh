#!/bin/bash
# deploy-infrastructure.sh
# Deploy the PharmacyHub infrastructure components to VPS

# Exit on any error
set -e

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Define usage function
usage() {
  echo "Usage: $0 <vps-ip> [username] [ssh-key]"
  echo
  echo "Arguments:"
  echo "  vps-ip    - IP address of the target VPS"
  echo "  username  - SSH username (default: ubuntu)"
  echo "  ssh-key   - Path to SSH private key (optional)"
  echo
  echo "Environment Variables:"
  echo "  CI        - Set to 'true' when running in CI environment"
  echo
  echo "Example:"
  echo "  $0 31.58.144.117 ubuntu ~/.ssh/id_rsa"
  echo "  $0 31.58.144.117"
  exit 1
}

# Check if VPS_IP is provided
if [ -z "$1" ]; then
  echo -e "${RED}Error: VPS IP address not provided.${NC}"
  usage
fi

VPS_IP="$1"
VPS_USER="${2:-ubuntu}"
SSH_KEY="${3:-}"

SSH_OPTS=""
if [ -n "$SSH_KEY" ]; then
  SSH_OPTS="-i $SSH_KEY"
fi

# Check if running in CI
if [ "$CI" == "true" ]; then
  echo -e "${YELLOW}Running in CI environment. Using modified deployment process.${NC}"
fi

echo -e "${YELLOW}Deploying infrastructure to VPS at $VPS_IP...${NC}"

# 1. Setup directory structure
echo -e "${YELLOW}Setting up directory structure on VPS...${NC}"
ssh $SSH_OPTS $VPS_USER@$VPS_IP "mkdir -p /tmp/pharmacyhub-deploy"
scp $SSH_OPTS ./scripts/setup-vps-directories.sh $VPS_USER@$VPS_IP:/tmp/pharmacyhub-deploy/

# Make script executable and run it
ssh $SSH_OPTS $VPS_USER@$VPS_IP "chmod +x /tmp/pharmacyhub-deploy/setup-vps-directories.sh && cd /tmp/pharmacyhub-deploy && CI=$CI ./setup-vps-directories.sh"

# 2. Setup Network (this step is necessary for CI/CD workflow)
echo -e "${YELLOW}Setting up Docker networks...${NC}"
cat > ./setup-networks.sh << 'EOF'
#!/bin/bash
# Create necessary Docker networks for PharmacyHub

set -e

# Create proxy-network if it doesn't exist
if ! docker network inspect proxy-network >/dev/null 2>&1; then
  echo "Creating proxy-network..."
  docker network create proxy-network
else
  echo "proxy-network already exists"
fi

# Create environment-specific networks
for ENV in "dev" "qa" "prod"; do
  NETWORK_NAME="pharmacyhub-${ENV}-network"
  
  if ! docker network inspect $NETWORK_NAME >/dev/null 2>&1; then
    echo "Creating $NETWORK_NAME..."
    docker network create $NETWORK_NAME
  else
    echo "$NETWORK_NAME already exists"
  fi
done

echo "Docker networks created successfully!"
EOF

scp $SSH_OPTS ./setup-networks.sh $VPS_USER@$VPS_IP:/tmp/pharmacyhub-deploy/
ssh $SSH_OPTS $VPS_USER@$VPS_IP "chmod +x /tmp/pharmacyhub-deploy/setup-networks.sh && /tmp/pharmacyhub-deploy/setup-networks.sh"

# 3. Setup Nginx Proxy Manager and Portainer
echo -e "${YELLOW}Deploying Nginx Proxy Manager and Portainer...${NC}"

# Create infrastructure directory if it doesn't exist
ssh $SSH_OPTS $VPS_USER@$VPS_IP "mkdir -p /opt/PharmacyHub/infrastructure/proxy"

# Copy docker-compose and setup scripts
scp $SSH_OPTS ./infrastructure/proxy/docker-compose.yml $VPS_USER@$VPS_IP:/opt/PharmacyHub/infrastructure/proxy/
scp $SSH_OPTS ./infrastructure/proxy/run-custom-setup.sh $VPS_USER@$VPS_IP:/opt/PharmacyHub/infrastructure/proxy/
scp $SSH_OPTS ./infrastructure/proxy/setup-proxy-configs.sh $VPS_USER@$VPS_IP:/opt/PharmacyHub/infrastructure/proxy/

# Make scripts executable
ssh $SSH_OPTS $VPS_USER@$VPS_IP "chmod +x /opt/PharmacyHub/infrastructure/proxy/run-custom-setup.sh"
ssh $SSH_OPTS $VPS_USER@$VPS_IP "chmod +x /opt/PharmacyHub/infrastructure/proxy/setup-proxy-configs.sh"

# Run the setup script
ssh $SSH_OPTS $VPS_USER@$VPS_IP "cd /opt/PharmacyHub/infrastructure/proxy && CI=$CI ./run-custom-setup.sh"

echo -e "${GREEN}VPS infrastructure setup completed successfully!${NC}"
echo -e "${GREEN}Nginx Proxy Manager: http://$VPS_IP:81 (admin@example.com / changeme)${NC}"
echo -e "${GREEN}Portainer: http://$VPS_IP:9000${NC}"

# Verify everything is running
echo -e "${YELLOW}Verifying infrastructure...${NC}"
ssh $SSH_OPTS $VPS_USER@$VPS_IP "
  echo 'Checking Docker networks...'
  docker network ls | grep -E 'proxy-network|pharmacyhub'
  
  echo 'Checking directory structure...'
  find /opt/PharmacyHub -type d -maxdepth 3 | sort
  
  echo 'Checking Nginx and Portainer containers...'
  docker ps | grep -E 'nginx-proxy-manager|portainer|npm-db' || echo 'Some containers are not running!'
"

echo -e "${GREEN}Deployment complete!${NC}"
