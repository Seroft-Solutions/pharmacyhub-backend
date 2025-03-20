#!/bin/bash
set -e

echo "Creating a properly formatted sudoers file..."
cat > /tmp/pharmacyhub-postgres-sudoers << 'SUDOERS'
root ALL=(ALL) NOPASSWD: /usr/bin/chown -R 999:999 /opt/PharmacyHub/*/data/postgres
root ALL=(ALL) NOPASSWD: /usr/bin/chmod 750 /opt/PharmacyHub/*/data/postgres
SUDOERS

# Validate the file
visudo -cf /tmp/pharmacyhub-postgres-sudoers
if [ $? -eq 0 ]; then
  # Backup the old file if it exists
  if [ -f /etc/sudoers.d/pharmacyhub-postgres ]; then
    cp /etc/sudoers.d/pharmacyhub-postgres /etc/sudoers.d/pharmacyhub-postgres.bak
  fi
  
  # Install the new file
  cp /tmp/pharmacyhub-postgres-sudoers /etc/sudoers.d/pharmacyhub-postgres
  chmod 440 /etc/sudoers.d/pharmacyhub-postgres
  
  echo "Sudoers file fixed successfully"
  
  # Verify file contents
  cat /etc/sudoers.d/pharmacyhub-postgres
else
  echo "Failed to create a valid sudoers file, check syntax"
  exit 1
fi

# Clean up
rm -f /tmp/pharmacyhub-postgres-sudoers
