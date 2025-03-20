#!/bin/bash
# setup-vps-directories.sh
# Creates the necessary directory structure on the VPS for all environments

# Exit on any error
set -e

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Base directory for all PharmacyHub deployments
CRM_BASE="/opt/PharmacyHub"

# Environment names
ENVIRONMENTS=("dev" "qa" "prod")

# Create base directory if it doesn't exist
mkdir -p $CRM_BASE
echo -e "${GREEN}Created base directory: $CRM_BASE${NC}"

# Loop through environments and create required directories
for ENV in "${ENVIRONMENTS[@]}"; do
  # Main environment directory
  ENV_DIR="$CRM_BASE/$ENV"
  mkdir -p $ENV_DIR
  echo -e "${GREEN}Created environment directory: $ENV_DIR${NC}"
  
  # Backend directories
  mkdir -p $ENV_DIR/backend
  mkdir -p $ENV_DIR/backend/logs
  
  # Frontend directory
  mkdir -p $ENV_DIR/frontend
  mkdir -p $ENV_DIR/frontend/logs
  
  # Data directories with appropriate namespacing
  mkdir -p $ENV_DIR/data/postgres
  mkdir -p $ENV_DIR/data/redis
  mkdir -p $ENV_DIR/data/keycloak
  mkdir -p $ENV_DIR/data/backups/postgres
  
  # Set proper permissions
  # Add NOPASSWD permission for postgres directory operations if needed
  if [ "$CI" != "true" ] && ! sudo -n -l | grep -q "NOPASSWD: /usr/bin/chown"; then
    echo -e "${YELLOW}Setting up NOPASSWD permissions for postgres directory management...${NC}"
    
    # Fix: Create a temporary file with proper sudoers syntax
    cat > /tmp/pharmacyhub-postgres-sudoers << 'EOF'
ubuntu ALL=(ALL) NOPASSWD:/usr/bin/chown -R 999:999 /opt/PharmacyHub/*/data/postgres
ubuntu ALL=(ALL) NOPASSWD:/usr/bin/chmod 750 /opt/PharmacyHub/*/data/postgres
EOF
    
    # Use visudo to safely install and validate the sudoers file
    # Only proceed if not in CI mode
    sudo visudo -cf /tmp/pharmacyhub-postgres-sudoers
    if [ $? -eq 0 ]; then
      sudo cp /tmp/pharmacyhub-postgres-sudoers /etc/sudoers.d/pharmacyhub-postgres
      sudo chmod 440 /etc/sudoers.d/pharmacyhub-postgres
      echo -e "${GREEN}Successfully created sudoers file${NC}"
    else
      echo -e "${RED}Failed to create valid sudoers file. Check syntax.${NC}"
      exit 1
    fi
    
    # Remove temporary file
    rm -f /tmp/pharmacyhub-postgres-sudoers
  else
    echo -e "${YELLOW}Skipping sudoers setup (CI mode or already configured)${NC}"
  fi
  
  # PostgreSQL data - postgres user (UID 999)
  # Skip if running in CI environment
  if [ "$CI" != "true" ]; then
    echo -e "${YELLOW}Setting PostgreSQL directory permissions...${NC}"
    # Try without sudo first - might work if permissions are already correct
    chown -R 999:999 "$ENV_DIR/data/postgres" 2>/dev/null || \
    # Try non-interactive sudo
    sudo -n chown -R 999:999 "$ENV_DIR/data/postgres" 2>/dev/null || \
    # Try regular sudo as last resort
    sudo chown -R 999:999 "$ENV_DIR/data/postgres" 2>/dev/null || \
    echo "Warning: Could not set Postgres directory ownership. You may need to set it manually."
    
    # Similar approach for chmod
    chmod 750 "$ENV_DIR/data/postgres" 2>/dev/null || \
    sudo -n chmod 750 "$ENV_DIR/data/postgres" 2>/dev/null || \
    sudo chmod 750 "$ENV_DIR/data/postgres" 2>/dev/null || \
    echo "Warning: Could not set Postgres directory permissions. You may need to set them manually."
  else
    echo -e "${YELLOW}Skipping PostgreSQL permissions (CI mode)${NC}"
  fi
  
  # Other directories - regular user
  # Only when not in CI mode
  if [ "$CI" != "true" ]; then
    echo -e "${YELLOW}Setting other directory permissions...${NC}"
    chown -R root:root $ENV_DIR/data/redis 2>/dev/null || true
    chown -R root:root $ENV_DIR/data/keycloak 2>/dev/null || true
    chown -R root:root $ENV_DIR/data/backups 2>/dev/null || true
    chown -R root:root $ENV_DIR/backend/logs 2>/dev/null || true
    chown -R root:root $ENV_DIR/frontend/logs 2>/dev/null || true
  else
    echo -e "${YELLOW}Skipping other permissions (CI mode)${NC}"
  fi
  
  echo -e "${GREEN}Created and configured data directories for $ENV environment${NC}"
done

# Create infrastructure directories if they don't exist
mkdir -p $CRM_BASE/infrastructure/proxy
mkdir -p $CRM_BASE/nginx/logs

# Only apply permissions when not in CI mode
if [ "$CI" != "true" ]; then
  chown -R root:root $CRM_BASE/nginx/logs 2>/dev/null || true
fi

echo -e "${GREEN}PharmacyHub VPS directory setup completed successfully!${NC}"
