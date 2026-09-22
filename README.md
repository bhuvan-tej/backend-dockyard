```
██████╗  █████╗  ██████╗██╗  ██╗███████╗███╗   ██╗██████╗    ██████╗  ██████╗  ██████╗██╗  ██╗██╗   ██╗ █████╗ ██████╗ ██████╗
██╔══██╗██╔══██╗██╔════╝██║ ██╔╝██╔════╝████╗  ██║██╔══██╗   ██╔══██╗██╔═══██╗██╔════╝██║ ██╔╝╚██╗ ██╔╝██╔══██╗██╔══██╗██╔══██╗
██████╔╝███████║██║     █████╔╝ █████╗  ██╔██╗ ██║██║  ██║   ██║  ██║██║   ██║██║     █████╔╝  ╚████╔╝ ███████║██████╔╝██║  ██║
██╔══██╗██╔══██║██║     ██╔═██╗ ██╔══╝  ██║╚██╗██║██║  ██║   ██║  ██║██║   ██║██║     ██╔═██╗   ╚██╔╝  ██╔══██║██╔══██╗██║  ██║
██████╔╝██║  ██║╚██████╗██║  ██╗███████╗██║ ╚████║██████╔╝   ██████╔╝╚██████╔╝╚██████╗██║  ██╗   ██║   ██║  ██║██║  ██║██████╔╝
╚═════╝ ╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═══╝╚═════╝    ╚═════╝  ╚═════╝  ╚═════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝
```

Backend-focused learning repo with runnable code, topic-wise notes, and small projects across Java, Spring, containers, orchestration, DSA, and design patterns.

> This README reflects only the content that is currently committed in the repository.

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

## 📦 Why this repo exists

This repo is a hands-on backend learning dockyard.

- learn by building, not by bookmarking tutorials
- keep every topic runnable and easy to revisit
- organize backend concepts by domain instead of mixing everything into one app
- build up interview-ready understanding through code, notes, and examples

## 🗂️ Repository structure

```text
backend-dockyard/
├── docker/             Docker fundamentals, intermediate topics, Spring + Docker demos
├── dsa/                Java DSA problems with explanation-first structure
├── design-patterns/    GoF patterns in plain Java
├── kubernetes/         Kubernetes concepts and deployment examples
├── spring-ai/          Spring AI apps focused on LLM patterns and semantic search
└── spring-boot/        Standalone Spring Boot apps and Java feature walkthroughs
```

## 🧭 Top-level map

| Area            | What it contains                                                              |
|-----------------|-------------------------------------------------------------------------------|
| docker          | Basics, intermediate Docker topics, and Spring Boot containerization examples |
| dsa             | Arrays, strings, and sorting-searching topics in Java                         |
| design-patterns | Creational design patterns in plain Java                                      |
| kubernetes      | Basics, intermediate, and advanced Kubernetes learning modules                |
| spring-ai       | Spring AI basics and semantic search apps                                     |
| spring-boot     | Runnable Spring Boot apps and Java feature walkthrough projects               |

## 🐳 Docker

| Track                   | Coverage                                                           | Folder                     |
|-------------------------|--------------------------------------------------------------------|----------------------------|
| Docker basics           | Containers, Dockerfile, volumes, networking, debugging, review     | docker/docker-basics       |
| Docker intermediate     | Multi-stage builds, Compose, optimization, image registry workflow | docker/docker-intermediate |
| Spring Boot with Docker | Spring setup, Redis cache example, full Compose stack              | docker/spring-docker       |

## ☸️ Kubernetes

| Track                   | Coverage                                | Folder                      |
|-------------------------|-----------------------------------------|-----------------------------|
| Kubernetes basics       | Concepts, pods, deployments, services   | kubernetes/k8s-basics       |
| Kubernetes intermediate | ConfigMap, Secret, probes, ingress      | kubernetes/k8s-intermediate |
| Kubernetes advanced     | Spring Boot deploy, HPA, CI/CD pipeline | kubernetes/k8s-advanced     |

## 🌱 Spring Boot projects

| Project            | What it covers                                                                | Stack                        |
|--------------------|-------------------------------------------------------------------------------|------------------------------|
| 01-rest-api-crud   | CRUD APIs, DTOs, validation, exception handling, pagination                   | Spring Boot, JPA, PostgreSQL |
| 02-qr-generator    | QR code generation and decoding, engine isolation, output handling            | Spring Boot, ZXing, H2       |
| 03-otp-jwt-auth    | OTP verification, JWT auth, refresh rotation, stateless security              | Spring Security, JWT, H2     |
| 04-virtual-threads | Platform threads vs virtual threads, executor comparison, blocking simulation | Spring Boot, Java 21         |
| 05-java-streams    | Stream creation, intermediate ops, terminal ops, collectors, parallel streams | Java Streams                 |
| 06-java8-features  | Lambdas, functional interfaces, method references, Optional, Date Time API    | Java 8                       |
| 07-java11-features | var, String APIs, files API, HttpClient                                       | Java 11                      |
| 08-java17-features | Records, sealed classes, pattern matching, switch expressions, text blocks    | Java 17                      |
| notes              | Topic-wise markdown notes under the Spring Boot domain                        | spring-boot/notes            |

## 🤖 Spring AI projects

| Project             | What it covers                                                        | Stack                                |
|---------------------|-----------------------------------------------------------------------|--------------------------------------|
| 01-spring-ai-basics | Prompting patterns, personas, templates, structured output, streaming | Spring AI, Ollama                    |
| 02-semantic-search  | Embeddings, cosine similarity, vector search, semantic retrieval      | Spring AI, embeddings, vector search |

## 🧠 DSA

| Topic                | What it covers                                                           | Folder                   |
|----------------------|--------------------------------------------------------------------------|--------------------------|
| 01-arrays            | Array patterns, brute force vs optimized thinking, worked examples       | dsa/01-arrays            |
| 02-strings           | String patterns, immutability, sliding window, palindrome-style problems | dsa/02-strings           |
| 03-sorting-searching | Sorting trade-offs, binary search patterns, partition-style reasoning    | dsa/03-sorting-searching |

## 🏗️ Design patterns

| Category      | Patterns included                                    | Folder                        |
|---------------|------------------------------------------------------|-------------------------------|
| 01-creational | Singleton, Factory Method, Abstract Factory, Builder | design-patterns/01-creational |

## 🛠️ Tooling snapshot

| Tool            | Use                                         |
|-----------------|---------------------------------------------|
| Java 17 and 21  | Language versions across committed projects |
| Spring Boot 3.x | Application framework                       |
| Maven Wrapper   | Build and run Spring projects               |
| Docker          | Container builds and local stacks           |
| Kubernetes      | Deployment and orchestration learning       |
| IntelliJ IDEA   | Main development environment                |

---

*If this helped you, drop a ⭐ — it keeps the motivation going.*