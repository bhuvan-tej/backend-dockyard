# ☸️ Horizontal Pod Autoscaler (HPA)

## 🎯 Goal

---
Understand how HPA automatically scales Pods based on CPU usage.
Prove it works by generating real load and watching Pods scale up,
then remove the load and watch them scale back down.

## 🤔 Why HPA Exists

---
```
WITHOUT HPA

9am  — traffic spike hits, CPU at 90%, app is slow
you notice → manually scale up → takes 5 minutes
users already experienced slowness

11pm — traffic is gone, 10 Pods sitting idle
you forget to scale down → paying for unused Pods all night

Weekend spike → you are not watching → nobody scales

WITH HPA

9am  — CPU hits 50% threshold
HPA adds Pods within seconds automatically
Users never experience slowness

11pm — CPU drops, HPA removes Pods automatically
Only 2 Pods running — minimum cost

Weekend → HPA handles it — no human needed
```

## 📐 How HPA Calculates Scaling

---
```
Desired replicas = ceil(current replicas x (current CPU / target CPU))
 
Example with 2 Pods, 250m CPU request, 50% target:
  Target CPU per Pod = 250m x 50% = 125m
 
  Current average CPU = 200m (80% utilisation)
  Desired = ceil(2 x (200 / 125)) = ceil(3.2) = 4 Pods
  HPA scales up to 4
 
  Load removed, current average CPU = 30m
  Desired = ceil(4 x (30 / 125)) = ceil(0.96) = 1 Pod
  But minReplicas = 2 → stays at 2 Pods
```

## ⚠️ HPA Requirement — Resource Requests Must Be Set

---
```
HPA calculates CPU percentage based on requests.
If a Pod has no CPU request set HPA cannot calculate usage.
HPA shows: <unknown>/50% and never scales.
 
Our spring-app.yaml already has this set:
  resources:
    requests:
      cpu: "250m"    ← HPA uses this as the 100% baseline
    limits:
      cpu: "500m"
```

## ⚙️ Metrics Server Requirement

---
```
HPA reads Pod metrics from the Metrics Server.
minikube includes it as an addon — enable it before using HPA.
 
Without Metrics Server:
  kubectl get hpa shows: <unknown>/50%
  HPA cannot make scaling decisions
 
With Metrics Server:
  kubectl get hpa shows: 12%/50%
  HPA scales correctly
```

## ⏱️ Scaling Behaviour

---
```
SCALE UP — fast, to handle traffic quickly
  stabilizationWindowSeconds: 30
  Add up to 2 Pods every 30 seconds
 
SCALE DOWN — slow, to avoid removing Pods too quickly
  stabilizationWindowSeconds: 120
  Remove 1 Pod every 60 seconds
  Waits 2 minutes of consistently low CPU before scaling down
 
WHY ASYMMETRIC?
  Scaling up too slowly = users experience slowness
  Scaling down too quickly = traffic spikes again and you are under-scaled
  Better to keep an extra Pod for 2 minutes than to restart scaling
```

## ✅ Prerequisites

---
```powershell
# Start minikube if not already running
minikube start
 
# Enable Metrics Server — required for HPA to read CPU metrics
minikube addons enable metrics-server
 
# Verify Metrics Server is running
kubectl get pods -n kube-system | Select-String "metrics-server"
# Wait until it shows Running — takes about 1 minute
 
# Verify spring-app from 01-spring-boot-k8s is still running
kubectl get pods -n spring-app
# Expected: postgres, redis and 2 spring-app Pods all Running
 
# If spring-app namespace is gone redeploy it
# cd kubernetes\k8s-advanced\01-spring-boot-k8s
# kubectl apply -f manifests/namespace.yaml
# kubectl apply -f manifests/ -n spring-app
 
# Start minikube tunnel in a SEPARATE PowerShell window
# Keep it running throughout this exercise
minikube tunnel
```

## ⚙️ Exercises

---
### 🧪 Exercise 1 — Apply the HPA

```powershell
# Navigate to the folder
cd kubernetes\k8s-advanced\02-hpa
 
# Apply the HPA
kubectl apply -f manifests/hpa.yaml -n spring-app
 
# Check HPA status
# TARGETS shows current CPU% / threshold%
# MINPODS and MAXPODS show the configured limits
# REPLICAS shows current Pod count
kubectl get hpa -n spring-app
 
# Wait 1-2 minutes for Metrics Server to collect data
# Then check again — TARGETS should show real values like 2%/50%
# instead of <unknown>/50%
kubectl get hpa -n spring-app
```

### 📊 Exercise 2 — Verify CPU Metrics Are Working

```powershell
# Check current CPU usage of spring-app Pods
# TARGETS in HPA should reflect this
kubectl top pods -n spring-app
 
# If kubectl top shows error enable metrics-server first
# minikube addons enable metrics-server
# Wait 2 minutes then retry
```

### 💥 Exercise 3 — Generate Load and Watch Scale Up

```powershell
# Open THREE PowerShell windows:
#
# Window 1 — watch HPA live
kubectl get hpa -n spring-app -w
#
# Window 2 — watch Pods live
kubectl get pods -n spring-app -w
#
# Window 3 — apply the load test
kubectl apply -f manifests/load-test.yaml -n spring-app
 
# In Window 3 — watch the load tester logs to confirm it is running
kubectl logs -f load-test -n spring-app
 
# In Window 1 — watch TARGETS CPU% rise above 50%
# Once it crosses 50% HPA will add Pods
# Example progression:
#   2%/50%    → 2 replicas   (idle)
#   45%/50%   → 2 replicas   (load increasing)
#   78%/50%   → 3 replicas   (HPA scaling up)
#   91%/50%   → 4 replicas   (still scaling)
#   55%/50%   → 4 replicas   (stabilising)
 
# In Window 2 — watch new Pods appear
# New Pods go through: Pending → ContainerCreating → Running
```

### 📉 Exercise 4 — Remove Load and Watch Scale Down

```powershell
# Delete the load test Pod
kubectl delete -f manifests/load-test.yaml -n spring-app
 
# Watch CPU drop in HPA
kubectl get hpa -n spring-app -w
 
# Scale down is slower than scale up
# stabilizationWindowSeconds: 120 means HPA waits 2 minutes
# of consistently low CPU before removing Pods
# You will see Pods removed one at a time every 60 seconds
kubectl get pods -n spring-app -w
 
# Eventually HPA settles back at minReplicas: 2
```

### 🔍 Exercise 5 — Inspect HPA Details

```powershell
# Full HPA description including events
kubectl describe hpa spring-app-hpa -n spring-app
 
# Look for Events section at the bottom:
#   Successful rescale: reason DesiredReplicas → 4
#   Successful rescale: reason DesiredReplicas → 2
# These show exactly when and why HPA scaled
```

### 🛑 Exercise 6 — Clean Up

```powershell
# Delete the HPA
kubectl delete -f manifests/hpa.yaml -n spring-app
 
# Verify HPA is gone
kubectl get hpa -n spring-app
# Expected: No resources found
 
# spring-app Deployment stays running for the next exercise
```