#!/bin/bash
# Configure server firewall for PharmacyHub infrastructure
# This script sets up ufw or firewalld to allow required ports

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Configuring firewall for PharmacyHub infrastructure...${NC}"

# Function to detect and configure firewall
configure_firewall() {
  # Check for UFW
  if command -v ufw >/dev/null 2>&1; then
    echo -e "${YELLOW}UFW detected, configuring...${NC}"
    
    # Check if UFW is active
    if ! ufw status | grep -q "Status: active"; then
      echo -e "${YELLOW}UFW is not active. Enabling...${NC}"
      # Enable UFW, allow SSH first to prevent lockout
      ufw allow ssh
      ufw enable
    fi
    
    # Configure required ports
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow 81/tcp
    ufw allow 9000/tcp
    
    echo -e "${GREEN}UFW configured successfully${NC}"
    echo -e "${YELLOW}Firewall status:${NC}"
    ufw status
    return 0
  fi
  
  # Check for firewalld
  if command -v firewall-cmd >/dev/null 2>&1; then
    echo -e "${YELLOW}firewalld detected, configuring...${NC}"
    
    # Check if firewalld is running
    if ! systemctl is-active --quiet firewalld; then
      echo -e "${YELLOW}firewalld is not running. Starting...${NC}"
      systemctl start firewalld
      systemctl enable firewalld
    fi
    
    # Configure required ports
    firewall-cmd --zone=public --add-port=80/tcp --permanent
    firewall-cmd --zone=public --add-port=443/tcp --permanent
    firewall-cmd --zone=public --add-port=81/tcp --permanent
    firewall-cmd --zone=public --add-port=9000/tcp --permanent
    
    # Reload firewall
    firewall-cmd --reload
    
    echo -e "${GREEN}firewalld configured successfully${NC}"
    echo -e "${YELLOW}Firewall status:${NC}"
    firewall-cmd --list-all
    return 0
  fi
  
  # Check for iptables
  if command -v iptables >/dev/null 2>&1; then
    echo -e "${YELLOW}Only iptables found. Basic configuration...${NC}"
    
    # Accept SSH (port 22)
    iptables -A INPUT -p tcp --dport 22 -j ACCEPT
    
    # Accept HTTP (port 80)
    iptables -A INPUT -p tcp --dport 80 -j ACCEPT
    
    # Accept HTTPS (port 443)
    iptables -A INPUT -p tcp --dport 443 -j ACCEPT
    
    # Accept Nginx Proxy Manager admin (port 81)
    iptables -A INPUT -p tcp --dport 81 -j ACCEPT
    
    # Accept Portainer (port 9000)
    iptables -A INPUT -p tcp --dport 9000 -j ACCEPT
    
    echo -e "${GREEN}iptables configured${NC}"
    echo -e "${YELLOW}Consider installing a firewall manager like ufw for easier management${NC}"
    return 0
  fi
  
  echo -e "${RED}No supported firewall (ufw, firewalld, iptables) found${NC}"
  return 1
}

# Run firewall configuration
if configure_firewall; then
  echo -e "${GREEN}Firewall configuration completed successfully${NC}"
  echo -e "${GREEN}The following ports are now open:${NC}"
  echo -e "  - 80/tcp (HTTP)"
  echo -e "  - 443/tcp (HTTPS)"
  echo -e "  - 81/tcp (Nginx Proxy Manager Admin)"
  echo -e "  - 9000/tcp (Portainer)"
else
  echo -e "${RED}Firewall configuration failed${NC}"
  echo -e "${YELLOW}Please manually configure your firewall to allow the following ports:${NC}"
  echo -e "  - 80/tcp (HTTP)"
  echo -e "  - 443/tcp (HTTPS)"
  echo -e "  - 81/tcp (Nginx Proxy Manager Admin)"
  echo -e "  - 9000/tcp (Portainer)"
  exit 1
fi
