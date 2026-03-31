## 📋 Quick Reference

---
```powershell
# Apply a YAML file to the cluster
kubectl apply -f file.yaml -n namespace
 
# Watch resources live — press Ctrl+C to stop
kubectl get pods -n namespace -w
 
# Get details including events
kubectl describe pod name -n namespace
kubectl describe deployment name -n namespace
 
# Scale a Deployment
kubectl scale deployment name --replicas=5 -n namespace
 
# Update the container image
kubectl set image deployment/name container=image:tag -n namespace
 
# Check rollout status
kubectl rollout status deployment/name -n namespace
 
# See rollout history
kubectl rollout history deployment/name -n namespace
 
# Roll back to previous version
kubectl rollout undo deployment/name -n namespace
 
# Shell into a Pod
kubectl exec -it pod-name -n namespace -- sh
 
# Delete resources
kubectl delete -f file.yaml -n namespace
kubectl delete pod name -n namespace
```