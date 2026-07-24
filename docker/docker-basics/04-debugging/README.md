# Debugging Containers

## 🎯 Goal
Learn every debugging technique — on **macOS or Windows**. This is the most
practically useful module. Interviewers always ask "how do you debug a broken
container?"

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

> 🧠 **No memorization:** you don't recall commands — you follow the tree.
> "What's the symptom?" → the branch tells you the command.

---

## ✅ Exercises — Debug Real Scenarios

### Exercise 1 — Reading Logs (Most Important Skill)

Logs are always the first place to look when something goes wrong.
The `docker logs` commands are identical everywhere — only *opening the browser*
and *waiting* differ.

### 🍎 macOS
```bash
docker run -d -p 8080:80 --name log-demo nginx
open "http://localhost:8080"      # generate some log entries
sleep 2
```
### 🪟 Windows
```powershell
docker run -d -p 8080:80 --name log-demo nginx
Start-Process "http://localhost:8080"
Start-Sleep -Seconds 2
```

Then (same on both platforms):
```bash
docker logs log-demo             # all logs since it started
docker logs -f log-demo          # follow live — Ctrl+C to stop
docker logs --tail 20 log-demo   # last 20 lines only
docker logs -t log-demo          # include timestamps
docker logs --since 2m log-demo  # only the last 2 minutes

# IMPORTANT — logs still work AFTER the container is stopped (critical in prod)
docker stop log-demo
docker logs log-demo

docker rm log-demo               # clean up
```

---

## Exercise 2 — Shell Inside a Running Container

When logs aren't enough, get inside and look around.
*(All commands below are identical on macOS and Windows.)*

```bash
docker run -d --name shell-demo nginx

# -it = interactive terminal (without it, the shell exits immediately)
docker exec -it shell-demo bash

# You're now INSIDE the Linux container:
cat /etc/os-release          # which Linux distro
ls /usr/share/nginx/html     # nginx html folder
env                          # all env variables
ps aux                       # all processes
exit                         # back to your host terminal

# Run a single command without opening a full shell:
docker exec shell-demo env
docker exec shell-demo ls /usr/share/nginx/html

docker rm -f shell-demo      # clean up
```

---

## Exercise 3 — Inspecting Container Metadata

`docker inspect` gives full details — network, ports, env, exit codes.
*(Identical on both platforms.)*

```bash
docker run -d --name inspect-demo -p 9090:80 nginx

docker inspect inspect-demo                                    # full JSON

docker inspect -f "{{.NetworkSettings.IPAddress}}" inspect-demo   # internal IP
docker inspect -f "{{.NetworkSettings.Ports}}"     inspect-demo   # port mappings
docker inspect -f "{{.Config.Env}}"                inspect-demo   # env variables
docker inspect -f "{{.RestartCount}}"              inspect-demo   # restart count
docker inspect -f "{{.State.ExitCode}}"            inspect-demo   # last exit code
docker inspect -f "{{.State.OOMKilled}}"           inspect-demo   # killed by OOM?

docker rm -f inspect-demo    # clean up
```

---

## Exercise 4 — Live Resource Monitoring

*(Identical on both platforms.)*

```bash
docker run -d --name app1 nginx
docker run -d --name app2 nginx

docker stats                        # live stream — Ctrl+C to stop
docker stats --no-stream            # single snapshot
docker stats app1 app2 --no-stream  # specific containers only

docker rm -f app1 app2              # clean up
```

---

## Exercise 5 — Simulate and Diagnose a Broken Container

### 🍎 macOS
```bash
# A container that crashes immediately with an error
docker run -d --name broken alpine sh -c "echo 'something went wrong' && exit 1"
sleep 2

docker ps               # 'broken' is NOT here — it already stopped
docker ps -a            # you'll see 'broken' with status Exited (1)
docker logs broken      # what it printed before crashing
docker inspect -f "{{.State.ExitCode}}" broken   # 1 = application error
docker rm broken

# A container that runs out of memory and gets killed
docker run -d --name oom-demo --memory="15m" alpine \
  sh -c "cat /dev/zero | head -c 50m > /dev/null"
sleep 3

docker inspect -f "{{.State.OOMKilled}}" oom-demo   # true = killed for OOM
docker inspect -f "{{.State.ExitCode}}"  oom-demo   # 137 = OOMKilled
docker rm oom-demo
```
### 🪟 Windows
```powershell
docker run -d --name broken alpine sh -c "echo 'something went wrong' && exit 1"
Start-Sleep -Seconds 2

docker ps
docker ps -a
docker logs broken
docker inspect -f "{{.State.ExitCode}}" broken
docker rm broken

docker run -d --name oom-demo --memory="15m" alpine `
  sh -c "cat /dev/zero | head -c 50m > /dev/null"
Start-Sleep -Seconds 3

docker inspect -f "{{.State.OOMKilled}}" oom-demo
docker inspect -f "{{.State.ExitCode}}"  oom-demo
docker rm oom-demo
```

---

## Exercise 6 — Copying Files Between Container and Host

The `docker cp` command is the same — only the *host path* differs.

### 🍎 macOS
```bash
docker run -d --name copy-demo nginx

# FROM container TO your Desktop
docker cp copy-demo:/etc/nginx/nginx.conf ~/Desktop/nginx.conf

# FROM your Mac TO the container
echo "<h1>Copied from macOS</h1>" > /tmp/test.html
docker cp /tmp/test.html copy-demo:/usr/share/nginx/html/test.html

docker exec copy-demo ls /usr/share/nginx/html   # verify
docker rm -f copy-demo
```
### 🪟 Windows
```powershell
docker run -d --name copy-demo nginx

docker cp copy-demo:/etc/nginx/nginx.conf "$env:USERPROFILE\Desktop\nginx.conf"

"<h1>Copied from Windows</h1>" | Set-Content "$env:TEMP\test.html"
docker cp "$env:TEMP\test.html" copy-demo:/usr/share/nginx/html/test.html

docker exec copy-demo ls /usr/share/nginx/html
docker rm -f copy-demo
```

---

## All Debugging Commands Reference

```bash
# Logs
docker logs NAME                    all logs since container started
docker logs -f NAME                 follow live, Ctrl+C to stop
docker logs --tail 50 NAME          last 50 lines only
docker logs -t NAME                 include timestamps
docker logs --since 5m NAME         logs from last 5 minutes

# Shell and exec
docker exec -it NAME bash           interactive bash shell inside container
docker exec -it NAME sh             use sh for Alpine images (no bash there)
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

# Copy files    (host path differs per OS — see Exercise 6)
docker cp NAME:/container/path  <host-path>      container → host
docker cp <host-path>  NAME:/container/path      host → container

# Force remove a running container without stopping first
docker rm -f NAME
```

---

## 📝 Interview Questions

**Q: How do you debug a container that keeps restarting?**
> Run `docker logs <name>` to read what the container printed before crashing.
> Then run `docker inspect <name>` to check the exit code.
> Exit code 137 means it ran out of memory — increase the memory limit.
> Exit code 1 means the application threw an error — read the stack trace in the logs.

**Q: How do you open a shell inside a running container?**
> `docker exec -it <name> bash` — or `sh` for Alpine-based images that don't have bash.

**Q: What does exit code 137 mean?**
> The container was OOMKilled — it exceeded its memory limit and the OS forcibly killed it.
> Fix by increasing the memory limit or reducing the application's memory usage.

**Q: How do you copy a file out of a container?**
> `docker cp <name>:/path/in/container <host-destination>`
> This works on both running and stopped containers.

---