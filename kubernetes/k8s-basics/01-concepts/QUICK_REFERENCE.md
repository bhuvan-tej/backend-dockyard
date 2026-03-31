## ✅ Prerequisites Check

---
```
# Check kubectl is installed
kubectl version --client
# Expected: Client Version: v1.x.x

# Check minikube is installed
minikube version
# Expected: minikube version: v1.x.x

# Check Docker Desktop is running
docker info
# Expected: server info without errors
```

## 🛠️ minikube Quick Reference

---
```
# Start a local Kubernetes cluster using Docker as the driver
# --driver=docker uses Docker Desktop which you already have running
# --memory=4096 gives minikube 4GB RAM
# --cpus=2 gives minikube 2 CPU cores
minikube start --driver=docker --memory=4096 --cpus=2

# Check minikube status
minikube status
# Expected:
# minikube: Running
# kubelet: Running
# apiserver: Running
#kubeconfig: Configured

# Check the node is ready
kubectl get nodes
# Expected:
# NAME       STATUS   ROLES           AGE
# minikube   Ready    control-plane   Xm
```

### 🔍 Check the Cluster

---
```powershell
# See overall cluster info
# Shows the API Server URL and other component URLs
kubectl cluster-info
 
# See detailed info about the node including CPU and memory
kubectl describe node minikube
```

### 🗂️ Explore Default 

---
```powershell
# List all namespaces that Kubernetes created by default
kubectl get namespaces
 
# Expected:
# default       your resources go here unless you specify otherwise
# kube-system   Kubernetes system components like DNS and the dashboard
# kube-public   publicly accessible cluster info

# Create a new namespace
kubectl create namespace backend-dockyard

# List all objects in all namespaces
# You will see the Kubernetes system Pods running
kubectl get all --all-namespaces
```

### 🎛️ Open the Kubernetes Dashboard

---
```powershell
# Opens a web UI in your browser to see all cluster resources visually
# Press Ctrl+C to close the proxy when done
minikube dashboard
```

```
# Check cluster status
minikube status

# Pause the cluster to save RAM when not learning
# Your data and config are preserved
minikube stop

# Resume the cluster
minikube start

# Open the web dashboard
minikube dashboard

# Delete the cluster completely and start fresh
# Warning: deletes everything
minikube delete
```