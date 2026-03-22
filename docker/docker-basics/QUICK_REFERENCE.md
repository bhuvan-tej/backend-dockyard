# 📋 Quick Reference — 🐳 Docker basics

---


## 🧱 Images

---
```
# Build an image from a Dockerfile in the current folder
# -t gives it a name and tag in the format name:tag
docker build -t name:tag .
 
# List all images downloaded or built on your machine
docker images
 
# Show each layer in an image and its size
# Useful for understanding what is taking up space
docker history name:tag
 
# Delete an image from your machine
docker rmi name:tag
```

## 📦 Containers

---
```
# Create and start a container from an image
# -d runs it in the background
# -p 8080:80 maps your Windows port 8080 to container port 80
# --name gives it a readable name
# nginx is the image name
docker run -d -p 8080:80 --name mycontainer nginx
 
# Run a container and automatically remove it when it exits
# Good for one-off tasks where you don't need the container afterwards
docker run --rm nginx
 
# Run a container with an environment variable
# -e sets a key=value pair the app can read with System.getenv()
docker run --rm -e MY_NAME="Alice" hello-docker:day2
 
# Run a container with a named volume
# -v volume-name:/path/inside maps the volume to a folder inside the container
docker run -v my-vol:/app/data myimage
 
# Run a container with a bind mount
# Maps a specific Windows folder into the container
docker run -v "C:\my\folder:/app/data" myimage
 
# List all currently running containers
docker ps
 
# List all containers including stopped ones
docker ps -a
 
# Stop a running container gracefully
# Docker sends SIGTERM and waits 10 seconds before force killing
docker stop mycontainer
 
# Remove a stopped container
docker rm mycontainer
 
# Force remove a running container without stopping first
docker rm -f mycontainer
```

## 🐞 Debugging

---
```
# Print all logs a container has produced
docker logs mycontainer
 
# Follow logs live, new lines appear as they happen
# Press Ctrl+C to stop following without stopping the container
docker logs -f mycontainer
 
# Show only the last 20 lines of logs
docker logs --tail 20 mycontainer
 
# Show logs with timestamps on each line
docker logs -t mycontainer
 
# Open an interactive shell inside a running container
# -it means interactive terminal
# bash is the shell — use sh for Alpine images
docker exec -it mycontainer bash
 
# Run a single command inside a container without opening a shell
docker exec mycontainer env
 
# Print full metadata of a container as JSON
docker inspect mycontainer
 
# Extract just the exit code from the metadata
docker inspect -f "{{.State.ExitCode}}" mycontainer
 
# Check if a container was killed because it ran out of memory
docker inspect -f "{{.State.OOMKilled}}" mycontainer
 
# Show live CPU and memory usage for all running containers
docker stats
 
# Show a single snapshot of resource usage without live streaming
docker stats --no-stream
 
# Copy a file from a container to your Windows machine
docker cp mycontainer:/path/inside "$env:USERPROFILE\Desktop\file.txt"
 
# Copy a file from your Windows machine into a container
docker cp "C:\my\file.txt" mycontainer:/path/inside
```

## 🗃️ Volumes

---
```
# Create a named volume
docker volume create my-vol
 
# List all volumes
docker volume ls
 
# See details of a volume including where it is stored
docker volume inspect my-vol
 
# Delete a volume — warning this deletes all data inside it
docker volume rm my-vol
```

## 🌐 Networks

---
```
# Create a custom network
docker network create my-net
 
# List all networks
docker network ls
 
# See details of a network including which containers are on it
docker network inspect my-net
 
# Delete a network
docker network rm my-net
```

## 🧹 Cleanup

---
```
# Remove all stopped containers, unused networks, dangling images
# Frees up disk space — safe to run anytime
docker system prune
 
# Show how much disk space Docker is using
docker system df
```