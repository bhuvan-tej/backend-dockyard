## 📋 Quick Reference

---
```powershell
# ConfigMap commands
kubectl get configmap -n namespace
kubectl get cm -n namespace
kubectl describe configmap name -n namespace
kubectl get configmap name -n namespace -o yaml
kubectl edit configmap name -n namespace
 
# Secret commands
kubectl get secret -n namespace
kubectl describe secret name -n namespace
kubectl get secret name -n namespace -o yaml
 
# Decode a Secret value on Windows PowerShell
kubectl get secret name -n namespace `
  -o jsonpath="{.data.KEY}" | `
  % { [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($_)) }
 
# Encode a value to base64 on Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("myvalue"))
 
# Restart Pods to pick up config changes
kubectl rollout restart deployment/name -n namespace
```