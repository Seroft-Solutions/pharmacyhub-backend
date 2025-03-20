#!/bin/bash
# Script to generate and set up Nginx Proxy Manager configurations

# Ensure proxy configuration directories exist
mkdir -p /opt/PharmacyHub/infrastructure/proxy/data/nginx/proxy_host
mkdir -p /opt/PharmacyHub/infrastructure/proxy/data/letsencrypt-acme-challenge
mkdir -p /opt/PharmacyHub/infrastructure/proxy/data/nginx/custom

# Create empty custom server_proxy.conf if it doesn't exist
if [ ! -f "/opt/PharmacyHub/infrastructure/proxy/data/nginx/custom/server_proxy.conf" ]; then
  touch "/opt/PharmacyHub/infrastructure/proxy/data/nginx/custom/server_proxy.conf"
fi

# Define environments and their corresponding configs
ENVS=("dev" "qa" "prod")

# Frontend configurations
for ENV in "${ENVS[@]}"; do
  # Set variables based on environment
  if [ "$ENV" == "prod" ]; then
    DOMAIN="www.pharmacyhub.pk"
    PORT="3000"
    CONTAINER="pharmacyhub-frontend-prod"
    CONFIG_ID="9"
  elif [ "$ENV" == "qa" ]; then
    DOMAIN="qa.pharmacyhub.pk"
    PORT="3000"
    CONTAINER="pharmacyhub-frontend-qa"
    CONFIG_ID="5"
  else
    DOMAIN="dev.pharmacyhub.pk"
    PORT="3000"
    CONTAINER="pharmacyhub-frontend-dev"
    CONFIG_ID="1"
  fi
  
  # Create frontend proxy config
  cat > "/opt/PharmacyHub/infrastructure/proxy/data/nginx/proxy_host/${CONFIG_ID}.conf" << EOF
# ------------------------------------------------------------
# ${DOMAIN}
# ------------------------------------------------------------

map \$scheme \$hsts_header {
    https   "max-age=63072000; preload";
}

server {
  set \$forward_scheme http;
  set \$server         "${CONTAINER}";
  set \$port           ${PORT};

  listen 80;
  listen [::]:80;

  server_name ${DOMAIN};

  access_log /data/logs/proxy-host-${CONFIG_ID}_access.log proxy;
  error_log /data/logs/proxy-host-${CONFIG_ID}_error.log warn;

  location / {
    # Proxy!
    include conf.d/include/proxy.conf;
  }

  # Custom
  include /data/nginx/custom/server_proxy[.]conf;
}
EOF

  echo "Created frontend proxy config for ${DOMAIN}"
done

# Backend configurations
for ENV in "${ENVS[@]}"; do
  # Set variables based on environment
  if [ "$ENV" == "prod" ]; then
    DOMAIN="api.pharmacyhub.pk"
    PORT="8080"
    CONTAINER="pharmacyhub-backend-prod"
    CONFIG_ID="10"
  elif [ "$ENV" == "qa" ]; then
    DOMAIN="api.qa.pharmacyhub.pk"
    PORT="8080"
    CONTAINER="pharmacyhub-backend-qa"
    CONFIG_ID="4"
  else
    DOMAIN="api.dev.pharmacyhub.pk"
    PORT="8081"
    CONTAINER="pharmacyhub-backend-dev"
    CONFIG_ID="3"
  fi
  
  # Create backend proxy config
  cat > "/opt/PharmacyHub/infrastructure/proxy/data/nginx/proxy_host/${CONFIG_ID}.conf" << EOF
# ------------------------------------------------------------
# ${DOMAIN}
# ------------------------------------------------------------

map \$scheme \$hsts_header {
    https   "max-age=63072000; preload";
}

server {
  set \$forward_scheme http;
  set \$server         "${CONTAINER}";
  set \$port           ${PORT};

  listen 80;
  listen [::]:80;

  server_name ${DOMAIN};

  access_log /data/logs/proxy-host-${CONFIG_ID}_access.log proxy;
  error_log /data/logs/proxy-host-${CONFIG_ID}_error.log warn;

  location / {
    # Proxy!
    include conf.d/include/proxy.conf;
  }

  # Custom
  include /data/nginx/custom/server_proxy[.]conf;
}
EOF

  echo "Created backend proxy config for ${DOMAIN}"
done

# Need to restart Nginx Proxy Manager to apply changes
echo "Restarting Nginx Proxy Manager to apply configurations..."
docker restart nginx-proxy-manager || true

echo "Proxy configurations setup complete!"
