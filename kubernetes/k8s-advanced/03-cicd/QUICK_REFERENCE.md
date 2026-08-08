## 📋 Quick Reference

---
```bash
# One-time setup — generate the base64 kubeconfig for the GitHub secret
# 🍎 macOS:    base64 -i ~/.kube/config | pbcopy
# 🐧 Linux:    base64 -w0 ~/.kube/config
# 🪟 Windows:  [Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\.kube\config"))
# Add it to GitHub → Settings → Secrets and variables → Actions
#   Name: KUBECONFIG_B64

# Trigger the pipeline — any push to main runs test → build → deploy
git add .
git commit -m "test: trigger CI/CD pipeline"
git push origin main

# Watch it: GitHub → repo → Actions tab

# Verify the deploy landed (image tag matches the latest commit SHA)
kubectl get pods -n spring-app -o jsonpath="{.items[*].spec.containers[*].image}"

# Rollout history and manual rollback
kubectl rollout history deployment/spring-app -n spring-app
kubectl rollout undo    deployment/spring-app -n spring-app

# Confirm the API still works (needs minikube tunnel running)
curl http://localhost/api/products
```

---
### 🧠 Remember
```
Image tag = git SHA        → traceable, unambiguous, easy rollback
if: failure() → rollout undo → automatic rollback on a bad deploy
KUBECONFIG_B64 secret       → how the runner reaches your cluster
Same pipeline, cloud vs local → only the kubeconfig changes
```

