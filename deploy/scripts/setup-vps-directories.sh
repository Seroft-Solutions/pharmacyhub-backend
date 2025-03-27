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
  
  # Set permissions directly without using sudoers
  # Use PostgreSQL's default UID (999) and GID (999)
  echo -e "${YELLOW}Setting PostgreSQL directory permissions...${NC}"
  # Skip if running in CI environment
  if [ "$CI" != "true" ]; then
    # Since we're running as root, we can set permissions directly
    chown -R 999:999 $ENV_DIR/data/postgres
    chmod 750 $ENV_DIR/data/postgres
    echo -e "${GREEN}Set Postgres directory permissions${NC}"
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
