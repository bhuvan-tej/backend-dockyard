# ☸️ Kubernetes — Start Here

> **Goal of this folder:** go from "I can run one container with Docker"
> to "I can run, scale, configure, expose and auto-deploy a real Spring
> Boot stack on a cluster" — with a clear mental model, not memorization.

Every module has a **README** (learn + do), an **INTERVIEW_QUESTIONS.md**
and a **QUICK_REFERENCE.md**. Some also ship the **manifests** (`.yaml`)
used in the exercises and a `docs/` folder with diagrams.

---

## 🗺️ The Path (do these in order)

```
k8s-basics/
  01-concepts            →  WHY K8s exists + control plane / node architecture
  02-pods-deployments    →  Bare Pod vs Deployment · self-healing · scaling · rollout
  03-services            →  Stable networking · ClusterIP vs NodePort · cluster DNS

k8s-intermediate/
  01-configmap-secret    →  Externalise config out of the image
  02-probes              →  liveness / readiness / startup · Actuator mapping
  03-ingress             →  One entry point · path/host routing · rewrite-target

k8s-advanced/
  01-spring-boot-k8s     →  Deploy the FULL stack: Spring Boot + Postgres + Redis
  02-hpa                 →  Auto-scale Pods on CPU with the Metrics Server
  03-cicd                →  GitHub Actions: test → build → push → deploy → rollback
```

---

## 🧠 The one mental model that ties it all together

Kubernetes is **declarative**: you write the *desired state* in YAML,
and the control plane continuously makes reality match it.

```
You DECLARE            Kubernetes GUARANTEES
──────────────────     ────────────────────────────────────────
Deployment (3 replicas) → always 3 healthy Pods (self-healing)
Service                 → a stable DNS name in front of changing Pod IPs
ConfigMap / Secret      → config injected at startup, separate from the image
Probes                  → only healthy Pods get traffic; broken ones restart
Ingress                 → one URL routed to many Services by path/host
HPA                     → replica count follows CPU load, within min/max
```

Every object follows the same 4-part YAML shape:

```yaml
apiVersion:   which API group handles this resource
kind:         the resource type (Pod, Deployment, Service, Ingress...)
metadata:     identity — name, namespace, labels
spec:         the desired state you want maintained
```

---

## 📝 How to remember each module (glanceable card)

Compress every topic to one screen — WHY it exists, the FIX, what to DERIVE,
one command, and the classic GOTCHA:

```
MODULE — Services
──────────────────────────────────────
WHY:    Pod IPs change on every restart — hardcoded URLs break
FIX:    a Service gives a stable DNS name + load balances matching Pods
DERIVE: how it finds Pods → label selector (app: my-app)
1 CMD:  kubectl expose deployment my-app --port 80
GOTCHA: ClusterIP is in-cluster only; NodePort/Ingress for outside access
```

If you can rebuild the card from the mental model above, you *understand*
it — you did not just memorize commands.

---

## 🧰 Tooling used here

| Tool              | Role                                           |
|-------------------|------------------------------------------------|
| `minikube`        | Single-node Kubernetes cluster on your machine |
| `kubectl`         | CLI to talk to the cluster's API server        |
| `minikube addons` | Enable extras: `ingress`, `metrics-server`     |
| GitHub Actions    | CI/CD pipeline that deploys into the cluster   |

> ⚠️ **Shell note:** command blocks are written **macOS / Linux first**
> (`cd kubernetes/...`, `grep`, `\` for line breaks). Where a command is
> genuinely OS-specific — base64 decode, kubeconfig encoding — the
> **🪟 Windows PowerShell** form is shown right beside it. The `kubectl`
> and `minikube` commands themselves are identical on every OS.

---

## ✅ Before you start

```bash
minikube start --driver=docker --memory=4096 --cpus=2
kubectl get nodes          # expect: minikube   Ready   control-plane

# Most basics/intermediate exercises use this namespace
kubectl create namespace backend-dockyard
```

Then begin with **`k8s-basics/01-concepts/`**.