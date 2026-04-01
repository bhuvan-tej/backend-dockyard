# ☸️ Pods and Deployments

## 🎯 Goal

---
Understand the difference between a bare Pod and a Deployment.
Deploy both to minikube. Prove self-healing by deleting a Pod
and watching Kubernetes recreate it automatically.

## 🧠 Pod vs Deployment — Get This Right Forever

---
```
BARE POD
  You create it → it runs
  It crashes    → it is gone, Kubernetes does NOT recreate it
  Node fails    → it is gone
  No scaling, no self-healing, no rolling updates
  Only use bare Pods for quick debugging, never for real apps
 
DEPLOYMENT
  You declare: I want 3 replicas of this Pod
  Kubernetes maintains that count forever
  Pod crashes   → new one created within seconds
  Node fails    → Pods rescheduled to healthy nodes
  Update image  → rolling update with zero downtime
  Bad deploy    → rollback in one command
  ALWAYS use Deployments for real applications
```

## 🔑 YAML Structure Every K8s File Follows

---
```yaml
apiVersion:   which API group handles this resource type
kind:         the resource type (Pod, Deployment, Service...)
metadata:     identity — name, namespace, labels
spec:         the desired state you want Kubernetes to maintain
```

## ⚙️ Exercises

### ✅ Prerequisites Check

---
```
# Start minikube fresh
minikube start --driver=docker --memory=4096 --cpus=2

minikube status
kubectl get nodes
# Expected: minikube   Ready   control-plane

# The namespace was deleted when you ran minikube delete
# Recreate it before applying any YAML
kubectl create namespace backend-dockyard

# Verify
kubectl get namespaces
# backend-dockyard should appear

# Navigate to the folder
cd kubernetes\k8s-basics\02-pods-deployments
```

---
### 🧪 Exercise 1 — Deploy a Bare Pod and See the Problem

```powershell
# Apply the pod.yaml to create a standalone Pod
# -f specifies the file
# -n specifies the namespace
kubectl apply -f pod.yaml -n backend-dockyard
 
# Watch the Pod start up
# -w means watch — live updates stream to your terminal
# Press Ctrl+C when status shows Running
kubectl get pods -n backend-dockyard -w
 
# See full details of the Pod including events at the bottom
# Events show what Kubernetes did to create and start it
kubectl describe pod my-first-pod -n backend-dockyard
 
# Now delete the Pod manually — simulating a crash
kubectl delete pod my-first-pod -n backend-dockyard
 
# Check if it came back
kubectl get pods -n backend-dockyard
# It is GONE and stays gone
# Nothing recreated it because there is no Deployment watching over it
# This is why you never use bare Pods for real apps
```

### 🚀 Exercise 2 — Deploy With a Deployment and See Self-Healing

---
```powershell
# Apply the deployment.yaml
kubectl apply -f deployment.yaml -n backend-dockyard
 
# Watch all 3 Pods being created live
kubectl get pods -n backend-dockyard -w
# Press Ctrl+C when all show Running
 
# See the Deployment details
kubectl get deployments -n backend-dockyard
 
# See full Deployment details including rollout status and events
kubectl describe deployment my-app -n backend-dockyard
```

### 💥 Exercise 3 — Prove Self-Healing

---
```powershell
# Get the names of the running Pods
kubectl get pods -n backend-dockyard
 
# Delete one Pod — simulating a crash
# Replace my-app-xxxxx-xxxxx with an actual Pod name from above
kubectl delete pod my-app-xxxxx-xxxxx -n backend-dockyard
 
# Watch immediately — a new Pod is created within seconds
kubectl get pods -n backend-dockyard -w
 
# The Deployment saw: actual=2, desired=3
# Controller Manager created a replacement automatically
# This is the core value of Kubernetes
```

### 📈 Exercise 4 — Scale Up and Down

---
```powershell
# Scale up to 5 replicas
# --replicas sets the new desired count
kubectl scale deployment my-app --replicas=5 -n backend-dockyard
 
# Watch 2 new Pods being created
kubectl get pods -n backend-dockyard -w
 
# Scale back down to 2 replicas
kubectl scale deployment my-app --replicas=2 -n backend-dockyard
 
# Watch 3 Pods terminate gracefully
kubectl get pods -n backend-dockyard -w
```

### 🔁 Exercise 5 — Rolling Update

---
```powershell
# Update the image to a newer version of nginx
# set image updates the container image inside the Deployment
# my-app=nginx:1.25-alpine means set container named my-app to this image
kubectl set image deployment/my-app my-app=nginx:1.25-alpine -n backend-dockyard
 
# Watch the rolling update — old Pods terminate, new ones start
kubectl get pods -n backend-dockyard -w
 
# Check the rollout status
kubectl rollout status deployment/my-app -n backend-dockyard
# Expected: successfully rolled out
```

### ⏪ Exercise 6 — Rollback

---
```powershell
# See the rollout history
# Shows each revision with the change that triggered it
kubectl rollout history deployment/my-app -n backend-dockyard
 
# Roll back to the previous version
kubectl rollout undo deployment/my-app -n backend-dockyard
 
# Watch Pods roll back to the previous image
kubectl get pods -n backend-dockyard -w
 
# Confirm rollback completed
kubectl rollout status deployment/my-app -n backend-dockyard
```

### 🔍 Exercise 7 — Shell Into a Pod

---
```powershell
# Get a Pod name to exec into
kubectl get pods -n backend-dockyard
 
# Open an interactive shell inside a running Pod
# Like docker exec but for Kubernetes
# Replace my-app-xxxxx-xxxxx with actual Pod name
kubectl exec -it my-app-xxxxx-xxxxx -n backend-dockyard -- sh
 
# Inside the Pod
hostname          # the Pod name
env               # environment variables
cat /etc/os-release
exit
```

### 🛑 Exercise 8 — Clean Up

---
```powershell
# Delete the Deployment — this also deletes all its Pods
kubectl delete -f deployment.yaml -n backend-dockyard
 
# Confirm all Pods are gone
kubectl get pods -n backend-dockyard
```