## 📋 Quick Reference

---
```powershell
# Watch Pods live
kubectl get pods -n namespace -w
 
# Describe Pod — see probe config and events
kubectl describe pod name -n namespace
 
# Watch events sorted by time
kubectl get events -n namespace --sort-by=".lastTimestamp"
 
# Watch events live
kubectl get events -n namespace --sort-by=".lastTimestamp" -w
 
# Edit a live resource in the cluster
kubectl edit deployment name -n namespace
 
# Delete all resources in a namespace
kubectl delete all --all -n namespace
``