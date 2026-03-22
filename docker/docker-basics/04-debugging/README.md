# Debugging Containers

## 🎯 Goal
Learn every debugging technique. This is the most practically useful.
Interviewers always ask "how do you debug a broken container?"

---

## 🔧 The Debugging Decision Tree

```
Container has a problem
        │
        ├── Won't start / keeps restarting?
        │     └──  docker logs <name>
        │          docker logs <name> --previous   (last crash)
        │
        ├── Started but behaving wrong?
        │     └──  docker exec -it <name> sh       (shell inside)
        │          docker exec <name> env          (check env vars)
        │
        ├── Port not accessible?
        │     └──  docker inspect <name>           (check PortBindings)
        │          docker ps                       (check port column)
        │
        ├── Using too much memory/CPU?
        │     └──  docker stats                    (live resource use)
        │
        └── Crashed with exit code?
              └──  docker inspect -f '{{.State}}' <name>
                   Exit 0   = normal
                   Exit 1   = application error
                   Exit 137 = OOMKilled (out of memory)
                   Exit 143 = SIGTERM (graceful stop)
```

---

## ✅ Exercises — Debug Real Scenarios

### Exercise 1 — Reading Logs (Most Important Skill)

Logs are always the first place to look when something goes wrong.

```powershell
# Start an nginx container running in the background
# -d means detached, runs in background without blocking your terminal
# -p 8080:80 maps your Windows port 8080 to container port 80
# --name gives it a readable name so you don't have to use the container ID
docker run -d -p 8080:80 --name log-demo nginx
 
# Open the browser so nginx generates some log entries
Start-Process "http://localhost:8080"
 
# Wait 2 seconds for the page to load and logs to appear
Start-Sleep -Seconds 2
 
# Print all logs the container has produced since it started
docker logs log-demo

# Follow logs live — new lines appear as they happen
# Press Ctrl+C to stop following, the container keeps running
docker logs -f log-demo

# Show only the last 20 lines instead of everything
docker logs --tail 20 log-demo

# Show logs with a timestamp on each line
# Useful when you need to match a log line to a specific time
docker logs -t log-demo

# Show only logs from the last 2 minutes
docker logs --since 2m log-demo

# IMPORTANT — logs still work after the container is stopped
# This is critical in production when a container crashes
docker stop log-demo
docker logs log-demo

# Clean up
docker rm log-demo
```

---

## Exercise 2 — Shell Inside a Running Container

When logs are not enough you need to get inside the container
and look around — check files, check processes, check env variables.

```powershell
# Start a container in the background to explore
# nginx is just used here as a convenient long-running container
docker run -d --name shell-demo nginx

# Open an interactive shell inside the running container
# -it means interactive terminal — without this the shell closes immediately
# bash is the shell we want to use
docker exec -it shell-demo bash

# You are now INSIDE the Linux container on your Windows machine
# Try these commands to understand what is inside

# See which Linux distribution this container is running
cat /etc/os-release

# List all files in the nginx html folder
ls /usr/share/nginx/html
 
# See all environment variables set inside the container
env
 
# See all processes running inside the container
ps aux
 
# Leave the container and return to your Windows terminal
exit

# Run a single command inside the container without opening a full shell
# Useful when you just want one piece of information
docker exec shell-demo env
 
# List files in a specific folder without entering the container
docker exec shell-demo ls /usr/share/nginx/html
 
# Clean up
docker rm -f shell-demo
```

---

## Exercise 3 — Inspecting Container Metadata

Docker inspect gives you the full details of a container —
network config, port mappings, env variables, exit codes and more.

```powershell
# Start a container to inspect
# -p 9090:80 maps your Windows port 9090 to container port 80
docker run -d --name inspect-demo -p 9090:80 nginx

# Print the full metadata of the container as JSON
# This is a lot of output — useful to scroll through once
docker inspect inspect-demo

# Extract just the internal IP address Docker assigned to the container
docker inspect -f "{{.NetworkSettings.IPAddress}}" inspect-demo
 
# Extract port mapping information
# Shows which container ports are mapped to which host ports
docker inspect -f "{{.NetworkSettings.Ports}}" inspect-demo
 
# Extract all environment variables set inside the container
docker inspect -f "{{.Config.Env}}" inspect-demo

# Check how many times the container has restarted
# A non-zero number means the container has been crashing
docker inspect -f "{{.RestartCount}}" inspect-demo
 
# Check the exit code from when the container last stopped
# 0 means clean exit, anything else means a problem
docker inspect -f "{{.State.ExitCode}}" inspect-demo
 
# Check if the container was killed because it ran out of memory
# true means the container exceeded its memory limit
docker inspect -f "{{.State.OOMKilled}}" inspect-demo
 
# Clean up
docker rm -f inspect-demo
```
 
---

## Exercise 4 — Live Resource Monitoring

```powershell
# Start two containers to monitor
docker run -d --name app1 nginx
docker run -d --name app2 nginx
 
# Show a live stream of resource usage for all running containers
# Columns: CPU%, memory used vs limit, network in/out, disk in/out
# Press Ctrl+C to stop the live stream
docker stats

# Show a single snapshot instead of a live stream
# Useful when you just want to check current usage once
docker stats --no-stream
 
# Monitor only specific containers by name
docker stats app1 app2 --no-stream
 
# Clean up
docker rm -f app1 app2
```
 
---

## Exercise 5 — Simulate and Diagnose a Broken Container

```powershell
# Simulate a container that crashes immediately with an error
# sh -c runs a shell command string inside the container
# exit 1 means the process ended with an error code
docker run -d --name broken alpine sh -c "echo 'something went wrong' && exit 1"

# Wait 2 seconds for it to finish
Start-Sleep -Seconds 2
 
# Check running containers — broken will NOT appear here because it already stopped
docker ps
 
# Check all containers including stopped ones
# You will see broken with status Exited (1)
docker ps -a
 
# Read the logs to see what the container printed before it crashed
docker logs broken
 
# Check the exit code
# 1 means the application exited with an error
docker inspect -f "{{.State.ExitCode}}" broken
 
# Clean up
docker rm broken

# Simulate a container that runs out of memory and gets killed
# --memory="15m" limits the container to only 15 megabytes of RAM
# The command tries to use 50 megabytes which exceeds the limit
docker run -d --name oom-demo --memory="15m" alpine `
  sh -c "cat /dev/zero | head -c 50m > /dev/null"
 
# Wait for it to get killed
Start-Sleep -Seconds 3
 
# Check if it was killed by the OS due to running out of memory
# true means it was OOMKilled (Out Of Memory Killed)
docker inspect -f "{{.State.OOMKilled}}" oom-demo
 
# The exit code for OOMKilled is always 137
docker inspect -f "{{.State.ExitCode}}" oom-demo
 
# Clean up
docker rm oom-demo
```

---

## Exercise 6 — Copying Files Between Container and Windows

```
# Start a container to copy files from
docker run -d --name copy-demo nginx

# Copy a file FROM the container TO your Windows Desktop
# Format: docker cp <container-name>:<path-inside-container> <path-on-windows>
docker cp copy-demo:/etc/nginx/nginx.conf "$env:USERPROFILE\Desktop\nginx.conf"

# The nginx config file is now on your Desktop — open it in IntelliJ

# Copy a file FROM your Windows machine TO the container
# Create a test HTML file first
"<h1>Copied from Windows</h1>" | Set-Content "$env:TEMP\test.html"

# Format: docker cp <path-on-windows> <container-name>:<path-inside-container>
docker cp "$env:TEMP\test.html" copy-demo:/usr/share/nginx/html/test.html

# Verify the file made it into the container
docker exec copy-demo ls /usr/share/nginx/html

# Clean up
docker rm -f copy-demo
```

---

## All Debugging Commands Reference

```powershell
# Logs
docker logs NAME                    all logs since container started
docker logs -f NAME                 follow live, Ctrl+C to stop
docker logs --tail 50 NAME          last 50 lines only
docker logs -t NAME                 include timestamps
docker logs --since 5m NAME         logs from last 5 minutes
 
# Shell and exec
docker exec -it NAME bash           interactive bash shell inside container
docker exec -it NAME sh             use sh for Alpine images, no bash there
docker exec NAME env                print all env variables
docker exec NAME ps aux             show processes running inside
 
# Inspect
docker inspect NAME                                       full JSON metadata
docker inspect -f "{{.State.ExitCode}}" NAME              exit code
docker inspect -f "{{.State.OOMKilled}}" NAME             was it killed by OOM
docker inspect -f "{{.RestartCount}}" NAME                how many times restarted
docker inspect -f "{{.NetworkSettings.IPAddress}}" NAME   internal IP
 
# Resources
docker stats                        live stream all containers
docker stats --no-stream            one snapshot
 
# Copy files
docker cp NAME:/container/path .\local-path         container to Windows
docker cp .\local-file NAME:/container/path         Windows to container
 
# Force remove a running container without stopping first
docker rm -f NAME
```
 
---

## 📝 Interview Questions

**Q: How do you debug a container that keeps restarting?**
> Run docker logs `docker logs <name>` to read what the container printed before crashing.
Then run docker inspect `docker inspect` to check the exit code.
Exit code 137 means the container ran out of memory so you increase the memory limit. 
Exit code 1 means the application threw an error so you read the stack trace in the logs.

**Q: How do you open a shell inside a running container?**
> `docker exec -it <name> bash` — or `sh` for Alpine-based images that don't have bash.

**Q: What does exit code 137 mean?**
> The container was OOMKilled — it exceeded its memory limit and the OS forcibly killed it. 
Fix by increasing the memory limit or reducing the application memory usage.

**Q: How do you copy a file out of a container?**
> `docker cp <name>:/path/in/container .\local-destination`
This works on both running and stopped containers.
 
---