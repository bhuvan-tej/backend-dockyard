## 📋 Quick Reference

---
```bash
# Build and push the Spring Boot image to GHCR
docker build -t ghcr.io/USERNAME/backend-dockyard/spring-docker-app:latest .
docker push ghcr.io/USERNAME/backend-dockyard/spring-docker-app:latest

# Enable the Ingress addon (needed for /api routing)
minikube addons enable ingress

# Apply the manifests in order
kubectl apply -f manifests/namespace.yaml
kubectl apply -f manifests/configmap.yaml -n spring-app
kubectl apply -f manifests/secret.yaml    -n spring-app
kubectl apply -f manifests/postgres.yaml  -n spring-app
kubectl apply -f manifests/redis.yaml     -n spring-app
kubectl apply -f manifests/spring-app.yaml -n spring-app

# Or apply the whole folder at once (after the namespace exists)
kubectl apply -f manifests/ -n spring-app

# Inspect everything in the namespace
kubectl get all      -n spring-app
kubectl get pvc      -n spring-app          # PostgreSQL storage — STATUS Bound
kubectl get ingress  -n spring-app
kubectl get pods     -n spring-app -w       # watch startup live

# Logs and rollout
kubectl logs -n spring-app deployment/spring-app --tail 20
kubectl rollout status  deployment/spring-app -n spring-app
kubectl rollout history deployment/spring-app -n spring-app

# Test through the Ingress (needs minikube tunnel running)
curl http://localhost/api/products
curl http://localhost/actuator/health

# Tear everything down in one command
kubectl delete namespace spring-app
```

