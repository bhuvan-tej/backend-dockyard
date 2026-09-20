```
██████╗  █████╗  ██████╗██╗  ██╗███████╗███╗   ██╗██████╗    ██████╗  ██████╗  ██████╗██╗  ██╗██╗   ██╗ █████╗ ██████╗ ██████╗
██╔══██╗██╔══██╗██╔════╝██║ ██╔╝██╔════╝████╗  ██║██╔══██╗   ██╔══██╗██╔═══██╗██╔════╝██║ ██╔╝╚██╗ ██╔╝██╔══██╗██╔══██╗██╔══██╗
██████╔╝███████║██║     █████╔╝ █████╗  ██╔██╗ ██║██║  ██║   ██║  ██║██║   ██║██║     █████╔╝  ╚████╔╝ ███████║██████╔╝██║  ██║
██╔══██╗██╔══██║██║     ██╔═██╗ ██╔══╝  ██║╚██╗██║██║  ██║   ██║  ██║██║   ██║██║     ██╔═██╗   ╚██╔╝  ██╔══██║██╔══██╗██║  ██║
██████╔╝██║  ██║╚██████╗██║  ██╗███████╗██║ ╚████║██████╔╝   ██████╔╝╚██████╔╝╚██████╗██║  ██╗   ██║   ██║  ██║██║  ██║██████╔╝
╚═════╝ ╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═══╝╚═════╝    ╚═════╝  ╚═════╝  ╚═════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝
```

---

## 🧰 Tech Stack

---
**Languages & Frameworks**

![Java](https://img.shields.io/badge/Java-17%20%2F%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Auth%20%26%20JWT-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-LLM%20%2F%20RAG%20%2F%20MCP-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build%20Tool-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**Databases & Caching**

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![H2](https://img.shields.io/badge/H2-In--Memory%20DB-1E88E5?style=for-the-badge&logo=databricks&logoColor=white)

**Containers & Orchestration**

![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![NGINX](https://img.shields.io/badge/NGINX-Reverse%20Proxy-009639?style=for-the-badge&logo=nginx&logoColor=white)

**Libraries, CI/CD & Tooling**

![JWT](https://img.shields.io/badge/JWT-JJWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Ollama](https://img.shields.io/badge/Ollama-Local%20LLM-000000?style=for-the-badge&logo=ollama&logoColor=white)
![Vector Store](https://img.shields.io/badge/Vector%20Store-RAG-4B8BBE?style=for-the-badge&logo=databricks&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-Boilerplate%20Killer-BC0031?style=for-the-badge&logo=lombok&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-IDE-000000?style=for-the-badge&logo=intellijidea&logoColor=white)

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
├── spring-ai/                  LLM / Spring AI apps — ChatClient, RAG, tools, MCP
├── microservices/              Interview-focused distributed system patterns
├── design-patterns/            GoF patterns in plain Java
└── dsa/                        Interview DSA in Java with full explanations
```

## 🐳 Docker

---
Learning Docker the right way — not just what commands to run but why each one exists, what goes wrong and how to debug it.

| Topic                                                     | Folder                                       | Status  |
|-----------------------------------------------------------|----------------------------------------------|---------|
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

| Topic                                             | Folder                                          | Status  |
|---------------------------------------------------|-------------------------------------------------|---------|
| K8s concepts — control plane, nodes, core objects | `kubernetes/k8s-basics/01-concepts`                | ✅ Done |
| Pods and Deployments — self healing and scaling   | `kubernetes/k8s-basics/02-pods-deployments`        | ✅ Done |
| Services — ClusterIP, NodePort, LoadBalancer      | `kubernetes/k8s-basics/03-services`                | ✅ Done |
| ConfigMap and Secrets                             | `kubernetes/k8s-intermediate/01-configmap-secret`  | ✅ Done |
| Liveness, Readiness and Startup probes            | `kubernetes/k8s-intermediate/02-probes`            | ✅ Done |
| Ingress and routing                               | `kubernetes/k8s-intermediate/03-ingress`           | ✅ Done |
| Spring Boot on K8s — full stack deploy            | `kubernetes/k8s-advanced/01-spring-boot-k8s`       | ✅ Done |
| HPA — auto scaling based on CPU and memory        | `kubernetes/k8s-advanced/02-hpa`                   | ✅ Done |
| GitHub Actions CI/CD pipeline to Kubernetes       | `kubernetes/k8s-advanced/03-cicd`                  | ✅ Done |

## 🌱 Spring Boot Projects

---
*Each project is a standalone runnable app with its own Dockerfile and Docker Compose file.*

| Project                                                                                                                          | Stack                             | Status     |
|----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|------------|
| REST API with full CRUD                                                                                                          | Spring Boot + JPA + PostgreSQL    | ✅ Done    |
| QR generator & decoder([API guide](spring-boot/02-qr-generator/qr-generator/API_GUIDE.md))                                       | Spring Boot + ZXing + H2          | ✅ Done    |
| OTP + JWT authentication([API guide](spring-boot/03-otp-jwt-auth/otp-auth/API_GUIDE.md))                                         | Spring Security + JWT (JJWT) + H2 | ✅ Done    |
| Spring AI basics — the 5 core LLM patterns([explained](spring-boot/04-spring-ai-basics/spring-ai-basics/SPRING_AI_EXPLAINED.md)) | Spring AI + Ollama                | ✅ Done    |
## 🛠️ Tools

---
| Tool                     | Purpose                     |
|--------------------------|-----------------------------|
| Java 17+                 | Language                    |
| Spring Boot 3            | Application framework       |
| Maven                    | Build tool                  |
| Docker Desktop with WSL2 | Containerisation on Windows |
| minikube and kubectl     | Local Kubernetes cluster    |
| IntelliJ IDEA            | IDE                         |

---
*If this helped you, drop a ⭐ — it keeps the motivation going.*