# 🐳 Docker Basics — Start Here

> **Goal of this folder:** understand Docker on *first glance*, with **zero memorization**.
> You won't memorize 30 commands. You'll learn **3 mental models** and *derive* every command from them.

---

## 🗺️ The Path (do these in order)

```
01-what-is-docker    →  WHY Docker exists + run your first container
02-dockerfile        →  Package YOUR app into an image
03-volumes-networks  →  Keep data alive + let containers talk
04-debugging         →  Fix a broken container (interviewers LOVE this)
05-review            →  Tie it all together with one final exercise
```

Each module has: a **README** (learn + do), and the folder shares an
`INTERVIEW_QUESTIONS.md` and a `QUICK_REFERENCE.md`.

---

## 🧠 Model A — "A container is just a Linux process in a box"

You never memorize commands. You ask: **"which of these 5 things am I doing?"**

```
┌─ 1. BUILD  the box   →  docker build      (Dockerfile → image)
├─ 2. RUN    the box   →  docker run        (image → running container)
├─ 3. LOOK   at boxes  →  docker ps · logs · inspect · stats
├─ 4. ENTER  the box   →  docker exec -it <name> sh
└─ 5. CLEAN  the box   →  docker stop · rm · rmi · prune
```

> 90% of daily Docker work is just these 5 verbs. Everything else is a flag.

---

## 🧠 Model B — Flags are *needs*, not trivia (derive, don't memorize)

Every `docker run` flag maps to a real-world need:

```
Need                          Flag           Memory hook
────────────────────────────  ─────────────  ──────────────────────────
"run in background"           -d             d = detached
"reach it from my browser"    -p 8080:80     p = port  (MINE:THEIRS)
"give it a name"              --name web     literally, a name
"pass a setting"              -e KEY=val      e = environment
"keep the data"               -v vol:/path    v = volume
"throw it away after"         --rm            rm = remove on exit
```

> **The one rule that removes most confusion:** `-p` is **`MINE:THEIRS`**
> (host port : container port). Your laptop's port on the left, always.

---

## 🧠 Model C — A Dockerfile is a recipe: read top→bottom, cached top→bottom

```
Stable stuff FIRST   (FROM, WORKDIR)      → cached, almost never rebuilds
Changing stuff LAST  (COPY code, RUN)     → rebuilds only what changed below
```

> The entire caching lecture in one sentence:
> **"Put what changes least at the top."**

The 4 core terms, as Java:

```
┌──────────────┬──────────────────────────────────┬─────────────────────┐
│ Image        │ Read-only blueprint / template   │ A Class definition  │
│ Container    │ A running instance of an image   │ new MyClass()       │
│ Dockerfile   │ Instructions to build an image   │ The build script    │
│ Registry     │ Remote storage for images        │ Maven Central       │
└──────────────┴──────────────────────────────────┴─────────────────────┘
```

---

## 📝 How to note each topic (glanceable card format)

For every module, compress your notes to **one screen** like this:

```
MODULE 03 — Volumes & Networks
────────────────────────────────
WHY:    containers forget everything on delete; IPs change on restart
FIX:    -v for memory (data),  --network for phone book (DNS by name)
DERIVE: data must persist? → volume.   two containers talk? → same network.
1 CMD:  docker run -v data:/path   ·   docker network create net
GOTCHA: container path is ALWAYS /linux/style — never C:\ or ~
```

If you can rebuild the card from the 3 models above, you've *understood* it —
not memorized it.

---

## 💻 macOS **and** Windows — both are covered

Every command block in this folder shows **two tabs**:

- 🍎 **macOS / Linux** — `bash` / `zsh`, uses `\` for line breaks, `~`, `open`
- 🪟 **Windows** — `PowerShell`, uses `` ` `` for line breaks, `$env:USERPROFILE`

Pick whichever matches your machine. The Docker commands themselves are
**identical** — only the *shell wrapper* (paths, line-continuation, opening a
browser/editor) differs.

| Task          | 🍎 macOS / Linux             | 🪟 Windows (PowerShell)      |
|---------------|------------------------------|------------------------------|
| Line continue | `\`                          | `` ` `` (backtick)           |
| Home folder   | `~` or `$HOME`               | `$env:USERPROFILE`           |
| Open browser  | `open http://localhost:8080` | `Start-Process "http://..."` |
| Edit a file   | `nano file` / `open -e file` | `notepad file`               |
| Current dir   | `$PWD`                       | `${PWD}`                     |

> **Container paths never change** — inside the container it's always Linux:
> `/app/data` ✅   (never `C:\...` or `~/...`).

> ### 🍏 Apple Silicon (M1/M2/M3) is handled for you
> The `eclipse-temurin:*-alpine` base images are **amd64-only** (no `arm64`
> build), so a plain build on Apple Silicon fails with
> `no match for platform in manifest: not found`. **Every Dockerfile in this
> repo pins `--platform=linux/amd64` on its `FROM` line**, so builds just work
> with no extra flags. Docker emulates the Intel image; on Intel/Windows the
> pin is a harmless no-op.

---

## ✅ Before you start

```bash
docker --version    # confirms Docker is installed
docker info         # confirms the Docker engine is actually running
```

If `docker info` errors, start **Docker Desktop** first and wait for the whale
icon to stop animating. Then begin with **`01-what-is-docker/`**.