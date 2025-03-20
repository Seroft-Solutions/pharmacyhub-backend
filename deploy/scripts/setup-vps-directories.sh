#!/bin/bash
# setup-vps-directories.sh
# Creates the necessary directory structure on the VPS for all environments

# Exit on any error
set -e

# Base directory for all PharmacyHub deployments
CRM_BASE="/opt/PharmacyHub"

# Environment names
ENVIRONMENTS=("dev" "qa" "prod")

# Create base directory if it doesn't exist
mkdir -p $CRM_BASE
echo "Created base directory: $CRM_BASE"

# Loop through environments and create required directories
for ENV in "${ENVIRONMENTS[@]}"; do
  # Main environment directory
  ENV_DIR="$CRM_BASE/$ENV"
  mkdir -p $ENV_DIR
  echo "Created environment directory: $ENV_DIR"
  
  # Backend directories
  mkdir -p $ENV_DIR/backend
  mkdir -p $ENV_DIR/backend/logs
  
  # Frontend directory
  mkdir -p $ENV_DIR/frontend
  mkdir -p $ENV_DIR/frontend/logs
  
  # Data directories with appropriate namespacing
  mkdir -p $ENV_DIR/data/postgres
  mkdir -p $ENV_DIR/data/redis
  # Keycloak directory removed as it's not needed for PharmacyHub
  mkdir -p $ENV_DIR/data/backups/postgres
  
  # Set proper permissions
  # Add NOPASSWD permission for postgres directory operations if needed
  if ! sudo -n -l | grep -q "NOPASSWD: /usr/bin/chown"; then
    echo "Setting up NOPASSWD permissions for postgres directory management..."
    echo "ubuntu ALL=(ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/*/data/postgres" | sudo tee /etc/sudoers.d/pharmacyhub-postgres
    echo "ubuntu ALL=(ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/*/data/postgres" | sudo tee -a /etc/sudoers.d/pharmacyhub-postgres
    sudo chmod 440 /etc/sudoers.d/pharmacyhub-postgres
  fi
  
  # PostgreSQL data - postgres user (UID 999)
  sudo -n chown -R 999:999 $ENV_DIR/data/postgres || sudo chown -R 999:999 $ENV_DIR/data/postgres || true
  sudo -n chmod 750 $ENV_DIR/data/postgres || sudo chmod 750 $ENV_DIR/data/postgres || true
  
  # Other directories - regular user
  chown -R ubuntu:ubuntu $ENV_DIR/data/redis || true
  # Keycloak directory ownership removed as it's not needed for PharmacyHub
  chown -R ubuntu:ubuntu $ENV_DIR/data/backups || true
  chown -R ubuntu:ubuntu $ENV_DIR/backend/logs || true
  chown -R ubuntu:ubuntu $ENV_DIR/frontend/logs || true
  
  echo "Created and configured data directories for $ENV environment"
done

# Create nginx directory for Nginx Proxy Manager logs if needed
mkdir -p $CRM_BASE/nginx/logs
chown -R ubuntu:ubuntu $CRM_BASE/nginx/logs || true

echo "PharmacyHub VPS directory setup completed successfully!"
