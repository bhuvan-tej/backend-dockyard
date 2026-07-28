## 📋 Quick Reference

---
```powershell
# Enable Metrics Server
minikube addons enable metrics-server
 
# Get HPA status
kubectl get hpa -n namespace
kubectl get hpa -n namespace -w          # watch live
kubectl describe hpa name -n namespace   # full details + events
 
# Check Pod CPU and memory usage
kubectl top pods -n namespace
kubectl top nodes
 
# Apply and delete load test
kubectl apply -f load-test.yaml -n namespace
kubectl delete -f load-test.yaml -n namespace
```