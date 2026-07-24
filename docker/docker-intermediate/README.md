# 🐳 Docker Intermediate — Start Here

> **Goal of this folder:** go from "I can run a container" to "I can build,
> wire, shrink and ship production images" — still with **zero memorization**.
> You'll learn **4 mental models** and *derive* every command from them.

---

## 🗺️ The Path (do these in order)

```
01-multi-stage   →  Build with the JDK, ship only the JRE  (70% smaller images)
02-compose       →  Run a whole app (Java + Postgres) with ONE command
03-optimisation  →  4 levers that make images smaller & builds faster
04-registry      →  git push, but for images (send them to GHCR)
```

> Prereq: finish `../docker-basics/` first. This folder assumes you already
> know `build`, `run`, `-p`, `-v`, `--network` and how a Dockerfile works.

---

## 🧠 Model A — Multi-stage = "the kitchen vs the delivery box"

```
STAGE 1 (builder)                STAGE 2 (runtime = the image you ship)
────────────────────             ─────────────────────────────────────
FROM ...-jdk-alpine AS builder   FROM ...-jre-alpine
  full kitchen: compiler,        just a plate: JRE + your .jar
  Maven, source code
         │                                ▲
         └──── COPY --from=builder ───────┘  (only the meal crosses)

The kitchen is THROWN AWAY. You deliver only the meal.
```

> Derive it, don't memorize it: **"Which stuff do I need to *build* but not to
> *run*?"** → that stuff belongs in an earlier stage and never gets copied out.
> Two rules are all you need: name a stage with **`AS name`**, pull from it with
> **`COPY --from=name`**.

---

## 🧠 Model B — Compose = "your `docker run` commands, written down"

Everything in `docker-compose.yml` is a flag you already know from basics:

```
docker run flag        →   compose key
──────────────────         ───────────────
--name web             →   services:  web:
-e KEY=val             →   environment:
-p 8080:80             →   ports:
-v data:/path          →   volumes:
--network net          →   networks:
(build from Dockerfile)→   build: { context, dockerfile }
(start order)          →   depends_on:
```

> One command replaces a page of `docker run`s: **`docker compose up -d`**.
> The whole lifecycle is just 4 verbs — see Model D below.

---

## 🧠 Model C — Optimisation = "smaller box, faster builds" (4 levers)

```
1. .dockerignore   →  stop SENDING junk to the daemon   (faster + safer)
2. Cache order     →  copy pom.xml BEFORE src/          (skip re-downloads)
3. Small base      →  alpine / jre instead of full jdk  (less size + fewer CVEs)
4. Multi-stage     →  Model A                           (biggest single win)
```

> The one sentence that covers lever 2: **"put what changes least at the top."**
> The one sentence for the whole folder: **"ship the meal, not the kitchen."**

---

## 🧠 Model D — Registry & Compose share the same 4-verb rhythm

```
COMPOSE                          REGISTRY
────────                         ─────────
up      start everything         build   make the image
down    stop & remove            tag     give it an address
logs    watch output             push    send it up   (like git push)
ps      what's running           pull    get it back  (like git clone)
```

> A registry is just **remote storage for images** — Maven Central, but for
> Docker. A tag is the full postal address:
> `ghcr.io / user / repo / image : version`.

---

## 📝 How to note each topic (glanceable card format)

For every module, compress your notes to **one screen** like this:

```
MODULE 01 — Multi-stage builds
────────────────────────────────
WHY:    JDK image ~400MB; at runtime you only need the JRE (~180MB)
FIX:    Stage 1 = jdk (build) · Stage 2 = jre (run) · copy only the .jar
DERIVE: "need it to build but not to run?" → earlier stage, don't copy out
2 CMDS: FROM ... AS builder   ·   COPY --from=builder /path .
GOTCHA: source code & compiler stay behind → smaller + safer image
```

If you can rebuild the card from the 4 models above, you've *understood* it —
not memorized it.

---

## 💻 macOS **and** Windows — both are covered

Every command block in this folder shows **two tabs**:

- 🍎 **macOS / Linux** — `bash` / `zsh`, uses `\` for line breaks, `~`, `open`
- 🪟 **Windows** — `PowerShell`, uses `` ` `` for line breaks, `$env:USERPROFILE`

The Docker commands themselves are **identical** — only the *shell wrapper*
(paths, line-continuation, making a test file, opening a browser) differs.

| Task                                  | 🍎 macOS / Linux             | 🪟 Windows (PowerShell)                   |
|---------------------------------------|------------------------------|-------------------------------------------|
| **Line continuation**                 | `\`                          | `` ` `` (backtick)                        |
| **Create a 100 MB file**              | `mkfile 100m big.bin`        | `fsutil file createnew big.bin 104857600` |
| **Rename a file**                     | `mv old.txt new.txt`         | `Rename-Item old.txt new.txt`             |
| **Delete a file**                     | `rm big.bin`                 | `Remove-Item big.bin`                     |
| **Filter command output**             | `... \| grep hello`          | `... \| Select-String hello`              |
| **Open a URL in the default browser** | `open http://localhost:8080` | `Start-Process "http://localhost:8080"`   |

> **Container paths never change** — inside a container it's always Linux:
> `/app`, `/build` ✅ (never `C:\...` or `~/...`).

> ### 🍏 Apple Silicon (M1/M2/M3) is handled for you
> The `eclipse-temurin:*-alpine` base images are **amd64-only** (no `arm64`
> build), so a plain build on Apple Silicon fails with
> `no match for platform in manifest: not found`. **Every Dockerfile in this
> repo pins `--platform=linux/amd64` on its `FROM` line**, so `docker build`
> and `docker compose up` just work with no extra flags. Docker emulates the
> Intel image; on Intel/Windows the pin is a harmless no-op.

---

## ✅ Before you start

```bash
docker --version           # Docker is installed
docker info                # the engine is actually running
docker compose version     # Compose v2 is available (note: "compose", no hyphen)
```

If `docker info` errors, start **Docker Desktop** and wait for the whale icon to
settle. Then begin with **`01-multi-stage/`**.