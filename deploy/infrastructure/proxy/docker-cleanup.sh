#!/bin/bash

# Script to clean up Docker resources - USE WITH CAUTION
# This will remove ALL containers, volumes, images, and networks

# Stop all running containers
echo "Stopping all running containers..."
docker stop $(docker ps -aq)

# Remove all containers
echo "Removing all containers..."
docker rm $(docker ps -aq)

# Remove all volumes
echo "Removing all Docker volumes..."
docker volume rm $(docker volume ls -q)

# Remove all images
echo "Removing all Docker images..."
docker rmi $(docker images -aq)

# Remove all networks
echo "Removing all Docker networks..."
docker network rm $(docker network ls -q)

# Optionally remove all dangling objects
echo "Removing all dangling Docker objects..."
docker system prune -a -f --volumes

echo "Docker cleanup complete."
