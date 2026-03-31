```
██████╗  █████╗  ██████╗██╗  ██╗███████╗███╗   ██╗██████╗    ██████╗  ██████╗  ██████╗██╗  ██╗██╗   ██╗ █████╗ ██████╗ ██████╗
██╔══██╗██╔══██╗██╔════╝██║ ██╔╝██╔════╝████╗  ██║██╔══██╗   ██╔══██╗██╔═══██╗██╔════╝██║ ██╔╝╚██╗ ██╔╝██╔══██╗██╔══██╗██╔══██╗
██████╔╝███████║██║     █████╔╝ █████╗  ██╔██╗ ██║██║  ██║   ██║  ██║██║   ██║██║     █████╔╝  ╚████╔╝ ███████║██████╔╝██║  ██║
██╔══██╗██╔══██║██║     ██╔═██╗ ██╔══╝  ██║╚██╗██║██║  ██║   ██║  ██║██║   ██║██║     ██╔═██╗   ╚██╔╝  ██╔══██║██╔══██╗██║  ██║
██████╔╝██║  ██║╚██████╗██║  ██╗███████╗██║ ╚████║██████╔╝   ██████╔╝╚██████╔╝╚██████╗██║  ██╗   ██║   ██║  ██║██║  ██║██████╔╝
╚═════╝ ╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═══╝╚═════╝    ╚═════╝  ╚═════╝  ╚═════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝
```

---
A place where backend systems are built, containerized, orchestrated and shipped.
Every folder is a topic. Every file is working code or a hands-on guide.
No copy-paste from tutorials. Built from scratch, one commit at a time.

## 📦 Why this Repo exists

---
Most developers learn Docker by reading articles and never actually running the commands.
Most developers learn design patterns by memorising definitions and never applying them.
Most developers prepare for system design interviews by watching videos and forgetting everything.

This repo is different. Every concept has working code. Every day has a git commit.
If you are reading this on my GitHub you are looking at real learning, not a showcase.

## 💡 What Is Being Built Here

---
```
backend-dockyard/
│
├── docker/                     Containerisation from zero to CI/CD
├── kubernetes/                 Orchestration from local cluster to production
├── spring-boot/                Real Spring Boot apps, each fully runnable
├── design-patterns/            GoF patterns applied to actual Spring Boot code
├── dsa/                        Interview DSA in Java with full explanations
└── system-design/              Architecture docs backed by working code
```

## 🐳 Docker

---
Learning Docker the right way — not just what commands to run but why each one exists, what goes wrong and how to debug it.

| Topic                                                     | Folder                                       | Status |
|-----------------------------------------------------------|----------------------------------------------|--------|
| What is Docker, images, containers, WSL2 on Windows       | `docker/docker-basics/01-what-is-docker`     | ✅ Done |
| Dockerfile — FROM WORKDIR COPY RUN ENV CMD, layer caching | `docker/docker-basics/02-dockerfile`         | ✅ Done |
| Volumes and networking — persistence and container DNS    | `docker/docker-basics/03-volumes-networks`   | ✅ Done |
| Debugging — logs, exec, inspect, stats, exit codes        | `docker/docker-basics/04-debugging`          | ✅ Done |
| Basic review and final exercise                           | `docker/docker-basics/05-review`             | ✅ Done |
| Multi-stage builds — smaller safer production images      | `docker/docker-intermediate/01-multi-stage`  | ✅ Done |
| Docker Compose — full stack with one command              | `docker/docker-intermediate/02-compose`      | ✅ Done |
| Image optimisation — layers, caching, .dockerignore       | `docker/docker-intermediate/03-optimisation` | ✅ Done |
| Pushing images to GitHub Container Registry               | `docker/docker-intermediate/04-registry`     | ✅ Done |
| Spring Boot containerised with profiles and Actuator      | `docker/spring-docker/01-spring-setup`       | ✅ Done |
| Redis caching with Spring Boot in Docker                  | `docker/spring-docker/02-redis-cache`        | ✅ Done |
| Full production Compose stack                             | `docker/spring-docker/03-full-compose`       | ✅ Done |

## ☸️ Kubernetes

---
*Starting after Docker is complete.*

| Topic                                             | Folder                                          | Status    |
|---------------------------------------------------|-------------------------------------------------|-----------|
| K8s concepts — control plane, nodes, core objects | `kubernetes/k8s-basics/01-concepts`             | ✅ Done    |
| Pods and Deployments — self healing and scaling   | `kubernetes/k8s-basics/02-deployments`          | ⬜ Pending |
| Services — ClusterIP, NodePort, LoadBalancer      | `kubernetes/k8s-basics/03-services`             | ⬜ Pending |
| ConfigMap and Secrets                             | `kubernetes/k8s-intermediate/01-config-secrets` | ⬜ Pending |
| Liveness, Readiness and Startup probes            | `kubernetes/k8s-intermediate/02-probes`         | ⬜ Pending |
| Ingress and routing                               | `kubernetes/k8s-intermediate/03-ingress`        | ⬜ Pending |
| HPA — auto scaling based on CPU and memory        | `kubernetes/k8s-advanced/01-hpa`                | ⬜ Pending |
| GitHub Actions CI/CD pipeline to Kubernetes       | `kubernetes/k8s-advanced/02-cicd`               | ⬜ Pending |

## 🌱 Spring Boot Projects

---
*Each project is a standalone runnable app with its own Dockerfile and Docker Compose file.*

| Project                 | Stack                          | Status    |
|-------------------------|--------------------------------|-----------|
| REST API with full CRUD | Spring Boot + JPA + PostgreSQL | ⬜ Pending |
| JWT authentication      | Spring Security + JWT          | ⬜ Pending |
| Event driven messaging  | Kafka + Spring Boot            | ⬜ Pending |
| Microservices           | Two services + API Gateway     | ⬜ Pending |

## 🧩 Design Patterns
 
---
*GoF patterns implemented in Java with real Spring Boot context, not toy examples.*

| Category   | Patterns                                                     | Status    |
|------------|--------------------------------------------------------------|-----------|
| Creational | Singleton, Factory, Builder, Prototype                       | ⬜ Pending |
| Structural | Adapter, Decorator, Facade, Proxy                            | ⬜ Pending |
| Behavioral | Strategy, Observer, Chain of Responsibility, Template Method | ⬜ Pending |

## 🧠 DSA

---
*Interview focused. Every solution has time and space complexity analysis and a plain English explanation.*

| Topic                                                           | Status    |
|-----------------------------------------------------------------|-----------|
| Arrays and Strings — two pointers, sliding window, prefix sums  | ⬜ Pending |
| Linked Lists — reversal, cycle detection, merge                 | ⬜ Pending |
| Trees and Graphs — BFS, DFS, binary search tree                 | ⬜ Pending |
| Dynamic Programming — memoization, tabulation, classic problems | ⬜ Pending |
| Sorting and Searching — QuickSort, MergeSort, binary search     | ⬜ Pending |

## 🏗️ System Design

---
*Written like real design documents. Working code where it matters.*

| Design             | Covers                                    | Status    |
|--------------------|-------------------------------------------|-----------|
| URL Shortener      | Hashing, redirect, scaling                | ⬜ Pending |
| Rate Limiter       | Token bucket, Redis backed, Spring filter | ⬜ Pending |
| Caching Strategies | Cache aside, write through, eviction      | ⬜ Pending |

## 🛠️ Tools

---
| Tool                     | Purpose                     |
|--------------------------|-----------------------------|
| Java 17                  | Language                    |
| Spring Boot 3            | Application framework       |
| Maven                    | Build tool                  |
| Docker Desktop with WSL2 | Containerisation on Windows |
| minikube and kubectl     | Local Kubernetes cluster    |
| IntelliJ IDEA            | IDE                         |
| GitHub Actions           | CI/CD pipeline              |

---
*If this helped you, drop a ⭐ — it keeps the motivation going.*