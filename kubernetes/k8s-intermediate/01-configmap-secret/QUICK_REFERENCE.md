## 📋 Quick Reference

---
```bash
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

# 🍎 macOS / 🐧 Linux — decode a Secret value
kubectl get secret name -n namespace \
  -o jsonpath="{.data.KEY}" | base64 -d ; echo

# 🍎 macOS / 🐧 Linux — encode a value to base64
echo -n "myvalue" | base64

# Restart Pods to pick up config changes
kubectl rollout restart deployment/name -n namespace
```

---
```bash
# 🪟 Windows PowerShell — decode a Secret value
kubectl get secret name -n namespace `
  -o jsonpath="{.data.KEY}" | `
  % { [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($_)) }

# 🪟 Windows PowerShell — encode a value to base64
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("myvalue"))
```