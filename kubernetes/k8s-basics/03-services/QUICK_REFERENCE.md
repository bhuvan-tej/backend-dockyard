## 📋 Quick Reference

```powershell
# List Services
kubectl get services -n namespace
kubectl get svc -n namespace
 
# See Service details and which Pods it routes to
kubectl describe service name -n namespace
 
# Open a NodePort Service in browser via minikube
minikube service service-name -n namespace
 
# Get the NodePort Service URL
minikube service service-name -n namespace --url
 
# Forward a local port to a Service
kubectl port-forward service/name local-port:service-port -n namespace
 
# Run a temporary Pod for testing
kubectl run test --rm -it --image=alpine --restart=Never -n namespace -- sh
```