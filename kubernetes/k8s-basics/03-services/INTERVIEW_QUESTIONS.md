## 💡 Interview Questions

---
**Q: Why do you need a Service in Kubernetes?**
> Pods are temporary and their IPs change on every restart.
A Service provides a stable DNS name and IP that never changes
regardless of Pod restarts. It also load balances traffic
automatically across all matching Pods.

**Q: What is the difference between ClusterIP and NodePort?**
> ClusterIP is only reachable inside the cluster — used for
Pod to Pod communication. NodePort opens a port on every node
making the app reachable from outside — used for local dev.
In production you use LoadBalancer or Ingress instead.

**Q: How does a Service know which Pods to route traffic to?**
> Through label selectors. The Service has a selector field
that matches Pod labels. All running Pods with matching labels
automatically receive traffic. New Pods are included as soon
as they start. Crashed Pods are excluded immediately.

**Q: What is kubectl port-forward and when do you use it?**
> It forwards a local port on your machine to a port inside the
cluster. Used for quick testing without exposing a NodePort.
It only works while the command is running — not for production.

**Q: What is the DNS format for a Service inside a cluster?**
> service-name.namespace.svc.cluster.local
Within the same namespace you can use just the service name.
Kubernetes DNS resolves this to the Service ClusterIP automatically.