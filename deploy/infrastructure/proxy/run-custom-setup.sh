#!/bin/bash
# Script for fixing Nginx Proxy Manager configuration issues

# Add color for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting advanced Nginx Proxy Manager configuration fix...${NC}"

# Create a function to wait for services to be up
wait_for_service() {
  local service=$1
  local max_attempts=30
  local wait_seconds=5
  local attempts=0
  
  echo -e "${YELLOW}Waiting for $service to be ready...${NC}"
  
  while [ $attempts -lt $max_attempts ]; do
    if [ "$service" == "db" ]; then
      if docker exec npm-db mysqladmin ping -h localhost -u npm -pnpm &> /dev/null; then
        echo -e "${GREEN}Database is ready!${NC}"
        return 0
      fi
    elif [ "$service" == "npm" ]; then
      if docker exec nginx-proxy-manager curl -s http://localhost:81 &> /dev/null; then
        echo -e "${GREEN}Nginx Proxy Manager is ready!${NC}"
        return 0
      fi
    fi
    
    attempts=$((attempts+1))
    echo "Attempt $attempts/$max_attempts, waiting $wait_seconds seconds..."
    sleep $wait_seconds
  done
  
  echo "Service $service did not become ready in time."
  return 1
}

# Stop services to start fresh
echo -e "${YELLOW}Stopping services...${NC}"
docker-compose down

# Make sure database directory exists
mkdir -p ./mysql

# Start database first
echo -e "${YELLOW}Starting database service...${NC}"
docker-compose up -d db

# Wait for database to be ready
wait_for_service "db"

# Initialize database if needed
echo -e "${YELLOW}Checking and initializing database...${NC}"
if ! docker exec npm-db mysql -u npm -pnpm -e "SHOW TABLES FROM npm" | grep -q "hosts"; then
  echo -e "${YELLOW}Initializing database schema...${NC}"
  
  # Create SQL schema from schema.json
  docker cp ./data/schema.json npm-db:/tmp/schema.json
  docker exec npm-db bash -c 'cat > /tmp/create_schema.sql << "EOF"
  CREATE TABLE IF NOT EXISTS hosts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    owner_user_id INT NOT NULL,
    domain_names JSON NOT NULL,
    forward_host VARCHAR(100) NOT NULL,
    forward_port INT NOT NULL,
    access_list_id INT DEFAULT 0,
    certificate_id INT DEFAULT 0,
    ssl_forced BOOLEAN DEFAULT FALSE,
    caching_enabled BOOLEAN DEFAULT FALSE,
    block_exploits BOOLEAN DEFAULT FALSE,
    advanced_config TEXT,
    meta JSON,
    allow_websocket_upgrade BOOLEAN DEFAULT TRUE, 
    http2_support BOOLEAN DEFAULT FALSE,
    forward_scheme VARCHAR(50) DEFAULT "http",
    enabled BOOLEAN DEFAULT TRUE,
    locations JSON
  );
  
  CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    roles JSON NOT NULL
  );
  
  -- Insert default admin user if not exists
  INSERT IGNORE INTO users (id, name, nickname, email, roles)
  VALUES (1, "Administrator", "Admin", "admin@example.com", "[\\"admin\\"]");
EOF'
  
  # Execute SQL
  docker exec npm-db bash -c 'mysql -u npm -pnpm npm < /tmp/create_schema.sql'
  
  # Copy production data
  echo -e "${YELLOW}Importing proxy host configurations...${NC}"
  docker cp ./data/production.json npm-db:/tmp/production.json
  
  # Import hosts from production.json
  docker exec npm-db bash -c 'cat > /tmp/import_hosts.sql << EOF
DELIMITER //
CREATE PROCEDURE import_hosts()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE host_id INT;
    DECLARE domain_names_json JSON;
    DECLARE locations_json JSON;
    DECLARE meta_json JSON;
    DECLARE forward_host_val VARCHAR(100);
    DECLARE forward_port_val INT;
    DECLARE forward_scheme_val VARCHAR(50);
    DECLARE has_record INT;
    
    -- Get count of existing records
    SELECT COUNT(*) INTO has_record FROM hosts;
    
    -- Only import if no hosts exist
    IF has_record = 0 THEN
        -- Import from file
        REPLACE INTO hosts (id, created_on, modified_on, owner_user_id, domain_names, forward_host, forward_port, 
                          access_list_id, certificate_id, ssl_forced, caching_enabled, block_exploits, 
                          advanced_config, meta, allow_websocket_upgrade, http2_support, forward_scheme, enabled, locations)
        SELECT id, created_on, modified_on, owner_user_id, CAST(domain_names AS JSON), forward_host, forward_port, 
               access_list_id, certificate_id, ssl_forced, caching_enabled, block_exploits, 
               advanced_config, CAST(meta AS JSON), allow_websocket_upgrade, http2_support, forward_scheme, enabled, CAST(locations AS JSON)
        FROM (
            SELECT 
                h.id,
                h.created_on,
                h.modified_on,
                h.owner_user_id,
                JSON_ARRAY(h.domain_names) as domain_names,
                h.forward_host,
                h.forward_port,
                h.access_list_id,
                h.certificate_id,
                h.ssl_forced,
                h.caching_enabled,
                h.block_exploits,
                h.advanced_config,
                h.meta,
                h.allow_websocket_upgrade,
                h.http2_support,
                h.forward_scheme,
                h.enabled,
                h.locations
            FROM JSON_TABLE(
                (SELECT CONVERT(content USING utf8) FROM 
                 (SELECT LOAD_FILE("/tmp/production.json") as content) as file),
                "$.hosts[*]" COLUMNS (
                    id INT PATH "$.id",
                    created_on VARCHAR(50) PATH "$.created_on",
                    modified_on VARCHAR(50) PATH "$.modified_on",
                    owner_user_id INT PATH "$.owner_user_id",
                    domain_names JSON PATH "$.domain_names",
                    forward_host VARCHAR(100) PATH "$.forward_host",
                    forward_port INT PATH "$.forward_port",
                    access_list_id INT PATH "$.access_list_id",
                    certificate_id INT PATH "$.certificate_id",
                    ssl_forced BOOLEAN PATH "$.ssl_forced",
                    caching_enabled BOOLEAN PATH "$.caching_enabled",
                    block_exploits BOOLEAN PATH "$.block_exploits",
                    advanced_config TEXT PATH "$.advanced_config",
                    meta JSON PATH "$.meta",
                    allow_websocket_upgrade BOOLEAN PATH "$.allow_websocket_upgrade",
                    http2_support BOOLEAN PATH "$.http2_support",
                    forward_scheme VARCHAR(50) PATH "$.forward_scheme",
                    enabled BOOLEAN PATH "$.enabled",
                    locations JSON PATH "$.locations"
                )
            ) as h
        ) as hosts_data;
        
        SELECT "Hosts imported successfully!" as message;
    ELSE
        SELECT "Hosts already exist, skipping import." as message;
    END IF;
END //
DELIMITER ;

CALL import_hosts();
DROP PROCEDURE IF EXISTS import_hosts;
EOF'
  
  # Execute import
  docker exec npm-db bash -c 'mysql -u npm -pnpm npm < /tmp/import_hosts.sql'
fi

# Start Nginx Proxy Manager
echo -e "${YELLOW}Starting Nginx Proxy Manager...${NC}"
docker-compose up -d npm

# Wait for Nginx Proxy Manager to be ready
wait_for_service "npm"

# Apply configurations
echo -e "${YELLOW}Applying proxy configurations...${NC}"
./setup-proxy-configs.sh

# Start Portainer
echo -e "${YELLOW}Starting Portainer...${NC}"
docker-compose up -d portainer

echo -e "${GREEN}Nginx Proxy Manager configuration fix completed successfully!${NC}"
echo -e "${GREEN}You can now access the admin UI at http://your-server-ip:81${NC}"
echo -e "${GREEN}Default login: admin@example.com / changeme${NC}"
