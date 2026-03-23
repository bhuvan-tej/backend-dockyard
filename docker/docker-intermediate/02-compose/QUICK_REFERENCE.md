## 📋 Docker Compose Commands Reference

```powershell
# Start all services in the background
docker compose up -d
 
# Start and rebuild images even if nothing changed
# Use this when you edit Main.java and want to see the changes
docker compose up -d --build
 
# Stop all containers but keep volumes and networks
docker compose down
 
# Stop all containers and delete volumes (wipes all data)
docker compose down -v
 
# Show status of all services
docker compose ps
 
# Follow logs from all services
docker compose logs -f
 
# Follow logs from one service
docker compose logs -f servicename
 
# Run a command inside a running service container
docker compose exec servicename sh
 
# Restart one service without restarting others
docker compose restart servicename
 
# Pull latest versions of all images
docker compose pull
 
# Build or rebuild images without starting containers
docker compose build
```