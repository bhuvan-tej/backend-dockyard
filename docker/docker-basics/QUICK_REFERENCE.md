# 📋 Quick Reference — 🐳 Docker Basics

> Docker commands are **identical on macOS and Windows**. Only *host paths* and
> the *shell wrapper* differ. Where that matters, both are shown.

---

## 🧱 Images

```bash
docker build -t name:tag .     # build an image from a Dockerfile in this folder
docker images                  # list all local images
docker history name:tag        # show each layer and its size
docker rmi name:tag            # delete an image
```

## 📦 Containers

```bash
docker run -d -p 8080:80 --name mycontainer nginx   # -d background, -p MINE:THEIRS
docker run --rm nginx                                # auto-remove on exit
docker run --rm -e MY_NAME="Alice" hello-docker:v2   # -e sets an env variable
docker run -v my-vol:/app/data myimage               # named volume
docker ps                                            # running containers
docker ps -a                                         # all containers (incl. stopped)
docker stop mycontainer                              # graceful stop (SIGTERM, 10s)
docker rm mycontainer                                # remove a stopped container
docker rm -f mycontainer                             # force-remove a running one
```

Bind mount (host path differs per OS):
```bash
# 🍎 macOS
docker run -v ~/my/folder:/app/data myimage
# 🪟 Windows (PowerShell)
docker run -v "$env:USERPROFILE\my\folder:/app/data" myimage
```

## 🐞 Debugging

```bash
docker logs mycontainer            # all logs
docker logs -f mycontainer         # follow live (Ctrl+C to stop)
docker logs --tail 20 mycontainer  # last 20 lines
docker logs -t mycontainer         # with timestamps
docker exec -it mycontainer bash   # shell inside (use sh for Alpine)
docker exec mycontainer env        # single command, no shell
docker inspect mycontainer         # full JSON metadata
docker inspect -f "{{.State.ExitCode}}" mycontainer    # exit code
docker inspect -f "{{.State.OOMKilled}}" mycontainer   # killed by OOM?
docker stats                       # live CPU/memory for all containers
docker stats --no-stream           # single snapshot
```

Copy files (host path differs per OS):
```bash
# 🍎 macOS
docker cp mycontainer:/path/inside ~/Desktop/file.txt
docker cp ~/my/file.txt mycontainer:/path/inside
# 🪟 Windows
docker cp mycontainer:/path/inside "$env:USERPROFILE\Desktop\file.txt"
docker cp "C:\my\file.txt" mycontainer:/path/inside
```

## 🗃️ Volumes

```bash
docker volume create my-vol    # create a named volume
docker volume ls               # list all volumes
docker volume inspect my-vol   # details, including where it's stored
docker volume rm my-vol        # delete (⚠️ deletes all data inside)
```

## 🌐 Networks

```bash
docker network create my-net    # create a custom network
docker network ls               # list all networks
docker network inspect my-net   # details, including connected containers
docker network rm my-net        # delete a network
```

## 🧹 Cleanup

```bash
docker system prune   # remove stopped containers, unused networks, dangling images
docker system df      # show how much disk Docker is using
```

---

## 🧠 Remember the 3 models (so you don't memorize this table)

```
A) 5 verbs:  BUILD · RUN · LOOK · ENTER · CLEAN
B) flags = needs:  -d background · -p MINE:THEIRS · -e env · -v volume · --rm throwaway
C) Dockerfile:  put what changes LEAST at the top → caching just works
```