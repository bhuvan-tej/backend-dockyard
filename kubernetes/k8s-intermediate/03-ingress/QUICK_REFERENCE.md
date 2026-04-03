## 📋 Quick Reference

---
```powershell
# Enable minikube addon
minikube addons enable ingress
 
# List all addons and their status
minikube addons list
 
# Get minikube node IP
minikube ip
 
# Ingress commands
kubectl get ingress -n namespace
kubectl describe ingress name -n namespace
kubectl edit ingress name -n namespace
 
# Watch Ingress controller logs
kubectl logs -f ingress-controller-pod -n ingress-nginx
```