#!/bin/bash
# PharmacyHub Infrastructure Management Script

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Display usage information
show_usage() {
  echo -e "${BLUE}PharmacyHub Infrastructure Management${NC}"
  echo -e "Usage: $0 [command]"
  echo -e ""
  echo -e "Commands:"
  echo -e "  ${GREEN}start${NC}    - Start all services"
  echo -e "  ${YELLOW}stop${NC}     - Stop all services"
  echo -e "  ${RED}restart${NC}  - Restart all services"
  echo -e "  ${BLUE}status${NC}   - Show status of all services"
  echo -e "  ${YELLOW}logs${NC}     - Show logs for all services"
  echo -e "  ${RED}reset${NC}    - Reset everything (data will be lost!)"
  echo -e "  ${GREEN}backup${NC}   - Backup configuration"
  echo -e "  ${BLUE}restore${NC}  - Restore from backup"
  echo -e "  ${YELLOW}help${NC}     - Show this help message"
  echo -e ""
  echo -e "Examples:"
  echo -e "  $0 start"
  echo -e "  $0 logs npm"
}

# Check if command is provided
if [ $# -eq 0 ]; then
  show_usage
  exit 1
fi

# Process command
case "$1" in
  start)
    echo -e "${GREEN}Starting all services...${NC}"
    docker-compose up -d
    echo -e "${GREEN}Services started${NC}"
    ;;

  stop)
    echo -e "${YELLOW}Stopping all services...${NC}"
    docker-compose stop
    echo -e "${YELLOW}Services stopped${NC}"
    ;;

  restart)
    echo -e "${YELLOW}Restarting all services...${NC}"
    docker-compose restart
    echo -e "${GREEN}Services restarted${NC}"
    ;;

  status)
    echo -e "${BLUE}Services status:${NC}"
    docker-compose ps
    
    echo -e "\n${BLUE}Container health:${NC}"
    docker inspect --format='{{.Name}} - {{.State.Health.Status}}' $(docker-compose ps -q) 2>/dev/null || echo -e "${YELLOW}Health status not available${NC}"
    ;;

  logs)
    if [ -z "$2" ]; then
      echo -e "${BLUE}Showing logs for all services:${NC}"
      docker-compose logs --tail=100
    else
      echo -e "${BLUE}Showing logs for $2:${NC}"
      docker-compose logs --tail=100 $2
    fi
    ;;

  reset)
    echo -e "${RED}WARNING: This will reset all services and remove all data!${NC}"
    read -p "Are you sure you want to continue? (y/n): " confirm
    if [[ "$confirm" =~ ^[Yy]$ ]]; then
      echo -e "${RED}Stopping all services...${NC}"
      docker-compose down -v
      
      echo -e "${RED}Removing data directories...${NC}"
      rm -rf ./data/* ./mysql/*
      
      echo -e "${YELLOW}Running setup script...${NC}"
      ./setup.sh
    else
      echo -e "${YELLOW}Reset cancelled${NC}"
    fi
    ;;

  backup)
    backup_dir="./backups"
    mkdir -p $backup_dir
    backup_file="$backup_dir/backup_$(date +%Y%m%d_%H%M%S).tar.gz"
    
    echo -e "${GREEN}Creating backup: $backup_file${NC}"
    tar -czf $backup_file ./data ./mysql
    
    echo -e "${GREEN}Backup created: $backup_file${NC}"
    ;;

  restore)
    if [ -z "$2" ]; then
      echo -e "${YELLOW}Available backups:${NC}"
      ls -1 ./backups/*.tar.gz 2>/dev/null || echo -e "${RED}No backups found${NC}"
      echo -e "${YELLOW}Usage: $0 restore <backup_file>${NC}"
    else
      if [ -f "$2" ]; then
        echo -e "${YELLOW}Stopping services...${NC}"
        docker-compose down
        
        echo -e "${YELLOW}Restoring from $2...${NC}"
        tar -xzf $2
        
        echo -e "${GREEN}Backup restored. Starting services...${NC}"
        docker-compose up -d
      else
        echo -e "${RED}Backup file $2 not found${NC}"
      fi
    fi
    ;;

  help|--help|-h)
    show_usage
    ;;

  *)
    echo -e "${RED}Unknown command: $1${NC}"
    show_usage
    exit 1
    ;;
esac
